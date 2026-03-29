package moonlight.ws.api.shorturl;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortUrlDto {

	public static final int CODE_LENGTH_MAX = 100;
	public static final int LONG_URL_LENGTH_MAX = 4096;
	public static final int SHORT_URL_LENGTH_MAX = 1024;

	private Long id;

	@JsonFormat(shape = Shape.STRING)
	private Instant created;

	private Long createdByUserId;

	@JsonFormat(shape = Shape.STRING)
	private Instant changed;

	private Long changedByUserId;

	private String code;

	private String longUrl;

	private String shortUrl;
}
