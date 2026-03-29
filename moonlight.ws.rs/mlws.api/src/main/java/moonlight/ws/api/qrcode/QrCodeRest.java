package moonlight.ws.api.qrcode;

import static jakarta.ws.rs.core.MediaType.*;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import moonlight.ws.api.RequiresAuthentication;

@Path("qr-code")
@Consumes(APPLICATION_JSON)
@RequiresAuthentication
public interface QrCodeRest {

	@GET
	@Path("{content:.+}")
	Response getQrCode(@Encoded @PathParam("content") String content, @BeanParam QrCodeMetaDto qrCodeMeta)
			throws Exception;

	@POST
	Response postQrCode(QrCodeMetaDto qrCodeMeta) throws Exception;
}
