package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.VersionChecker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GearPage {
    protected static TextComponent gearPage(Player targetPlayer) {

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

    private static String parseGearPlaceholders(String string, Player targetPlayer) {

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

    private static void gearMultiComponentLine(TextComponent textComponent, TextComponent line, int i, Player targetPlayer, boolean brackets, int bracketCount) {

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
                                PlayerStatusScreen.setHoverText(line, parsedLine);
                        }
            } else if (!(hoverLines.contains("{") && hoverLines.contains("}")))
                PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.gearHoverLines[i]);

        }
        if (!PlayerStatusMenuConfig.gearHoverLines[i].isEmpty())
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PlayerStatusMenuConfig.gearHoverLines[i]));

        textComponent.addExtra(line);
    }

    private static ChatColor unicodeColorizer(Material material) {
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
}
