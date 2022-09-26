package com.magmaguy.elitemobs.utils;

import java.util.List;

public class MapListInterpreter {
    private static void parsingErrorMessage(String key, Object value, String scriptName) {
        new WarningMessage("Failed to read value " + value + " for key " + key + " in script " + scriptName);
    }

    public static String parseString(String key, Object value, String scriptName) {
        return (String) value;
    }

    public static List<String> parseStringList(String key, Object value, String scriptName) {
        return (List<String>) value;
    }

    public static Boolean parseBoolean(String key, Object value, String scriptName) {
        try {
            if (value instanceof Boolean boo)
                return boo;
            else
                return Boolean.parseBoolean((String) value);
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
    }

    public static Integer parseInteger(String key, Object value, String scriptName) {
        try {
            if (value instanceof Integer intValue)
                return intValue;
            else if (value instanceof String stringValue)
                return Integer.parseInt(stringValue);
            else {
                new WarningMessage("Failed to get integer value from " + value + " in script " + scriptName);
                return null;
            }
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
    }


    public static Double parseDouble(String key, Object value, String scriptName) {
        try {
            return (Double) value;
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
    }

    public static <T extends Enum<T>> T parseEnum(String key, Object value, Class<T> enumClass, String scriptName) {
        try {
            return Enum.valueOf(enumClass, (String) value);
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
        }
        return null;
    }
}
