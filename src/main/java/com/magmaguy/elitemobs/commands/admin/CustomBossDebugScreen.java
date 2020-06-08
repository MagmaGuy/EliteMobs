package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.custombosses.RegionalBossEntity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class CustomBossDebugScreen {

    public CustomBossDebugScreen(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage("[EliteMobs] Correct command syntax: /em debug [filename]");
            player.sendMessage("Currently only works with regional bosses!");
            player.sendMessage("Also works with fragments of filenames (i.e. 'pirate' instead of 'pirate_1.yml')");
            player.sendMessage("Also works with fragments of boss names (as they show up in-game, careful with color codes)");
            return;
        }

        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        bookMeta.setTitle(player.getDisplayName() + " stats");
        bookMeta.setAuthor("EliteMobs");
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

        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
    }

}
