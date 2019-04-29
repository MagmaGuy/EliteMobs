package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.ItemsUniqueConfig;
import com.magmaguy.elitemobs.events.BossSpecialAttackDamage;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.EventMessage;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.items.uniqueitems.ZombieKingsAxe;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.TimedBossMobEntity;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ZombieKing implements Listener {

    private static HashSet<EliteMobEntity> zombieKingList = new HashSet<>();

    public static EliteMobEntity getZombieKing(Zombie zombie) {
        if (zombieKingList.isEmpty()) return null;
        for (EliteMobEntity bossMobEntity : zombieKingList)
            if (bossMobEntity.getLivingEntity().equals(zombie))
                return bossMobEntity;
        return null;
    }

    public static void spawnZombieKing(Location location) {

        DeadMoon.entityQueued = false;

        int kingLevel = DynamicBossLevelConstructor.findDynamicBossLevel();
        TimedBossMobEntity zombieKing = new TimedBossMobEntity(EntityType.ZOMBIE, location, kingLevel,
                ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_ZOMBIE_KING_NAME), CreatureSpawnEvent.SpawnReason.NATURAL);

        zombieKingList.add(zombieKing);
        zombieKingFlair((Zombie) zombieKing.getLivingEntity());

        ((Zombie) zombieKing.getLivingEntity()).setBaby(false);

        zombieKing.getLivingEntity().getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        zombieKing.getLivingEntity().getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        zombieKing.getLivingEntity().getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        zombieKing.getLivingEntity().getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        zombieKing.getLivingEntity().getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_AXE));

        EventMessage.sendEventMessage(zombieKing.getLivingEntity(), ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_EVENT_ANNOUNCEMENT_TEXT));

    }

    private static void zombieKingFlair(Zombie zombieKing) {

        new BukkitRunnable() {

            UUID uuid = zombieKing.getUniqueId();

            @Override
            public void run() {

                Zombie localZombieKing = zombieKing;

                if (!zombieKing.isValid())
                    if (Bukkit.getEntity(uuid) != null && Bukkit.getEntity(uuid).isValid())
                        localZombieKing = (Zombie) Bukkit.getEntity(uuid);

                if (!zombieKing.isDead()) {

                    localZombieKing.getWorld().spawnParticle(Particle.FLAME, localZombieKing.getLocation(), 4, 0.05, 0.15, 0.05, 0.03);
                    localZombieKing.getWorld().spawnParticle(Particle.FLAME, localZombieKing.getLocation().add(new Vector(0, 0.5, 0)), 3, 0.05, 0.15, 0.05, 0.03);
                    localZombieKing.getWorld().spawnParticle(Particle.FLAME, localZombieKing.getLocation().add(new Vector(0, 1, 0)), 2, 0.05, 0.15, 0.05, 0.03);

                } else
                    cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static HashSet<EliteMobEntity> flamethrowerCooldown = new HashSet<>();
    private static HashSet<EliteMobEntity> unholySmiteCooldown = new HashSet<>();
    private static HashSet<EliteMobEntity> minionsCooldown = new HashSet<>();

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) return;
        EliteMobEntity eliteMobEntity = getZombieKing((Zombie) event.getEntity());
        if (eliteMobEntity == null) return;

        LivingEntity livingEntity;

        if (event.getDamager() instanceof LivingEntity)
            livingEntity = (LivingEntity) event.getDamager();
        else if (event.getDamager() instanceof Projectile &&
                ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
            livingEntity = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
        else
            livingEntity = null;

        if (livingEntity == null) return;

        if (ThreadLocalRandom.current().nextDouble() < 0.20) {

            if (ThreadLocalRandom.current().nextDouble() < 0.33) {

                if (!PowerCooldown.isInCooldown(eliteMobEntity, flamethrowerCooldown)) {

                    PowerCooldown.startCooldownTimer(eliteMobEntity, flamethrowerCooldown,
                            20 * ConfigValues.eventsConfig.getInt(EventsConfig.ZOMBIE_KING_FLAMETHROWER_INTERVAL));
                    initializeFlamethrower(event.getEntity().getLocation().clone(), livingEntity.getLocation().clone(), (LivingEntity) event.getEntity());

                }

            } else if (ThreadLocalRandom.current().nextDouble() < 0.66) {

                if (!PowerCooldown.isInCooldown(eliteMobEntity, unholySmiteCooldown)) {

                    PowerCooldown.startCooldownTimer(eliteMobEntity, unholySmiteCooldown,
                            20 * ConfigValues.eventsConfig.getInt(EventsConfig.ZOMBIE_KING_UNHOLY_SMITE_INTERVAL));
                    initializeUnholySmite((LivingEntity) event.getEntity());

                }

            } else {

                if (!PowerCooldown.isInCooldown(eliteMobEntity, minionsCooldown)) {

                    PowerCooldown.startCooldownTimer(eliteMobEntity, minionsCooldown,
                            20 * ConfigValues.eventsConfig.getInt(EventsConfig.ZOMBIE_KING_SUMMON_MINIONS_INTERVAL));
                    initializeSummonMinions((LivingEntity) event.getEntity());

                }

            }

        }

    }

    private static void initializeFlamethrower(Location sourceLocation, Location targetLocation, LivingEntity shooter) {

        Vector toTarget = targetLocation.clone().subtract(sourceLocation).toVector().normalize();
        shooter.setAI(false);

        initializeFlamethrower(sourceLocation, toTarget, shooter, false);

    }

    public static void initializeFlamethrower(Location sourceLocation, Vector targetVector, LivingEntity shooter, boolean shotByPlayer) {

        createDamagePath(sourceLocation.add(new Vector(0, 1, 0)), targetVector, shooter, shotByPlayer);

    }

    private static void createDamagePath(Location sourceLocation, Vector toTarget, LivingEntity shooter, boolean shotByPlayer) {

        int flamePoints = 20;

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > flamePoints)
                    cancel();

                Location newLocation = sourceLocation.add(toTarget);

                flamethrowerDamage(newLocation.clone(), shooter, toTarget, shotByPlayer);

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static void flamethrowerDamage(Location location, LivingEntity shooter, Vector directionVector, boolean shotByPlayer) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > 3 * 20) {

                    if (!(shooter instanceof Player)) shooter.setAI(true);
                    cancel();

                }

                /*
                Reserve first second to early warning, remaining two seconds for damage
                 */
                if (counter > 10) {

                    if (counter < 2 * 20) {

                        for (int i = 0; i < 5; i++) {

                            double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getX();
                            double offsetY = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getY();
                            double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getZ();
                            double offsetVelocity = ThreadLocalRandom.current().nextDouble() + 0.05;

                            if (shooter instanceof Player)
                                shooter.getLocation().getWorld().spawnParticle(Particle.FLAME, shooter.getEyeLocation().clone().add(directionVector), 0, offsetX, offsetY, offsetZ, offsetVelocity);
                            else
                                shooter.getLocation().getWorld().spawnParticle(Particle.FLAME, shooter.getEyeLocation().clone().subtract(new Vector(0, 0.5, 0)).clone().add(directionVector), 0, offsetX, offsetY, offsetZ, offsetVelocity);

                        }

                    }

                    for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {

                        if (!entity.equals(shooter) && entity instanceof LivingEntity) {

                            if (shotByPlayer && entity instanceof Player) {

                                //TODO: add pvp mechanics here

                            } else {
                                BossSpecialAttackDamage.dealSpecialDamage(shooter, (LivingEntity) entity, 1);
                            }

                        }

                    }

                }

                if (counter < 10 || counter > 2 * 20) {

                    for (int i = 0; i < 5; i++) {

                        double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getX();
                        double offsetY = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getY();
                        double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1 + directionVector.getZ();
                        double offsetVelocity = ThreadLocalRandom.current().nextDouble() + 0.05;

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

            double offsetVelocity = ThreadLocalRandom.current().nextDouble() * 2;

            double offsetLocationX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5 + livingEntity.getLocation().getX();
            double offsetLocationZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5 + livingEntity.getLocation().getZ();

            Location offsetLocation = new Location(livingEntity.getWorld(), offsetLocationX, livingEntity.getLocation().getY(), offsetLocationZ);

            livingEntity.getLocation().getWorld().spawnParticle(particle, offsetLocation, 0, directionVector.getX(), directionVector.getY(), directionVector.getZ(), offsetVelocity);

        }

    }

    private void spawnUnholySmitePhaseTwoParticle(Vector directionVector, LivingEntity livingEntity, Particle particle) {

        for (int i = 0; i < 10; i++) {

            double offsetVelocity = ThreadLocalRandom.current().nextDouble() * 2;

            double offsetLocationX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 3 + livingEntity.getLocation().getX();
            double offsetLocationZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 3 + livingEntity.getLocation().getZ();

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

    private void initializeSummonMinions(LivingEntity zombieKing) {

        zombieKing.setAI(false);
        summonMinions(zombieKing);

    }

    private void summonMinions(LivingEntity zombieKing) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > 20 * 3) {

                    int amountOfSummonedMobs = 10;

                    for (int i = 0; i < amountOfSummonedMobs; i++)
                        TheReturned.theReturnedConstructor(50, (Zombie) zombieKing);

                    zombieKing.setAI(true);
                    cancel();

                }

                zombieKing.getWorld().spawnParticle(Particle.PORTAL, zombieKing.getLocation().add(new Vector(0, 1, 0)), 50, 0.01, 0.01, 0.01, 1);

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!event.getEntity().getType().equals(EntityType.ZOMBIE)) return;
        EliteMobEntity eliteMobEntity = getZombieKing((Zombie) event.getEntity());
        if (eliteMobEntity == null) return;

        Zombie zombieKing = (Zombie) event.getEntity();

        if (ConfigValues.itemsUniqueConfig.getBoolean(ItemsUniqueConfig.ENABLE_ZOMBIE_KING_AXE)) {
            ZombieKingsAxe zombieKingAxe = new ZombieKingsAxe();
            ItemStack zombieKingAxeItemStack = zombieKingAxe.constructItemStack((int) MobTierFinder.findMobTier(eliteMobEntity.getLevel()));

            zombieKing.getWorld().dropItem(zombieKing.getLocation(), zombieKingAxeItemStack);
        }

        if (event.getEntity().getKiller() != null) {

            String newMessage = ConfigValues.eventsConfig.getString(EventsConfig.DEAD_MOON_EVENT_PLAYER_END_TEXT)
                    .replace("$player", event.getEntity().getKiller().getDisplayName());

            EventMessage.sendEventMessage(event.getEntity(), newMessage);

        } else
            EventMessage.sendEventMessage(event.getEntity(), ConfigValues.eventsConfig.getString(
                    ChatColorConverter.convert(EventsConfig.DEAD_MOON_EVENT_OTHER_END_TEXT)));


    }

}
