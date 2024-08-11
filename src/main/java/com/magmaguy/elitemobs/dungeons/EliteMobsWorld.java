package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class EliteMobsWorld {
    private static final HashMap<UUID, EliteMobsWorld> eliteMobsWorlds = new HashMap<>();
    @Getter
    private final DungeonPackagerConfigFields dungeonPackagerConfigFields;
    @Getter
    private final boolean allowExplosions;

    private EliteMobsWorld(UUID worldUUID, DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        this.dungeonPackagerConfigFields = dungeonPackagerConfigFields;
        this.allowExplosions = dungeonPackagerConfigFields.isAllowExplosions();
        eliteMobsWorlds.put(worldUUID, this);
    }

    public static void shutdown() {
        eliteMobsWorlds.clear();
    }

    @Nullable
    public static EliteMobsWorld getEliteMobsWorld(UUID worldUUID) {
        return eliteMobsWorlds.get(worldUUID);
    }

    public static boolean isEliteMobsWorld(UUID worldUUID) {
        return eliteMobsWorlds.containsKey(worldUUID);
    }

    public static void create(UUID woldUUID, DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        new EliteMobsWorld(woldUUID, dungeonPackagerConfigFields);
    }

    public static void destroy(UUID worldUUID) {
        eliteMobsWorlds.remove(worldUUID);
    }

}
