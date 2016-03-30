package com.github.momogentoo.pdfboxprintln.core;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.*;

/**
 * PDFBuilder
 * <p>
 * A Wrapper of PDFBox low-level API
 */
public class PDFBuilder implements Closeable {
    private static final float DEFAULT_PAGE_MARGIN = 40;
    private static final float DEFAULT_LINE_SPACE = 5;
    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int DEFAULT_PAGE_NUMBER_FONT_SIZE = 6;
    private static final String DEFAULT_PAGE_NUMBER_PATTERN = "Page Number %d";

    private static final Logger LOGGER = Logger.getLogger(PDFBuilder.class);

    private PDDocument document;
    private PageOrientation pageOrientation = PageOrientation.LANDSCAPE;
    private PDFont defaultFont = PDType1Font.HELVETICA_BOLD;
    private int textFontSize = DEFAULT_FONT_SIZE;
    private float pageMargin = DEFAULT_PAGE_MARGIN;
    private float lineSpace = DEFAULT_LINE_SPACE;
    private PDFPageSize pageSize = PDFPageSize.LETTER;
    private int pageNumber;
    private String pageNumberPattern = DEFAULT_PAGE_NUMBER_PATTERN;
    private boolean outputPageNumber = true;
    private int pageNumberFontSize = DEFAULT_PAGE_NUMBER_FONT_SIZE;


    private float fontHeight;

    // Max lines on current page
    private int maxLines = -1;

    // available lines to use on current page
    private int availableLines = -1;

    // current line on page
    private int curLines = 0;

    // current working page
    private PDPage curPage;

    // current content stream on working page
    private PDPageContentStream contentStream;

    // current text position
    private float cur_x = 0, cur_y = 0;

    // max used height on page, including margin
    private float used_height = 0;

    public PDFBuilder() {

        document = new PDDocument();

        fontHeight = getFontHeight(defaultFont, textFontSize);
        pageNumber = 0;
    }

    public PageOrientation getPageOrientation() {
        return pageOrientation;
    }

    public void setPageOrientation(PageOrientation pageOrientation) {
        this.pageOrientation = pageOrientation;
    }

    public int getTextFontSize() {
        return textFontSize;
    }

    /**
     * Set new text font size to use for subsequent texts
     *
     * <p> this operation will also make PDFBuilder recalculate available lines to use on current page </p>
     *
     * @param fontSize
     */
    public void setTextFontSize(int fontSize) {
        this.textFontSize = fontSize;

        // Re-calculate font height and available lines
        fontHeight = getFontHeight(defaultFont, textFontSize);

        if (curPage != null) {
            availableLines = estimateMaxLines(used_height - lineSpace - pageMargin, fontHeight, 0, 0);
            LOGGER.debug("Recalculated available lines:" + availableLines
                    + " used height: " + used_height);
        }
    }

    public PDPage getCurrentPage() {
        return curPage;
    }

    public PDFPageSize getPageSize() {
        return pageSize;
    }

    public void setPageSize(PDFPageSize pageSize) {
        this.pageSize = pageSize;
    }

    public float getPageMargin() {
        return pageMargin;
    }

    public void setPageMargin(float pageMargin) {
        this.pageMargin = pageMargin;
    }

    public float getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(float lineSpace) {
        this.lineSpace = lineSpace;
    }

    public boolean isOutputPageNumber() {
        return outputPageNumber;
    }

    public void setOutputPageNumber(boolean outputPageNumber) {
        this.outputPageNumber = outputPageNumber;
    }

    public String getPageNumberPattern() {
        return pageNumberPattern;
    }

    public void setPageNumberPattern(String pageNumberPattern) {
        this.pageNumberPattern = pageNumberPattern;
    }

    public int getPageNumberFontSize() {
        return pageNumberFontSize;
    }

    public void setPageNumberFontSize(int pageNumberFontSize) {
        this.pageNumberFontSize = pageNumberFontSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public PDPage createPDPage(PDFPageSize pageSize) {

        // Select page size
        PDRectangle pdRectangle;
        switch (pageSize) {
            case A0:
                pdRectangle = PDPage.PAGE_SIZE_A0;
                break;
            case A1:
                pdRectangle = PDPage.PAGE_SIZE_A1;
                break;
            case A2:
                pdRectangle = PDPage.PAGE_SIZE_A2;
                break;
            case A3:
                pdRectangle = PDPage.PAGE_SIZE_A3;
                break;
            case A4:
                pdRectangle = PDPage.PAGE_SIZE_A4;
                break;
            case A5:
                pdRectangle = PDPage.PAGE_SIZE_A5;
                break;
            case A6:
                pdRectangle = PDPage.PAGE_SIZE_A6;
                break;
            case LETTER:
            default:
                pdRectangle = PDPage.PAGE_SIZE_LETTER;
                break;
        }

        PDPage page = new PDPage(pdRectangle);

        if (pageOrientation == PageOrientation.LANDSCAPE) {
            page.setRotation(90);
        }

        return page;
    }

    /**
     * Create Page content stream according current page orientation
     * @param document
     * @param page
     * @return
     * @throws IOException
     */
    public PDPageContentStream createPDPageContentStream(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream content = new PDPageContentStream(document, page);

        // LANDSCAPE
        if (Integer.valueOf(90).equals(page.getRotation())) { // Rotation could be null
            content.concatenate2CTM(0, 1, -1, 0, page.getMediaBox().getWidth(), 0);
        }

        return content;
    }

    /**
     * Estimate total lines available in given height of media box according to font height and page margins
     * @param maxHeight Max height available to use (including margin)
     * @param fontHeight Font height
     * @param marginTop Page margin at top of page
     * @param marginBottom Page margin at bottom of page
     * @return
     */
    public int estimateMaxLines(float maxHeight, float fontHeight, float marginTop, float marginBottom) {

        return (int) ((maxHeight - marginTop - marginBottom) / (fontHeight + lineSpace)) + 1;
    }

    public float estimateStringWidth(PDFont font, String text, int fontSize) throws IOException {
        return font.getStringWidth(text) * fontSize / 1000;
    }

    public float estimateStringWidth(String text, int fontSize) throws IOException {
        return estimateStringWidth(defaultFont, text, fontSize);
    }

    /**
     * Estimate font heigh by font and font size
     * @param font Font to use
     * @param fontSize Font size like 12, 13
     * @return
     */
    public float getFontHeight(PDFont font, int fontSize) {
        return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    public void close() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        document.close();
    }

    /**
     * Get effective page height according to page orientation
     *
     * @param page
     * @return
     */
    public float getEffectivePageHeight(PDPage page) {
        PDRectangle pageSize = page.getMediaBox();
        float effHeight = pageSize.getHeight();

        if (Integer.valueOf(90).equals(page.getRotation())) {
            effHeight = pageSize.getWidth();
        }

        return effHeight;
    }

    /**
     * Get effective page width according to page orientation
     *
     * @param page
     * @return
     */
    public float getEffectivePageWidth(PDPage page) {
        PDRectangle pageSize = page.getMediaBox();
        float effWidth = pageSize.getWidth();

        if (Integer.valueOf(90).equals(page.getRotation())) {
            effWidth = pageSize.getHeight();
        }

        return effWidth;
    }

    /**
     * Print multiple cells as a line
     *
     * <p>Text Alignment is not supported yet</p>
     *
     * @param cells
     */
    public void println(Serializable[]cells, Integer []cellWidths, TextAttributes []attributes) throws IOException {
        checkNewPage(false);

        float totalLineWidth = getEffectivePageWidth(curPage) - getPageMargin() * 2;
        float totalCellWidth = 0;
        float x = pageMargin;
        float y = getNextLineYCord();
        TextAttributes defaultAttr = new TextAttributes();

        for (Integer cellWidth : cellWidths) {
            totalCellWidth += cellWidth;
        }

        float widthTaken = 0;
        // Calculate prorated width for each cell
        for (int i = 0; (i < cells.length); ++i) {
            float widthOfCell = totalLineWidth * cellWidths[i] / totalCellWidth;
            x = getPageMargin() + widthTaken;

            TextAttributes attribute;

            if (attributes != null) {
                attribute = attributes[i];
            }
            else {
                defaultAttr.setFontSize(textFontSize);
                attribute = defaultAttr;
            }

            // Calculate a couple of attributes
            attribute.setBgX(x);
            attribute.setBgY(y);
            attribute.setBgHeight(getFontHeight(defaultFont, attribute.getFontSize()));
            attribute.setBgWidth(widthOfCell);
            attribute.setFontSize(textFontSize);
            print(x, y, cells[i].toString(), attribute);

            widthTaken += widthOfCell;
        }

        cur_x = x;
        cur_y = y;
        used_height = y;

        curLines++;
        availableLines--;
    }

    private float getNextLineYCord() {
        float y;

        // New page
        if (used_height == -1) {
            y = getEffectivePageHeight(curPage) - pageMargin - curLines * (fontHeight + lineSpace);
        }
        else {
            y = used_height - fontHeight - lineSpace;
        }

        return y;
    }

    /**
     * Add a blank line
     * @throws IOException
     */
    public void println() throws IOException {
        println("", Color.black, Color.white, TextAlignment.LEFT);
    }

    /**
     * Print a line of text
     * @param text
     * @throws IOException
     */
    public void println(String text, TextAlignment alignment) throws IOException {
        println(text, Color.black, Color.white, alignment);
    }

    /**
     * Print a line of text in specified font color/background color
     * @param text
     * @param fontColor
     * @param backgroundColor
     * @param alignment
     * @throws IOException
     */
    public void println(String text, Color fontColor, Color backgroundColor, TextAlignment alignment) throws IOException {
        checkNewPage(false);

        // Calculate new text position for this text line
        float x;
        float textWidth = estimateStringWidth(defaultFont, text, textFontSize);
        switch (alignment) {
            default:
            case LEFT:
                x = pageMargin;
                break;
            case RIGHT:
                x = getEffectivePageWidth(curPage) - pageMargin - textWidth;
                break;
            case MIDDLE:
                x = (getEffectivePageWidth(curPage) - textWidth) / 2;
                break;
        }

        float y = getNextLineYCord(); // = getEffectivePageHeight(curPage) - pageMargin - curLines * (fontHeight + lineSpace);

        print(x, y, text,
                new TextAttributes(textFontSize,
                        fontColor,
                        backgroundColor,
                        getEffectivePageWidth(curPage) - pageMargin * 2,
                        fontHeight,
                        alignment).setBgX(pageMargin).setBgY(y));

        cur_x = x;
        cur_y = y;
        used_height = y;

        curLines++;
        availableLines--;
    }


    /**
     * "Draw" a text onto PDF document page
     *
     * <p>NOTE: Don't add new lines after document is saved </p>
     *
     * @param text
     * @throws IOException
     */
    public void print(float x, float y, String text,
                      TextAttributes attributes) throws IOException {


        // if background color is changed, draw a rectangle and fill
        if (attributes.getBgColor() != Color.white
                && attributes.getBgX() != -1
                && attributes.getBgY() != -1
                && attributes.getBgHeight() != -1
                && attributes.getBgWidth() != -1) {
            contentStream.setNonStrokingColor(attributes.getBgColor());
            contentStream.fillRect(attributes.getBgX(), attributes.getBgY(), attributes.getBgWidth(), attributes.getBgHeight());
        }

        // output text
        contentStream.beginText();
        contentStream.setFont(defaultFont, attributes.getFontSize());


        LOGGER.debug("Moving text position to "
                + "x=" + x + " "
                + "y=" + y);

        contentStream.moveTextPositionByAmount(x, y);
        contentStream.setNonStrokingColor(attributes.getFgColor());
        contentStream.drawString(text);
        contentStream.endText();

//        contentStream.drawLine(x, y, x + 100, y);

    }

    /**
     * Save to a file
     * @param filename
     * @throws IOException
     * @throws COSVisitorException
     */
    public void save(String filename) throws IOException, COSVisitorException {
        contentStream.close();
        document.save(filename);
    }

    /**
     * Save to an output stream
     * @param outputStream
     * @throws IOException
     * @throws COSVisitorException
     */
    public void save(OutputStream outputStream) throws IOException, COSVisitorException {
        contentStream.close();
        document.save(outputStream);
    }

    /**
     * Save to a file instance
     * @param file
     * @throws IOException
     * @throws COSVisitorException
     */
    public void save(File file) throws IOException, COSVisitorException {
        contentStream.close();
        document.save(file);
    }

    /**
     * Add page number text to middle/bottom of page
     * @param page
     * @param number
     * @param pattern
     * @throws IOException
     */
    private void addPageNumber(PDPage page, int number, String pattern) throws IOException {
        float effWidth = getEffectivePageWidth(page);
        String pageNumberText = String.format(pattern, number);
        float textWidth = estimateStringWidth(defaultFont, pageNumberText, pageNumberFontSize);
        float x = (effWidth - textWidth) / 2;
        float y = pageMargin / 2;

        print(x, y, pageNumberText, new TextAttributes().setFontSize(pageNumberFontSize));
    }

    /**
     * Force to create a new page in document for subsequent operations
     * @throws IOException
     */
    public void forceNewPage() throws IOException {
        checkNewPage(true);
    }

    /**
     * Check if it is necessary to create new page
     * @return true for new page created, false for continuing on current page
     * @throws IOException
     */
    private boolean checkNewPage(boolean forceNewPage) throws IOException {
        boolean newPageCreated = false;

        // Check if it is necessary to create new page
        if (forceNewPage || curPage == null || availableLines <= 0) {
            newPageCreated = true;

            LOGGER.debug("Creating new page: page size: " + pageSize);

            PDPage page = createPDPage(pageSize);
            document.addPage(page);
            curPage = page;
            pageNumber++;

            maxLines = availableLines = estimateMaxLines(getEffectivePageHeight(page),
                    getFontHeight(defaultFont, textFontSize),
                    pageMargin, pageMargin);

            curLines = 0;

            // Close previous content stream
            if (contentStream != null) {
                contentStream.close();
            }

            // Create new content stream
            contentStream = createPDPageContentStream(document, curPage);

            // Output page number string
            if (outputPageNumber) {
                addPageNumber(curPage, pageNumber, pageNumberPattern);
            }

            cur_x = 0;
            cur_y = getEffectivePageHeight(page);
            used_height = -1;
        }

        return newPageCreated;
    }

}
