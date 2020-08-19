package com.magmaguy.elitemobs.playerdata;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.quests.EliteQuest;
import com.magmaguy.elitemobs.quests.PlayerQuests;
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
import java.util.Iterator;
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

    public PlayerStatusScreen(Player player, int page) {
        generateBook(player, player);
    }

    private ItemStack generateBook(Player requestingPlayer, Player targetPlayer) {
        TextComponent[] pages = new TextComponent[50];
        int pageCounter = 1;
        pageCounter++;
        int statsPage = -1;
        int gearPage = -1;
        int commandsPage = -1;
        int questsPage = -1;
        int bossTrackingPage = -1;
        if (PlayerStatusMenuConfig.doStatsPage) {
            statsPage = pageCounter;
            pages[pageCounter] = statsPage(targetPlayer);
            pageCounter++;
        }
        if (PlayerStatusMenuConfig.doGearPage) {
            gearPage = pageCounter;
            pages[pageCounter] = gearPage(targetPlayer);
            pageCounter++;
        }
        if (PlayerStatusMenuConfig.doCommandsPage) {
            commandsPage = pageCounter;
            pages[pageCounter] = commandsPage();
            pageCounter++;
        }
        if (PlayerStatusMenuConfig.doQuestTrackingPage) {
            questsPage = pageCounter;
            for (TextComponent textComponent : questsPage(targetPlayer)) {
                pages[pageCounter] = textComponent;
                pageCounter++;
            }
        }

        if (PlayerStatusMenuConfig.doBossTrackingPage) {
            bossTrackingPage = pageCounter;
            for (TextComponent textComponent : bossTrackingPage(targetPlayer)) {
                pages[pageCounter] = textComponent;
                pageCounter++;
            }
        }

        pages[0] = coverPage(statsPage, gearPage, commandsPage, questsPage, bossTrackingPage);

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

    private TextComponent coverPage(int statsPage, int gearPage, int commandsPage, int questsPage, int bossTrackingPage) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {
            TextComponent line = new TextComponent(
                    PlayerStatusMenuConfig.indexTextLines[i]
                            .replace("$statsPage", statsPage + "")
                            .replace("$gearPage", gearPage + "")
                            .replace("$commandsPage", commandsPage + "")
                            .replace("$questsPage", questsPage + "")
                            .replace("$bossTrackingPage", bossTrackingPage + "")
                            + "\n");

            if (!PlayerStatusMenuConfig.indexHoverLines[i].isEmpty())
                setHoverText(line, PlayerStatusMenuConfig.indexHoverLines[i]);

            if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$statsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$statsPage", statsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$gearPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$gearPage", gearPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$commandsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$commandsPage", commandsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$questsPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$questsPage", questsPage + "")));
            else if (PlayerStatusMenuConfig.indexCommandLines[i].contains("$bossTrackingPage"))
                line.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, PlayerStatusMenuConfig.indexCommandLines[i].replace("$bossTrackingPage", bossTrackingPage + "")));

            else
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.indexCommandLines[i]));

            textComponent.addExtra(line);
        }

        return textComponent;

    }

    private TextComponent statsPage(Player targetPlayer) {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {
            TextComponent line = new TextComponent(PlayerStatusMenuConfig.statsTextLines[i]
                    .replace("$money", EconomyHandler.checkCurrency(targetPlayer.getUniqueId()) + "")
                    .replace("$guildtier", AdventurersGuildConfig.getShortenedRankName(GuildRank.getGuildPrestigeRank(targetPlayer), GuildRank.getActiveGuildRank(targetPlayer)))
                    .replace("$kills", PlayerData.getKills(targetPlayer.getUniqueId()) + "")
                    .replace("$highestkill", PlayerData.getHighestLevelKilled(targetPlayer.getUniqueId()) + "")
                    .replace("$deaths", PlayerData.getDeaths(targetPlayer.getUniqueId()) + "")
                    .replace("$quests", PlayerData.getQuestsCompleted(targetPlayer.getUniqueId()) + "")
                    .replace("$score", PlayerData.getScore(targetPlayer.getUniqueId()) + "") + "\n");

            if (!PlayerStatusMenuConfig.statsHoverLines[i].isEmpty())
                setHoverText(line, PlayerStatusMenuConfig.statsHoverLines[i]);

            if (!PlayerStatusMenuConfig.statsCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.statsCommandLines[i]));

            textComponent.addExtra(line);
        }

        return textComponent;

    }

    private void setHoverText(TextComponent textComponent, String text) {
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
    }

    private TextComponent gearPage(Player targetPlayer) {


        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {

            TextComponent line;

            if (!PlayerStatusMenuConfig.gearTextLines[i].contains("{")) {
                line = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.gearTextLines[i], targetPlayer) + "\n");
                gearMultiComponentLine(textComponent, line, i, targetPlayer, false, 0);
            } else {
                TextComponent prePlaceholderElements = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.gearTextLines[i].split("\\{")[0], targetPlayer));
                gearMultiComponentLine(textComponent, prePlaceholderElements, i, targetPlayer, false, 0);
                for (int j = 0; j < PlayerStatusMenuConfig.gearTextLines[i].split("\\{").length; j++) {
                    TextComponent placeholderString = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.gearTextLines[i].split("\\{")[j].split("}")[0], targetPlayer));
                    gearMultiComponentLine(textComponent, placeholderString, i, targetPlayer, true, j);
                    if (PlayerStatusMenuConfig.gearTextLines[i].split("}").length > j
                            && PlayerStatusMenuConfig.gearTextLines[i].split("}")[j].contains("{")) {
                        TextComponent spaceBetweenPlaceholders = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.gearTextLines[i].split("}")[j].split("\\{")[0], targetPlayer));
                        gearMultiComponentLine(textComponent, spaceBetweenPlaceholders, i, targetPlayer, false, 0);
                    }
                }
                TextComponent spaceAfterPlaceholders = new TextComponent("\n");
                gearMultiComponentLine(textComponent, spaceAfterPlaceholders, i, targetPlayer, false, 0);
            }


        }

        return textComponent;
    }

    private String parseGearPlaceholders(String string, Player targetPlayer) {

        if (string.contains("$helmettier"))
            if (targetPlayer.getEquipment().getHelmet() != null)
                return unicodeColorizer(targetPlayer.getEquipment().getHelmet().getType()) +
                        string.replace("$helmettier",
                                ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).helmet.getTier(targetPlayer.getInventory().getHelmet(), true) + "");
            else
                return string.replace("$helmettier",
                        ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).helmet.getTier(targetPlayer.getInventory().getHelmet(), true) + "");
        if (string.contains("$chestplatetier"))
            if (targetPlayer.getEquipment().getChestplate() != null)
                return unicodeColorizer(targetPlayer.getEquipment().getChestplate().getType()) +
                        string.replace("$chestplatetier",
                                ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).chestplate.getTier(targetPlayer.getInventory().getChestplate(), true) + "");
            else
                return string.replace("$chestplatetier",
                        ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).chestplate.getTier(targetPlayer.getInventory().getChestplate(), true) + "");
        if (string.contains("$leggingstier"))
            if (targetPlayer.getEquipment().getLeggings() != null)
                return unicodeColorizer(targetPlayer.getEquipment().getLeggings().getType()) +
                        string.replace("$leggingstier",
                                ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).leggings.getTier(targetPlayer.getInventory().getLeggings(), true) + "");
            else
                return string.replace("$leggingstier",
                        ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).leggings.getTier(targetPlayer.getInventory().getLeggings(), true) + "");
        if (string.contains("$bootstier"))
            if (targetPlayer.getEquipment().getBoots() != null)
                return unicodeColorizer(targetPlayer.getEquipment().getBoots().getType()) +
                        string.replace("$bootstier",
                                ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).boots.getTier(targetPlayer.getInventory().getBoots(), true) + "");
            else
                return string.replace("$bootstier",
                        ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).boots.getTier(targetPlayer.getInventory().getBoots(), true) + "");
        if (string.contains("$mainhandtier"))
            return unicodeColorizer(targetPlayer.getEquipment().getItemInMainHand().getType()) +
                    string.replace("$mainhandtier",
                            ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).mainhand.getTier(targetPlayer.getInventory().getItemInMainHand(), true) + "");
        if (string.contains("$offhandtier"))
            return unicodeColorizer(targetPlayer.getEquipment().getItemInOffHand().getType()) +
                    string.replace("$offhandtier",
                            ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).offhand.getTier(targetPlayer.getInventory().getItemInOffHand(), true) + "");
        if (string.contains("$damage"))
            return string.replace("$damage", ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).baseDamage() + "");
        if (string.contains("$armor"))
            return string.replace("$armor", ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).baseDamageReduction() + "");
        if (string.contains("$threat"))
            return string.replace("$threat", ElitePlayerInventory.playerInventories.get(targetPlayer.getUniqueId()).getNaturalMobSpawnLevel(true) + "");

        return string;

    }

    private void gearMultiComponentLine(TextComponent textComponent, TextComponent line, int i, Player targetPlayer, boolean brackets, int bracketCount) {

        if (!PlayerStatusMenuConfig.gearHoverLines[i].isEmpty()) {
            String hoverLines = PlayerStatusMenuConfig.gearHoverLines[i];
            if (hoverLines.contains("$helmet"))
                ShareItem.setItemHoverEvent(line, targetPlayer.getInventory().getHelmet());
            else if (hoverLines.contains("$chestplate"))
                ShareItem.setItemHoverEvent(line, targetPlayer.getInventory().getChestplate());
            else if (hoverLines.contains("$leggings"))
                ShareItem.setItemHoverEvent(line, targetPlayer.getEquipment().getLeggings());
            else if (hoverLines.contains("$boots"))
                ShareItem.setItemHoverEvent(line, targetPlayer.getInventory().getBoots());
            else if (brackets) {
                if (hoverLines.contains("{") && hoverLines.contains("}"))
                    for (int j = 0; j < hoverLines.split("\\{").length; j++)
                        if (bracketCount == j) {
                            String parsedLine = hoverLines.split("\\{")[j].split("}")[0];
                            if (parsedLine.contains("$mainhand")) {
                                ShareItem.setItemHoverEvent(line, targetPlayer.getInventory().getItemInMainHand());
                            } else if (parsedLine.contains("$offhand")) {
                                ShareItem.setItemHoverEvent(line, targetPlayer.getInventory().getItemInOffHand());
                            } else
                                setHoverText(line, parsedLine);
                        }
            } else if (!(hoverLines.contains("{") && hoverLines.contains("}")))
                setHoverText(line, PlayerStatusMenuConfig.gearHoverLines[i]);

        }
        if (!PlayerStatusMenuConfig.gearHoverLines[i].isEmpty())
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.gearHoverLines[i]));

        textComponent.addExtra(line);
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
                if (!VersionChecker.currentVersionIsUnder(16, 0)) {
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

    private TextComponent[] bossTrackingPage(Player player) {

        TextComponent configTextComponent = new TextComponent();

        for (int i = 0; i < 3; i++) {

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.bossTrackerTextLines[i] + "\n");

            if (!PlayerStatusMenuConfig.bossTrackerHoverLines[i].isEmpty())
                setHoverText(line, PlayerStatusMenuConfig.bossTrackerHoverLines[i]);

            if (!PlayerStatusMenuConfig.bossTrackerCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.bossTrackerCommandLines[i]));

            configTextComponent.addExtra(line);
        }

        ArrayList<TextComponent> textComponents = new ArrayList<>();
        int counter = 0;
        for (Iterator<CustomBossEntity> customBossEntityIterator = CustomBossEntity.trackableCustomBosses.iterator(); customBossEntityIterator.hasNext(); ) {
            CustomBossEntity customBossEntity = customBossEntityIterator.next();
            if (customBossEntity == null ||
                    customBossEntity.advancedGetEntity() == null ||
                    customBossEntity.advancedGetEntity().isDead()) {
                customBossEntityIterator.remove();
                continue;
            }
            TextComponent message = new TextComponent(customBossEntity.bossBarMessage(player, customBossEntity.customBossConfigFields.getLocationMessage()) + "\n");
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlayerStatusMenuConfig.onBossTrackHover).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + player.getName() + " " + customBossEntity.uuid));
            textComponents.add(message);

            counter++;
        }

        if (counter == 0) {
            TextComponent[] textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
            return textComponent;
        } else {
            TextComponent[] textComponent = new TextComponent[(int) Math.ceil(counter / 6d)];
            int internalCounter = 0;
            for (TextComponent text : textComponents) {
                if (internalCounter % 6 == 0)
                    textComponent[(int) Math.floor(internalCounter / 6d)] = configTextComponent;
                textComponent[(int) Math.floor(internalCounter / 6d)].addExtra(text);
                internalCounter++;
            }
            return textComponent;
        }
    }

    private TextComponent[] questsPage(Player targetPlayer) {

        TextComponent configTextComponent = new TextComponent();

        for (int i = 0; i < 3; i++) {

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.questTrackerTextLines[i] + "\n");

            if (!PlayerStatusMenuConfig.questTrackerHoverLines[i].isEmpty())
                setHoverText(line, PlayerStatusMenuConfig.questTrackerHoverLines[i]);

            if (!PlayerStatusMenuConfig.questTrackerCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.questTrackerCommandLines[i]));

            configTextComponent.addExtra(line);
        }

        ArrayList<TextComponent> textComponents = new ArrayList<>();
        int counter = 0;

        for (EliteQuest eliteQuest : PlayerQuests.getData(targetPlayer).quests) {
            TextComponent quest = new TextComponent(ChatColor.BLACK + ChatColor.stripColor(eliteQuest.getQuestStatus()) + " \n");
            quest.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs quest cancel " + eliteQuest.getUuid()));
            setHoverText(quest, PlayerStatusMenuConfig.onQuestTrackHover);
            textComponents.add(quest);
            counter++;
        }

        if (counter == 0) {
            TextComponent[] textComponent = new TextComponent[1];
            textComponent[0] = configTextComponent;
            return textComponent;
        } else {
            TextComponent[] textComponent = new TextComponent[(int) Math.ceil(counter / 6d)];
            int internalCounter = 0;
            for (TextComponent text : textComponents) {
                if (internalCounter % 6 == 0)
                    textComponent[(int) Math.floor(internalCounter / 6d)] = configTextComponent;
                textComponent[(int) Math.floor(internalCounter / 6d)].addExtra(text);
                internalCounter++;
            }
            return textComponent;
        }
    }

    private TextComponent commandsPage() {

        TextComponent textComponent = new TextComponent();

        for (int i = 0; i < 13; i++) {

            TextComponent line = new TextComponent(PlayerStatusMenuConfig.commandsTextLines[i] + "\n");

            if (!PlayerStatusMenuConfig.commandsHoverLines[i].isEmpty())
                setHoverText(line, PlayerStatusMenuConfig.commandsHoverLines[i]);

            if (!PlayerStatusMenuConfig.commandsCommandLines[i].isEmpty())
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.commandsCommandLines[i]));

            textComponent.addExtra(line);
        }
        return textComponent;
    }

}
