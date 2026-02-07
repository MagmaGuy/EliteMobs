package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
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

import java.util.*;

/**
 * Menu for selecting active skill bonuses.
 * <p>
 * Two-stage menu:
 * 1. Weapon type selection
 * 2. Skill selection for chosen weapon type
 */
public class SkillBonusMenu {

    /**
     * Opens the weapon type selection menu.
     */
    public static void openWeaponSelectMenu(Player player) {
        String menuName = SkillBonusMenuConfig.getWeaponSelectMenuName();
        if (DefaultConfig.useResourcePackModels()) {
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE0D\uDB83\uDEF5          " + menuName;
        }
        menuName = ChatColorConverter.convert(menuName);

        Inventory inventory = Bukkit.createInventory(player, 36, menuName);

        // Set weapon type items
        inventory.setItem(SkillBonusMenuConfig.getSwordsSlot(), SkillBonusMenuConfig.getSwordsItem());
        inventory.setItem(SkillBonusMenuConfig.getAxesSlot(), SkillBonusMenuConfig.getAxesItem());
        inventory.setItem(SkillBonusMenuConfig.getBowsSlot(), SkillBonusMenuConfig.getBowsItem());
        inventory.setItem(SkillBonusMenuConfig.getCrossbowsSlot(), SkillBonusMenuConfig.getCrossbowsItem());
        inventory.setItem(SkillBonusMenuConfig.getTridentsSlot(), SkillBonusMenuConfig.getTridentsItem());
        inventory.setItem(SkillBonusMenuConfig.getHoesSlot(), SkillBonusMenuConfig.getHoesItem());
        inventory.setItem(SkillBonusMenuConfig.getArmorSlot(), SkillBonusMenuConfig.getArmorItem());
        inventory.setItem(SkillBonusMenuConfig.getMacesSlot(), SkillBonusMenuConfig.getMacesItem());
        inventory.setItem(SkillBonusMenuConfig.getSpearsSlot(), SkillBonusMenuConfig.getSpearsItem());

        player.openInventory(inventory);
        SkillBonusMenuEvents.weaponSelectMenus.add(inventory);
    }

    /**
     * Opens the skill selection menu for a specific weapon type.
     */
    public static void openSkillSelectMenu(Player player, SkillType skillType) {
        String menuName = SkillBonusMenuConfig.getSkillSelectMenuName().replace("%skill_type%", skillType.getDisplayName());
        if (DefaultConfig.useResourcePackModels()) {
            menuName = ChatColor.WHITE + "\uDB83\uDEF1\uDB83\uDE0C\uDB83\uDEF5          " + menuName;
        }
        menuName = ChatColorConverter.convert(menuName);

        Inventory inventory = Bukkit.createInventory(player, 54, menuName);

        // Get player's skill level for this type
        long xp = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        int skillLevel = SkillXPCalculator.levelFromTotalXP(xp);

        // Get active skills for this player
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType);

        // Get all skills for this type
        List<SkillBonusConfigFields> skills = SkillBonusesConfig.getEnabledBySkillType(skillType);

        // Sort by unlock tier
        skills.sort(Comparator.comparingInt(SkillBonusConfigFields::getUnlockTier));

        // Create menu data with slot mapping
        SkillMenuData menuData = new SkillMenuData(skillType, player.getUniqueId());

        // Group skills by tier
        Map<Integer, List<SkillBonusConfigFields>> tierMap = new LinkedHashMap<>();
        for (SkillBonusConfigFields skillConfig : skills) {
            tierMap.computeIfAbsent(skillConfig.getUnlockTier(), k -> new ArrayList<>()).add(skillConfig);
        }

        // Layout: Row 1 = tier1 + tier2, Row 3 = tier3 + tier4
        // -000-000- pattern: left group starts at offset 1, right group at offset 5
        int[][] tierSlots = {
            {10, 11, 12},  // Tier 1 (row 1, left)
            {14, 15, 16},  // Tier 2 (row 1, right)
            {28, 29, 30},  // Tier 3 (row 3, left)
            {32, 33, 34},  // Tier 4 (row 3, right)
        };

        for (int tier = 1; tier <= 4; tier++) {
            List<SkillBonusConfigFields> tierSkills = tierMap.getOrDefault(tier, List.of());
            int[] slots = tierSlots[tier - 1];
            for (int i = 0; i < tierSkills.size() && i < slots.length; i++) {
                SkillBonusConfigFields skillConfig = tierSkills.get(i);
                ItemStack skillItem = createSkillItem(skillConfig, skillLevel, activeSkillIds);
                inventory.setItem(slots[i], skillItem);
                menuData.getSlotToSkill().put(slots[i], skillConfig);
            }
        }

        // Add navigation items
        inventory.setItem(SkillBonusMenuConfig.getBackSlot(), SkillBonusMenuConfig.getBackItem());
        inventory.setItem(SkillBonusMenuConfig.getInfoSlot(), createInfoItem(player, skillType, skillLevel, activeSkillIds));

        player.openInventory(inventory);
        SkillBonusMenuEvents.skillSelectMenus.put(inventory, menuData);
    }

    /**
     * Creates an item stack representing a skill in the menu.
     */
    private static ItemStack createSkillItem(SkillBonusConfigFields skillConfig, int playerLevel, List<String> activeSkillIds) {
        boolean isActive = activeSkillIds.contains(skillConfig.getSkillId());
        boolean isUnlocked = playerLevel >= skillConfig.getRequiredLevel();

        Material material = skillConfig.getDisplayMaterial();
        String name;
        List<String> lore = new ArrayList<>();

        if (isActive) {
            name = SkillBonusMenuConfig.getActivePrefix() + skillConfig.getName();
        } else if (isUnlocked) {
            name = SkillBonusMenuConfig.getUnlockedPrefix() + skillConfig.getName();
        } else {
            name = SkillBonusMenuConfig.getLockedPrefix() + skillConfig.getName();
            material = Material.GRAY_DYE; // Override material for locked skills
        }

        // Add skill description
        lore.addAll(skillConfig.getDescription());
        lore.add("");

        // Add skill type and tier info
        lore.add("&7Type: &f" + skillConfig.getBonusType().name());
        lore.add("&7Tier: &f" + skillConfig.getUnlockTier() + " (Level " + skillConfig.getRequiredLevel() + ")");

        // Add current value at player's level
        if (isUnlocked) {
            double value = skillConfig.calculateValue(playerLevel);
            lore.add("&7Value at your level: &a" + String.format("%.1f", value));
        }

        lore.add("");

        // Add status-specific lore
        if (isActive) {
            lore.add(SkillBonusMenuConfig.getClickToDeactivate());
        } else if (isUnlocked) {
            lore.add(SkillBonusMenuConfig.getClickToActivate());
        } else {
            lore.add(SkillBonusMenuConfig.getLockedLore().replace("%level%", String.valueOf(skillConfig.getRequiredLevel())));
        }

        return ItemStackGenerator.generateItemStack(material, name, lore);
    }

    /**
     * Creates the info item showing player's skill status.
     */
    private static ItemStack createInfoItem(Player player, SkillType skillType, int skillLevel, List<String> activeSkillIds) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Skill Type: &f" + skillType.getDisplayName());
        lore.add("&7Your Level: &f" + skillLevel);
        lore.add("&7Active Skills: &f" + activeSkillIds.size() + "/3");
        lore.add("");

        if (!activeSkillIds.isEmpty()) {
            lore.add("&6Active Skills:");
            for (String skillId : activeSkillIds) {
                SkillBonusConfigFields config = SkillBonusesConfig.getBySkillId(skillId);
                if (config != null) {
                    lore.add("&7- " + config.getName());
                }
            }
        }

        lore.add("");
        lore.add("&7Skills unlock at levels:");
        lore.add("&a Tier 1: &fLevel 10");
        lore.add("&e Tier 2: &fLevel 25");
        lore.add("&6 Tier 3: &fLevel 50");
        lore.add("&c Tier 4: &fLevel 75");

        return ItemStackGenerator.generateItemStack(Material.BOOK, "&eSkill Info", lore);
    }

    /**
     * Event handler for skill bonus menus.
     */
    public static class SkillBonusMenuEvents implements Listener {
        private static final Set<Inventory> weaponSelectMenus = new HashSet<>();
        private static final Map<Inventory, SkillMenuData> skillSelectMenus = new HashMap<>();

        public static void shutdown() {
            weaponSelectMenus.clear();
            skillSelectMenus.clear();
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player player)) return;

            // Handle weapon selection menu
            if (EliteMenu.isEliteMenu(event, weaponSelectMenus)) {
                event.setCancelled(true);
                if (!EliteMenu.isTopMenu(event)) return;
                if (!SharedShopElements.itemNullPointerPrevention(event)) return;
                handleWeaponSelectClick(player, event);
                return;
            }

            // Handle skill selection menu
            if (skillSelectMenus.containsKey(event.getInventory())) {
                event.setCancelled(true);
                if (!EliteMenu.isTopMenu(event)) return;
                if (!SharedShopElements.itemNullPointerPrevention(event)) return;
                handleSkillSelectClick(player, event);
            }
        }

        private void handleWeaponSelectClick(Player player, InventoryClickEvent event) {
            int slot = event.getSlot();

            SkillType selectedType = null;
            if (slot == SkillBonusMenuConfig.getSwordsSlot()) selectedType = SkillType.SWORDS;
            else if (slot == SkillBonusMenuConfig.getAxesSlot()) selectedType = SkillType.AXES;
            else if (slot == SkillBonusMenuConfig.getBowsSlot()) selectedType = SkillType.BOWS;
            else if (slot == SkillBonusMenuConfig.getCrossbowsSlot()) selectedType = SkillType.CROSSBOWS;
            else if (slot == SkillBonusMenuConfig.getTridentsSlot()) selectedType = SkillType.TRIDENTS;
            else if (slot == SkillBonusMenuConfig.getHoesSlot()) selectedType = SkillType.HOES;
            else if (slot == SkillBonusMenuConfig.getArmorSlot()) selectedType = SkillType.ARMOR;
            else if (slot == SkillBonusMenuConfig.getMacesSlot()) selectedType = SkillType.MACES;
            else if (slot == SkillBonusMenuConfig.getSpearsSlot()) selectedType = SkillType.SPEARS;

            if (selectedType != null) {
                openSkillSelectMenu(player, selectedType);
            }
        }

        private void handleSkillSelectClick(Player player, InventoryClickEvent event) {
            SkillMenuData menuData = skillSelectMenus.get(event.getInventory());
            if (menuData == null) return;

            int slot = event.getSlot();

            // Handle back button
            if (slot == SkillBonusMenuConfig.getBackSlot()) {
                openWeaponSelectMenu(player);
                return;
            }

            // Handle info item (do nothing)
            if (slot == SkillBonusMenuConfig.getInfoSlot()) {
                return;
            }

            // Get skill config from slot mapping
            SkillBonusConfigFields skillConfig = menuData.getSlotToSkill().get(slot);
            if (skillConfig == null) return;

            String skillId = skillConfig.getSkillId();
            UUID playerUUID = player.getUniqueId();
            SkillType skillType = menuData.getSkillType();

            // Check if skill is unlocked
            long xp = PlayerData.getSkillXP(playerUUID, skillType);
            int playerLevel = SkillXPCalculator.levelFromTotalXP(xp);
            if (playerLevel < skillConfig.getRequiredLevel()) {
                player.sendMessage(ChatColorConverter.convert("&cThis skill requires level " + skillConfig.getRequiredLevel() + "!"));
                return;
            }

            // Toggle skill active state
            List<String> activeSkills = PlayerSkillSelection.getActiveSkills(playerUUID, skillType);
            if (activeSkills.contains(skillId)) {
                // Deactivate
                PlayerSkillSelection.removeActiveSkill(playerUUID, skillType, skillId);
                player.sendMessage(ChatColorConverter.convert("&eDeactivated skill: " + skillConfig.getName()));

                // Call skill deactivation
                SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
                if (skill != null) {
                    skill.onDeactivate(player);
                }
            } else {
                // Check if at max
                if (activeSkills.size() >= PlayerSkillSelection.MAX_ACTIVE_SKILLS) {
                    player.sendMessage(ChatColorConverter.convert(SkillBonusMenuConfig.getMaxActiveSkillsReached()));
                    return;
                }

                // Activate
                PlayerSkillSelection.addActiveSkill(playerUUID, skillType, skillId);
                player.sendMessage(ChatColorConverter.convert("&aActivated skill: " + skillConfig.getName()));

                // Call skill activation
                SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
                if (skill != null) {
                    skill.onActivate(player);
                }
            }

            // Refresh menu
            openSkillSelectMenu(player, skillType);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            weaponSelectMenus.remove(event.getInventory());
            skillSelectMenus.remove(event.getInventory());
        }
    }

    /**
     * Data class for skill selection menu.
     * Stores the skill type and a slot-to-skill mapping for click handling.
     */
    private static class SkillMenuData {
        @Getter
        private final SkillType skillType;
        @Getter
        private final UUID playerUUID;
        @Getter
        private final Map<Integer, SkillBonusConfigFields> slotToSkill = new HashMap<>();

        public SkillMenuData(SkillType skillType, UUID playerUUID) {
            this.skillType = skillType;
            this.playerUUID = playerUUID;
        }
    }
}
