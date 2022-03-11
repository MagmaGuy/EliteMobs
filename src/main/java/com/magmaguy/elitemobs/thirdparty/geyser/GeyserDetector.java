package com.magmaguy.elitemobs.thirdparty.geyser;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.api.Geyser;
import org.geysermc.api.session.Connection;

public class GeyserDetector {

    public static boolean bedrockPlayer(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot"))
            return false;
        Connection playerConnection = Geyser.api().connectionByUuid(player.getUniqueId());
        return playerConnection != null;
    }
}
