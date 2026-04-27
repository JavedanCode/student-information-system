package util;

import javax.swing.*;
import javax.swing.text.*;

public class ValidationUtil {

    // -----------------------------
    // LIMIT TEXT LENGTH
    // -----------------------------
    public static void limitTextLength(JTextField field, int maxLength) {
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length,
                                        String text, AttributeSet attrs)
                            throws BadLocationException {

                        int currentLength = fb.getDocument().getLength();
                        int overLimit = (currentLength + text.length()) - maxLength;

                        if (overLimit <= 0) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });
    }

    // -----------------------------
    // ONLY LETTERS (NAME)
    // -----------------------------
    public static void onlyLetters(JTextField field) {
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length,
                                        String text, AttributeSet attrs)
                            throws BadLocationException {

                        if (text.matches("[a-zA-Z ]*")) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });
    }

    // -----------------------------
    // ONLY ALPHANUMERIC
    // -----------------------------
    public static void onlyAlphaNumeric(JTextField field) {
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length,
                                        String text, AttributeSet attrs)
                            throws BadLocationException {

                        if (text.matches("[a-zA-Z0-9]*")) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });
    }

    // -----------------------------
    // ONLY NUMBERS (INT)
    // -----------------------------
    public static void onlyNumbers(JTextField field) {
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length,
                                        String text, AttributeSet attrs)
                            throws BadLocationException {

                        if (text.matches("\\d*")) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });
    }

    // -----------------------------
    // ONLY FLOAT NUMBERS
    // -----------------------------
    public static void onlyFloat(JTextField field) {
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length,
                                        String text, AttributeSet attrs)
                            throws BadLocationException {

                        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                        String newText = current.substring(0, offset) + text + current.substring(offset);

                        if (newText.matches("\\d*(\\.\\d*)?")) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });
    }
}