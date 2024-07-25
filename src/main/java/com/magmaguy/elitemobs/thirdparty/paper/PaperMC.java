package com.magmaguy.elitemobs.thirdparty.paper;

import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.entity.Player;

public class PaperMC {
    @Getter
    private static boolean paperMC = false;

    public static void initialize() {
        try {
            paperMC = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        } catch (ClassNotFoundException e) {
            Logger.info("Not paper");
        }
    }

    public static boolean checkClient(Player player) {

        return true;
    }
}
