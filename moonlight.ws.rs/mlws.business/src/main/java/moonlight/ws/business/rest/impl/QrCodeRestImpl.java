package moonlight.ws.business.rest.impl;

import static java.nio.charset.StandardCharsets.*;
import static moonlight.ws.base.util.StringUtil.*;
import static moonlight.ws.base.util.UrlUtil.*;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.core.Variant.VariantListBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.ImageFormat;
import moonlight.ws.api.RestConst;
import moonlight.ws.api.qrcode.QrCodeMetaDto;
import moonlight.ws.api.qrcode.QrCodeRest;

@RequestScoped
@Slf4j
public class QrCodeRestImpl implements QrCodeRest {

	@Inject
	protected Request request;

	@Override
	public Response getQrCode(String _content, QrCodeMetaDto qrCodeMeta) throws Exception {
		log.info("contentFromClient: " + _content);
		String content = URLDecoder.decode(_content, StandardCharsets.UTF_8);
		if (qrCodeMeta == null) {
			qrCodeMeta = new QrCodeMetaDto();
		}
		content = workaroundFixBrokenUrl(content);
		log.info("contentForQrCode: " + content);
		qrCodeMeta.setContent(content);
		byte[] qrCode = generateQrCode(qrCodeMeta);
		return Response //
				.ok(qrCode, qrCodeMeta.getImageFormat().mediaType) //
				.header(RestConst.HEADER_CACHE_CONTROL, "max-age=604800, immutable") //
				.build();
	}

	@Override
	public Response postQrCode(@NonNull QrCodeMetaDto qrCodeMeta) throws Exception {
		byte[] qrCode = generateQrCode(qrCodeMeta);
		return Response //
				.ok(qrCode, qrCodeMeta.getImageFormat().mediaType) //
				.header(RestConst.HEADER_CACHE_CONTROL, "no-store") //
				.build();
	}

	protected byte[] generateQrCode(@NonNull QrCodeMetaDto qrCodeMeta) throws Exception {
		if (isEmpty(qrCodeMeta.getContent())) {
			throw new BadRequestException("content must not be null/empty!");
		}
		if (qrCodeMeta.getWidth() == null || qrCodeMeta.getWidth() <= 0) {
			qrCodeMeta.setWidth(QrCodeMetaDto.WIDTH_DEFAULT);
		}
		if (qrCodeMeta.getHeight() == null || qrCodeMeta.getHeight() <= 0) {
			qrCodeMeta.setHeight(QrCodeMetaDto.HEIGHT_DEFAULT);
		}
		if (qrCodeMeta.getWidth() < QrCodeMetaDto.WIDTH_MIN) {
			qrCodeMeta.setWidth(QrCodeMetaDto.WIDTH_MIN);
		}
		if (qrCodeMeta.getWidth() > QrCodeMetaDto.WIDTH_MAX) {
			qrCodeMeta.setWidth(QrCodeMetaDto.WIDTH_MAX);
		}
		if (qrCodeMeta.getHeight() < QrCodeMetaDto.HEIGHT_MIN) {
			qrCodeMeta.setHeight(QrCodeMetaDto.HEIGHT_MIN);
		}
		if (qrCodeMeta.getHeight() > QrCodeMetaDto.HEIGHT_MAX) {
			qrCodeMeta.setHeight(QrCodeMetaDto.HEIGHT_MAX);
		}
		if (qrCodeMeta.getImageFormat() == null) {
			List<Variant> variants = VariantListBuilder.newInstance().mediaTypes(List.of(ImageFormat.values()).stream()
					.map(imgFmt -> imgFmt.mediaType).collect(Collectors.toList()).toArray(new MediaType[0])).build();
			Variant variant = request.selectVariant(variants);
			ImageFormat imageFormat = variant != null ? ImageFormat.of(variant.getMediaType()) : ImageFormat.png;
			qrCodeMeta.setImageFormat(imageFormat);
		}
		if (qrCodeMeta.getErrorCorrectionLevel() == null) {
			qrCodeMeta.setErrorCorrectionLevel(moonlight.ws.api.ErrorCorrectionLevel.M);
		}
		Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
		// generates QR code with Low level(L) error correction capability
		hints.put(EncodeHintType.ERROR_CORRECTION, toErrorCorrectionLevel(qrCodeMeta.getErrorCorrectionLevel()));
		// invoking the user-defined method that creates the QR code

		// the BitMatrix class represents the 2D matrix of bits
		// MultiFormatWriter is a factory class that finds the appropriate Writer
		// subclass for the BarcodeFormat requested and encodes the barcode with the
		// supplied contents.
		// MN: I have no idea why the String is cloned in this way. Maybe to clean it
		// up. I took it from here:
		// https://www.tpointtech.com/generating-qr-code-in-java
		BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeMeta.getContent().getBytes(UTF_8), UTF_8),
				BarcodeFormat.QR_CODE, qrCodeMeta.getWidth(), qrCodeMeta.getHeight(), hints);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(matrix, qrCodeMeta.getImageFormat().name(), out);
		return out.toByteArray();
	}

	private static ErrorCorrectionLevel toErrorCorrectionLevel(
			@NonNull moonlight.ws.api.ErrorCorrectionLevel errorCorrectionLevel) {
		return ErrorCorrectionLevel.valueOf(errorCorrectionLevel.name());
	}
}
