package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.advancedcombat.progression.InputProfile;

import java.util.ArrayList;
import java.util.List;

final class ClassMenuText {
    private ClassMenuText() {
    }

    static String inputName(InputProfile profile) {
        return switch (profile) {
            case JAVA_HOTBAR_LAYER -> "F ability layer";
        };
    }

    static List<String> wrap(String prefix, String text) {
        List<String> output = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (!line.isEmpty() && line.length() + word.length() + 1 > 44) {
                output.add(prefix + line);
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) output.add(prefix + line);
        return output;
    }
}
