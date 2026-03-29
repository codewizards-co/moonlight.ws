package moonlight.ws.api;

import moonlight.ws.api.party.SupplierDto;

public interface RestConst {

	/**
	 * <a href=
	 * "https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Common_non-standard_request_fields">Common
	 * non-standard HTTP-header-request-field</a> used for tracing a client's
	 * request across client-server-boundaries.
	 */
	String HEADER_X_REQUEST_ID = "X-Request-ID";

	/**
	 * HTTP-header for cache-control.
	 */
	String HEADER_CACHE_CONTROL = "Cache-Control";

	String HEADER_AUTH = "Authorization";

	/**
	 * Query-parameter for the page-number.
	 *
	 * @see Filter#getPageNumber()
	 */
	String QUERY_PAGE_NUMBER = "pageNumber";

	/**
	 * Query-parameter for the page-size.
	 *
	 * @see Filter#getPageSize()
	 */
	String QUERY_PAGE_SIZE = "pageSize";

	/**
	 * Query-parameter for sorting.
	 *
	 * @see Filter#getSort()
	 */
	String QUERY_SORT = "sort";

	/**
	 * Query-parameter for including deleted entities. By default, deleted entities
	 * are not included, i.e. {@code includeDeleted=false} by default.
	 */
	String QUERY_INCLUDE_DELETED = "includeDeleted";

	/**
	 * Query-parameter for fetching relation-properties. A comma-separated list of
	 * property-paths to be fetched in the same HTTP-request.
	 * <p>
	 * For example, the {@link SupplierDto#getParty() Supplier.party} would be fully
	 * loaded (instead of just a hollow object with an {@code id}), if the
	 * query-parameter {@code fetch} contains {@code party} when querying a single
	 * supplier or a list of suppliers.
	 */
	String QUERY_FETCH = "fetch";

	/**
	 * Auth-token to be stored in a cookie when using cookie-based authentication.
	 * This token is mapped via the {@code AuthCookieRegistry}. It is an
	 * SHA-256-hash (hex-encoded) of the user's bearer-token.
	 */
	String COOKIE_AUTH_TOKEN = "AUTH_TOKEN";

	/**
	 * Variable used in log patterns to output the authenticated user. MDC means
	 * Mapped Diagnostic Context. It is supported by {@code org.slf4j.MDC} in
	 * combination with some logging-systems (not all support MDC -- logback and
	 * log4j certainly do).
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * <code>%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p (%X{username}) [%c] (%t) %s%e%n</code>
	 * </pre>
	 */
	String MDC_USERNAME = "username";
}
