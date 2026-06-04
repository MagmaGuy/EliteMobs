package com.magmaguy.elitemobs.quests.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OffsetFontUtilTest {

    @Test
    void zeroProducesEmptyString() {
        assertEquals("", OffsetFontUtil.pixels(0));
    }

    @Test
    void positiveAndNegativeOffsetsSumToRequestedPixels() {
        for (int value = -2000; value <= 2000; value++) {
            assertEquals(value, OffsetFontUtil.totalAdvance(OffsetFontUtil.pixels(value)),
                    "advance mismatch for " + value);
        }
    }

    @Test
    void shiftDoesNotAlterVisibleContent() {
        String content = "Hello";
        assertEquals(content, stripOffsets(OffsetFontUtil.shift(content, 0)));
        assertEquals(content, stripOffsets(OffsetFontUtil.shift(content, 31)));
        assertEquals(content, stripOffsets(OffsetFontUtil.shift(content, -60)));
    }

    @Test
    void shiftPadsBySide() {
        // Both directions pad with positive (rightward) advance, on opposite sides:
        // a right shift of N pads the left by +2N; a left shift of N pads the right by +2N.
        // The padding moves the centered content's center by ±N regardless of content width.
        assertEquals(2 * 31, OffsetFontUtil.totalAdvance(OffsetFontUtil.shift("", 31)));
        assertEquals(2 * 60, OffsetFontUtil.totalAdvance(OffsetFontUtil.shift("", -60)));
    }

    private static String stripOffsets(String string) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < string.length()) {
            int codepoint = string.codePointAt(i);
            i += Character.charCount(codepoint);
            if (codepoint < 0xF0F00 || codepoint > 0xF0F1A) builder.appendCodePoint(codepoint);
        }
        return builder.toString();
    }
}
