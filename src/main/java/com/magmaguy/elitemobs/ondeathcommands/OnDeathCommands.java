package com.magmaguy.elitemobs.ondeathcommands;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class OnDeathCommands implements Listener {

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (MobCombatSettingsConfig.commandsOnDeath.isEmpty()) return;
        parseConsoleCommand(MobCombatSettingsConfig.commandsOnDeath, event);
    }

    public static void parseConsoleCommand(List<String> configStrings, EliteMobDeathEvent event) {
        for (String string : configStrings) {
            if (string.contains("$level"))
                string = string.replace("$level", event.getEliteMobEntity().getLevel() + "");
            if (string.contains("$name"))
                string = string.replace("$name", event.getEliteMobEntity().getName());
            if (string.contains("$locationWorldName"))
                string = string.replace("$locationWorldName", event.getEliteMobEntity().getLivingEntity().getLocation().getWorld().getName());
            if (string.contains("$locationX"))
                string = string.replace("$locationX", event.getEliteMobEntity().getLivingEntity().getLocation().getX() + "");
            if (string.contains("$locationY"))
                string = string.replace("$locationY", event.getEliteMobEntity().getLivingEntity().getLocation().getY() + "");
            if (string.contains("$locationZ"))
                string = string.replace("$locationZ", event.getEliteMobEntity().getLivingEntity().getLocation().getZ() + "");
            if (string.contains("$players")) {
                if (event.getEliteMobEntity().hasDamagers())
                    for (Player player : event.getEliteMobEntity().getDamagers().keySet())
                        runConsoleCommand(string.replace("$players", player.getName()));
            }
            runConsoleCommand(string);
        }
    }

    private static void runConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

}
