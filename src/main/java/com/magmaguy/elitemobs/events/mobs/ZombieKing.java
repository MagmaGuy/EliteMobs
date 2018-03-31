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

package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import com.magmaguy.elitemobs.events.BossSpecialAttackDamage;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.EventMessage;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ZombieKing implements Listener {

    public static void spawnZombieKing(Zombie zombie) {

        DeadMoon.entityQueued = false;

        int kingLevel = 1000;
        Location location = zombie.getLocation();

        Zombie zombieKing = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        zombieKing.setMetadata(MetadataHandler.ZOMBIE_KING, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        zombieKing.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, kingLevel));
        zombieKing.setMetadata(MetadataHandler.CUSTOM_ARMOR, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        zombieKing.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        zombieKing.setMetadata(MetadataHandler.CUSTOM_STACK, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        zombieKing.setMetadata(MetadataHandler.EVENT_CREATURE, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        NameHandler.customUniqueNameAssigner(zombieKing, ChatColorConverter.chatColorConverter(ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_ZOMBIE_KING_NAME)));

        zombieKing.setRemoveWhenFarAway(false);
        zombieKing.setBaby(false);

        zombieKing.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        zombieKing.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        zombieKing.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        zombieKing.getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET));
        zombieKing.getEquipment().setItemInMainHand(new ItemStack(Material.GOLD_AXE));

        AggressiveEliteMobConstructor.constructAggressiveEliteMob(zombieKing);
        zombieKingFlair(zombieKing);

        String coordinates = zombieKing.getLocation().getBlockX() + ", " + zombieKing.getLocation().getBlockY() + ", " + zombieKing.getLocation().getBlockZ();
        String sendString = ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_EVENT_ANNOUNCEMENT_TEXT).replace("$location", coordinates);
        String worldName = "";

        if (zombieKing.getWorld().getName().contains("_")) {

            for (String string : zombieKing.getWorld().getName().split("_")) {

                worldName += string.substring(0, 1).toUpperCase() + string.toLowerCase() + " ";

            }

        } else {

            worldName = zombieKing.getWorld().getName().substring(0, 1).toUpperCase() + zombieKing.getWorld().getName().substring(1).toLowerCase();

        }

        sendString = sendString.replace("$world", worldName);

        sendString = ChatColorConverter.chatColorConverter(sendString);

        EventMessage.sendEventMessage(sendString);

    }

    private static void zombieKingFlair(Zombie zombieKing) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (zombieKing.isValid() && !zombieKing.isDead()) {

                    zombieKing.getWorld().spawnParticle(Particle.FLAME, zombieKing.getLocation(), 4, 0.05, 0.15, 0.05, 0.03);
                    zombieKing.getWorld().spawnParticle(Particle.FLAME, zombieKing.getLocation().add(new Vector(0, 0.5, 0)), 3, 0.05, 0.15, 0.05, 0.03);
                    zombieKing.getWorld().spawnParticle(Particle.FLAME, zombieKing.getLocation().add(new Vector(0, 1, 0)), 2, 0.05, 0.15, 0.05, 0.03);

                } else {

                    cancel();

                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static Random random = new Random();

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!event.getEntity().hasMetadata(MetadataHandler.ZOMBIE_KING)) return;

        LivingEntity livingEntity;

        if (event.getDamager() instanceof LivingEntity) {

            livingEntity = (LivingEntity) event.getDamager();

        } else if (event.getDamager() instanceof Projectile &&
                ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {

            livingEntity = (LivingEntity) ((Projectile) event.getDamager()).getShooter();

        } else {

            livingEntity = null;

        }

        if (livingEntity == null) return;

        if (random.nextDouble() < 0.20) {

            if (random.nextDouble() < 0.50) {

                if (!event.getEntity().hasMetadata(MetadataHandler.ZOMBIE_KING_FLAMETHROWER_COOLDOWN)) {

                    PowerCooldown.cooldownTimer(event.getEntity(), MetadataHandler.ZOMBIE_KING_FLAMETHROWER_COOLDOWN, 20 * 20);
                    initializeFlamethrower(event.getEntity().getLocation(), livingEntity.getLocation(), (LivingEntity) event.getEntity());

                }

            } else {

                if (!event.getEntity().hasMetadata(MetadataHandler.ZOMBIE_KING_UNHOLY_SMITE_COOLDOWN)) {

                    PowerCooldown.cooldownTimer(event.getEntity(), MetadataHandler.ZOMBIE_KING_UNHOLY_SMITE_COOLDOWN, 20 * 20);
                    initializeUnholySmite((LivingEntity) event.getEntity());

                }

            }

        }

    }

    private static void initializeFlamethrower(Location sourceLocation, Location targetLocation, LivingEntity shooter) {

        Vector toTarget = targetLocation.clone().subtract(sourceLocation).toVector().normalize();
        shooter.setAI(false);

        initializeFlamethrower(sourceLocation, toTarget, shooter);

    }

    public static void initializeFlamethrower(Location sourceLocation, Vector targetVector, LivingEntity shooter) {

        createDamagePath(sourceLocation.add(new Vector(0, 1, 0)), targetVector, shooter);

    }

    private static void createDamagePath(Location sourceLocation, Vector toTarget, LivingEntity shooter) {

        int flamePoints = 20;

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > flamePoints) {

                    cancel();

                }

                Location armorStandLocation = sourceLocation.add(toTarget);

                ArmorStand flamethrowerDamagePoint = (ArmorStand) armorStandLocation.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
                flamethrowerDamagePoint.setMetadata(MetadataHandler.ARMOR_STAND_DISPLAY, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                flamethrowerDamagePoint.setVisible(false);
                flamethrowerDamagePoint.setMarker(true);
                flamethrowerDamagePoint.setGravity(false);
                flamethrowerDamage(flamethrowerDamagePoint, shooter, toTarget);

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static void flamethrowerDamage(ArmorStand armorStand, LivingEntity shooter, Vector directionVector) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > 3 * 20) {

                    if (!(shooter instanceof Player)) shooter.setAI(true);
                    armorStand.remove();
                    cancel();

                }

                /*
                Reserve first second to early warning, remaining two seconds for damage
                 */
                if (counter > 10) {


                    if (counter < 2 * 20) {

                        for (int i = 0; i < 5; i++) {

                            double offsetX = (random.nextDouble() - 0.5) * 0.1 + directionVector.getX();
                            double offsetY = (random.nextDouble() - 0.5) * 0.1 + directionVector.getY();
                            double offsetZ = (random.nextDouble() - 0.5) * 0.1 + directionVector.getZ();
                            double offsetVelocity = random.nextDouble() + 0.05;

                            if (shooter instanceof Player)
                                shooter.getLocation().getWorld().spawnParticle(Particle.FLAME, shooter.getEyeLocation().clone().add(directionVector), 0, offsetX, offsetY, offsetZ, offsetVelocity);
                            else
                                shooter.getLocation().getWorld().spawnParticle(Particle.FLAME, shooter.getEyeLocation().subtract(new Vector(0, 0.5, 0)).clone().add(directionVector), 0, offsetX, offsetY, offsetZ, offsetVelocity);

                        }

                    }

                    for (Entity entity : armorStand.getNearbyEntities(1, 1, 1)) {

                        if (!entity.equals(shooter) && entity instanceof LivingEntity) {

                            BossSpecialAttackDamage.dealSpecialDamage(shooter, (LivingEntity) entity, 1);

                        }

                    }

                }

                if (counter < 10 || counter > 2 * 20) {

                    for (int i = 0; i < 5; i++) {

                        double offsetX = (random.nextDouble() - 0.5) * 0.1 + directionVector.getX();
                        double offsetY = (random.nextDouble() - 0.5) * 0.1 + directionVector.getY();
                        double offsetZ = (random.nextDouble() - 0.5) * 0.1 + directionVector.getZ();
                        double offsetVelocity = random.nextDouble() + 0.05;

                        if (shooter instanceof Player)
                            shooter.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, shooter.getEyeLocation().clone().add(directionVector), 0, offsetX, offsetY, offsetZ, offsetVelocity);
                        else
                            shooter.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, shooter.getEyeLocation().subtract(new Vector(0, 0.5, 0)).clone().add(directionVector), 0, offsetX, offsetY, offsetZ, offsetVelocity);
                    }

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);

    }

    private void initializeUnholySmite(LivingEntity livingEntity) {

        //todo: add visual warning
        livingEntity.setAI(false);
        unholySmitePhaseOne(livingEntity, Particle.SMOKE_NORMAL, false, 10);


    }

    private void unholySmitePhaseOne(LivingEntity zombieKing, Particle particle, boolean doDamage, double duration) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > duration) {

                    if (!doDamage) {

                        unholySmitePhaseOne(zombieKing, Particle.FLAME, true, 20 * 2 + 1);
                        unholySmitePhaseTwo(zombieKing, Particle.SMOKE_NORMAL, false, 10);

                    }

                    cancel();

                }

                Vector directionVector = new Vector(0, 1, 0);

                if (!doDamage) {

                    spawnUnholySmitePhaseOneParticle(directionVector, zombieKing, particle);

                } else {

                    spawnUnholySmitePhaseOneParticle(directionVector, zombieKing, particle);
                    unholySmiteDamage(zombieKing, 0.5, 50, 0.5);

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void unholySmitePhaseTwo(LivingEntity zombieKing, Particle particle, boolean doDamage, double duration) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > duration) {

                    if (!doDamage) {

                        unholySmitePhaseTwo(zombieKing, Particle.FLAME, true, 20 * 2 + 0.5);
                        unholySmitePhaseThree(zombieKing, Particle.SMOKE_NORMAL, false, 10);

                    }
                    cancel();

                }

                Vector directionVector = new Vector(0, 1, 0);

                if (!doDamage) {

                    spawnUnholySmitePhaseTwoParticle(directionVector, zombieKing, particle);

                } else {

                    spawnUnholySmitePhaseTwoParticle(directionVector, zombieKing, particle);
                    unholySmiteDamage(zombieKing, 3, 50, 3);

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void unholySmitePhaseThree(LivingEntity zombieKing, Particle particle, boolean doDamage, double duration) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > duration) {

                    if (!doDamage) unholySmitePhaseThree(zombieKing, Particle.FLAME, true, 20 * 2);
                    else zombieKing.setAI(true);
                    cancel();

                }

                if (!doDamage) {

                    spawnUnholySmitePhaseThreeParticle(zombieKing, particle);

                } else {

                    spawnUnholySmitePhaseThreeParticle(zombieKing, particle);
                    unholySmiteDamage(zombieKing, 5, 4, 5);

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }


    private void spawnUnholySmitePhaseOneParticle(Vector directionVector, LivingEntity livingEntity, Particle particle) {

        for (int i = 0; i < 10; i++) {

            double offsetVelocity = random.nextDouble() * 2;

            double offsetLocationX = (random.nextDouble() - 0.5) * 0.5 + livingEntity.getLocation().getX();
            double offsetLocationZ = (random.nextDouble() - 0.5) * 0.5 + livingEntity.getLocation().getZ();

            Location offsetLocation = new Location(livingEntity.getWorld(), offsetLocationX, livingEntity.getLocation().getY(), offsetLocationZ);

            livingEntity.getLocation().getWorld().spawnParticle(particle, offsetLocation, 0, directionVector.getX(), directionVector.getY(), directionVector.getZ(), offsetVelocity);

        }

    }

    private void spawnUnholySmitePhaseTwoParticle(Vector directionVector, LivingEntity livingEntity, Particle particle) {

        for (int i = 0; i < 10; i++) {

            double offsetVelocity = random.nextDouble() * 2;

            double offsetLocationX = (random.nextDouble() - 0.5) * 3 + livingEntity.getLocation().getX();
            double offsetLocationZ = (random.nextDouble() - 0.5) * 3 + livingEntity.getLocation().getZ();

            Location offsetLocation = new Location(livingEntity.getWorld(), offsetLocationX, livingEntity.getLocation().getY(), offsetLocationZ);

            livingEntity.getLocation().getWorld().spawnParticle(particle, offsetLocation, 0, directionVector.getX(), directionVector.getY(), directionVector.getZ(), offsetVelocity);

        }

    }

    private void spawnUnholySmitePhaseThreeParticle(LivingEntity livingEntity, Particle particle) {

        livingEntity.getLocation().getWorld().spawnParticle(particle, livingEntity.getLocation(), 50, 0.01, 0.01, 0.01, 0.1);

    }

    private void unholySmiteDamage(LivingEntity zombieKing, double range1, double range2, double range3) {

        for (Entity entity : zombieKing.getNearbyEntities(range1, range2, range3)) {

            if (entity instanceof LivingEntity && !entity.equals(zombieKing)) {

                BossSpecialAttackDamage.dealSpecialDamage(zombieKing, (LivingEntity) entity, 1);
            }

        }

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.ZOMBIE_KING) && event.getEntity() instanceof Zombie) {

            Zombie zombieKing = (Zombie) event.getEntity();

            ItemStack zombieKingAxe = UniqueItemConstructor.uniqueItems.get(UniqueItemConstructor.ZOMBIE_KING_AXE);
            zombieKing.getWorld().dropItem(zombieKing.getLocation(), zombieKingAxe);

            if (event.getEntity().getKiller() != null) {

                String newMessage = ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_EVENT_PLAYER_END_TEXT).replace("$player", event.getEntity().getKiller().getDisplayName());

                EventMessage.sendEventMessage(newMessage);

            } else {

                EventMessage.sendEventMessage(ConfigValues.eventsConfig.getString(ChatColorConverter.chatColorConverter(EventsConfig.DEAD_MOON_EVENT_OTHER_END_TEXT)));

            }


        }

    }

}
