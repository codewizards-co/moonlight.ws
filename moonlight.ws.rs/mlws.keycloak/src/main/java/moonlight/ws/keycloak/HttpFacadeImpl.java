package moonlight.ws.keycloak;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.security.cert.X509Certificate;

import org.keycloak.adapters.spi.AuthenticationError;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.spi.LogoutError;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.UriUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Implementation of {@link HttpFacade}. An instance of this class is used to
 * glue the Keycloak-code into the {@link KeycloakAuthenticationFilter}.
 */
public class HttpFacadeImpl implements HttpFacade {

	protected HttpServletResponse response;
	protected HttpServletRequest request;
	protected RequestFacade requestFacade = new RequestFacade();
	protected ResponseFacade responseFacade = new ResponseFacade();
	protected MultivaluedHashMap<String, String> queryParameters;

	public HttpFacadeImpl(HttpServletResponse response, HttpServletRequest request) {
		this.response = response;
		this.request = request;
	}

	@Override
	public X509Certificate[] getCertificateChain() {
		throw new IllegalStateException("Not supported yet");
	}

	@Override
	public Request getRequest() {
		return requestFacade;
	}

	@Override
	public Response getResponse() {
		return responseFacade;
	}

	protected class RequestFacade implements Request {

		private InputStream inputStream;

		@Override
		public String getURI() {
			StringBuffer buf = request.getRequestURL();
			if (request.getQueryString() != null) {
				buf.append('?').append(request.getQueryString());
			}
			return buf.toString();
		}

		@Override
		public String getRelativePath() {
			String uri = request.getRequestURI();
			String contextPath = request.getContextPath();
			String servletPath = uri.substring(uri.indexOf(contextPath) + contextPath.length());

			if ("".equals(servletPath)) {
				servletPath = "/";
			}

			return servletPath;
		}

		@Override
		public boolean isSecure() {
			return request.isSecure();
		}

		@Override
		public String getFirstParam(String param) {
			return request.getParameter(param);
		}

		@Override
		public String getQueryParamValue(String paramName) {
			if (queryParameters == null) {
				queryParameters = UriUtils.decodeQueryString(request.getQueryString());
			}
			return queryParameters.getFirst(paramName);
		}

		@Override
		public Cookie getCookie(String cookieName) {
			if (request.getCookies() == null) {
				return null;
			}
			jakarta.servlet.http.Cookie cookie = null;
			for (jakarta.servlet.http.Cookie c : request.getCookies()) {
				if (c.getName().equals(cookieName)) {
					cookie = c;
					break;
				}
			}
			if (cookie == null) {
				return null;
			}
			return new Cookie(cookie.getName(), cookie.getValue(), cookie.getVersion(), cookie.getDomain(),
					cookie.getPath());
		}

		@Override
		public List<String> getHeaders(String name) {
			Enumeration<String> headers = request.getHeaders(name);
			if (headers == null) {
				return null;
			}
			List<String> list = new ArrayList<String>();
			while (headers.hasMoreElements()) {
				list.add(headers.nextElement());
			}
			return list;
		}

		@Override
		public InputStream getInputStream() {
			return getInputStream(false);
		}

		@Override
		public InputStream getInputStream(boolean buffered) {
			if (inputStream != null) {
				return inputStream;
			}

			if (buffered) {
				try {
					return inputStream = new BufferedInputStream(request.getInputStream());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			try {
				return request.getInputStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getMethod() {
			return request.getMethod();
		}

		@Override
		public String getHeader(String name) {
			return request.getHeader(name);
		}

		@Override
		public String getRemoteAddr() {
			return request.getRemoteAddr();
		}

		@Override
		public void setError(AuthenticationError error) {
			request.setAttribute(AuthenticationError.class.getName(), error);

		}

		@Override
		public void setError(LogoutError error) {
			request.setAttribute(LogoutError.class.getName(), error);
		}
	}

	protected class ResponseFacade implements Response {
		protected boolean ended;

		@Override
		public void setStatus(int status) {
			response.setStatus(status);
		}

		@Override
		public void addHeader(String name, String value) {
			response.addHeader(name, value);
		}

		@Override
		public void setHeader(String name, String value) {
			response.setHeader(name, value);
		}

		@Override
		public void resetCookie(String name, String path) {
			setCookie(name, "", path, null, 0, false, false);
		}

		@Override
		public void setCookie(String name, String value, String path, String domain, int maxAge, boolean secure,
				boolean httpOnly) {
			StringBuffer cookieBuf = new StringBuffer();
			appendCookieValue(cookieBuf, 1, name, value, path, domain, null, maxAge, secure, httpOnly);
			String cookie = cookieBuf.toString();
			response.addHeader("Set-Cookie", cookie);
		}

		@Override
		public OutputStream getOutputStream() {
			try {
				return response.getOutputStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendError(int code) {
			try {
				response.sendError(code);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void sendError(int code, String message) {
			try {
				response.sendError(code, message);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void end() {
			ended = true;
		}

		public boolean isEnded() {
			return ended;
		}
	}

	/**
	 * GMT timezone - all HTTP dates are on GMT
	 */
	public final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
	/**
	 * Pattern used for old cookies
	 */
	private final static String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";

	private final static DateFormat OLD_COOKIE_FORMAT = new SimpleDateFormat(OLD_COOKIE_PATTERN, Locale.US);
	static {
		OLD_COOKIE_FORMAT.setTimeZone(GMT_ZONE);
	}

	public static String formatOldCookie(Date d) {
		String ocf = null;
		synchronized (OLD_COOKIE_FORMAT) {
			ocf = OLD_COOKIE_FORMAT.format(d);
		}
		return ocf;
	}

	public static void formatOldCookie(Date d, StringBuffer sb, FieldPosition fp) {
		synchronized (OLD_COOKIE_FORMAT) {
			OLD_COOKIE_FORMAT.format(d, sb, fp);
		}
	}

	private static final String ancientDate = formatOldCookie(new Date(10000));

	public static void appendCookieValue(StringBuffer headerBuf, int version, String name, String value, String path,
			String domain, String comment, int maxAge, boolean isSecure, boolean httpOnly) {
		StringBuffer buf = new StringBuffer();
// Servlet implementation checks name
		buf.append(name);
		buf.append("=");
// Servlet implementation does not check anything else

// NOTE!!! BROWSERS REALLY DON'T LIKE QUOTING
//maybeQuote2(version, buf, value);
		buf.append(value);

// Add version 1 specific information
		if (version == 1) {
// Version=1 ... required
			buf.append("; Version=1");

// Comment=comment
			if (comment != null) {
				buf.append("; Comment=");
//maybeQuote2(version, buf, comment);
				buf.append(comment);
			}
		}

// Add domain information, if present
		if (domain != null) {
			buf.append("; Domain=");
//maybeQuote2(version, buf, domain);
			buf.append(domain);
		}

// Max-Age=secs ... or use old "Expires" format
// TODO RFC2965 Discard
		if (maxAge >= 0) {
// Wdy, DD-Mon-YY HH:MM:SS GMT ( Expires Netscape format )
			buf.append("; Expires=");
// To expire immediately we need to set the time in past
			if (maxAge == 0)
				buf.append(ancientDate);
			else
				formatOldCookie(new Date(System.currentTimeMillis() + maxAge * 1000L), buf, new FieldPosition(0));

			buf.append("; Max-Age=");
			buf.append(maxAge);
		}

// Path=path
		if (path != null) {
			buf.append("; Path=");
			buf.append(path);
		}

// Secure
		if (isSecure) {
			buf.append("; Secure");
		}

// HttpOnly
		if (httpOnly) {
			buf.append("; HttpOnly");
		}

		headerBuf.append(buf);
	}
}
