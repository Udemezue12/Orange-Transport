package com.astrotech.transport.utilities.pdf;


import com.astrotech.transport.dto.response.PdfTicketResponse;

import com.astrotech.transport.exceptions.PdfGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfGenerator {

    private final TemplateEngine templateEngine;
    private final QrCodeGenerator qrCodeGenerator;

    public byte[] generateTicketPdf(PdfTicketResponse ticket) {

        var context = new Context();

        var qrCode = qrCodeGenerator.generateBase64(
                ticket.verificationToken(),
                180,
                180
        );

        context.setVariable("ticket", ticket);
        context.setVariable("qrCode", qrCode);

        var renderedHtml =
                templateEngine.process(
                        "ticket-template",
                        context
                );

        try (
                var outputStream = new ByteArrayOutputStream()) {

            var renderer = new ITextRenderer();

            renderer.setDocumentFromString(renderedHtml);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new PdfGenerationException(
                    "Failed to generate ticket PDF",
                    e
            );
        }
    }
}