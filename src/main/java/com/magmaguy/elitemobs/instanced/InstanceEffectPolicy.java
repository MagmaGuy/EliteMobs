package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** Revalidates scripted status effects when they execute, including delayed callbacks. */
public final class InstanceEffectPolicy {
    private InstanceEffectPolicy() {}

    public static boolean canAffect(EliteEntity source, LivingEntity target) {
        if (target == null || !target.isValid()) return false;
        if (!(target instanceof Player player)) return true;
        Location origin = source == null ? null : source.getLocation();
        if (!player.isOnline() || player.isDead() || origin == null
                || !player.getWorld().equals(origin.getWorld())) return false;
        if (source instanceof InstancedBossEntity boss && boss.getDungeonInstance() != null)
            return isParticipant(boss.getDungeonInstance(), player);
        // Reinforcements and later boss phases may use ordinary CustomBossEntity instances.
        for (DungeonInstance dungeon : DungeonInstance.getDungeonInstances())
            if (player.getWorld().equals(dungeon.getWorld())) return isParticipant(dungeon, player);
        return true;
    }

    private static boolean isParticipant(DungeonInstance dungeon, Player player) {
        return !dungeon.isDefunct() && PlayerData.getMatchInstance(player) == dungeon
                && dungeon.getPlayers().contains(player);
    }
}
