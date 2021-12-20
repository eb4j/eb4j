package io.github.eb4j.ext;

import org.apache.commons.text.translate.CharSequenceTranslator;

import java.io.IOException;
import java.io.Writer;


/**
 * Translates escaped values of the form  UTF16 \\u\d\d\d\d or UTF32 \\u\d\d\d\d\d\d\d\d to Unicode.
 */
public class UnicodeUnescaper extends CharSequenceTranslator {

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        if (input.charAt(index) == '\\' && index + 1 < input.length()) {
            int i = 2;
            int len;
            if (input.charAt(index + 1) == 'u') {
                len = 4;
            } else if  (input.charAt(index + 1) == 'U') {
                len = 8;
            } else {
                return 0;
            }
            if (index + i + len <= input.length()) {
                // Get 'l' hex digits
                final CharSequence bmp = input.subSequence(index + i, index + i + len);
                try {
                    final int value = Integer.parseInt(bmp.toString(), 16);
                    out.write(Character.toChars(value));
                    return i + len;
                } catch (final NumberFormatException nfe) {
                    throw new IllegalArgumentException("Unable to parse unicode value: " + bmp, nfe);
                }
            }
            throw new IllegalArgumentException("Less than 4 hex digits in unicode value: '"
                    + input.subSequence(index, input.length())
                    + "' due to end of CharSequence");
        }
        return 0;
    }

}
