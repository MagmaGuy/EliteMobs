package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CommandRunner {

    public static void runCommandFromList(List<String> commands, List<ConfigPlaceholder> placeholders) {
        if (commands == null || commands.isEmpty()) return;
        for (String string : commands)
            if (placeholders.isEmpty()) {
                OnDeathCommands.RunChance runChance = new OnDeathCommands.RunChance(string);
                string = runChance.getString();
                if (ThreadLocalRandom.current().nextDouble() < runChance.getChance())
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), string);
            } else {
                for (ConfigPlaceholder configPlaceholder : placeholders)
                    string = string.replace(configPlaceholder.placeholder, configPlaceholder.value);
                OnDeathCommands.RunChance runChance = new OnDeathCommands.RunChance(string);
                string = runChance.getString();
                if (ThreadLocalRandom.current().nextDouble() < runChance.getChance())
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), string);
            }
    }

    public class ConfigPlaceholder {
        private final String placeholder;
        private final String value;

        public ConfigPlaceholder(String placeholder, String value) {
            this.placeholder = placeholder;
            this.value = value;
        }
    }
}
