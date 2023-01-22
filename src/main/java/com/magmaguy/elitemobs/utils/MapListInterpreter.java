package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MapListInterpreter {
    private static void parsingErrorMessage(String key, Object value, String scriptName) {
        new WarningMessage("Failed to read value " + value + " for key " + key + " in script " + scriptName);
    }

    public static String parseString(String key, Object value, String scriptName) {
        return ChatColorConverter.convert(value + "");
    }

    public static List<String> parseStringList(String key, Object value, String scriptName) {
        if (value == null) return new ArrayList<>();
        try {
            return ChatColorConverter.convert((List) value);
        } catch (Exception ex) {
            new WarningMessage("Failed to get string list for key " + key + " with value " + value + " in script " + scriptName);
            return new ArrayList<>();
        }
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
            else if (value instanceof String stringValue) {
                if (stringValue.contains("~")) {
                    String[] strings = stringValue.split("~");
                    return ThreadLocalRandom.current().nextInt(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]) + 1);
                }
                return Integer.parseInt(stringValue);
            } else {
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
            if (value instanceof Integer integer)
                return integer + 0D;
            else if (value instanceof Double dbl)
                return dbl;
            else if (value instanceof String string)
                return Double.parseDouble(string);
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
        return null;
    }

    public static <T extends Enum<T>> T parseEnum(String key, Object value, Class<T> enumClass, String scriptName) {
        try {
            return Enum.valueOf(enumClass, (String) value);
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
        }
        return null;
    }

    public static <T extends Enum<T>> List<T> parseEnumList(String key, Object value, Class<T> enumClass, String scriptName) {
        try {
            if (value instanceof List<?> valueList) {
                List<T> parsedList = new ArrayList<>();
                for (Object rawEnum : valueList) {
                    if (!(rawEnum instanceof String)) {
                        new WarningMessage("Expected string, got something else!");
                        parsingErrorMessage(key, value, scriptName);
                        continue;
                    }
                    parsedList.add(Enum.valueOf(enumClass, (String) rawEnum));
                }
                return parsedList;
            } else {
                parsingErrorMessage(key, value, scriptName);
                return null;
            }
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
        }
        return null;
    }


    public static <T extends Enum<T>> List<List<T>> parseEnumListList(String key, Object value, Class<T> enumClass, String scriptName) {
        List<List<T>> parsedList = new ArrayList<>();
        try {
            if (value instanceof List<?> valueList) {
                for (Object rawLastList : valueList) {
                    if (!(rawLastList instanceof List<?>)) {
                        new WarningMessage("Expected list of list, got something else!");
                        parsingErrorMessage(key, value, scriptName);
                        return parsedList;
                    }
                    List<T> lastList = new ArrayList<>();
                    for (Object rawEnum : (List) rawLastList) {
                        if (!(rawEnum instanceof String)) {
                            new WarningMessage("Expected string, got something else!");
                            parsingErrorMessage(key, value, scriptName);
                            continue;
                        }
                        lastList.add(Enum.valueOf(enumClass, (String) rawEnum));
                    }
                    parsedList.add(lastList);
                }

            } else {
                new WarningMessage("Expected list, got something else!");
                parsingErrorMessage(key, value, scriptName);
                return parsedList;
            }
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return parsedList;
        }
        return parsedList;
    }

    public static Vector parseVector(String key, Object value, String scriptName) {
        try {
            String[] strings = ((String) value).split(",");
            return new Vector(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]));
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
        }
        return new Vector(0, 0, 0);
    }
}
