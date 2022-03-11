package com.magmaguy.elitemobs.thirdparty.paper;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PaperMC {
    @Getter
    private static boolean paperMC = false;

    public static void initialize() {
        try {
            paperMC = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().info("Not paper");
        }
    }

    public static boolean checkClient(Player player) {

        return true;
    }
}
