package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.magmacore.instance.InstanceProtector;
import com.magmaguy.magmacore.instance.WorldProtectionRules;
import com.magmaguy.magmacore.util.TemporaryWorldManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks dungeon-world ownership independently of protection settings and
 * delegates enabled protection rules to MagmaCore's {@link InstanceProtector}.
 */
public class EliteMobsWorld {

    private static final HashMap<UUID, EliteMobsWorld> eliteMobsWorlds = new HashMap<>();
    private static final Set<String> loadingWorlds = new HashSet<>();

    @Getter
    private final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    private final boolean allowExplosions;

    private EliteMobsWorld(UUID worldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        this.allowExplosions = contentPackagesConfigFields.isAllowExplosions();

        eliteMobsWorlds.put(worldUUID, this);

        if (!contentPackagesConfigFields.isProtect()) return;

        World world = Bukkit.getWorld(worldUUID);
        if (world == null) return;
        InstanceProtector.addProtectedWorld(world, rulesFor(contentPackagesConfigFields));
    }

    private static WorldProtectionRules rulesFor(ContentPackagesConfigFields fields) {
        return new WorldProtectionRules()
                .setAllowExplosions(fields.isAllowExplosions())
                .setAllowLiquidFlow(fields.isAllowLiquidFlow())
                .setAllowElytra(fields.isAllowElytra())
                .setPreventFlyToggle(CombatTagConfig.isPreventFlyToggleInDungeons())
                .setPreventFriendlyFire(!DungeonsConfig.isFriendlyFireInDungeons())
                .setPreventVanillaMobSpawning(true)
                .setFireDamageMultiplier(DungeonsConfig.getFireDamageMultiplier())
                .setPoisonDamageMultiplier(DungeonsConfig.getPoisonDamageMultiplier())
                .setWitherDamageMultiplier(DungeonsConfig.getWitherDamageMultiplier());
    }

    public static void shutdown() {
        eliteMobsWorlds.keySet().removeIf(worldUUID -> Bukkit.getWorld(worldUUID) == null);
    }

    @Nullable
    public static EliteMobsWorld getEliteMobsWorld(UUID worldUUID) {
        return eliteMobsWorlds.get(worldUUID);
    }

    public static boolean isEliteMobsWorld(UUID worldUUID) {
        if (eliteMobsWorlds.containsKey(worldUUID)) return true;
        World world = Bukkit.getWorld(worldUUID);
        return world != null && loadingWorlds.contains(world.getName());
    }

    /** Registers ownership before Bukkit's synchronous world-load listeners query it. */
    public static World loadWorld(String worldName, World.Environment environment,
                                  ContentPackagesConfigFields fields) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Dungeon worlds must load on the server thread");
        boolean added = loadingWorlds.add(worldName);
        try {
            World world = TemporaryWorldManager.loadVoidTemporaryWorld(worldName, environment);
            if (world != null) create(world.getUID(), fields);
            return world;
        } finally {
            if (added) loadingWorlds.remove(worldName);
        }
    }

    public static void create(UUID worldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
        new EliteMobsWorld(worldUUID, contentPackagesConfigFields);
    }

    public static void destroy(UUID worldUUID) {
        EliteMobsWorld removed = eliteMobsWorlds.remove(worldUUID);
        if (removed == null) return;
        World world = Bukkit.getWorld(worldUUID);
        if (world != null) InstanceProtector.removeProtectedWorld(world);
    }
}
