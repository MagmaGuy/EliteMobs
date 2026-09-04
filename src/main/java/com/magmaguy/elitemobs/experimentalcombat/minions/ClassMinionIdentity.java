package com.magmaguy.elitemobs.experimentalcombat.minions;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/** Stable PDC identity shared by minion lifecycle and hostile-target authorization. */
public final class ClassMinionIdentity {
    private static final String KEY_NAME = "class_minion";

    private final NamespacedKey key;

    ClassMinionIdentity(Plugin plugin) {
        this.key = new NamespacedKey(Objects.requireNonNull(plugin, "plugin"), KEY_NAME);
    }

    void mark(Entity entity) {
        Objects.requireNonNull(entity, "entity").getPersistentDataContainer()
                .set(key, PersistentDataType.BYTE, (byte) 1);
    }

    boolean matches(Entity entity) {
        return entity != null && entity.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public static boolean isMinion(Entity entity) {
        Plugin plugin = MetadataHandler.PLUGIN;
        return plugin != null && entity != null && entity.getPersistentDataContainer().has(
                new NamespacedKey(plugin, KEY_NAME), PersistentDataType.BYTE);
    }
}
