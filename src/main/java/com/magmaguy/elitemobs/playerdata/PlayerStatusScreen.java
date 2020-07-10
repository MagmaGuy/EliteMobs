package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.utils.BookMaker;
import com.magmaguy.elitemobs.utils.VersionChecker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
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
        TextComponent[] pages = new TextComponent[50];
        pages[0] = coverPage(targetPlayer);
        pages[1] = statsPage(targetPlayer);
        pages[2] = gearPage(targetPlayer);
        pages[3] = commandsPage();
        pages[4] = bossTrackingPage(targetPlayer);

        int counter = 0;
        for (TextComponent textComponent : pages) {
            if (textComponent != null)
                counter++;
        }

        TextComponent[] finalPages = new TextComponent[counter];
        int secondCounter = 0;
        for (TextComponent textComponent : pages) {
            if (textComponent != null) {
                finalPages[secondCounter] = textComponent;
                secondCounter++;
            }
        }

        return BookMaker.generateBook(requestingPlayer, finalPages);
    }

    private TextComponent coverPage(Player targetPlayer) {
        TextComponent textComponent = new TextComponent();

        TextComponent adventurersGuild = new TextComponent(
                ChatColorConverter.convert("&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                        "&5&l/ag &7- &6EliteMobs Hub\n" +
                        "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"));
        setHoverText(adventurersGuild,
                "CLICK TO USE\n" +
                        "The place where you can find\n" +
                        "NPCs that give quests, buy and\n" +
                        "sell items, give advice and more!");
        adventurersGuild.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ag"));
        textComponent.addExtra(adventurersGuild);

        textComponent.addExtra("\n");

        TextComponent index = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Index:\n\n");
        textComponent.addExtra(index);

        TextComponent statsPage = new TextComponent(ChatColor.DARK_AQUA + "p. 2" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + "Stats\n\n");
        setHoverText(statsPage, "Click to go!");
        statsPage.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2"));
        textComponent.addExtra(statsPage);

        TextComponent gearComponent = new TextComponent(ChatColor.DARK_AQUA + "p. 3" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + "Gear\n\n");
        setHoverText(gearComponent, "Click to go!");
        gearComponent.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "3"));
        textComponent.addExtra(gearComponent);

        TextComponent commandsComponent = new TextComponent(ChatColor.DARK_AQUA + "p. 4" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + "Commands\n\n");
        setHoverText(commandsComponent, "Click to go!");
        commandsComponent.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "4"));
        textComponent.addExtra(commandsComponent);

        TextComponent trackingComponent = new TextComponent(ChatColor.DARK_AQUA + "p. 5" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + "Boss Tracking\n\n");
        setHoverText(trackingComponent, "Click to go!");
        trackingComponent.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "5"));
        textComponent.addExtra(trackingComponent);

        return textComponent;
    }

    private TextComponent statsPage(Player targetPlayer) {
        TextComponent textComponent = new TextComponent();

        TextComponent header = new TextComponent(
                ChatColorConverter.convert("&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                        "&5&lPlayer Stats:\n" +
                        "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n\n"));
        textComponent.addExtra(header);

        TextComponent moneyComponent = new TextComponent(ChatColorConverter.convert("&2Money: &a") + EconomyHandler.checkCurrency(targetPlayer.getUniqueId()) + "\n\n");
        setHoverText(moneyComponent,
                "Kill Elite Mobs to loot currency or\n" +
                        "sell their drops in /em shop or\n" +
                        "complete quests in /em quest!");
        textComponent.addExtra(moneyComponent);
        TextComponent prestigeComponent = new TextComponent(ChatColorConverter.convert("&6Guild Tier: &3") + AdventurersGuildConfig.getShortenedRankName(GuildRank.getGuildPrestigeRank(targetPlayer), GuildRank.getActiveGuildRank(targetPlayer)) + "\n");
        setHoverText(prestigeComponent,
                "Prestige Tier and Guild Rank:\n" +
                        "Guild Rank determines how good your loot can " +
                        "be, sets your bonus from the Prestige Tier, among " +
                        "other things. The Prestige Tier unlocks extremely " +
                        "powerful rewards, like increased max health, chance " +
                        "to dodge/crit, increased currency rewards and more! " +
                        "You can unlock Guild Ranks and Prestige Tiers at /ag!\n" +
                        "⚜ = prestige rank, ✧ = guild rank!");
        textComponent.addExtra(prestigeComponent);
        TextComponent killsComponents = new TextComponent(ChatColorConverter.convert("&4Elite Kills: &c") + PlayerData.getKills(targetPlayer.getUniqueId()) + "\n");
        setHoverText(killsComponents, "Amount of Elite Mobs killed.");
        textComponent.addExtra(killsComponents);
        TextComponent highestLevelKilledComponent = new TextComponent(ChatColorConverter.convert("&4Max Lvl Killed: &c") + PlayerData.getHighestLevelKilled(targetPlayer.getUniqueId()) + "\n");
        setHoverText(highestLevelKilledComponent,
                "Level of the highest Elite Mob killed.\n" +
                        "Elite Mob levels are based on the tier\n" +
                        "of your gear! Higher tiers, higher\n" +
                        "Elite Mob levels!\n" +
                        "Note: only non-exploity kills get counted!");
        textComponent.addExtra(highestLevelKilledComponent);
        TextComponent deathsComponent = new TextComponent(ChatColorConverter.convert("&4Elite Deaths: &c") + PlayerData.getDeaths(targetPlayer.getUniqueId()) + "\n");
        setHoverText(deathsComponent, "Times killed by Elite Mobs.");
        textComponent.addExtra(deathsComponent);
        TextComponent questCompletedComponent = new TextComponent(ChatColorConverter.convert("&5Quests Completed: &d") + PlayerData.getQuestsCompleted(targetPlayer.getUniqueId()) + "\n\n");
        setHoverText(questCompletedComponent,
                "Amount of EliteMobs quests completed\n" +
                        "You can take quests on at /em quest\n" +
                        "CURRENTLY NOT IMPLEMENTED");
        textComponent.addExtra(questCompletedComponent);
        TextComponent scoreComponent = new TextComponent(ChatColorConverter.convert("&bScore: &3") + PlayerData.getScore(targetPlayer.getUniqueId()) + "\n");
        setHoverText(scoreComponent,
                "Your EliteMobs score. It goes up\n" +
                        "when you kill and elite mob,\n" +
                        "and it goes down when you die\n" +
                        "to an elite. Higher level\n" +
                        "elites give more score.");
        textComponent.addExtra(scoreComponent);

        return textComponent;
    }

    private void setHoverText(TextComponent textComponent, String text) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
    }

    private TextComponent gearPage(Player targetPlayer) {

        TextComponent header = new TextComponent(
                ChatColorConverter.convert("&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                        "&7&lArmor & Weapons:\n" +
                        "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                        "&8&lGear Tiers:\n"));

        TextComponent helmetString = new TextComponent("          ☠ - 0" + "\n");
        TextComponent chestplateString = new TextComponent("          ▼ - 0" + "\n");
        TextComponent leggingsString = new TextComponent("          Π - 0" + "\n");
        TextComponent bootsString = new TextComponent("          ╯╰ - 0" + "\n");
        TextComponent mainHandString = new TextComponent("    ⚔ - 0");
        TextComponent offHandString = new TextComponent("    ⛨ - 0" + "\n");

        if (targetPlayer.getEquipment() != null) {
            if (targetPlayer.getEquipment().getHelmet() != null) {
                helmetString = new TextComponent(unicodeColorizer(targetPlayer.getEquipment().getHelmet().getType()) + "☠" + ChatColor.BLACK + " - " + (int) ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getHelmet()) + "\n");
                ShareItem.setItemHoverEvent(helmetString, targetPlayer.getEquipment().getHelmet());
            }
            if (targetPlayer.getEquipment().getChestplate() != null) {
                chestplateString = new TextComponent(unicodeColorizer(targetPlayer.getEquipment().getChestplate().getType()) + "▼" + ChatColor.BLACK + " - " + (int) ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getChestplate()) + "\n");
                ShareItem.setItemHoverEvent(chestplateString, targetPlayer.getEquipment().getChestplate());
            }
            if (targetPlayer.getEquipment().getLeggings() != null) {
                leggingsString = new TextComponent(unicodeColorizer(targetPlayer.getEquipment().getLeggings().getType()) + "Π" + ChatColor.BLACK + " - " + (int) ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getLeggings()) + "\n");
                ShareItem.setItemHoverEvent(leggingsString, targetPlayer.getEquipment().getLeggings());
            }
            if (targetPlayer.getEquipment().getBoots() != null) {
                bootsString = new TextComponent(unicodeColorizer(targetPlayer.getEquipment().getBoots().getType()) + "╯╰" + ChatColor.BLACK + " - " + (int) ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getBoots()) + "\n");
                ShareItem.setItemHoverEvent(bootsString, targetPlayer.getEquipment().getBoots());
            }
            if (targetPlayer.getEquipment().getItemInMainHand() != null && !targetPlayer.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
                mainHandString = new TextComponent(unicodeColorizer(targetPlayer.getEquipment().getItemInMainHand().getType()) + "⚔" + ChatColor.BLACK + " - " + (int) ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getItemInMainHand()));
                ShareItem.setItemHoverEvent(mainHandString, targetPlayer.getEquipment().getItemInMainHand());
            }
            if (targetPlayer.getEquipment().getItemInOffHand() != null && !targetPlayer.getEquipment().getItemInOffHand().getType().equals(Material.AIR)) {
                offHandString = new TextComponent(" | " + unicodeColorizer(targetPlayer.getEquipment().getItemInOffHand().getType()) + "⛨" + ChatColor.BLACK + " - " + (int) ItemTierFinder.findBattleTier(targetPlayer.getEquipment().getItemInOffHand()) + "\n");
                ShareItem.setItemHoverEvent(offHandString, targetPlayer.getEquipment().getItemInOffHand());
            }
        }

        TextComponent finalComponent = new TextComponent();
        finalComponent.addExtra(header);
        finalComponent.addExtra(helmetString);
        finalComponent.addExtra(chestplateString);
        finalComponent.addExtra(leggingsString);
        finalComponent.addExtra(bootsString);
        finalComponent.addExtra(mainHandString);
        finalComponent.addExtra(offHandString);

        finalComponent.addExtra("\n");

        finalComponent.addExtra("Armor ilvl: " + ItemTierFinder.findArmorSetTier(targetPlayer) + "\n");
        finalComponent.addExtra("Weapon ilvl: " + ItemTierFinder.findWeaponTier(targetPlayer) + "\n");

        return finalComponent;
    }

    private ChatColor unicodeColorizer(Material material) {
        switch (material) {
            case DIAMOND_AXE:
            case DIAMOND_SWORD:
            case DIAMOND_HOE:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case DIAMOND_SHOVEL:
            case DIAMOND_PICKAXE:
                return ChatColor.AQUA;
            case IRON_AXE:
            case IRON_SWORD:
            case IRON_HOE:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case IRON_SHOVEL:
            case IRON_PICKAXE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case STONE_AXE:
            case STONE_SWORD:
            case STONE_HOE:
            case STONE_SHOVEL:
            case STONE_PICKAXE:
                return ChatColor.GRAY;
            case GOLDEN_AXE:
            case GOLDEN_SWORD:
            case GOLDEN_HOE:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case GOLDEN_SHOVEL:
            case GOLDEN_PICKAXE:
                return ChatColor.YELLOW;
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case WOODEN_AXE:
            case WOODEN_SWORD:
            case WOODEN_HOE:
            case WOODEN_SHOVEL:
            case WOODEN_PICKAXE:
                return ChatColor.GOLD;
            case TURTLE_HELMET:
                return ChatColor.GREEN;
            default:
                if (VersionChecker.currentVersionIsUnder(16, 0)) {
                    if (material.equals(Material.NETHERITE_HELMET) ||
                            material.equals(Material.NETHERITE_CHESTPLATE) ||
                            material.equals(Material.NETHERITE_LEGGINGS) ||
                            material.equals(Material.NETHERITE_BOOTS) ||
                            material.equals(Material.NETHERITE_SWORD) ||
                            material.equals(Material.NETHERITE_AXE) ||
                            material.equals(Material.NETHERITE_HOE) ||
                            material.equals(Material.NETHERITE_PICKAXE))
                        return ChatColor.BLACK;
                }
                return ChatColor.DARK_GRAY;
        }
    }

    private TextComponent bossTrackingPage(Player player) {
        TextComponent textComponent = new TextComponent(ChatColorConverter.convert(
                "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                        "&4&lBoss Tracker:\n" +
                        "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"));

        for (CustomBossEntity customBossEntity : CustomBossEntity.trackableCustomBosses) {
            TextComponent message = new TextComponent(customBossEntity.bossBarMessage(player, customBossEntity.customBossConfigFields.getLocationMessage()) + "\n");
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to track/untrack!").create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + player.getName() + " " + customBossEntity.uuid));
            textComponent.addExtra(message);
        }
        return textComponent;
    }

    private TextComponent commandsPage() {
        TextComponent titleComponent = new TextComponent(ChatColorConverter.convert(
                "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n" +
                        "&3&lCommands:\n" +
                        "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n\n"));

        TextComponent clickToRun = new TextComponent(ChatColorConverter.convert("&8&lClick to run!\n\n"));

        TextComponent adventurersGuild = new TextComponent(ChatColorConverter.convert("&5/ag\n\n"));
        setHoverText(adventurersGuild,
                "CLICK TO USE\n" +
                        "The place where you can find\n" +
                        "NPCs that give quests, buy and\n" +
                        "sell items, give advice and more!");
        adventurersGuild.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ag"));

        TextComponent shareItem = new TextComponent(ChatColorConverter.convert("&5/shareitem\n"));
        setHoverText(shareItem,
                "CLICK TO USE\n" +
                        "Shares the item you're holding\n" +
                        "on chat!");
        shareItem.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shareitem"));
        TextComponent textComponent = new TextComponent();
        textComponent.addExtra(titleComponent);
        textComponent.addExtra(clickToRun);
        textComponent.addExtra(adventurersGuild);
        textComponent.addExtra(shareItem);
        return textComponent;
    }

}
