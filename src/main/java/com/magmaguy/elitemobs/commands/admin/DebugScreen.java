package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.utils.BookMaker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DebugScreen {

    public static void open(Player player, String argument) {
        if (Bukkit.getPlayer(argument) != null)
            new PlayerStatusScreen(player, Bukkit.getPlayer(argument));
        else openBossScreen(player, argument);
    }

    private static void openBossScreen(Player player, String argument) {

        List<String> pages = new ArrayList<String>();

        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet()) {
            if (!regionalBossEntity.getCustomBossConfigFields().getFileName().contains(argument) &&
                    !regionalBossEntity.getCustomBossConfigFields().getName().toLowerCase().contains(argument.toLowerCase()))
                continue;
            String page = regionalBossEntity.getCustomBossConfigFields().getFileName() + "\n";
            page += "Name: " + ChatColorConverter.convert(regionalBossEntity.getCustomBossConfigFields().getName()) + ChatColor.BLACK + "\n";
            page += "Level: " + regionalBossEntity.getCustomBossConfigFields().getLevel() + "\n";
            if (regionalBossEntity.customBossEntity != null && regionalBossEntity.customBossEntity.advancedGetEntity() != null) {
                page += "Is Alive (MC): " + !regionalBossEntity.customBossEntity.advancedGetEntity().isDead() + "\n";
                page += "XYZ: " +
                        regionalBossEntity.customBossEntity.advancedGetEntity().getLocation().getBlockX() + ", " +
                        regionalBossEntity.customBossEntity.advancedGetEntity().getLocation().getBlockY() + ", " +
                        regionalBossEntity.customBossEntity.advancedGetEntity().getLocation().getBlockZ() + "\n";
                page += "Has AI: " + !regionalBossEntity.customBossEntity.advancedGetEntity().hasAI() + "\n";
            } else
                page += "Is Alive (MC): false\n";
            page += "Is Persistent: " + regionalBossEntity.getCustomBossConfigFields().getIsPersistent() + "\n";
            if (regionalBossEntity.getCustomBossConfigFields().getIsPersistent())
                page += "Is Respawning: " + regionalBossEntity.isRespawning() + "\n";
            pages.add(page);
        }

        BookMaker.generateBook(player, pages);

    }

}
