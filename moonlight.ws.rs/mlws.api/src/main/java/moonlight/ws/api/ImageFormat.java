package moonlight.ws.api;

import jakarta.ws.rs.core.MediaType;
import lombok.NonNull;

public enum ImageFormat {
	// best choice first

	png(MediaType.valueOf("image/png")), //

	jpg(MediaType.valueOf("image/jpeg")), //

	jpeg(MediaType.valueOf("image/jpeg")), //

	gif(MediaType.valueOf("image/gif")), //

// AVIF and WEBP are not (yet) supported by ImageIO (which is used by zxing)
//	avif(MediaType.valueOf("image/avif")), //
//
//	webp(MediaType.valueOf("image/webp")), //

	bmp(MediaType.valueOf("image/bmp")) //
	;

	private ImageFormat(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public final MediaType mediaType;

	public static ImageFormat of(@NonNull MediaType mediaType) {
		for (ImageFormat imageFormat : values()) {
			if (mediaType.isCompatible(imageFormat.mediaType)) {
				return imageFormat;
			}
		}
		throw new IllegalArgumentException("No ImageFormat found for this mediaType: " + mediaType);
	}
}
