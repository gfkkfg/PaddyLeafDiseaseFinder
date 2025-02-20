package com.example.paddyleafdiseasedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGenerator {

    public static void generateReport(Context context,
                                      String disease,
                                      float confidence,
                                      Bitmap leafImage,
                                      String fileName,
                                      String details,
                                      String symptoms,
                                      String causes,
                                      String management,
                                      String prevention) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 1500, 2).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Title
        paint.setTextSize(18);
        canvas.drawText("Paddy Leaf Disease Report", 20, 30, paint);

        // Disease Name and Confidence
        paint.setTextSize(14);
        canvas.drawText("Disease Detected: " + disease, 20, 70, paint);
        canvas.drawText("Confidence: " + String.format("%.2f", (confidence * 100)) + "%", 20, 90, paint);

        // Add the leaf image if available
        if (leafImage != null) {
            int newWidth = 200;
            int newHeight = 200;
            Bitmap scaledImage = Bitmap.createScaledBitmap(leafImage, newWidth, newHeight, false);
            canvas.drawBitmap(scaledImage, 20, 120, paint);
        }

        // Add details, symptoms, causes, management, and prevention
        paint.setTextSize(12);
        float textY = 350; // Start below the image
        canvas.drawText("Details:", 20, textY, paint);
        textY = addMultilineText(canvas, paint, details, 20, textY + 20, 550);

        canvas.drawText("Symptoms:", 20, textY + 10, paint);
        textY = addMultilineText(canvas, paint, symptoms, 20, textY + 30, 550);

        canvas.drawText("Causes:", 20, textY + 10, paint);
        textY = addMultilineText(canvas, paint, causes, 20, textY + 30, 550);

        canvas.drawText("Management:", 20, textY + 10, paint);
        textY = addMultilineText(canvas, paint, management, 20, textY + 30, 550);

        canvas.drawText("Prevention:", 20, textY + 10, paint);
        addMultilineText(canvas, paint, prevention, 20, textY + 30, 550);

        document.finishPage(page);

        // Save the PDF to the app's private directory
        File pdfFolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "DiseaseReports");
        if (!pdfFolder.exists() && !pdfFolder.mkdirs()) {
            Log.e("PDFGenerator", "Failed to create directory: " + pdfFolder.getAbsolutePath());
            return;
        }

        File pdfFile = new File(pdfFolder, fileName);
        try {
            document.writeTo(new FileOutputStream(pdfFile));
            Log.d("PDFGenerator", "PDF saved to: " + pdfFile.getAbsolutePath());
            Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("PDFGenerator", "Error writing PDF: ", e);
        } finally {
            document.close();
        }
    }

    // Helper method for handling multiline text wrapping
    private static float addMultilineText(Canvas canvas, Paint paint, String text, float x, float y, float maxWidth) {
        int lineHeight = (int) (paint.descent() - paint.ascent());
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.isEmpty()) continue;
            StringBuilder currentLine = new StringBuilder();
            for (String word : line.split(" ")) {
                if (paint.measureText(currentLine + word) > maxWidth) {
                    canvas.drawText(currentLine.toString(), x, y, paint);
                    y += lineHeight;
                    currentLine = new StringBuilder(word + " ");
                } else {
                    currentLine.append(word).append(" ");
                }
            }
            canvas.drawText(currentLine.toString(), x, y, paint);
            y += lineHeight;
        }

        return y;
    }
}
