package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Getter;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class EliteMobsWorld {
    private static final HashMap<UUID, EliteMobsWorld> eliteMobsWorlds = new HashMap<>();
    @Getter
    private final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    private boolean allowExplosions;

    private EliteMobsWorld(UUID worldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        if (!contentPackagesConfigFields.isProtect()) return;
        this.allowExplosions = contentPackagesConfigFields.isAllowExplosions();
        eliteMobsWorlds.put(worldUUID, this);
    }

    public static void shutdown() {
        eliteMobsWorlds.keySet().removeIf(worldUUID -> Bukkit.getWorld(worldUUID) == null);
    }

    @Nullable
    public static EliteMobsWorld getEliteMobsWorld(UUID worldUUID) {
        return eliteMobsWorlds.get(worldUUID);
    }

    public static boolean isEliteMobsWorld(UUID worldUUID) {
        return eliteMobsWorlds.containsKey(worldUUID);
    }

    public static void create(UUID woldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
        new EliteMobsWorld(woldUUID, contentPackagesConfigFields);
    }

    public static void destroy(UUID worldUUID) {
        eliteMobsWorlds.remove(worldUUID);
    }

}
