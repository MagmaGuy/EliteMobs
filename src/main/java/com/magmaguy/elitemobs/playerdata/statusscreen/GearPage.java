package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public class GearPage {
    protected static TextComponent gearPage(Player targetPlayer) {

        TextComponent textComponent = new TextComponent();
        GearProfile.Snapshot gearProfile = GearProfile.capture(targetPlayer);

        for (int i = 0; i < 13; i++) {

            TextComponent line;
            if (PlayerStatusMenuConfig.getGearTextLines()[i] == null) continue;
            if (!PlayerStatusMenuConfig.getGearTextLines()[i].contains("{")) {
                line = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.getGearTextLines()[i], targetPlayer, gearProfile) + "\n");
                gearMultiComponentLine(textComponent, line, i, targetPlayer, gearProfile, false, 0);
            } else {
                TextComponent prePlaceholderElements = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.getGearTextLines()[i].split("\\{")[0], targetPlayer, gearProfile));
                gearMultiComponentLine(textComponent, prePlaceholderElements, i, targetPlayer, gearProfile, false, 0);
                for (int j = 0; j < PlayerStatusMenuConfig.getGearTextLines()[i].split("\\{").length; j++) {
                    TextComponent placeholderString = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.getGearTextLines()[i].split("\\{")[j].split("}")[0], targetPlayer, gearProfile));
                    gearMultiComponentLine(textComponent, placeholderString, i, targetPlayer, gearProfile, true, j);
                    if (PlayerStatusMenuConfig.getGearTextLines()[i].split("}").length > j
                            && PlayerStatusMenuConfig.getGearTextLines()[i].split("}")[j].contains("{")) {
                        TextComponent spaceBetweenPlaceholders = new TextComponent(parseGearPlaceholders(PlayerStatusMenuConfig.getGearTextLines()[i].split("}")[j].split("\\{")[0], targetPlayer, gearProfile));
                        gearMultiComponentLine(textComponent, spaceBetweenPlaceholders, i, targetPlayer, gearProfile, false, 0);
                    }
                }
                TextComponent spaceAfterPlaceholders = new TextComponent("\n");
                gearMultiComponentLine(textComponent, spaceAfterPlaceholders, i, targetPlayer, gearProfile, false, 0);
            }


        }

        return textComponent;
    }

    private static String parseGearPlaceholders(String string, Player targetPlayer, GearProfile.Snapshot gearProfile) {

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
        return GearProfile.resolve(string, gearProfile);

    }

    private static void gearMultiComponentLine(TextComponent textComponent, TextComponent line, int i, Player targetPlayer,
                                               GearProfile.Snapshot gearProfile, boolean brackets, int bracketCount) {

        if (PlayerStatusMenuConfig.getGearHoverLines()[i] != null && !PlayerStatusMenuConfig.getGearHoverLines()[i].isEmpty()) {
            String hoverLines = PlayerStatusMenuConfig.getGearHoverLines()[i];
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
                                PlayerStatusScreen.setHoverText(line, GearProfile.resolve(parsedLine, gearProfile));
                        }
            } else if (!(hoverLines.contains("{") && hoverLines.contains("}")))
                PlayerStatusScreen.setHoverText(line, GearProfile.resolve(PlayerStatusMenuConfig.getGearHoverLines()[i], gearProfile));

        }

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
                if (material.equals(Material.NETHERITE_HELMET) ||
                        material.equals(Material.NETHERITE_CHESTPLATE) ||
                        material.equals(Material.NETHERITE_LEGGINGS) ||
                        material.equals(Material.NETHERITE_BOOTS) ||
                        material.equals(Material.NETHERITE_SWORD) ||
                        material.equals(Material.NETHERITE_AXE) ||
                        material.equals(Material.NETHERITE_HOE) ||
                        material.equals(Material.NETHERITE_PICKAXE))
                    return ChatColor.BLACK;
                return ChatColor.DARK_GRAY;
        }
    }

    protected static void gearPage(Player requestingPlayer, Player targetPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 54, PlayerStatusMenuConfig.getGearChestMenuName());
        GearProfile.Snapshot gearProfile = GearProfile.capture(targetPlayer);
        //head
        if (hasItem(targetPlayer.getInventory().getArmorContents()[3]))
            inventory.setItem(11, targetPlayer.getInventory().getArmorContents()[3]);
        //main hand
        if (hasItem(targetPlayer.getInventory().getItemInMainHand()))
            inventory.setItem(19, targetPlayer.getInventory().getItemInMainHand());
        //chestplate
        if (hasItem(targetPlayer.getInventory().getArmorContents()[2]))
            inventory.setItem(20, targetPlayer.getInventory().getArmorContents()[2]);
        //shield
        if (hasItem(targetPlayer.getInventory().getItemInOffHand()))
            inventory.setItem(21, targetPlayer.getInventory().getItemInOffHand());
        //leggings
        if (hasItem(targetPlayer.getInventory().getArmorContents()[1]))
            inventory.setItem(29, targetPlayer.getInventory().getArmorContents()[1]);
        //boots
        if (hasItem(targetPlayer.getInventory().getArmorContents()[0]))
            inventory.setItem(38, targetPlayer.getInventory().getArmorContents()[0]);
        inventory.setItem(PlayerStatusMenuConfig.getGearDamageSlot(),
                resolveItemPlaceholders(PlayerStatusMenuConfig.getGearDamageItem().clone(), gearProfile));
        inventory.setItem(PlayerStatusMenuConfig.getGearArmorSlot(),
                resolveItemPlaceholders(PlayerStatusMenuConfig.getGearArmorItem().clone(), gearProfile));
        inventory.setItem(PlayerStatusMenuConfig.getGearThreatSlot(),
                resolveItemPlaceholders(PlayerStatusMenuConfig.getGearThreatItem().clone(), gearProfile));
        inventory.setItem(53, PlayerStatusMenuConfig.getBackItem());
        if (requestingPlayer.openInventory(inventory) == null) return;
        StatusInventorySafety.protect(inventory);
        GearPageEvents.pageInventories.add(inventory);
    }

    private static boolean hasItem(ItemStack itemStack) {
        return itemStack != null && !itemStack.getType().isAir();
    }

    private static ItemStack resolveItemPlaceholders(ItemStack itemStack, GearProfile.Snapshot gearProfile) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(GearProfile.resolve(itemMeta.getDisplayName(), gearProfile));
        if (itemMeta.hasLore())
            itemMeta.setLore(itemMeta.getLore().stream()
                    .map(line -> GearProfile.resolve(line, gearProfile))
                    .toList());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static class GearPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);
            if (event.getSlot() == 53) {
                player.closeInventory();
                CoverPage.coverPage(player);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            pageInventories.remove(event.getInventory());
        }
    }
}
