package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.shaded.luaj.vm2.Lua;
import com.magmaguy.shaded.luaj.vm2.LuaString;
import com.magmaguy.shaded.luaj.vm2.ast.*;
import com.magmaguy.shaded.luaj.vm2.parser.LuaParser;
import com.magmaguy.shaded.luaj.vm2.parser.LuaParserTokenManager;
import com.magmaguy.shaded.luaj.vm2.parser.ParseException;
import com.magmaguy.shaded.luaj.vm2.parser.SimpleCharStream;
import com.magmaguy.shaded.luaj.vm2.parser.TokenMgrError;

import java.io.StringReader;
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
 * {@code and}/{@code or}, or through a local only ever assigned that literal ({@code PREFIX .. "line"}). Text built at
 * run time stays English.
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
        // LuaJ stores columns as shorts.
        if (source.lines().anyMatch(line -> line.length() >= Short.MAX_VALUE)) return source;
        Collection<Exp.Constant> literals;
        try {
            literals = findTextLiterals(source);
        } catch (ParseException | TokenMgrError | RuntimeException unreadable) {
            // The compiler reports a broken script on its own; it just stays English.
            return source;
        }

        int[] lineStarts = source.lines().mapToInt(String::length).toArray();
        for (int i = 0, offset = 0; i < lineStarts.length; i++) {
            int length = lineStarts[i];
            lineStarts[i] = offset;
            offset += length + 1;
        }
        Map<String, String> translations = new HashMap<>();
        TreeMap<Integer, Replacement> replacements = new TreeMap<>();
        for (Exp.Constant literal : literals) {
            String english = literal.value.tojstring();
            String translated = translations.computeIfAbsent(english, TranslationsConfig::translateText);
            // A literal passed without parentheses, f"text", carries no position and stays English.
            if (translated.equals(english) || literal.beginLine <= 0 || literal.endLine > lineStarts.length) continue;
            int start = lineStarts[literal.beginLine - 1] + literal.beginColumn - 1;
            int end = lineStarts[literal.endLine - 1] + literal.endColumn;
            if (start < 0 || end > source.length() || start >= end
                    || "'\"[".indexOf(source.charAt(start)) < 0 || "'\"]".indexOf(source.charAt(end - 1)) < 0) {
                Logger.warn("Could not place translated text in Lua power " + fileName + "; it stays in English.");
                return source;
            }
            replacements.put(start, new Replacement(end, quote(translated)));
        }

        StringBuilder translatedSource = new StringBuilder(source);
        for (Map.Entry<Integer, Replacement> replacement : replacements.descendingMap().entrySet())
            translatedSource.replace(replacement.getKey(), replacement.getValue().end(), replacement.getValue().literal());
        return translatedSource.toString();
    }

    private static Collection<Exp.Constant> findTextLiterals(String source) throws ParseException {
        Chunk chunk = new LuaParser(new LuaParserTokenManager(new SourceStream(source))).Chunk();
        chunk.accept(new NameResolver());

        Map<Variable, Exp.Constant> constantLocals = new IdentityHashMap<>();
        chunk.accept(new Visitor() {
            @Override
            public void visit(Stat.LocalAssign stat) {
                if (stat.values != null)
                    for (int i = 0; i < Math.min(stat.names.size(), stat.values.size()); i++)
                        if (stat.values.get(i) instanceof Exp.Constant constant && constant.value instanceof LuaString)
                            constantLocals.put(((Name) stat.names.get(i)).variable, constant);
                super.visit(stat);
            }
        });

        Set<Exp.Constant> literals = Collections.newSetFromMap(new IdentityHashMap<>());
        chunk.accept(new Visitor() {
            @Override
            public void visit(Exp.MethodCall call) {
                Integer textArguments = TEXT_ARGUMENTS.get(call.name);
                if (textArguments != null && call.args.exps != null)
                    for (int i = 0; i < Math.min(textArguments, call.args.exps.size()); i++)
                        collect((Exp) call.args.exps.get(i), constantLocals, literals);
                super.visit(call);
            }
        });
        return literals;
    }

    private static void collect(Exp exp, Map<Variable, Exp.Constant> constantLocals, Set<Exp.Constant> literals) {
        if (exp instanceof Exp.Constant constant) {
            if (constant.value instanceof LuaString) literals.add(constant);
        } else if (exp instanceof Exp.ParensExp parens) {
            collect(parens.exp, constantLocals, literals);
        } else if (exp instanceof Exp.BinopExp binop) {
            // The left of "and" is a condition, whose literals are compared rather than shown.
            if (binop.op == Lua.OP_CONCAT || binop.op == Lua.OP_OR) collect(binop.lhs, constantLocals, literals);
            if (binop.op == Lua.OP_CONCAT || binop.op == Lua.OP_OR || binop.op == Lua.OP_AND)
                collect(binop.rhs, constantLocals, literals);
        } else if (exp instanceof Exp.NameExp name && name.name.variable != null && name.name.variable.isConstant()) {
            Exp.Constant initial = constantLocals.get(name.name.variable);
            if (initial != null) literals.add(initial);
        }
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

    private record Replacement(int end, String literal) {
    }

    /** LuaJ's lexer counts a tab as eight columns; one column per character lets positions index the source. */
    private static final class SourceStream extends SimpleCharStream {
        SourceStream(String source) {
            super(new StringReader(source));
            tabSize = 1;
        }
    }
}
