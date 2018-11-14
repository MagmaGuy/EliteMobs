package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;

public class EntityTracker implements Listener {

    /*
    These HashSets track basically everything for live plugin entities
     */
    private static HashSet<LivingEntity> superMobs = new HashSet<>();
    private static HashSet<EliteMobEntity> eliteMobs = new HashSet<>();
    private static HashSet<LivingEntity> eliteMobsLivingEntities = new HashSet<>();

    private static HashSet<LivingEntity> naturalEntities = new HashSet<>();
    private static HashSet<ArmorStand> armorStands = new HashSet<>();
    private static HashSet<Item> itemVisualEffects = new HashSet<>();

    /*
    This HashSet shouldn't really be scanned during runtime for aside from the occasional updates, it mostly exists to
    cull entities once the server shuts down
     */
    private static HashSet<Entity> cullablePluginEntities = new HashSet<>();

    /*
    Starts tracking elite mobs
     */
    public static void registerEliteMob(EliteMobEntity eliteMobEntity) {
        eliteMobs.add(eliteMobEntity);
        eliteMobsLivingEntities.add(eliteMobEntity.getLivingEntity());
        registerCullableEntity(eliteMobEntity.getLivingEntity());
    }

    /*
    Starts tracking super mob
     */
    public static void registerSuperMob(LivingEntity livingEntity) {
        if (!SuperMobProperties.isValidSuperMobType(livingEntity)) return;
        superMobs.add(livingEntity);
    }

    /*
    Registers mobs that spawn naturally, necessary for elite mob rewards
     */
    public static void registerNaturalEntity(LivingEntity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return;
        naturalEntities.add(entity);
    }


    public static void registerArmorStands(ArmorStand armorStand) {
        armorStands.add(armorStand);
        registerCullableEntity(armorStand);
    }

    public static void registerItemVisualEffects(Item item) {
        itemVisualEffects.add(item);
        registerCullableEntity(item);
    }

    /*
    Starts tracking any entity generated or managed by EliteMobs, useful for when they need to be culled
    Does not include super mobs to avoid culling them by mistake
     */
    public static void registerCullableEntity(Entity entity) {
        cullablePluginEntities.add(entity);
    }

    public static void checkEntityState() {

        new BukkitRunnable() {
            @Override
            public void run() {
                updateSuperMobs();
                updateEliteMobEntities();
                updateLivingEntities();
                updateCullables();
                updateAmorStands();
                updateItems();
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60 * 5, 20 * 60 * 5);

    }

    private static void updateSuperMobs() {
        Iterator iterator = superMobs.iterator();
        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            if (!entity.isValid())
                iterator.remove();
        }
    }

    private static void updateLivingEntities() {
        Iterator iterator = naturalEntities.iterator();
        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            if (!entity.isValid())
                iterator.remove();
        }
    }

    private static void updateEliteMobEntities() {
        Iterator iterator = eliteMobs.iterator();
        while (iterator.hasNext()) {
            Entity entity = ((EliteMobEntity) iterator.next()).getLivingEntity();
            if (entity.isDead() ||
                    !entity.isValid() && ((LivingEntity) entity).getRemoveWhenFarAway())
                iterator.remove();
        }
    }

    private static void updateAmorStands() {
        Iterator iterator = armorStands.iterator();
        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            if (!entity.isValid())
                iterator.remove();
        }
    }

    private static void updateCullables() {
        Iterator iterator = cullablePluginEntities.iterator();
        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            if (!entity.isValid())
                iterator.remove();
        }
    }

    private static void updateItems() {
        Iterator iterator = itemVisualEffects.iterator();
        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            if (!entity.isValid())
                iterator.remove();
        }
    }

    public static boolean isSuperMob(Entity entity) {
        if (!SuperMobProperties.isValidSuperMobType(entity)) return false;
        return superMobs.contains(entity);
    }

    public static boolean isEliteMob(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return false;
        return eliteMobsLivingEntities.contains(entity);
    }

    public static EliteMobEntity getEliteMobEntity(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return null;
        for (EliteMobEntity eliteMobEntity : eliteMobs)
            if (eliteMobEntity.getLivingEntity().equals(entity))
                return eliteMobEntity;
        return null;
    }

    public static boolean isNaturalEntity(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return false;
        return naturalEntities.contains(entity);
    }

    public static boolean isItemVisualEffect(Entity entity) {
        return (itemVisualEffects.contains(entity));
    }

    public static boolean hasPower(ElitePower mobPower, Entity entity) {
        EliteMobEntity eliteMobEntity = getEliteMobEntity(entity);
        if (eliteMobEntity == null) return false;
        return eliteMobEntity.hasPower(mobPower);
    }

    public static boolean hasPower(ElitePower mobPower, EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity == null) return false;
        return eliteMobEntity.hasPower(mobPower);
    }

    public static void unregisterCullableEntity(Entity entity) {
        cullablePluginEntities.remove(entity);
    }

    public static void unregisterItemEntity(Item item) {
        itemVisualEffects.remove(item);
    }

    /*
    Used outside this class to remove individual elite mobs
     */
    public static void unregisterEliteMob(EliteMobEntity eliteMobEntity) {
        eliteMobs.remove(eliteMobEntity);
        naturalEntities.remove(eliteMobEntity.getLivingEntity());
        cullablePluginEntities.remove(eliteMobEntity.getLivingEntity());
    }

    /*
    Used in this class to do mass wipes of data
     */
    private static void unregisterEliteMob(Entity entity) {
        if (!isEliteMob(entity)) return;
        EliteMobEntity eliteMobEntity = getEliteMobEntity(entity);
        if (eliteMobEntity == null) return;
        eliteMobs.remove(eliteMobEntity);
        eliteMobsLivingEntities.remove(eliteMobEntity.getLivingEntity());
    }

    public static void unregisterArmorStand(Entity armorStand) {
        if (!armorStand.getType().equals(EntityType.ARMOR_STAND)) return;
        armorStands.remove(armorStand);
    }

    public static HashSet<EliteMobEntity> getEliteMobs() {
        return eliteMobs;
    }

    public static boolean getIsArmorStand(Entity entity) {
        if (!entity.getType().equals(EntityType.ARMOR_STAND)) return false;
        return (armorStands.contains(entity));
    }

    public static void unregisterSuperMob(Entity entity) {
        if (!SuperMobProperties.isValidSuperMobType(entity)) return;
        superMobs.remove(entity);
    }

    public static void unregisterNaturalEntity(Entity entity) {
        if (EliteMobProperties.isValidEliteMobType(entity)) return;
        naturalEntities.remove(entity);
    }

    /*
    Custom spawn reasons can be considered as natural spawns under specific config options
     */
    @EventHandler(priority = EventPriority.LOW)
    public void registerNaturalEntity(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) ||
                event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM) &&
                        !ConfigValues.defaultConfig.getBoolean(DefaultConfig.STRICT_SPAWNING_RULES))
            registerNaturalEntity(event.getEntity());
    }

    /*
    Natural entities get unregistered from being natural when exploit abuse is detected from the players
     */
    public static void unregisterNaturalEntity(LivingEntity livingEntity) {
        naturalEntities.remove(livingEntity);
    }

    public static void shutdownPurger() {

        for (Entity entity : cullablePluginEntities)
            entity.remove();

        eliteMobs.clear();
        eliteMobsLivingEntities.clear();
        superMobs.clear();
        itemVisualEffects.clear();
        armorStands.clear();
        naturalEntities.clear();

    }

    /*
    This is run in sync for performance reasons
     */
    public static void wipeEntity(Entity entity) {
        unregisterEliteMob(entity);
        unregisterSuperMob(entity);
        unregisterNaturalEntity(entity);
        unregisterArmorStand(entity);
        unregisterCullableEntity(entity);
    }

    public static void chunkWiper(ChunkUnloadEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : event.getChunk().getEntities())
                    wipeEntity(entity);
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static void deathWipe(EntityDeathEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                wipeEntity(event.getEntity());
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

}
