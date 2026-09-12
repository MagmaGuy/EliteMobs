package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.playerdata.PlayerItem;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Temporary regression coverage for broken-item bars surviving a player quit. */
class BossBarUtilTest {
    @BeforeEach
    void openServer() {
        MockBukkit.mock();
    }

    @AfterEach
    void closeServer() {
        BossBarUtil.shutdown();
        BossBarOrderManager.shutdown();
        MockBukkit.unmock();
    }

    @Test
    void clearingPlayerRemovesBrokenBarAndAllowsItToBeShownAfterRelog() {
        Player player = MockBukkit.getMock().addPlayer();
        UUID playerId = player.getUniqueId();

        BossBarUtil.DisplayBrokenItemBossBar(PlayerItem.EquipmentSlot.MAINHAND, player, "Broken sword");
        assertEquals(1, BossBarUtil.brokenPlayerItem.get(playerId).size());
        assertEquals(1, BossBarUtil.bossBars.size());

        // ElitePlayerInventoryEvents invokes this on PlayerQuitEvent. A later login
        // may use the same UUID, so the old slot entry must not suppress a new bar.
        BossBarUtil.clearPlayer(playerId);
        assertTrue(BossBarUtil.brokenPlayerItem.get(playerId).isEmpty());
        assertTrue(BossBarUtil.bossBars.isEmpty());

        BossBarUtil.DisplayBrokenItemBossBar(PlayerItem.EquipmentSlot.MAINHAND, player, "Broken sword");
        assertEquals(1, BossBarUtil.brokenPlayerItem.get(playerId).size());
        assertEquals(1, BossBarUtil.bossBars.size());
    }
}
