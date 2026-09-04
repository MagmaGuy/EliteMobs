package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.geyser.GeyserDetector;
import com.magmaguy.magmacore.util.VersionChecker;
import org.bukkit.entity.Player;

/** Shared capability policy for menus which use modern dialogs with an inventory fallback. */
public final class MenuPresentation {
    private MenuPresentation() {
    }

    public static boolean supportsDialogs(Player player) {
        return PlayerData.getUseBookMenus(player.getUniqueId())
                && !GeyserDetector.bedrockPlayer(player)
                && !DefaultConfig.isOnlyUseBedrockMenus()
                && !VersionChecker.serverVersionOlderThan(21, 6);
    }
}
