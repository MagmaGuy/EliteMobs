package com.magmaguy.elitemobs.ondeathcommands;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class OnDeathCommands implements Listener {

    public static void parseConsoleCommand(List<String> configStrings, EliteMobDeathEvent event) {
        Player topDamager = null, secondDamager = null, thirdDamager = null;

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
            if (string.contains("$level"))
                string = string.replace("$level", event.getEliteEntity().getLevel() + "");
            if (string.contains("$name"))
                string = string.replace("$name", event.getEliteEntity().getName());
            if (string.contains("$locationWorldName"))
                string = string.replace("$locationWorldName", event.getEliteEntity().getLivingEntity().getLocation().getWorld().getName());
            if (string.contains("$locationX"))
                string = string.replace("$locationX", event.getEliteEntity().getLivingEntity().getLocation().getX() + "");
            if (string.contains("$locationY"))
                string = string.replace("$locationY", event.getEliteEntity().getLivingEntity().getLocation().getY() + "");
            if (string.contains("$locationZ"))
                string = string.replace("$locationZ", event.getEliteEntity().getLivingEntity().getLocation().getZ() + "");
            if (string.contains("$damager1name"))
                if (topDamager != null)
                    string = string.replace("$damager1name", topDamager.getName());
                else
                    string = "";
            if (string.contains("$damager2name"))
                if (secondDamager != null)
                    string = string.replace("$damager2name", secondDamager.getName());
                else
                    string = "";
            if (string.contains("$damager3name"))
                if (thirdDamager != null)
                    string = string.replace("$damager3name", thirdDamager.getName());
                else
                    string = "";
            if (string.contains("$players")) {
                if (event.getEliteEntity().hasDamagers())
                    for (Player player : event.getEliteEntity().getDamagers().keySet())
                        runConsoleCommand(string.replace("$players", player.getName()));
            }
            runConsoleCommand(string);
        }
    }

    private static HashMap<Player, Double> sortByComparator(HashMap<Player, Double> unsortMap, final boolean order) {

        List<Map.Entry<Player, Double>> list = new LinkedList<Map.Entry<Player, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Player, Double>>() {
            public int compare(Map.Entry<Player, Double> o1,
                               Map.Entry<Player, Double> o2) {
                if (order)
                    return o1.getValue().compareTo(o2.getValue());
                else
                    return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Player, Double> sortedMap = new LinkedHashMap<Player, Double>();
        for (Map.Entry<Player, Double> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());

        return sortedMap;
    }

    private static void runConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (MobCombatSettingsConfig.commandsOnDeath.isEmpty()) return;
        parseConsoleCommand(MobCombatSettingsConfig.commandsOnDeath, event);
    }

}
