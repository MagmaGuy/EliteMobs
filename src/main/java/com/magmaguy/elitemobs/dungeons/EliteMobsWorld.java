package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.magmacore.instance.InstanceProtector;
import com.magmaguy.magmacore.instance.WorldProtectionRules;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Façade over MagmaCore's {@link InstanceProtector} that keeps EliteMobs'
 * existing per-world config (ContentPackagesConfigFields) accessible while
 * delegating actual event handling to the shared MagmaCore listener.
 *
 * Behaviorally identical to the pre-move implementation; the difference is
 * that the protection event handlers now live in
 * {@code com.magmaguy.magmacore.instance.InstanceProtector} and the same
 * suite can be applied to non-EliteMobs worlds via that API.
 */
public class EliteMobsWorld {

    private static final HashMap<UUID, EliteMobsWorld> eliteMobsWorlds = new HashMap<>();

    @Getter
    private final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    private final boolean allowExplosions;

    private EliteMobsWorld(UUID worldUUID, ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        this.allowExplosions = contentPackagesConfigFields.isAllowExplosions();

        if (!contentPackagesConfigFields.isProtect()) return;

        eliteMobsWorlds.put(worldUUID, this);

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
        return eliteMobsWorlds.containsKey(worldUUID);
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
