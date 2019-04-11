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

package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.ReinforcementMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.ChatColorConverter.convert;

/**
 * Created by MagmaGuy on 07/05/2017.
 */
public class ZombieTeamRocket extends MajorPower implements Listener {

    private static final ItemStack TEAM_ROCKET_CHESTPIECE = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
    private static final ItemStack TEAM_ROCKET_LEGGINGS = new ItemStack(Material.LEATHER_LEGGINGS, 1);
    private static final ItemStack TEAM_ROCKET_BOOTS = new ItemStack(Material.LEATHER_BOOTS, 1);
    private static final ItemStack TEAM_ROCKET_HELMET = new ItemStack(Material.LEATHER_HELMET, 1);
    public static HashSet<EliteMobEntity> activatedZombies = new HashSet<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;
        if (ThreadLocalRandom.current().nextDouble() < 0.20) return;
        if (activatedZombies.contains(eliteMobEntity)) return;
        activatedZombies.add(eliteMobEntity);

        LeatherArmorMeta teamRocketChestpieceMeta = (LeatherArmorMeta) TEAM_ROCKET_CHESTPIECE.getItemMeta();
        teamRocketChestpieceMeta.setColor(Color.WHITE);
        TEAM_ROCKET_CHESTPIECE.setItemMeta(teamRocketChestpieceMeta);

        LeatherArmorMeta teamRocketLeggingsMeta = (LeatherArmorMeta) TEAM_ROCKET_CHESTPIECE.getItemMeta();
        teamRocketLeggingsMeta.setColor(Color.WHITE);
        TEAM_ROCKET_LEGGINGS.setItemMeta(teamRocketLeggingsMeta);

        LeatherArmorMeta teamRocketBootsMeta = (LeatherArmorMeta) TEAM_ROCKET_BOOTS.getItemMeta();
        teamRocketBootsMeta.setColor(Color.BLACK);
        TEAM_ROCKET_BOOTS.setItemMeta(teamRocketBootsMeta);

        ItemStack jesseHelmet = TEAM_ROCKET_HELMET.clone();
        LeatherArmorMeta jesseHelmetMeta = (LeatherArmorMeta) jesseHelmet.getItemMeta();
        jesseHelmetMeta.setColor(Color.PURPLE);
        jesseHelmet.setItemMeta(jesseHelmetMeta);

        ItemStack jamesHelmet = TEAM_ROCKET_HELMET.clone();
        LeatherArmorMeta jamesHelmetMeta = (LeatherArmorMeta) jamesHelmet.getItemMeta();
        jamesHelmetMeta.setColor(Color.BLUE);
        jamesHelmet.setItemMeta(jamesHelmetMeta);

        ReinforcementMobEntity jesseReinforcement = new ReinforcementMobEntity(EntityType.ZOMBIE, eliteMobEntity.getLivingEntity().getLocation(), eliteMobEntity.getLevel(), "Jesse", CreatureSpawnEvent.SpawnReason.CUSTOM);
        jesseReinforcement.getLivingEntity().getEquipment().setHelmet(jesseHelmet);
        jesseReinforcement.getLivingEntity().getEquipment().setChestplate(TEAM_ROCKET_CHESTPIECE);
        jesseReinforcement.getLivingEntity().getEquipment().setLeggings(TEAM_ROCKET_LEGGINGS);
        jesseReinforcement.getLivingEntity().getEquipment().setBoots(TEAM_ROCKET_BOOTS);

        ReinforcementMobEntity jamesReinforcement = new ReinforcementMobEntity(EntityType.ZOMBIE, eliteMobEntity.getLivingEntity().getLocation(), eliteMobEntity.getLevel(), "James", CreatureSpawnEvent.SpawnReason.CUSTOM);
        jamesReinforcement.getLivingEntity().getEquipment().setHelmet(jamesHelmet);
        jamesReinforcement.getLivingEntity().getEquipment().setChestplate(TEAM_ROCKET_CHESTPIECE);
        jamesReinforcement.getLivingEntity().getEquipment().setLeggings(TEAM_ROCKET_LEGGINGS);
        jamesReinforcement.getLivingEntity().getEquipment().setBoots(TEAM_ROCKET_BOOTS);

        Ocelot meowth = (Ocelot) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(eliteMobEntity.getLivingEntity().getLocation(), EntityType.OCELOT);
        meowth.setCustomName("Meowth");

        (eliteMobEntity.getLivingEntity()).getEquipment().setChestplate(TEAM_ROCKET_CHESTPIECE);
        (eliteMobEntity.getLivingEntity()).getEquipment().setLeggings(TEAM_ROCKET_LEGGINGS);
        (eliteMobEntity.getLivingEntity()).getEquipment().setBoots(TEAM_ROCKET_BOOTS);
        eliteMobEntity.setHasCustomPowers(true);

        new BukkitRunnable() {

            int counter = 1;

            @Override
            public void run() {

                switch (counter) {

                    case 1:
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(true);
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(0)));
                        break;
                    case 2:
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(1)));
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 3:
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(2)));
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 4:
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(3)));
                        break;
                    case 5:
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(4)));
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 6:
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(5)));
                        break;
                    case 7:
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(6)));
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 8:
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(7)));
                        break;
                    case 9:
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(8)));
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 10:
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(9)));
                        break;
                    case 11:
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(10)));
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 12:
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(11)));
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 13:
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(12)));
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 14:
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(13)));
                        break;
                    case 15:
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(false);
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(14)));
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(true);
                        break;
                    case 16:
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(15)));
                        break;
                    case 17:
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(false);
                        meowth.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieTeamRocket.Intro").get(16)));
                        meowth.setCustomNameVisible(true);
                        break;
                    case 18:
                        jesseReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getString("ZombieTeamRocket.Jesse name")));
                        jesseReinforcement.getLivingEntity().setCustomNameVisible(true);
                        jamesReinforcement.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getString("ZombieTeamRocket.James name")));
                        jamesReinforcement.getLivingEntity().setCustomNameVisible(true);
                        meowth.setCustomName(convert(ConfigValues.translationConfig.getString("ZombieTeamRocket.Meowth name")));
                    case 30:
                        meowth.remove();
                        cancel();
                        break;

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 15 * 2);


    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!EntityTracker.hasPower(this, event.getEntity())) return;

        event.getEntity().setCustomName(convert(ConfigValues.translationConfig.getString("ZombieTeamRocket.DeathMessage")));
        event.getEntity().setVelocity(new Vector(0, 2, 0));

    }

}