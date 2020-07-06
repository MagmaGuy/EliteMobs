package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.utils.BookMaker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatusScreen implements Listener {

    public PlayerStatusScreen(Player requestingPlayer, Player targetPlayer) {
        ItemStack writtenBook = generateBook(requestingPlayer, targetPlayer);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        List<String> pages = bookMeta.getPages();
        String debugPage = "";
        debugPage += "Is in memory: " + PlayerData.isInMemory(targetPlayer);
        ArrayList<String> newPages = new ArrayList<>(pages);
        newPages.add(debugPage);
        BookMaker.generateBook(requestingPlayer, newPages);
    }

    public PlayerStatusScreen(Player player) {
        generateBook(player, player);
    }

    private ItemStack generateBook(Player requestingPlayer, Player targetPlayer) {
        String page =
                "Money: " + EconomyHandler.checkCurrency(targetPlayer.getUniqueId()) + "\n" +
                        "Prestige Rank: " + GuildRank.getGuildPrestigeRank(targetPlayer) + "\n" +
                        "Guild Rank: " + GuildRank.getActiveGuildRank(targetPlayer) + "\n" +
                        "EliteMobs slain: " + PlayerData.getKills(targetPlayer.getUniqueId()) + "\n" +
                        "Highest Level slain: " + PlayerData.getHighestLevelKilled(targetPlayer.getUniqueId()) + "\n" +
                        "Times slain by Elites: " + PlayerData.getDeaths(targetPlayer.getUniqueId()) + "\n" +
                        "Quests completed: " + PlayerData.getQuestsCompleted(targetPlayer.getUniqueId()) + "\n" +
                        "EliteMobs Score: " + PlayerData.getScore(targetPlayer.getUniqueId());

        String helmetString = "          ☠ - 0 ilvl" + "\n";
        String chestplateString = "          ▼ - 0 ilvl" + "\n";
        String leggingsString = "          Π - 0 ilvl" + "\n";
        String bootsString = "          ╯╰ - 0 ilvl" + "\n";
        String mainHandString = "    ⚔ - 0";
        String offHandString = "    ⛨ - 0" + "\n";

        if (targetPlayer.getEquipment() != null) {
            if (targetPlayer.getEquipment().getHelmet() != null)
                helmetString = "          ☠ - " + ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getHelmet()) + " ilvl\n";
            if (targetPlayer.getEquipment().getChestplate() != null)
                chestplateString = "          ▼ - " + ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getChestplate()) + " ilvl\n";
            if (targetPlayer.getEquipment().getLeggings() != null)
                leggingsString = "          Π - " + ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getLeggings()) + " ilvl\n";
            if (targetPlayer.getEquipment().getBoots() != null)
                bootsString = "          ╯╰ - " + ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getBoots()) + " ilvl\n";
            if (targetPlayer.getEquipment().getItemInMainHand() != null && !targetPlayer.getEquipment().getItemInMainHand().getType().equals(Material.AIR))
                mainHandString = "    ⚔ - " + ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getItemInMainHand());
            if (targetPlayer.getEquipment().getItemInOffHand() != null && !targetPlayer.getEquipment().getItemInOffHand().getType().equals(Material.AIR))
                offHandString = "    ⛨ - : " + ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getItemInOffHand()) + "\n";
        }

        String materials = helmetString + chestplateString + leggingsString + bootsString + mainHandString + offHandString + "\n" +
                "Armor ilvl: " + ItemTierFinder.findArmorSetTier(targetPlayer) + "\n" +
                "Weapon ilvl: " + ItemTierFinder.findWeaponTier(targetPlayer) + "\n";

        List<String> pages = new ArrayList<String>();
        pages.add(page);
        pages.add(materials);

        return BookMaker.generateBook(requestingPlayer, pages);
    }

}
