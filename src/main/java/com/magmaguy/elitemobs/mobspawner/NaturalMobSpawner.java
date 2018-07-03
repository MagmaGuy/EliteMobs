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

package com.magmaguy.elitemobs.mobspawner;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.CheckTierCommand;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalMobSpawner implements Listener {

    private int range = Bukkit.getServer().getViewDistance() * 16;

    public void naturalMobProcessor(Entity entity) {

        List<Player> closePlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.getWorld().equals(entity.getWorld()) && player.getLocation().distance(entity.getLocation()) < range) {

                if (!player.getGameMode().equals(GameMode.SPECTATOR) && (!player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) ||
                        player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) && !player.getMetadata(MetadataHandler.VANISH_NO_PACKET).get(0).asBoolean())) {

                    closePlayers.add(player);

                }

            }

        }

        int eliteMobLevel = 1;

        for (Player player : closePlayers) {

            int finalTieredMobLevel = MobLevelCalculator.determineMobLevel(player);

            eliteMobLevel += finalTieredMobLevel;

            if (eliteMobLevel > ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.NATURAL_ELITEMOB_LEVEL_CAP))
                eliteMobLevel = ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.NATURAL_ELITEMOB_LEVEL_CAP);

        }

        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.INCREASE_DIFFICULTY_WITH_SPAWN_DISTANCE)) {

            int levelIncrement = SpawnRadiusDifficultyIncrementer.distanceFromSpawnLevelIncrease((LivingEntity) entity);
            eliteMobLevel += levelIncrement;

        }

        if (eliteMobLevel < 2) return;

        entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, eliteMobLevel));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(entity);

    }

}
