package com.magmaguy.elitemobs.powers.lua;

import lombok.Getter;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.util.EnumSet;

@Getter
public class LuaPowerDefinition {

    private final String fileName;
    private final File sourceFile;
    private final String source;
    private final int priority;
    private final EnumSet<LuaPowerHook> hooks;

    public LuaPowerDefinition(String fileName, File sourceFile, String source, int priority, EnumSet<LuaPowerHook> hooks) {
        this.fileName = fileName;
        this.sourceFile = sourceFile;
        this.source = source;
        this.priority = priority;
        this.hooks = hooks.clone();
    }

    public static LuaPowerDefinition validate(String fileName, File sourceFile, String source) {
        Globals globals = LuaPowerEnvironmentFactory.createGlobals();
        LuaTable scriptTable = evaluate(fileName, source, globals);
        int apiVersion = extractIntField(scriptTable, "api_version", fileName, true);
        if (apiVersion != 1) {
            throw new IllegalArgumentException("Lua power " + fileName + " has unsupported api_version " + apiVersion + ". Expected 1.");
        }

        int priority = extractIntField(scriptTable, "priority", fileName, false);
        EnumSet<LuaPowerHook> hooks = EnumSet.noneOf(LuaPowerHook.class);
        LuaValue currentKey = LuaValue.NIL;
        while (true) {
            Varargs next = scriptTable.next(currentKey);
            currentKey = next.arg1();
            if (currentKey.isnil()) {
                break;
            }
            String key = currentKey.checkjstring();
            if ("api_version".equals(key) || "priority".equals(key)) {
                continue;
            }
            LuaPowerHook hook = LuaPowerHook.fromKey(key);
            if (hook == null) {
                throw new IllegalArgumentException("Lua power " + fileName + " contains unsupported top-level key '" + key + "'.");
            }
            if (!(scriptTable.get(key) instanceof LuaFunction)) {
                throw new IllegalArgumentException("Lua power " + fileName + " key '" + key + "' must be a function.");
            }
            hooks.add(hook);
        }

        return new LuaPowerDefinition(fileName, sourceFile, source, priority, hooks);
    }

    public LuaTable instantiate() {
        return evaluate(fileName, source, LuaPowerEnvironmentFactory.createGlobals());
    }

    public boolean supportsHook(LuaPowerHook hook) {
        return hook != null && hooks.contains(hook);
    }

    private static LuaTable evaluate(String fileName, String source, Globals globals) {
        LuaValue chunk = globals.load(source, fileName);
        LuaValue result = chunk.call();
        if (!(result instanceof LuaTable scriptTable)) {
            throw new IllegalArgumentException("Lua power " + fileName + " must return a table.");
        }
        return scriptTable;
    }

    private static int extractIntField(LuaTable table, String key, String fileName, boolean required) {
        LuaValue value = table.get(key);
        if (value.isnil()) {
            if (required) {
                throw new IllegalArgumentException("Lua power " + fileName + " is missing required field '" + key + "'.");
            }
            return 0;
        }
        if (!value.isnumber()) {
            throw new IllegalArgumentException("Lua power " + fileName + " field '" + key + "' must be a number.");
        }
        return value.toint();
    }
}
