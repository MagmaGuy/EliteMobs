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
            if (!regionalBossEntity.getCustomBossesConfigFields().getFilename().contains(argument) &&
                    !regionalBossEntity.getCustomBossesConfigFields().getName().toLowerCase().contains(argument.toLowerCase()))
                continue;
            String page = regionalBossEntity.getCustomBossesConfigFields().getFilename() + "\n";
            page += "Name: " + ChatColorConverter.convert(regionalBossEntity.getCustomBossesConfigFields().getName()) + ChatColor.BLACK + "\n";
            page += "Level: " + regionalBossEntity.getCustomBossesConfigFields().getLevel() + "\n";
            if (regionalBossEntity.getLivingEntity() != null) {
                page += "Is Alive (MC): " + !regionalBossEntity.getLivingEntity().isDead() + "\n";
                page += "XYZ: " +
                        regionalBossEntity.getLocation().getBlockX() + ", " +
                        regionalBossEntity.getLocation().getBlockY() + ", " +
                        regionalBossEntity.getLocation().getBlockZ() + "\n";
                page += "Has AI: " + !regionalBossEntity.getLivingEntity().hasAI() + "\n";
            } else
                page += "Is Alive (MC): false\n";
            if (regionalBossEntity.getLocation() != null && player.getWorld().equals(regionalBossEntity.getLocation().getWorld()))
                page += "Spawn distance: X=" + (int) (player.getLocation().getX() - regionalBossEntity.getSpawnLocation().getX())
                        + " | Y=" + (int) (player.getLocation().getY() - regionalBossEntity.getSpawnLocation().getY()) +
                        " | Z=" + (int) (player.getLocation().getZ() - regionalBossEntity.getSpawnLocation().getZ()) + "\n";
            page += "Is Persistent: " + regionalBossEntity.getCustomBossesConfigFields().isPersistent() + "\n";
            if (regionalBossEntity.getCustomBossesConfigFields().isPersistent())
                page += "Is Respawning: " + regionalBossEntity.isRespawning() + "\n";
            pages.add(page);
        }

        BookMaker.generateBook(player, pages);

    }

}
