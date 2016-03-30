package com.github.momogentoo.pdfboxprintln.example;

import com.github.momogentoo.pdfboxprintln.core.*;
import org.apache.pdfbox.exceptions.COSVisitorException;

import java.awt.*;
import java.io.IOException;

/**
 * PDFPrintLnExample
 */
public class PDFPrintLnExample {
    private static final String TEST_TEXT = "The quick brown fox jumps over the lazy dog";
    private static final String TEXT_ON_BLANK_PAGE = "This page is intentionally left blank";

   public static void main(String []args) {
       PDFBuilder pdfBuilder = new PDFBuilder();

       try {

           // Add a big title first
           pdfBuilder.setTextFontSize(30);
           pdfBuilder.println("This is a Report", TextAlignment.MIDDLE);

           // Insert a blank line
           pdfBuilder.println();

           pdfBuilder.setTextFontSize(12);
           pdfBuilder.println("Hello World", TextAlignment.LEFT.LEFT);

           // Intentionally insert 1 blank page
           pdfBuilder.forceNewPage();

           // Calculate text position
           // Get width of text on page
           float textWidth = pdfBuilder.estimateStringWidth(TEXT_ON_BLANK_PAGE, 9);

           // Print in middle of page
           pdfBuilder.print((pdfBuilder.getEffectivePageWidth(pdfBuilder.getCurrentPage()) - textWidth) / 2, // X
                   (pdfBuilder.getEffectivePageHeight(pdfBuilder.getCurrentPage()) / 2),  // Y
                   TEXT_ON_BLANK_PAGE, // Text
                   new TextAttributes().setFontSize(9).setFgColor(Color.red)); // Text Attribute

           // Next, output an array of strings and align by predefined (prorated) widths
           pdfBuilder.forceNewPage();
           String []cells = new String[] {"Foo", "Bar"};
           Integer []widths = new Integer[] {30, 70};
           TextAttributes []textAttributes = new TextAttributes[]{
                   new TextAttributes().setBgColor(Color.lightGray),
                   new TextAttributes().setBgColor(Color.yellow)
           };

           pdfBuilder.println(cells, widths, textAttributes);
           pdfBuilder.println(cells, widths, textAttributes);


           // Output - The quick brown fox jumps over the lazy dog - new page will be created automatically
           // And change page size to A6 / Portrait orientation
           pdfBuilder.setPageSize(PDFPageSize.A6);
           pdfBuilder.setPageOrientation(PageOrientation.PORTRAIT);
           pdfBuilder.setTextFontSize(9);
           pdfBuilder.forceNewPage();

           for (int i = 0; i < 80; ++i) {
               pdfBuilder.println(TEST_TEXT, TextAlignment.LEFT);
           }


           pdfBuilder.save("PDFPrintLnExample.pdf");

       } catch (IOException e) {
           e.printStackTrace();
       } catch (COSVisitorException e) {
           e.printStackTrace();
       }
   }
}
