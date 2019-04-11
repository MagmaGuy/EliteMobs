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

package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalEliteMobSpawnEventHandler {

    private static int range = Bukkit.getServer().getViewDistance() * 16;

    /**
     * This manages Elite Mob that are spawned naturally. It takes a mob that spawns normally in the world, randomizes
     * its chance to become an Elite Mob, scans the area around it for players, finds what combat tier those players,
     * finds if there are additional players, increases the tier of the Elite Mob accordingly and adds sets it as a new
     * Elite Mob
     *
     * @param entity      Entity to check for Elite Mob conversion
     * @param spawnReason Reason for the mob spawning
     */
    public static void naturalMobProcessor(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {

        int eliteMobLevel = getNaturalMobLevel(entity.getLocation());
        if (eliteMobLevel < 0) return;

        EliteMobEntity eliteMobEntity = new EliteMobEntity((LivingEntity) entity, eliteMobLevel, spawnReason);

        if (spawnReason.equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
            eliteMobEntity.setHasSpecialLoot(false);

    }

    /**
     * This gets the level the natural Elite Mob should have. This level is determined by the power of the armor and weapons
     * the players are wearing, as well as by how many players are in the area.
     *
     * @param spawnLocation Location to scan around for players
     * @return
     */
    public static int getNaturalMobLevel(Location spawnLocation) {

        List<Player> closePlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld().equals(spawnLocation.getWorld()) && player.getLocation().distanceSquared(spawnLocation) < Math.pow(range, 2))
                if (!player.getGameMode().equals(GameMode.SPECTATOR) && (!player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) ||
                        player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) && !player.getMetadata(MetadataHandler.VANISH_NO_PACKET).get(0).asBoolean()))
                    closePlayers.add(player);

        int eliteMobLevel = 1;
        int playerCount = 0;

        for (Player player : closePlayers) {

            int individualPlayerThreat = MobLevelCalculator.determineMobLevel(player);
            playerCount++;

            if (individualPlayerThreat > eliteMobLevel)
                eliteMobLevel = individualPlayerThreat;

        }

        /*
        Party system modifier
        Each player adds a +0.2 tier bonus
         */
        eliteMobLevel += playerCount * 0.2 * MobTierFinder.PER_TIER_LEVEL_INCREASE;

        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.INCREASE_DIFFICULTY_WITH_SPAWN_DISTANCE)) {

            int levelIncrement = SpawnRadiusDifficultyIncrementer.distanceFromSpawnLevelIncrease(spawnLocation);
            eliteMobLevel += levelIncrement;

        }

        if (playerCount == 0 || eliteMobLevel < 1) return 0;

        return eliteMobLevel;

    }

}
