package io.github.eb4j.ext;

import org.apache.commons.text.translate.CodePointTranslator;

import java.io.IOException;
import java.io.Writer;


/**
 * Escape Unicode values to the form  UTF16 \\u\d\d\d\d or UTF32 \\u\d\d\d\d\d\d\d\d forme.
 */
public class UnicodeEscaper extends CodePointTranslator {

    private static final char[] HEX_DIGITS = new char[] {'0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F'};

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean translate(final int codepoint, final Writer out) throws IOException {
        if (codepoint <= 0x7e) {
            return false;
        }

        if (codepoint > 0xffff) {
            out.write("\\U");
            out.write(HEX_DIGITS[(codepoint >> 28) & 15]);
            out.write(HEX_DIGITS[(codepoint >> 24) & 15]);
            out.write(HEX_DIGITS[(codepoint >> 20) & 15]);
            out.write(HEX_DIGITS[(codepoint >> 16) & 15]);
        } else {
            out.write("\\u");
        }
        out.write(HEX_DIGITS[(codepoint >> 12) & 15]);
        out.write(HEX_DIGITS[(codepoint >> 8) & 15]);
        out.write(HEX_DIGITS[(codepoint >> 4) & 15]);
        out.write(HEX_DIGITS[(codepoint) & 15]);

        return true;
    }
}
