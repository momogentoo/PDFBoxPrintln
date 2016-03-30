package com.github.momogentoo.pdfboxprintln.core;

import java.awt.*;

/**
 * TextAttributes
 */
public class TextAttributes {
    private static final int DEFAULT_FONT_SIZE = 12;
    private int fontSize = DEFAULT_FONT_SIZE;
    private Color fgColor = Color.black;
    private Color bgColor = Color.white;
    private float bgX = -1;
    private float bgY = -1;
    private float bgWidth = -1;
    private float bgHeight = -1;
    private TextAlignment alignment = TextAlignment.LEFT;

    public TextAttributes() {
    }

    public TextAttributes(int fontSize, Color fgColor, Color bgColor, float bgWidth, float bgHeight, TextAlignment alignment) {
        this.fontSize = fontSize;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.bgWidth = bgWidth;
        this.bgHeight = bgHeight;
        this.alignment = alignment;
    }

    public int getFontSize() {
        return fontSize;
    }

    public TextAttributes setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public Color getFgColor() {
        return fgColor;
    }

    public TextAttributes setFgColor(Color fgColor) {
        this.fgColor = fgColor;
        return this;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public TextAttributes setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public float getBgWidth() {
        return bgWidth;
    }

    public TextAttributes setBgWidth(float bgWidth) {
        this.bgWidth = bgWidth;
        return this;
    }

    public float getBgHeight() {
        return bgHeight;
    }

    public TextAttributes setBgHeight(float bgHeight) {
        this.bgHeight = bgHeight;
        return this;
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    public TextAttributes setAlignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public float getBgX() {
        return bgX;
    }

    public TextAttributes setBgX(float bgX) {
        this.bgX = bgX;
        return this;
    }

    public float getBgY() {
        return bgY;
    }

    public TextAttributes setBgY(float bgY) {
        this.bgY = bgY;
        return this;
    }
}
