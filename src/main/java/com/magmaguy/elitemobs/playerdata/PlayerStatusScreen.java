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
                        "Quests completed: " + PlayerData.getQuestsCompleted(player.getUniqueId()) + "\n" +
                        "EliteMobs Score: " + PlayerData.getScore(player.getUniqueId());

        String helmetString = "          ☠ - 0 ilvl" + "\n";
        String chestplateString = "          ▼ - 0 ilvl" + "\n";
        String leggingsString = "          Π - 0 ilvl" + "\n";
        String bootsString = "          ╯╰ - 0 ilvl" + "\n";
        String mainHandString = "    ⚔ - 0";
        String offHandString = "    ⛨ - 0" + "\n";

        if (player.getEquipment() != null) {
            if (player.getEquipment().getHelmet() != null)
                helmetString = "          ☠ - " + ItemTierFinder.findBattleTier(player.getEquipment().getHelmet()) + " ilvl\n";
            if (player.getEquipment().getChestplate() != null)
                chestplateString = "          ▼ - " + ItemTierFinder.findBattleTier(player.getEquipment().getChestplate()) + " ilvl\n";
            if (player.getEquipment().getLeggings() != null)
                leggingsString = "          Π - " + ItemTierFinder.findBattleTier(player.getEquipment().getLeggings()) + " ilvl\n";
            if (player.getEquipment().getBoots() != null)
                bootsString = "          ╯╰ - " + ItemTierFinder.findBattleTier(player.getEquipment().getBoots()) + " ilvl\n";
            if (player.getEquipment().getItemInMainHand() != null && !player.getEquipment().getItemInMainHand().getType().equals(Material.AIR))
                mainHandString = "    ⚔ - " + ItemTierFinder.findBattleTier(player.getEquipment().getItemInMainHand());
            if (player.getEquipment().getItemInOffHand() != null && !player.getEquipment().getItemInOffHand().getType().equals(Material.AIR))
                offHandString = "    ⛨ - : " + ItemTierFinder.findBattleTier(player.getEquipment().getItemInOffHand()) + "\n";
        }

        String materials = helmetString + chestplateString + leggingsString + bootsString + mainHandString + offHandString + "\n" +
                "Armor ilvl: " + ItemTierFinder.findArmorSetTier(player) + "\n" +
                "Weapon ilvl: " + ItemTierFinder.findWeaponTier(player) + "\n";

        List<String> pages = new ArrayList<String>();
        pages.add(page);
        pages.add(materials);

        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);
        player.openBook(writtenBook);
    }

}
