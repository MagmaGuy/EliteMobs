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

package com.magmaguy.elitemobs.naturalmobspawner;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.elitedrops.ItemRankHandler;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.DamageAdjuster;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalMobSpawner implements Listener {

    private EliteMobs plugin;
    private int range = Bukkit.getServer().getViewDistance() * 16;


    public NaturalMobSpawner(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

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

            int armorRating = armorRatingHandler(player);
            int potionEffectRating = potionEffectRankCalculator(player);

            int threatLevel = armorRating + potionEffectRating;

            eliteMobLevel += threatLevel;

            if (eliteMobLevel > ConfigValues.defaultConfig.getInt(DefaultConfig.NATURAL_ELITEMOB_LEVEL_CAP)) {

                eliteMobLevel = ConfigValues.defaultConfig.getInt(DefaultConfig.NATURAL_ELITEMOB_LEVEL_CAP);

            }

        }

        if (eliteMobLevel < 2) return;

        entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, eliteMobLevel));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(entity);

    }


    private int armorRatingHandler(Player player) {

        int armorRating = 0;

        if (player.getEquipment().getHelmet() != null) {

            ItemStack helmet = player.getEquipment().getHelmet();

            armorRating += itemThreatCalculator(helmet);

        }

        if (player.getEquipment().getChestplate() != null) {

            ItemStack chestplate = player.getEquipment().getChestplate();

            armorRating += itemThreatCalculator(chestplate);

        }

        if (player.getEquipment().getLeggings() != null) {

            ItemStack leggings = player.getEquipment().getLeggings();

            armorRating += itemThreatCalculator(leggings);

        }

        if (player.getEquipment().getBoots() != null) {

            ItemStack boots = player.getEquipment().getBoots();

            armorRating += itemThreatCalculator(boots);

        }

        armorRating += weaponGrabber(player);

        return armorRating;

    }

    private int weaponGrabber (Player player) {

        List<ItemStack> itemList = new ArrayList<>();

        if (player.getInventory().getItemInOffHand() != null && !player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) {

            itemList.add(player.getInventory().getItemInOffHand());

        }

        for (int i = 0; i < 9; i++) {

            if (player.getInventory().getItem(i) != null && !player.getInventory().getItem(i).getType().equals(Material.AIR)) {

                Material material = player.getInventory().getItem(i).getType();

                if (material.equals(Material.DIAMOND_SWORD) || material.equals(Material.DIAMOND_AXE) ||
                        material.equals(Material.IRON_SWORD) || material.equals(Material.IRON_AXE) ||
                        material.equals(Material.STONE_SWORD) || material.equals(Material.STONE_AXE) ||
                        material.equals(Material.GOLD_SWORD) || material.equals(Material.GOLD_AXE) ||
                        material.equals(Material.WOOD_SWORD) || material.equals(Material.WOOD_AXE) ||
                        material.equals(Material.BOW)) {

                    itemList.add(player.getInventory().getItem(i));

                }

            }

        }

        int highestThreat = 0;

        if (itemList.size() > 0) {

            for (ItemStack itemStack : itemList) {

                int currentThreat = ItemRankHandler.guessItemThreat(itemStack);

                if (currentThreat > highestThreat) {

                    highestThreat = currentThreat;

                }

            }

        }

        return highestThreat;

    }

    private int itemThreatCalculator(ItemStack itemStack) {

        return ItemRankHandler.guessItemThreat(itemStack);

    }

    private int potionEffectRankCalculator (Player player) {

        int potionEffectRank = 0;

        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {

            potionEffectRank += (player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() + 1) *
                    DamageAdjuster.ENCHANTMENT_OR_POTION_EFFECT_THREAT_INCREMENTER;

        }

        if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {

            potionEffectRank += (player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() + 1) *
                    DamageAdjuster.ENCHANTMENT_OR_POTION_EFFECT_THREAT_INCREMENTER;

        }

        return potionEffectRank;

    }

}
