package moonlight.ws.api.qrcode;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.api.ErrorCorrectionLevel;
import moonlight.ws.api.ImageFormat;

@Getter
@Setter
public class QrCodeMetaDto {

	public static final int WIDTH_DEFAULT = 100;
	public static final int WIDTH_MIN = 50;
	public static final int WIDTH_MAX = 5000;

	public static final int HEIGHT_DEFAULT = 100;
	public static final int HEIGHT_MIN = 50;
	public static final int HEIGHT_MAX = 5000;

	private String content;

	@QueryParam("imageFormat")
	private ImageFormat imageFormat;

	@QueryParam("width")
	private Integer width;

	@QueryParam("height")
	private Integer height;

	@QueryParam("errorCorrectionLevel")
	private ErrorCorrectionLevel errorCorrectionLevel;
}
