package com.magmaguy.elitemobs.tagger;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class PersistentTagger {
    @Getter
    private static final String ELITE_ENTITY = "EliteEntity";
    @Getter
    private static final String NPC_ENTITY = "NPCEntity";
    @Getter
    private static final String SUPER_MOB = "SuperMob";
    @Getter
    private static final String VISUAL_EFFECT = "VisualEffect";
    @Getter
    private static final String ELITE_PROJECTILE = "EliteProjectile";
    private PersistentTagger() {
    }

    public static void tag(@NotNull Entity entity, String key, String value) {
        entity.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING, value);
    }

    public static void tagElite(Entity entity, @NotNull UUID eliteUUID) {
        tag(entity, ELITE_ENTITY, eliteUUID.toString());
    }

    public static boolean isEliteEntity(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, ELITE_ENTITY), PersistentDataType.STRING);
    }

    public static EliteEntity getEliteEntity(Entity entity) {
        UUID uuid = getUUID(entity, ELITE_ENTITY);
        if (uuid == null) return null;
        return EntityTracker.getEliteMobEntities().get(uuid);
    }

    public static void tagNPC(Entity entity, @NotNull UUID npcUUID) {
        tag(entity, NPC_ENTITY, npcUUID.toString());
    }

    public static boolean isNPC(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, NPC_ENTITY), PersistentDataType.STRING);
    }

    public static NPCEntity getNPC(Entity entity) {
        UUID uuid = getUUID(entity, NPC_ENTITY);
        if (uuid == null) return null;
        return EntityTracker.getNpcEntities().get(uuid);
    }

    public static void tagSuperMob(Entity entity) {
        tag(entity, SUPER_MOB, entity.getType().toString());
    }

    public static boolean isSuperMob(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, SUPER_MOB), PersistentDataType.STRING);
    }

    public static void tagVisualEffect(Entity entity) {
        tag(entity, VISUAL_EFFECT, entity.getType().toString());
    }

    public static boolean isVisualEffect(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, VISUAL_EFFECT), PersistentDataType.STRING);
    }

    public static void tagEliteProjectile(Projectile projectile){
        tag(projectile, ELITE_PROJECTILE, "");
    }

    public static boolean isEliteProjectile(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, ELITE_PROJECTILE), PersistentDataType.STRING);
    }

    @Nullable
    public static UUID getUUID(Entity entity, String key) {
        if (entity == null) return null;
        String uuidString = entity.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING);
        if (uuidString == null) return null;
        return UUID.fromString(uuidString);
    }

}
