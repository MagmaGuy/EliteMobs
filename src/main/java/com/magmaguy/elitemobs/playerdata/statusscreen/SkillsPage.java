package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Skills page for the player status screen.
 * Displays all skill levels and XP progress.
 */
public class SkillsPage {

    private SkillsPage() {
    }

    /**
     * Creates the skills page for book-based UI.
     */
    protected static TextComponent skillsPage(Player targetPlayer) {
        TextComponent textComponent = new TextComponent();

        // Header
        TextComponent header = new TextComponent(PlayerStatusMenuConfig.getSkillsPageHeader() + "\n\n");
        textComponent.addExtra(header);

        // Add each skill
        for (SkillType skillType : SkillType.values()) {
            long totalXP = PlayerData.getSkillXP(targetPlayer.getUniqueId(), skillType);
            int level = SkillXPCalculator.levelFromTotalXP(totalXP);
            long xpInLevel = SkillXPCalculator.xpProgressInCurrentLevel(totalXP);
            long xpForNext = SkillXPCalculator.xpToNextLevel(level);
            double progress = SkillXPCalculator.levelProgress(totalXP);

            // Create progress bar
            String progressBar = createProgressBar(progress);

            String levelLine = PlayerStatusMenuConfig.getSkillsPageLevelFormat()
                    .replace("$skillName", skillType.getDisplayName())
                    .replace("$level", String.valueOf(level));
            String xpLine = PlayerStatusMenuConfig.getSkillsPageXpFormat()
                    .replace("$progressBar", progressBar)
                    .replace("$currentXp", formatNumber(xpInLevel))
                    .replace("$nextXp", formatNumber(xpForNext));

            TextComponent line = new TextComponent(levelLine + "\n" + xpLine + "\n\n");
            textComponent.addExtra(line);
        }

        return textComponent;
    }

    /**
     * Creates the skills page for inventory-based UI.
     */
    protected static void skillsPage(Player targetPlayer, Player requestingPlayer) {
        Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, SkillsConfig.getSkillsMenuTitle().replace("&", "\u00A7"));

        // Skill slots: spread across two rows to fit all 9 skills
        int[] skillSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20};
        int slotIndex = 0;

        for (SkillType skillType : SkillType.values()) {
            if (slotIndex >= skillSlots.length) break;

            ItemStack skillItem = createSkillItem(targetPlayer, skillType);
            inventory.setItem(skillSlots[slotIndex], skillItem);
            slotIndex++;
        }

        // Back button
        inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());

        requestingPlayer.openInventory(inventory);
        SkillsPageEvents.pageInventories.add(inventory);
    }

    /**
     * Creates an item representing a skill with its current level and XP.
     */
    private static ItemStack createSkillItem(Player player, SkillType skillType) {
        Material material = getMaterialForSkill(skillType);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        long totalXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        int level = SkillXPCalculator.levelFromTotalXP(totalXP);

        // Display name with level
        meta.setDisplayName(PlayerStatusMenuConfig.getSkillItemDisplayNameFormat()
                .replace("$level", String.valueOf(level))
                .replace("$skillName", skillType.getDisplayName()));

        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(PlayerStatusMenuConfig.getSkillItemSelectLore1());
        lore.add(PlayerStatusMenuConfig.getSkillItemSelectLore2());

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Gets an appropriate material to represent each skill.
     */
    private static Material getMaterialForSkill(SkillType skillType) {
        return switch (skillType) {
            case ARMOR -> Material.DIAMOND_CHESTPLATE;
            case SWORDS -> Material.DIAMOND_SWORD;
            case AXES -> Material.DIAMOND_AXE;
            case BOWS -> Material.BOW;
            case CROSSBOWS -> Material.CROSSBOW;
            case TRIDENTS -> Material.TRIDENT;
            case HOES -> Material.DIAMOND_HOE;
            case MACES -> Material.MACE;
            case SPEARS -> {
                try {
                    yield Material.IRON_SPEAR;
                } catch (NoSuchFieldError e) {
                    yield Material.TRIDENT; // Fallback for pre-1.21.11
                }
            }
        };
    }

    /**
     * Creates a visual progress bar.
     */
    private static String createProgressBar(double progress) {
        int totalBars = 20;
        int filledBars = (int) (progress * totalBars);
        int emptyBars = totalBars - filledBars;

        StringBuilder bar = new StringBuilder("\u00A7a");
        for (int i = 0; i < filledBars; i++) {
            bar.append("|");
        }
        bar.append("\u00A77");
        for (int i = 0; i < emptyBars; i++) {
            bar.append("|");
        }

        return bar.toString();
    }

    /**
     * Formats large numbers with K, M suffixes.
     */
    private static String formatNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }

    public static class SkillsPageEvents implements Listener {
        private static final Set<Inventory> pageInventories = new HashSet<>();

        public static void shutdown() {
            pageInventories.clear();
        }

        @EventHandler
        public void onInventoryInteract(InventoryClickEvent event) {
            Player player = ((Player) event.getWhoClicked()).getPlayer();
            if (!pageInventories.contains(event.getInventory())) return;
            event.setCancelled(true);

            // Back button
            if (event.getSlot() == 26) {
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
