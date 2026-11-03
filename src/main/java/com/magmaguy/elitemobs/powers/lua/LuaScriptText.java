package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.ast.*;
import com.magmaguy.shaded.luaj.vm2.parser.LuaParser;
import com.magmaguy.shaded.luaj.vm2.parser.LuaParserConstants;
import com.magmaguy.shaded.luaj.vm2.parser.ParseException;
import com.magmaguy.shaded.luaj.vm2.parser.TokenMgrError;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Translates the player-facing text of a Lua power file before it compiles.
 * <p>
 * Content packages write their lines as string literals, and an import overwrites the .lua file, so a translation
 * cannot live in the file. Each literal that reaches a text call is instead looked up with
 * {@link TranslationsConfig#translateText(String)} and replaced in the source by its translation; the script then runs
 * exactly as written.
 * <p>
 * A literal counts as text when it reaches the message argument of {@code send_message}, {@code show_action_bar} or
 * {@code show_boss_bar}, or the title and subtitle of {@code show_title}: directly, through {@code ..}, as a value of
 * {@code and}/{@code or}, through a local only ever assigned that literal ({@code PREFIX .. "line"}), or through the
 * locals, helper parameters, tables and helper results {@link LuaScriptTextFlow} follows ({@code say(c, "line")},
 * {@code LINES[i]}, {@code CFG.PREFIX}). Text built at run time stays English, and so does a file LuaJ's parser cannot
 * read: it rejects {@code \'} inside a single-quoted string, which the compiler accepts. The English comes from the
 * literal's UTF-8 source, since the parser's own value drops characters outside Latin-1.
 */
final class LuaScriptText {

    private static final Map<String, Integer> TEXT_ARGUMENTS = Map.of(
            "send_message", 1,
            "show_action_bar", 1,
            "show_boss_bar", 1,
            "show_title", 2);

    private LuaScriptText() {
    }

    static String translate(String source, String fileName) {
        if (TranslationsConfig.isEnglish()) return source;
        List<Integer> lineStarts = new ArrayList<>();
        for (int start = 0; ; start = source.indexOf('\n', start) + 1) {
            lineStarts.add(start);
            int end = source.indexOf('\n', start);
            // LuaJ stores columns as shorts.
            if ((end < 0 ? source.length() : end) - start >= Short.MAX_VALUE) return source;
            if (end < 0) break;
        }
        Collection<Exp.Constant> literals;
        try {
            literals = findTextLiterals(source);
        } catch (ParseException | TokenMgrError | RuntimeException unreadable) {
            Logger.warn("Could not read the text of Lua power " + fileName + " for translation (" + unreadable.getMessage()
                    + "); it stays in English.");
            return source;
        }

        Map<String, String> translations = new HashMap<>();
        TreeMap<Integer, Replacement> replacements = new TreeMap<>();
        for (Exp.Constant literal : literals) {
            Placement placement = place(source, lineStarts, literal);
            String english = placement != null ? placement.english() : literal.value.tojstring();
            String translated = translations.computeIfAbsent(english, TranslationsConfig::translateText);
            // A literal passed without parentheses, f"text", carries no position and stays English.
            if (translated.equals(english) || literal.beginLine <= 0) continue;
            if (placement == null) {
                Logger.warn("Could not place translated text in Lua power " + fileName + "; it stays in English.");
                return source;
            }
            replacements.put(placement.start(), new Replacement(placement.end(), quote(translated)));
        }

        StringBuilder translatedSource = new StringBuilder(source);
        for (Map.Entry<Integer, Replacement> replacement : replacements.descendingMap().entrySet())
            translatedSource.replace(replacement.getKey(), replacement.getValue().end(), replacement.getValue().literal());
        return translatedSource.toString();
    }

    /**
     * Where the literal sits in the source and the text it holds, or null when its span cannot be confirmed as exactly
     * that one literal, so a replacement can only ever cover it.
     * <p>
     * LuaJ's parser keeps only the low byte of each character of a string, so its value loses anything outside
     * Latin-1 (an em dash becomes a control character). The compiler reads the source as UTF-8, so the text is read
     * again from the literal's UTF-8 bytes, one character per byte, which the parser keeps whole.
     */
    private static Placement place(String source, List<Integer> lineStarts, Exp.Constant literal) {
        int start = offset(source, lineStarts, literal.beginLine, literal.beginColumn);
        int last = offset(source, lineStarts, literal.endLine, literal.endColumn);
        // LuaJ starts a local's initial value at the token before it, the = or the , of the assignment.
        while (start >= 0 && start < last && "'\"[".indexOf(source.charAt(start)) < 0) start++;
        if (start < 0 || last <= start) return null;
        String text = source.substring(start, last + 1);
        LuaValue parsed = parseLiteral(text);
        if (parsed == null || !parsed.raweq(literal.value)) return null;
        LuaValue value = parseLiteral(new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        return value == null ? null : new Placement(start, last + 1, value.tojstring());
    }

    /** The value of the text when it is exactly one string literal, or null. */
    private static LuaValue parseLiteral(String text) {
        try {
            LuaParser parser = new LuaParser(new StringReader(text));
            LuaValue value = parser.Str();
            return parser.getNextToken().kind == LuaParserConstants.EOF ? value : null;
        } catch (ParseException | TokenMgrError | RuntimeException notALiteral) {
            return null;
        }
    }

    /** The index of the character LuaJ reports at this line and column, or -1. LuaJ counts a tab as one column. */
    private static int offset(String source, List<Integer> lineStarts, int line, int column) {
        if (line < 1 || line > lineStarts.size() || column < 1) return -1;
        int start = lineStarts.get(line - 1);
        int end = source.indexOf('\n', start);
        int index = start + column - 1;
        return index < (end < 0 ? source.length() : end) ? index : -1;
    }

    private static Collection<Exp.Constant> findTextLiterals(String source) throws ParseException {
        Chunk chunk = new LuaParser(new StringReader(source)).Chunk();
        chunk.accept(new NameResolver());
        return new LuaScriptTextFlow(chunk, TEXT_ARGUMENTS).literals();
    }

    private static String quote(String text) {
        StringBuilder literal = new StringBuilder(text.length() + 2).append('"');
        for (char character : text.toCharArray()) {
            switch (character) {
                case '\\' -> literal.append("\\\\");
                case '"' -> literal.append("\\\"");
                case '\n' -> literal.append("\\n");
                case '\r' -> literal.append("\\r");
                default -> {
                    if (character < 0x20 || character == 0x7f) literal.append(String.format("\\%03d", (int) character));
                    else literal.append(character);
                }
            }
        }
        return literal.append('"').toString();
    }

    private record Placement(int start, int end, String english) {
    }

    private record Replacement(int end, String literal) {
    }
}
