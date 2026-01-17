package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptDouble;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptVector;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MapListInterpreter {
    private static void parsingErrorMessage(String key, Object value, String scriptName) {
        Logger.warn("Failed to read value " + value + " for key " + key + " in script " + scriptName);
    }

    public static String parseString(String key, Object value, String scriptName) {
        return ChatColorConverter.convert(value + "");
    }

    public static List<String> parseStringList(String key, Object value, String scriptName) {
        if (value == null) return new ArrayList<>();
        try {
            return ChatColorConverter.convert((List) value);
        } catch (Exception ex) {
            Logger.warn("Failed to get string list for key " + key + " with value " + value + " in script " + scriptName);
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
                Logger.warn("Failed to get integer value from " + value + " in script " + scriptName);
                return null;
            }
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
    }

    public static ScriptInteger parseScriptInteger(String key, Object value, String scriptName) {
        try {
            if (value instanceof Integer intValue)
                return new ScriptInteger(intValue);
            else if (value instanceof String stringValue) {
                if (stringValue.contains("~")) {
                    String[] strings = stringValue.split("~");
                    return new ScriptInteger(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
                }
                return new ScriptInteger(Integer.parseInt(stringValue));
            } else {
                Logger.warn("Failed to get integer value from " + value + " in script " + scriptName);
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
                return integer.doubleValue();
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

    public static ScriptDouble parseScriptDouble(String key, Object value, String scriptName) {
        try {
            if (value instanceof Integer integer)
                return new ScriptDouble(integer.doubleValue());
            else if (value instanceof Double dbl)
                return new ScriptDouble(dbl);
            else if (value instanceof String string) {
                if (((String) value).contains("~")) {
                    String[] strings = ((String) value).split("~");
                    return new ScriptDouble(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]));
                }
                return new ScriptDouble(Double.parseDouble(string));
            }
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
        Logger.warn("Failed to parse " + value + " as double in " + scriptName + " for key " + key);
        return null;
    }

    public static Float parseFloat(String key, Object value, String scriptName) {
        try {
            if (value instanceof Integer integer)
                return integer.floatValue();
            else if (value instanceof Double dbl)
                return dbl.floatValue();
            else if (value instanceof String string)
                return Float.parseFloat(string);
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
        return null;
    }

    public static ScriptFloat parseScriptFloat(String key, Object value, String scriptName) {
        try {
            if (value instanceof Integer integer)
                return new ScriptFloat(integer.floatValue());
            else if (value instanceof Double dbl)
                return new ScriptFloat(dbl.floatValue());
            else if (value instanceof Float flt)
                return new ScriptFloat(flt);
            else if (value instanceof String string) {
                if (((String) value).contains("~")) {
                    String[] strings = ((String) value).split("~");
                    return new ScriptFloat(Float.parseFloat(strings[0]), Float.parseFloat(strings[1]));
                }
                return new ScriptFloat(Float.parseFloat(string));
            }
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
            return null;
        }
        Logger.warn("Failed to parse " + value + " as double in " + scriptName + " for key " + key);
        return null;
    }

    public static <T extends Enum<T>> T parseEnum(String key, Object value, Class<T> enumClass, String scriptName) {
        if (enumClass.isAssignableFrom(Particle.class))
            value = LegacyValueConverter.parseParticle((String) value);
        else if (enumClass.isAssignableFrom(PotionEffectType.class))
            value = LegacyValueConverter.parsePotionEffect((String) value);
        else if (enumClass.isAssignableFrom(Material.class))
            value = LegacyValueConverter.parseMaterial((String) value);
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
                        Logger.warn("Expected string, got something else!");
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
                        Logger.warn("Expected list of list, got something else!");
                        parsingErrorMessage(key, value, scriptName);
                        return parsedList;
                    }
                    List<T> lastList = new ArrayList<>();
                    for (Object rawEnum : (List) rawLastList) {
                        if (!(rawEnum instanceof String)) {
                            Logger.warn("Expected string, got something else!");
                            parsingErrorMessage(key, value, scriptName);
                            continue;
                        }
                        lastList.add(Enum.valueOf(enumClass, (String) rawEnum));
                    }
                    parsedList.add(lastList);
                }

            } else {
                Logger.warn("Expected list, got something else!");
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

    public static ScriptVector parseScriptVector(String key, Object value, String scriptName) {
        try {
            String[] strings = ((String) value).split(",");
            return new ScriptVector(parseScriptFloat(key, strings[0], scriptName), parseScriptFloat(key, strings[1], scriptName), parseScriptFloat(key, strings[2], scriptName));
        } catch (Exception ex) {
            parsingErrorMessage(key, value, scriptName);
        }
        return new ScriptVector(new ScriptFloat(0), new ScriptFloat(0), new ScriptFloat(0));
    }
}
