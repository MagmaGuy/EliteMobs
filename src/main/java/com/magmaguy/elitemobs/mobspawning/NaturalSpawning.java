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

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalSpawning implements Listener {

    private static int range = Bukkit.getServer().getViewDistance() * 16;

    public static void naturalMobProcessor(Entity entity) {

        List<Player> closePlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld().equals(entity.getWorld()) && player.getLocation().distanceSquared(entity.getLocation()) < Math.pow(range, 2))
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
        Add in party system modifier
        Each player adds a +0.2 tier bonus
         */
        eliteMobLevel += playerCount * 0.2 * MobTierFinder.PER_TIER_LEVEL_INCREASE;

        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.INCREASE_DIFFICULTY_WITH_SPAWN_DISTANCE)) {

            int levelIncrement = SpawnRadiusDifficultyIncrementer.distanceFromSpawnLevelIncrease((LivingEntity) entity);
            eliteMobLevel += levelIncrement;

        }

        if (playerCount == 0 || eliteMobLevel < 1) return;

        EliteMobEntity eliteMobEntity = new EliteMobEntity((LivingEntity) entity, eliteMobLevel);

    }

}
