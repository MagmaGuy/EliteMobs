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

import org.bukkit.configuration.Configuration;

public class EventsConfig {

    public static final String CONFIG_NAME = "events.yml";
    public static final String ENABLE_EVENTS = "Enable events";
    public static final String MINIMUM_ONLINE_PLAYERS = "Minimum amount of online players for event to trigger";
    public static final String MAXIMUM_ONLINE_PLAYERS = "Maximum amount of online players after which the event frequency won't increase";
    public static final String MINIMUM_EVENT_FREQUENCY = "Minimum event frequency (minutes)";
    public static final String MAXIMUM_EVENT_FREQUENCY = "Maximum event frequency (minutes)";
    private static final String ENABLED_EVENTS = "Enabled events.";
    public static final String TREASURE_GOBLIN_SMALL_ENABLED = ENABLED_EVENTS + "Small treasure goblin";
    public static final String DEAD_MOON_ENABLED = ENABLED_EVENTS + "Dead moon";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_ANNOUNCEMENT_TEXT = "Small treasure goblin event announcement text";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_PLAYER_END_TEXT = "Small treasure goblin killed by players message";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_OTHER_END_TEXT = "Small treasure goblin nondescript death message";
    public static final String SMALL_TREASURE_GOBLIN_REWARD = "Treasure goblin extra loot drop amount";
    public static final String DEAD_MOON_EVENT_ANNOUNCEMENT_TEXT = "Dead Moon event announcement text";
    public static final String DEAD_MOON_EVENT_PLAYER_END_TEXT = "Zombie King killed by players message";
    public static final String DEAD_MOON_EVENT_OTHER_END_TEXT = "Zombie King nondescript death message";
    private static final String DEAD_MOON_NAMES = "Dead moon names.";
    public static final String DEAD_MOON_THE_RETURNED_NAME = DEAD_MOON_NAMES + "Name of The Returned";
    public static final String DEAD_MOON_ZOMBIE_KING_NAME = DEAD_MOON_NAMES + "Name of the Zombie King";
    private static final String EVENT_WEIGHT = "Event weight.";
    public static final String SMALL_TREASURE_GOBLIN_EVENT_WEIGHT = EVENT_WEIGHT + "Small treasure goblin";
    public static final String DEAD_MOON_EVENT_WEIGHT = EVENT_WEIGHT + "Blood moon";

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(ENABLE_EVENTS, true);
        configuration.addDefault(MINIMUM_ONLINE_PLAYERS, 2);
        configuration.addDefault(MAXIMUM_ONLINE_PLAYERS, 100);
        configuration.addDefault(MINIMUM_EVENT_FREQUENCY, 45);
        configuration.addDefault(MAXIMUM_EVENT_FREQUENCY, 10);
        configuration.addDefault(TREASURE_GOBLIN_SMALL_ENABLED, true);
        configuration.addDefault(DEAD_MOON_ENABLED, true);
        configuration.addDefault(SMALL_TREASURE_GOBLIN_EVENT_ANNOUNCEMENT_TEXT, "&7&m-----------------------------------------------------\n&7[&aEvent&7] &fA &ctreasure goblin &fhas appeared in&c $world &fat&c $location \n &fMake sure to take a hunting party with you!\n&7&m-----------------------------------------------------");
        configuration.addDefault(SMALL_TREASURE_GOBLIN_EVENT_PLAYER_END_TEXT, "A Treasure Goblin has been slain by $player!");
        configuration.addDefault(SMALL_TREASURE_GOBLIN_EVENT_OTHER_END_TEXT, "A Treasure Goblin has been slain!");
        configuration.addDefault(SMALL_TREASURE_GOBLIN_REWARD, 10);
        configuration.addDefault(DEAD_MOON_EVENT_ANNOUNCEMENT_TEXT, "&7&m-----------------------------------------------------\n&7[&aEvent&7] &fA &cZombie King &fhas appeared in&c $world &fat&c $location \n &fMake sure to take a hunting party with you!\n&7&m-----------------------------------------------------");
        configuration.addDefault(DEAD_MOON_EVENT_PLAYER_END_TEXT, "The Zombie King has been slain by $player!");
        configuration.addDefault(DEAD_MOON_EVENT_OTHER_END_TEXT, "The Zombie King has been slain!");
        configuration.addDefault(DEAD_MOON_THE_RETURNED_NAME, "&2The Returned");
        configuration.addDefault(DEAD_MOON_ZOMBIE_KING_NAME, "&4Zombie King");
        configuration.addDefault(SMALL_TREASURE_GOBLIN_EVENT_WEIGHT, 40);
        configuration.addDefault(DEAD_MOON_EVENT_WEIGHT, 20);
        configuration.options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
