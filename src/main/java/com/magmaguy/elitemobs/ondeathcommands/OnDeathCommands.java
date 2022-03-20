package com.magmaguy.elitemobs.ondeathcommands;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class OnDeathCommands implements Listener {

    public static void parseConsoleCommand(List<String> configStrings, EliteMobDeathEvent event) {
        Player topDamager = null;
        Player secondDamager = null;
        Player thirdDamager = null;
        HashMap<Player, Double> sortedMap = sortByComparator(event.getEliteEntity().getDamagers(), false);
        Iterator<Player> sortedMapIterator = sortedMap.keySet().iterator();
        for (int i = 1; i < 4; i++) {
            if (i > sortedMap.size())
                break;
            Player nextPlayer = sortedMapIterator.next();
            switch (i) {
                case 1:
                    topDamager = nextPlayer;
                    break;
                case 2:
                    secondDamager = nextPlayer;
                    break;
                case 3:
                    thirdDamager = nextPlayer;
                    break;
            }
        }
        for (String string : configStrings) {
            double chance = 1;
            string = parseCommonCommandStrings(string, event.getEliteEntity());
            if (string.contains("$damager1name")) {
                if (topDamager != null)
                    string = string.replace("$damager1name", topDamager.getName());
                else
                    string = "";
            }
            if (string.contains("$damager2name")) {
                if (secondDamager != null)
                    string = string.replace("$damager2name", secondDamager.getName());
                else
                    string = "";
            }
            if (string.contains("$damager3name")) {
                if (thirdDamager != null)
                    string = string.replace("$damager3name", thirdDamager.getName());
                else
                    string = "";
            }
            RunChance runChance = new RunChance(string);
            string = runChance.getString();
            chance = runChance.getChance();
            if (string.contains("$players")) {
                if (event.getEliteEntity().hasDamagers())
                    for (Player player : event.getEliteEntity().getDamagers().keySet())
                        if (chance == 1 || ThreadLocalRandom.current().nextDouble() < chance)
                            runConsoleCommand(string.replace("$players", player.getName()));
                        else return;
            } else if (chance == 1 || ThreadLocalRandom.current().nextDouble() < chance) runConsoleCommand(string);
        }
    }

    public static String parseCommonCommandStrings(String string, EliteEntity eliteEntity) {
        if (string.contains("$level"))
            string = string.replace("$level", eliteEntity.getLevel() + "");
        if (string.contains("$name"))
            string = string.replace("$name", eliteEntity.getName());
        if (eliteEntity.getLocation() != null) {
            if (string.contains("$locationWorldName") && eliteEntity.getLocation() != null)
                string = string.replace("$locationWorldName", Objects.requireNonNull(eliteEntity.getLocation().getWorld()).getName());
            if (string.contains("$locationX"))
                string = string.replace("$locationX", eliteEntity.getLocation().getX() + "");
            if (string.contains("$locationY"))
                string = string.replace("$locationY", eliteEntity.getLocation().getY() + "");
            if (string.contains("$locationZ"))
                string = string.replace("$locationZ", eliteEntity.getLocation().getZ() + "");
        }
        return string;
    }

    private static HashMap<Player, Double> sortByComparator(Map<Player, Double> unsortMap, final boolean order) {

        List<Map.Entry<Player, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> {
            if (order)
                return o1.getValue().compareTo(o2.getValue());
            else
                return o2.getValue().compareTo(o1.getValue());
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Player, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Player, Double> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());

        return sortedMap;
    }

    private static void runConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (MobCombatSettingsConfig.getCommandsOnDeath().isEmpty()) return;
        parseConsoleCommand(MobCombatSettingsConfig.getCommandsOnDeath(), event);
    }

    public static class RunChance {
        @Getter
        private double chance = 1;
        @Getter
        private String string;

        public RunChance(String commandString) {
            this.string = commandString;
            if (string.contains("$chance=")) {
                try {
                    String chanceString = string.split("\\$chance=")[1];
                    chanceString = chanceString.split("\\$")[0];
                    chance = Double.parseDouble(chanceString);
                    String fullString = "$chance=" + chanceString + "$";
                    string = string.replace(fullString, "");
                } catch (Exception ex) {
                    new WarningMessage("Failed to get the chance value of the command " + string + " . The correct format should be $chance=X.Y$", true);
                }
            }
        }
    }

}
