package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.magmacore.util.VersionChecker;
import org.bukkit.entity.Player;

/** Shared capability policy for menus which use modern dialogs with an inventory fallback. */
public final class MenuPresentation {
    private MenuPresentation() {
    }

    public static boolean supportsDialogs(Player player) {
        return PlayerData.getUseBookMenus(player.getUniqueId())
                && !usesInventoryMenusForBedrock(player)
                && !DefaultConfig.isOnlyUseBedrockMenus()
                && !VersionChecker.serverVersionOlderThan(21, 6);
    }

    /** Bedrock players get inventory menus unless the server lets Geyser turn dialogs into forms. */
    public static boolean usesInventoryMenusForBedrock(Player player) {
        if (!BedrockChecker.isBedrock(player)) return false;
        return !DefaultConfig.isAllowDialogMenusForBedrockPlayers() || VersionChecker.serverVersionOlderThan(21, 6);
    }
}
