package com.magmaguy.elitemobs.utils;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.playerdata.PlayerItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BossBarUtil {
    public static HashSet<BossBar> bossBars = new HashSet<>();
    public static ArrayListMultimap<UUID, PlayerBrokenItemBar> brokenPlayerItem = ArrayListMultimap.create();

    private BossBarUtil() {
    }

    public static void shutdown() {
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

    public static void HideBrokenItemBossBar(PlayerItem.EquipmentSlot equipmentSlot, Player player) {
        UUID playerUUID = player.getUniqueId();
        List<PlayerBrokenItemBar> arrayList = brokenPlayerItem.get(playerUUID);
        PlayerBrokenItemBar storedPlayerBrokenItemBar = null;

        for (PlayerBrokenItemBar playerBrokenItemBar : arrayList) {
            if (playerBrokenItemBar.getEquipmentSlot().equals(equipmentSlot)) {
                storedPlayerBrokenItemBar = playerBrokenItemBar;
                DestroyBossBar(playerBrokenItemBar.bossBar);
                break;
            }
        }

        brokenPlayerItem.remove(playerUUID, storedPlayerBrokenItemBar);
    }

    private static BossBar CreateBossBar(Player player, String title, BarColor barColor, BarStyle barStyle) {
        BossBar bossBar = Bukkit.createBossBar(title, barColor, barStyle);
        bossBar.addPlayer(player);
        bossBars.add(bossBar);
        return bossBar;
    }

    private static void DestroyBossBar(BossBar bossBar) {
        bossBar.removeAll();
        bossBars.remove(bossBar);
    }

    private static class PlayerBrokenItemBar {
        @Getter
        private final PlayerItem.EquipmentSlot equipmentSlot;
        @Getter
        private final BossBar bossBar;

        private PlayerBrokenItemBar(PlayerItem.EquipmentSlot equipmentSlot, Player player, String title, BarColor barColor, BarStyle barStyle) {
            this.equipmentSlot = equipmentSlot;
            bossBar = CreateBossBar(player, title, barColor, barStyle);
        }
    }


}
