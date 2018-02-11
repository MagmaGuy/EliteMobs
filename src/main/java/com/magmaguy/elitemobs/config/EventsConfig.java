/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

public class EventsConfig {

    private CustomConfigLoader customConfigLoader = new CustomConfigLoader();

    public static final String ENABLE_EVENTS = "Enable events";
    public static final String MINIMUM_ONLINE_PLAYERS = "Minimum amount of online players for event to trigger";
    public static final String MAXIMUM_ONLINE_PLAYERS = "Maximum amount of online players after which the event frequency won't increase";
    public static final String MINIMUM_EVENT_FREQUENCY = "Minimum event frequency (minutes)";
    public static final String MAXIMUM_EVENT_FREQUENCY = "Maximum event frequency (minutes)";
    public static final String ENABLED_EVENTS = "Enabled events";
    public static final String TREASURE_GOBLIN_SMALL = "Small treasure goblin";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_ANNOUNCEMENT_TEXT = "Small treasure goblin event announcement text";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_PLAYER_END_TEXT = "Small treasure goblin killed by players message";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_OTHER_END_TEXT = "Small treasure goblin nondescript death message";

    public void initializeEventsConfig() {

        this.getEventsConfig().addDefault(ENABLE_EVENTS, true);
        this.getEventsConfig().addDefault(MINIMUM_ONLINE_PLAYERS, 2);
        this.getEventsConfig().addDefault(MAXIMUM_ONLINE_PLAYERS, 100);
        this.getEventsConfig().addDefault(MINIMUM_EVENT_FREQUENCY, 30);
        this.getEventsConfig().addDefault(MAXIMUM_EVENT_FREQUENCY, 10);
        this.getEventsConfig().addDefault(ENABLED_EVENTS + "." + TREASURE_GOBLIN_SMALL, true);
        this.getEventsConfig().addDefault(SMALL_TREASURE_GOBLIN_EVENT_ANNOUNCEMENT_TEXT, "A treasure goblin has appeared in $world at $location\n" +
                "Make sure to take a hunting party with you!");
        this.getEventsConfig().addDefault(SMALL_TREASURE_GOBLIN_EVENT_PLAYER_END_TEXT, "A Treasure Goblin has been slain by $player!");
        this.getEventsConfig().addDefault(SMALL_TREASURE_GOBLIN_EVENT_OTHER_END_TEXT, "A Treasure Goblin has been slain!");

        getEventsConfig().options().copyDefaults(true);
        saveDefaultCustomConfig();
        saveCustomConfig();

    }

    public FileConfiguration getEventsConfig() {

        return customConfigLoader.getCustomConfig("events.yml");

    }

    public void reloadCustomConfig() {

        customConfigLoader.reloadCustomConfig("events.yml");

    }

    public void saveCustomConfig() {

        customConfigLoader.saveCustomDefaultConfig("events.yml");

    }

    public void saveDefaultCustomConfig() {

        customConfigLoader.saveDefaultCustomConfig("events.yml");

    }

}
