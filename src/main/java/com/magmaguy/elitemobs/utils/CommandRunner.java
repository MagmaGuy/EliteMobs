package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

import java.util.List;

public class CommandRunner {

    public class ConfigPlaceholder {
        private final String placeholder;
        private final String value;

        public ConfigPlaceholder(String placeholder, String value) {
            this.placeholder = placeholder;
            this.value = value;
        }
    }

    public static void runCommandFromList(List<String> commands, List<ConfigPlaceholder> placeholders) {
        if (commands == null || commands.isEmpty()) return;
        for (String string : commands)
            if (placeholders.isEmpty())
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), string);
            else {
                for (ConfigPlaceholder configPlaceholder : placeholders)
                    string = string.replace(configPlaceholder.placeholder, configPlaceholder.value);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), string);
            }
    }
}
