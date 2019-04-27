package com.magmaguy.elitemobs.utils;

public class ObfuscatedStringHandler {

    public static String obfuscateString(String originalString) {

        String insert = "§";
        int period = 1;

        StringBuilder stringBuilder = new StringBuilder(originalString.length() + insert.length() * (originalString.length() / period));

        int index = 0;
        String prefix = "";

        while (index < originalString.length()) {

            stringBuilder.append(prefix);
            prefix = insert;
            stringBuilder.append(originalString, index, Math.min(index + period, originalString.length()));
            index += period;

        }

        String finalString = stringBuilder.toString();

        finalString = "§" + finalString;

        return finalString;

    }

    public static boolean checkString(String obfuscatedString, String targetString) {
        return deobfuscateString(obfuscatedString).contains(targetString);
    }

    public static String deobfuscateString(String obfuscatedString) {
        return obfuscatedString.replace("§", "");
    }

}
