package com.astrotech.transport.utilities.pdf;

import com.astrotech.transport.exceptions.PdfGenerationException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;

@Component
public class QrCodeGenerator {

    public String generateBase64(
            String value,
            int width,
            int height
    ) {
        try {
            var hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.MARGIN, 1);

            var writer = new QRCodeWriter();

            var matrix = writer.encode(
                    value,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            var image = MatrixToImageWriter.toBufferedImage(matrix);

            try (var outputStream = new ByteArrayOutputStream()) {

                ImageIO.write(
                        image,
                        "PNG",
                        outputStream
                );

                return Base64.getEncoder()
                        .encodeToString(outputStream.toByteArray());
            }

        } catch (Exception e) {
            throw new PdfGenerationException(
                    "Failed to generate QR code",
                    e
            );
        }
    }
}
