package com.magmaguy.elitemobs.powers.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LuaPowerTableConverter {

    private LuaPowerTableConverter() {
    }

    static Object toJavaObject(LuaValue value) {
        if (value == null || value.isnil()) {
            return null;
        }
        if (value.isboolean()) {
            return value.toboolean();
        }
        if (value.isnumber()) {
            double number = value.todouble();
            int integer = (int) number;
            if (Double.compare(number, integer) == 0) {
                return integer;
            }
            return number;
        }
        if (value.isstring()) {
            return value.tojstring();
        }
        if (value.istable()) {
            LuaTable table = value.checktable();
            return isArrayTable(table) ? toJavaList(table) : toJavaMap(table);
        }
        return value.tojstring();
    }

    static Map<String, Object> toJavaMap(LuaTable table) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = table.next(key);
            key = next.arg1();
            if (key.isnil()) {
                break;
            }
            if (key.isstring()) {
                result.put(key.tojstring(), toJavaObject(next.arg(2)));
            } else if (key.isnumber()) {
                result.put(String.valueOf(key.toint()), toJavaObject(next.arg(2)));
            }
        }
        return result;
    }

    private static List<Object> toJavaList(LuaTable table) {
        List<Object> result = new ArrayList<>();
        int length = table.length();
        for (int index = 1; index <= length; index++) {
            result.add(toJavaObject(table.get(index)));
        }
        return result;
    }

    private static boolean isArrayTable(LuaTable table) {
        int numericEntries = 0;
        int highestIndex = 0;

        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = table.next(key);
            key = next.arg1();
            if (key.isnil()) {
                break;
            }
            if (!key.isnumber()) {
                return false;
            }
            int index = key.toint();
            if (index <= 0) {
                return false;
            }
            numericEntries++;
            highestIndex = Math.max(highestIndex, index);
        }

        return numericEntries == highestIndex;
    }
}
