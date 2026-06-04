package com.magmaguy.elitemobs.quests.dialogue;

/**
 * Builds invisible "negative space" font strings used to position boss-bar dialogue content
 * horizontally without touching the actual text glyphs (so every language keeps rendering in
 * Minecraft's native fonts).
 *
 * <p>The matching glyphs are declared in {@code em_rsp_defaults/.../font/default.json}:
 * <ul>
 *     <li>{@link #BOX_GLYPH} (U+F0F00) renders {@code elitemobs:gui/dialoguebox.png}.</li>
 *     <li>A {@code space} provider maps U+F0F01..U+F0F0A to advances +1..+512 and
 *         U+F0F11..U+F0F1A to advances -1..-512.</li>
 * </ul>
 * Any pixel advance is composed by summing those power-of-two glyphs.
 */
public final class OffsetFontUtil {
    /**
     * The dialogue box is wider than Minecraft's 256px bitmap-glyph limit, so it is sliced into two
     * halves rendered side by side. {@link #BOX_GLYPH} is the left half (U+F0F00), {@link #BOX_GLYPH_RIGHT}
     * the right half (U+F0F0B). {@link #boxGlyph()} composes them with a 1px seam correction.
     */
    public static final String BOX_GLYPH = new String(Character.toChars(0xF0F00));
    public static final String BOX_GLYPH_RIGHT = new String(Character.toChars(0xF0F0B));

    /**
     * The two box halves joined into one renderable string. A bitmap glyph advances by its width + 1px,
     * which would leave a 1px seam between the halves, so a -1px offset is inserted to butt them flush.
     */
    public static String boxGlyph() {
        return BOX_GLYPH + pixels(-1) + BOX_GLYPH_RIGHT;
    }

    private static final int[] MAGNITUDES = {512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
    private static final int[] POSITIVE = {0xF0F0A, 0xF0F09, 0xF0F08, 0xF0F07, 0xF0F06, 0xF0F05, 0xF0F04, 0xF0F03, 0xF0F02, 0xF0F01};
    private static final int[] NEGATIVE = {0xF0F1A, 0xF0F19, 0xF0F18, 0xF0F17, 0xF0F16, 0xF0F15, 0xF0F14, 0xF0F13, 0xF0F12, 0xF0F11};

    private OffsetFontUtil() {
    }

    /**
     * Returns an invisible string whose total horizontal advance equals {@code advance} pixels.
     * Positive values shift the following text right, negative values shift it left.
     */
    public static String pixels(int advance) {
        if (advance == 0) return "";
        int[] glyphs = advance > 0 ? POSITIVE : NEGATIVE;
        int remaining = Math.abs(advance);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAGNITUDES.length && remaining > 0; i++) {
            while (remaining >= MAGNITUDES[i]) {
                builder.appendCodePoint(glyphs[i]);
                remaining -= MAGNITUDES[i];
            }
        }
        return builder.toString();
    }

    /**
     * Shifts a (boss-bar centered) piece of content horizontally by {@code pixelsRight} pixels,
     * independent of the content's own width. Positive moves right, negative moves left.
     *
     * <p>Because a boss-bar title is centered on the total advance of the string, padding one side
     * by {@code 2 * |pixelsRight|} moves the content's center by exactly {@code pixelsRight} while
     * leaving the content itself untouched.
     */
    public static String shift(String content, int pixelsRight) {
        if (pixelsRight == 0) return content;
        if (pixelsRight > 0) return pixels(2 * pixelsRight) + content;
        return content + pixels(-2 * pixelsRight);
    }

    /**
     * Sums the advances of the offset glyphs in a string (other characters are ignored).
     * Exposed for verification/testing of {@link #pixels(int)}.
     */
    public static int totalAdvance(String string) {
        int total = 0;
        int i = 0;
        while (i < string.length()) {
            int codepoint = string.codePointAt(i);
            i += Character.charCount(codepoint);
            for (int m = 0; m < MAGNITUDES.length; m++) {
                if (codepoint == POSITIVE[m]) {
                    total += MAGNITUDES[m];
                    break;
                }
                if (codepoint == NEGATIVE[m]) {
                    total -= MAGNITUDES[m];
                    break;
                }
            }
        }
        return total;
    }
}
