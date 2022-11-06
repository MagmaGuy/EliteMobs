package com.magmaguy.elitemobs;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 13/05/2017.
 */
public class ChatColorConverter {

    private ChatColorConverter() {
    }

    public static String convert(String string) {
        if (string == null) return "";
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> convert(List<?> list) {
        List<String> convertedList = new ArrayList<>();
        for (Object value : list)
            convertedList.add(convert(value + ""));
        return convertedList;
    }

}
