package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.events.timedevents.DeadMoonEvent;
import com.magmaguy.elitemobs.events.timedevents.MeteorEvent;
import com.magmaguy.elitemobs.events.timedevents.SmallTreasureGoblinEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.events.premade.DeadMoonEventConfig;
import com.magmaguy.elitemobs.config.events.premade.MeteorEventConfig;
import com.magmaguy.elitemobs.config.events.premade.SmallTreasureGoblinEventConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

import static com.magmaguy.elitemobs.utils.WeightedProbability.pickWeighedProbability;

public class EventLauncher {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    public void eventRepeatingTask() {

        if (!ConfigValues.eventsConfig.getBoolean(EventsConfig.ENABLE_EVENTS)) return;

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (verifyPlayerCount()) {

                    //if enough time has passed, run the event and reset the counter
                    if (timeCheck(counter)) {
                        eventSelector();
                        counter = 0;
                        return;
                    }

                    counter++;

                }

            }

        }.runTaskTimer(plugin, 20 * 60, 20 * 60);

    }

    public static boolean verifyPlayerCount() {

        return ConfigValues.eventsConfig.getInt(EventsConfig.MINIMUM_ONLINE_PLAYERS) <= Bukkit.getOnlinePlayers().size();

    }

    private boolean timeCheck(int counter) {

        if (ConfigValues.eventsConfig.getInt(EventsConfig.MAXIMUM_ONLINE_PLAYERS) <= Bukkit.getOnlinePlayers().size()) {

            return counter >= ConfigValues.eventsConfig.getInt(EventsConfig.MAXIMUM_EVENT_FREQUENCY);

        }

        if (ConfigValues.eventsConfig.getInt(EventsConfig.MINIMUM_ONLINE_PLAYERS) == Bukkit.getOnlinePlayers().size()) {

            return counter >= ConfigValues.eventsConfig.getInt(EventsConfig.MINIMUM_EVENT_FREQUENCY);

        }

        int minPlayers = ConfigValues.eventsConfig.getInt(EventsConfig.MINIMUM_ONLINE_PLAYERS);
        int maxPlayers = ConfigValues.eventsConfig.getInt(EventsConfig.MAXIMUM_ONLINE_PLAYERS);
        double minFrequency = ConfigValues.eventsConfig.getInt(EventsConfig.MINIMUM_EVENT_FREQUENCY);
        double maxFrequency = ConfigValues.eventsConfig.getInt(EventsConfig.MAXIMUM_EVENT_FREQUENCY);

        //y=mx slope = m
        double slope = ((maxFrequency - minFrequency) / (maxPlayers - minPlayers));

        double newTimerValue = slope * Bukkit.getOnlinePlayers().size() + minFrequency;

        return counter >= newTimerValue;

    }

    private void eventSelector() {

        //todo: once more events are added, randomize which one gets picked in a weighed fashion
        HashMap<String, Double> weighedConfigValues = new HashMap<>();
        if (SmallTreasureGoblinEventConfig.isEnabled)
            weighedConfigValues.put("smalltreasuregoblin", SmallTreasureGoblinEventConfig.weight);

        if (DeadMoonEventConfig.isEnabled)
            weighedConfigValues.put("deadmoon", DeadMoonEventConfig.weight);

        if (MeteorEventConfig.isEnabled)
            weighedConfigValues.put("meteor", MeteorEventConfig.weight);

        if (weighedConfigValues.isEmpty()) return;

        switch (pickWeighedProbability(weighedConfigValues)) {

            case "smalltreasuregoblin":
                new SmallTreasureGoblinEvent();
                return;
            case "deadmoon":
                new DeadMoonEvent();
                return;
            case "meteor":
                new MeteorEvent();
                return;
        }

    }


}
