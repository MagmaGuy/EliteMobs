package com.magmaguy.elitemobs.thirdparty.geyser;

import com.magmaguy.elitemobs.utils.Developer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class GeyserDetector {
    private static FloodgateApi floodgateApi = FloodgateApi.getInstance();

    public static boolean bedrockPlayer(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Geyser")) {
            Developer.message("geyser could not be detected");
            if (Bukkit.getPluginManager().isPluginEnabled("GeyserMC"))
                Developer.message("geyser was under the name GeyserMC");
            return false;
        }
        return floodgateApi.isFloodgatePlayer(player.getUniqueId());
    }
}
