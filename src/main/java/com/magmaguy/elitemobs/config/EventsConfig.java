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
    public static final String KRAKEN_ENABLED = ENABLED_EVENTS + "Kraken";
    public static final String BALROG_ENABLED = ENABLED_EVENTS + "Balrog";
    public static final String FAE_ENABLED = ENABLED_EVENTS + "Fae";
    public static final String TREASURE_GOBLIN_NAME = "Name of mob in Treasure Goblin events";
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
    public static final String EVENT_TIMEOUT_TIME = "Time before boss mob escapes the map";
    public static final String EVENT_TIMEOUT_MESSAGE = "Message for when boss mobs gets culled";
    public static final String SPIRIT_WALK_HIT_INTERVAL = "Number of hits before spirit walk gets activated";
    public static final String ZOMBIE_KING_FLAMETHROWER_INTERVAL = "Time interval between zombie king flamethrower attacks";
    public static final String ZOMBIE_KING_UNHOLY_SMITE_INTERVAL = "Time interval between zombie king unholy smite attacks";
    public static final String ZOMBIE_KING_SUMMON_MINIONS_INTERVAL = "Time interval between zombie king minion summons";
    public static final String TREASURE_GOBLIN_GOLD_SHOTGUN_INTERVAL = "Time interval between treasure goblin gold shotgun shots";
    public static final String TREASURE_GOBLIN_RADIAL_EXPLOSION = "Time interval between treasure goblin radial explosions";
    public static final String KRAKEN_CHANCE_ON_FISH = "Kraken on fish chance";
    public static final String KRAKEN_NAME = "Name of mob in Kraken event";
    public static final String BALROG_CHANCE_ON_MINE = "Balrog chance on mine";
    public static final String BALROG_NAME = "Name of mob in Balrog event";
    public static final String BALROG_TRASH_MOB_NAME = "Name of trash mobs in Balrog event";
    public static final String FAE_NAME = "Name of mob in Fae event";
    public static final String FAE_CHANCE_ON_CHOP = "Fae chance on chop";
    public static final String ANNOUNCEMENT_BROADCAST_WORLD_ONLY = "Only broadcast event message in event world";

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
        configuration.addDefault(TREASURE_GOBLIN_NAME, "&eTreasure Goblin");
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
        configuration.addDefault(EVENT_TIMEOUT_TIME, 30);
        configuration.addDefault(EVENT_TIMEOUT_MESSAGE, "$bossmob has escaped!");
        configuration.addDefault(SPIRIT_WALK_HIT_INTERVAL, 15);
        configuration.addDefault(ZOMBIE_KING_FLAMETHROWER_INTERVAL, 20);
        configuration.addDefault(ZOMBIE_KING_UNHOLY_SMITE_INTERVAL, 20);
        configuration.addDefault(ZOMBIE_KING_SUMMON_MINIONS_INTERVAL, 20);
        configuration.addDefault(TREASURE_GOBLIN_GOLD_SHOTGUN_INTERVAL, 20);
        configuration.addDefault(TREASURE_GOBLIN_RADIAL_EXPLOSION, 20);
        configuration.addDefault(KRAKEN_ENABLED, true);
        configuration.addDefault(BALROG_ENABLED, true);
        configuration.addDefault(KRAKEN_CHANCE_ON_FISH, 0.005);
        configuration.addDefault(KRAKEN_NAME, "&1Kraken");
        configuration.addDefault(BALROG_CHANCE_ON_MINE, 0.005);
        configuration.addDefault(BALROG_NAME, "&cBalrog");
        configuration.addDefault(BALROG_TRASH_MOB_NAME, "&cRaug");
        configuration.addDefault(FAE_ENABLED, true);
        configuration.addDefault(FAE_NAME, "&aFae");
        configuration.addDefault(FAE_CHANCE_ON_CHOP, 0.001);
        configuration.addDefault(ANNOUNCEMENT_BROADCAST_WORLD_ONLY, false);

        configuration.options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}
