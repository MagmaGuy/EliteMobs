package com.magmaguy.elitemobs.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBossEntity extends EliteMobEntity implements Listener {

    public static CustomBossEntity constructCustomBoss(String fileName, Location location, int mobLevel) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossConfigFields.getCustomBossMobConfigFields(fileName);
        if (!customBossMobsConfigAttributes.isEnabled()) return null;

        HashSet<ElitePower> elitePowers = new HashSet<>();
        for (String powerName : customBossMobsConfigAttributes.getPowers())
            if (ElitePower.getElitePower(powerName) != null)
                elitePowers.add(ElitePower.getElitePower(powerName));
            else
                Bukkit.getLogger().warning("[EliteMobs] Warning: power name " + powerName + " is not registered! Skipping it for custom mob construction...");

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                EntityType.valueOf(customBossMobsConfigAttributes.getEntityType()),
                location,
                mobLevel,
                elitePowers);
    }

    private CustomBossConfigFields customBossConfigFields;
    private int hitCounter = 0;
    private List<ItemStack> uniqueLootList = new ArrayList<>();

    public CustomBossEntity(CustomBossConfigFields customBossConfigFields, EntityType entityType, Location location, int mobLevel, HashSet<ElitePower> elitePowers) {
        super(entityType, location, mobLevel, customBossConfigFields.getName(), elitePowers, CreatureSpawnEvent.SpawnReason.CUSTOM);
        super.setDamageMultiplier(customBossConfigFields.getDamageMultiplier());
        super.setHealthMultiplier(customBossConfigFields.getHealthMultiplier());
        this.customBossConfigFields = customBossConfigFields;
        if (customBossConfigFields.getSpawnMessage() != null)
            Bukkit.broadcastMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        setEquipment(customBossConfigFields.getHelmet(),
                customBossConfigFields.getChestplate(),
                customBossConfigFields.getLeggings(),
                customBossConfigFields.getBoots(),
                customBossConfigFields.getMainHand(),
                customBossConfigFields.getOffHand());
        if (customBossConfigFields.getTimeout() > 1)
            startEscapeMechanismDelay(customBossConfigFields.getTimeout());
        if (entityType.equals(EntityType.ZOMBIE))
            ((Zombie) super.getLivingEntity()).setBaby(customBossConfigFields.isBaby());
        else if (entityType.equals(EntityType.DROWNED))
            ((Drowned) super.getLivingEntity()).setBaby(customBossConfigFields.isBaby());
        super.setPersistent(customBossConfigFields.getIsPersistent());
        if (customBossConfigFields.getTrails() != null) startBossTrails();
        if (customBossConfigFields.getLocationMessage() != null) sendLocation();
    }

    private void startEscapeMechanismDelay(int timeout) {

        if (timeout < 1) return;

        new BukkitRunnable() {

            @Override
            public void run() {
                if (CustomBossEntity.super.getLivingEntity().isDead()) return;
                CustomBossEntity.super.remove();
                if (customBossConfigFields.getEscapeMessage() != null)
                    Bukkit.broadcastMessage(ChatColorConverter.convert(customBossConfigFields.getEscapeMessage()));

            }

        }.runTaskLater(MetadataHandler.PLUGIN, (long) (20 * 60 * timeout));

    }

    public void startBossTrails() {
        for (String string : this.customBossConfigFields.getTrails()) {
            try {
                Particle particle = Particle.valueOf(string);
                doParticleTrail(particle);
            } catch (Exception ex) {
            }
            try {
                Material material = Material.valueOf(string);
                doItemTrail(material);
            } catch (Exception ex) {
            }
        }
    }

    private void doParticleTrail(Particle particle) {
        new BukkitRunnable() {
            UUID bossUUID = CustomBossEntity.super.getLivingEntity().getUniqueId();

            @Override
            public void run() {
                //In case of boss death, stop the effect
                if (CustomBossEntity.super.getLivingEntity() == null || CustomBossEntity.super.getLivingEntity().isDead()) {
                    cancel();
                    return;
                }
                //In case of chunk being unloaded, pause the effect
                if (!CustomBossEntity.super.getLivingEntity().isValid()) {
                    if (Bukkit.getEntity(bossUUID).getLocation().getChunk().isLoaded())
                        return;
                    else
                        CustomBossEntity.super.setLivingEntity((LivingEntity) Bukkit.getEntity(bossUUID));
                }
                //All conditions cleared, do the boss flair effect
                Location entityCenter = getLivingEntity().getLocation().clone().add(0, getLivingEntity().getHeight() / 2, 0);
                getLivingEntity().getWorld().spawnParticle(particle, entityCenter, 1, 0.1, 0.1, 0.1, 0.05);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doItemTrail(Material material) {
        new BukkitRunnable() {
            UUID bossUUID = CustomBossEntity.super.getLivingEntity().getUniqueId();

            @Override
            public void run() {
                //In case of boss death, stop the effect
                if (CustomBossEntity.super.getLivingEntity() == null || CustomBossEntity.super.getLivingEntity().isDead()) {
                    cancel();
                    return;
                }
                //In case of chunk being unloaded, pause the effect
                if (!CustomBossEntity.super.getLivingEntity().isValid()) {
                    if (Bukkit.getEntity(bossUUID).getLocation().getChunk().isLoaded())
                        return;
                    else
                        CustomBossEntity.super.setLivingEntity((LivingEntity) Bukkit.getEntity(bossUUID));
                }
                //All conditions cleared, do the boss flair effect
                Location entityCenter = getLivingEntity().getLocation().clone().add(0, getLivingEntity().getHeight() / 2, 0);
                Item item = VisualItemInitializer.intializeItem(ItemStackGenerator.generateItemStack
                        (material, "visualItem", Arrays.asList(ThreadLocalRandom.current().nextDouble() + "")), entityCenter);
                item.setVelocity(new Vector(
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        item.remove();
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }


    public List<ItemStack> getUniqueLootList() {
        return this.uniqueLootList;
    }

    public int getHitsCounter() {
        return this.hitCounter;
    }

    public void resetHitsCounter() {
        this.hitCounter = 0;
    }

    public void incrementHitsCounter() {
        this.hitCounter++;
    }

    public void setEquipment(Material helmet, Material chestplate, Material leggings, Material boots, Material mainHand, Material offHand) {
        super.getLivingEntity().getEquipment().setHelmet(ItemStackGenerator.generateItemStack(helmet));
        super.getLivingEntity().getEquipment().setChestplate(ItemStackGenerator.generateItemStack(chestplate));
        super.getLivingEntity().getEquipment().setLeggings(ItemStackGenerator.generateItemStack(leggings));
        super.getLivingEntity().getEquipment().setBoots(ItemStackGenerator.generateItemStack(boots));
        super.getLivingEntity().getEquipment().setItemInMainHand(ItemStackGenerator.generateItemStack(mainHand));
        super.getLivingEntity().getEquipment().setItemInOffHand(ItemStackGenerator.generateItemStack(offHand));
    }

    public static class CustomBossEntityEvents implements Listener {
        @EventHandler
        public void onEliteMobDeath(EliteMobDeathEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            CustomBossEntity customBossEntity = (CustomBossEntity) event.getEliteMobEntity();

            //Do loot
//            if (!customBossEntity.getUniqueLootList().isEmpty()) {
//                for (ItemStack itemStack : customBossEntity.getUniqueLootList())
//                    customBossEntity.getLivingEntity().getWorld().dropItem(customBossEntity.getLivingEntity().getLocation(), itemStack);
//            }

            //Do death message
            String playersList = "";
            for (Player player : event.getEliteMobEntity().getDamagers()) {
                if (playersList.isEmpty())
                    playersList += player.getDisplayName();
                else
                    playersList += ", " + player.getDisplayName();
            }

            if (customBossEntity.customBossConfigFields.getDeathMessage() != null)
                Bukkit.broadcastMessage(ChatColorConverter.convert(customBossEntity.customBossConfigFields.getDeathMessage().replace("$players", playersList)));

        }
    }

    private void sendLocation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getLivingEntity().isDead()) {
                    cancel();
                    return;
                }
                String locationString = getLivingEntity().getLocation().getX() + ", " + getLivingEntity().getLocation().getY() + ", " + +getLivingEntity().getLocation().getZ();
                BossBar bossBar = Bukkit.createBossBar(ChatColorConverter.convert(customBossConfigFields.getLocationMessage().replace("$location", locationString)),
                        BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
                bossBar.setProgress(getHealth() / getMaxHealth());
                for (Player player : getLivingEntity().getWorld().getPlayers()) {
                    bossBar.addPlayer(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            bossBar.removePlayer(player);
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 60 * 3);

    }

}
