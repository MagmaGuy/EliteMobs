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

package com.magmaguy.elitemobs.mobcustomizer;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class ArmorHandler {

    public static void ArmorHandler(Entity entity) {

        if (VersionChecker.currentVersionIsUnder(12, 2)) return;

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_ARMOR)) return;

        LivingEntity livingEntity = (LivingEntity) entity;

        livingEntity.getEquipment().setItemInMainHandDropChance(0);
        livingEntity.getEquipment().setHelmetDropChance(0);
        livingEntity.getEquipment().setChestplateDropChance(0);
        livingEntity.getEquipment().setLeggingsDropChance(0);
        livingEntity.getEquipment().setBootsDropChance(0);

        if (entity.hasMetadata(MetadataHandler.CUSTOM_ARMOR)) return;

        if (entity instanceof Zombie || entity instanceof PigZombie ||
                entity instanceof Skeleton || entity instanceof WitherSkeleton) {

            int mobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

            ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.AIR));
            ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.AIR));
            ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.AIR));
            ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.AIR));

            if (mobLevel >= 17) {

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS)) {
                    ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                }

            }

            if (mobLevel >= 18) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

            }

            if (mobLevel >= 19) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

            }

            if (mobLevel >= 20) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

            }

            if (mobLevel >= 22) {

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS)) {
                    ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                }

            }

            if (mobLevel >= 23) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

            }

            if (mobLevel >= 24) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

            }

            if (mobLevel >= 25) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

            }

            if (mobLevel >= 27) {

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS)) {
                    ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                }

            }

            if (mobLevel >= 28) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

            }

            if (mobLevel >= 29) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

            }

            if (mobLevel >= 30) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

            }

            if (mobLevel >= 32) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

            }

            if (mobLevel >= 33) {

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS)) {
                    ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                }

            }

            if (mobLevel >= 34) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

            }

            if (mobLevel >= 35) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

            }

        }

    }

}
