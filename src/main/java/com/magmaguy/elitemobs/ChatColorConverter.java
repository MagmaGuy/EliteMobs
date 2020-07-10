package com.magmaguy.elitemobs;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 13/05/2017.
 */
public class ChatColorConverter {

    public static String convert(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> convert(List<String> list) {
        List<String> convertedList = new ArrayList<>();
        for (String string : list)
            convertedList.add(convert(string));
        return convertedList;
    }

}
