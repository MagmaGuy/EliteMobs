package com.magmaguy.elitemobs.utils;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.playerdata.PlayerItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BossBarUtil {
    public static HashSet<BossBar> bossBars = new HashSet<>();
    public static ArrayListMultimap<UUID, PlayerBrokenItemBar> brokenPlayerItem = ArrayListMultimap.create();

    private BossBarUtil() {
    }

    public static void shutdown() {
        for (PlayerBrokenItemBar playerBrokenItemBar : new ArrayList<>(brokenPlayerItem.values()))
            playerBrokenItemBar.destroy();
        bossBars.forEach(BossBar::removeAll);
        bossBars.clear();
        brokenPlayerItem.clear();
    }

    public static void DisplayBrokenItemBossBar(PlayerItem.EquipmentSlot equipmentSlot, Player player, String title) {
        UUID playerUUID = player.getUniqueId();
        List<PlayerBrokenItemBar> arrayList = brokenPlayerItem.get(playerUUID);
        boolean alreadyExists = false;
        for (PlayerBrokenItemBar playerBrokenItemBar : arrayList) {
            if (playerBrokenItemBar.getEquipmentSlot().equals(equipmentSlot)) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            brokenPlayerItem.put(playerUUID, new PlayerBrokenItemBar(equipmentSlot, player, title, BarColor.RED, BarStyle.SOLID));
        }
    }

    /**
     * Drops every broken-item bar this player has. Entries used to survive a relog:
     * the stale entry then blocked any new bar for that slot (the dedup counted it
     * as already shown) while its BossBar was still bound to the previous Player
     * object, so the warning silently never came back after rejoining.
     */
    public static void clearPlayer(UUID playerUUID) {
        for (PlayerBrokenItemBar playerBrokenItemBar : new ArrayList<>(brokenPlayerItem.get(playerUUID)))
            playerBrokenItemBar.destroy();
        brokenPlayerItem.removeAll(playerUUID);
    }

    public static void HideBrokenItemBossBar(PlayerItem.EquipmentSlot equipmentSlot, Player player) {
        UUID playerUUID = player.getUniqueId();
        List<PlayerBrokenItemBar> arrayList = brokenPlayerItem.get(playerUUID);
        PlayerBrokenItemBar storedPlayerBrokenItemBar = null;

        for (PlayerBrokenItemBar playerBrokenItemBar : arrayList) {
            if (playerBrokenItemBar.getEquipmentSlot().equals(equipmentSlot)) {
                storedPlayerBrokenItemBar = playerBrokenItemBar;
                playerBrokenItemBar.destroy();
                break;
            }
        }

        if (storedPlayerBrokenItemBar != null)
            brokenPlayerItem.remove(playerUUID, storedPlayerBrokenItemBar);
    }

    private static BossBar CreateBossBar(Player player, String title, BarColor barColor, BarStyle barStyle) {
        BossBar bossBar = Bukkit.createBossBar(title, barColor, barStyle);
        BossBarOrderManager.show(player, bossBar);
        bossBars.add(bossBar);
        return bossBar;
    }

    private static void DestroyBossBar(Player player, BossBar bossBar) {
        BossBarOrderManager.hide(player, bossBar);
        bossBar.removeAll();
        bossBars.remove(bossBar);
    }

    private static class PlayerBrokenItemBar {
        @Getter
        private final PlayerItem.EquipmentSlot equipmentSlot;
        @Getter
        private final BossBar bossBar;
        private final Player player;

        private PlayerBrokenItemBar(PlayerItem.EquipmentSlot equipmentSlot, Player player, String title, BarColor barColor, BarStyle barStyle) {
            this.equipmentSlot = equipmentSlot;
            this.player = player;
            bossBar = CreateBossBar(player, title, barColor, barStyle);
        }

        private void destroy() {
            DestroyBossBar(player, bossBar);
        }
    }


}
