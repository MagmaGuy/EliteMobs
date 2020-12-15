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

    public DebugScreen(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("[EliteMobs] Correct command syntax: /em debug [customBossFilename] OR /em debug [playerName]");
            player.sendMessage("Currently only works with regional bosses!");
            player.sendMessage("Also works with fragments of filenames (i.e. 'pirate' instead of 'pirate_1.yml')");
            player.sendMessage("Also works with fragments of boss names (as they show up in-game, careful with color codes)");
            return;
        }

        if (Bukkit.getPlayer(args[1]) != null)
            new PlayerStatusScreen(player, Bukkit.getPlayer(args[1]));
        else CustomBossDebugScreen(player, args);

    }

    public void CustomBossDebugScreen(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("[EliteMobs] Correct command syntax: /em debug [filename]");
            player.sendMessage("Currently only works with regional bosses!");
            player.sendMessage("Also works with fragments of filenames (i.e. 'pirate' instead of 'pirate_1.yml')");
            player.sendMessage("Also works with fragments of boss names (as they show up in-game, careful with color codes)");
            return;
        }

        List<String> pages = new ArrayList<String>();

        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntityList()) {
            if (!regionalBossEntity.getCustomBossConfigFields().getFileName().contains(args[1]) &&
                    !regionalBossEntity.getCustomBossConfigFields().getName().toLowerCase().contains(args[1].toLowerCase()))
                continue;
            String page = regionalBossEntity.getCustomBossConfigFields().getFileName() + "\n";
            page += "Name: " + ChatColorConverter.convert(regionalBossEntity.getCustomBossConfigFields().getName()) + ChatColor.BLACK + "\n";
            page += "Level: " + regionalBossEntity.getCustomBossConfigFields().getLevel() + "\n";
            page += "Is Alive (EM): " + regionalBossEntity.isAlive + "\n";
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
            page += "Respawning: " + regionalBossEntity.inCooldown;
            pages.add(page);
        }

        BookMaker.generateBook(player, pages);

    }

}
