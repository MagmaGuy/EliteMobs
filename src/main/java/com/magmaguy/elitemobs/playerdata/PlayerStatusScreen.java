package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatusScreen implements Listener {

    public PlayerStatusScreen(Player player) {
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        bookMeta.setTitle(player.getDisplayName() + " stats");
        bookMeta.setAuthor("EliteMobs");

        String page =
                "Money: " + EconomyHandler.checkCurrency(player.getUniqueId()) + "\n" +
                        "Prestige Rank: " + GuildRank.getGuildPrestigeRank(player) + "\n" +
                        "Guild Rank: " + GuildRank.getActiveGuildRank(player) + "\n" +
                        "EliteMobs slain: " + PlayerData.getKills(player.getUniqueId()) + "\n" +
                        "Highest Level slain: " + PlayerData.getHighestLevelKilled(player.getUniqueId()) + "\n" +
                        "Times slain by Elites: " + PlayerData.getDeaths(player.getUniqueId()) + "\n" +
                        "Quests completed: " + PlayerData.getQuestsCompleted(player.getUniqueId());

        String helmetString = "Helmet ilvl = 0" + "\n";
        String chestplateString = "Chestplate ilvl = 0" + "\n";
        String leggingsString = "Leggings ilvl = 0" + "\n";
        String bootsString = "Boots ilvl = 0" + "\n";
        String mainHandString = "Main Hand ilvl = 0" + "\n";
        String offHandString = "Offhand ilvl = 0" + "\n";

        if (player.getEquipment() != null) {
            if (player.getEquipment().getHelmet() != null)
                helmetString = "Helmet ilvl: " + ItemTierFinder.findBattleTier(player.getEquipment().getHelmet()) + "\n";
            if (player.getEquipment().getChestplate() != null)
                chestplateString = "Chestplate ilvl: " + ItemTierFinder.findBattleTier(player.getEquipment().getChestplate()) + "\n";
            if (player.getEquipment().getLeggings() != null)
                leggingsString = "Leggings ilvl: " + ItemTierFinder.findBattleTier(player.getEquipment().getLeggings()) + "\n";
            if (player.getEquipment().getBoots() != null)
                bootsString = "Boots ilvl: " + ItemTierFinder.findBattleTier(player.getEquipment().getBoots()) + "\n";
            if (player.getEquipment().getItemInMainHand() != null && !player.getEquipment().getItemInMainHand().getType().equals(Material.AIR))
                mainHandString = "Main Hand ilvl: " + ItemTierFinder.findBattleTier(player.getEquipment().getItemInMainHand()) + "\n";
            if (player.getEquipment().getItemInOffHand() != null && !player.getEquipment().getItemInOffHand().getType().equals(Material.AIR))
                offHandString = "Boots ilvl: " + ItemTierFinder.findBattleTier(player.getEquipment().getItemInOffHand()) + "\n";
        }

        String materials = helmetString + chestplateString + leggingsString + bootsString + mainHandString + offHandString + "\n" +
                "Armor ilvl (dmg-): " + ItemTierFinder.findArmorSetTier(player) + "\n" +
                "Weapon ilvl (dmg+): " + ItemTierFinder.findWeaponTier(player) + "\n";

        List<String> pages = new ArrayList<String>();
        pages.add(page);
        pages.add(materials);

        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
    }

}
