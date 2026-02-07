package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Configuration for the skill bonus selection menu.
 */
public class SkillBonusMenuConfig extends MenusConfigFields {

    @Getter
    private static String weaponSelectMenuName;
    @Getter
    private static String skillSelectMenuName;

    // Weapon type selection items
    @Getter
    private static ItemStack swordsItem;
    @Getter
    private static int swordsSlot;
    @Getter
    private static ItemStack axesItem;
    @Getter
    private static int axesSlot;
    @Getter
    private static ItemStack bowsItem;
    @Getter
    private static int bowsSlot;
    @Getter
    private static ItemStack crossbowsItem;
    @Getter
    private static int crossbowsSlot;
    @Getter
    private static ItemStack tridentsItem;
    @Getter
    private static int tridentsSlot;
    @Getter
    private static ItemStack hoesItem;
    @Getter
    private static int hoesSlot;
    @Getter
    private static ItemStack armorItem;
    @Getter
    private static int armorSlot;
    @Getter
    private static ItemStack macesItem;
    @Getter
    private static int macesSlot;
    @Getter
    private static ItemStack spearsItem;
    @Getter
    private static int spearsSlot;

    // Skill status items
    @Getter
    private static ItemStack backItem;
    @Getter
    private static int backSlot;
    @Getter
    private static ItemStack infoItem;
    @Getter
    private static int infoSlot;

    // Skill status colors/lore
    @Getter
    private static String lockedPrefix;
    @Getter
    private static String unlockedPrefix;
    @Getter
    private static String activePrefix;
    @Getter
    private static String lockedLore;
    @Getter
    private static String unlockedLore;
    @Getter
    private static String activeLore;
    @Getter
    private static String clickToActivate;
    @Getter
    private static String clickToDeactivate;
    @Getter
    private static String maxActiveSkillsReached;

    public SkillBonusMenuConfig() {
        super("skill_bonus_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        weaponSelectMenuName = ConfigurationEngine.setString(file, fileConfiguration, "weaponSelectMenuName", "&6Select Weapon Type", true);
        skillSelectMenuName = ConfigurationEngine.setString(file, fileConfiguration, "skillSelectMenuName", "&6%skill_type% Skills", true);

        // Weapon type items
        swordsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "swordsItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD, "&6Swords", List.of("&7View sword skills", "&7Click to select")), true);
        swordsSlot = ConfigurationEngine.setInt(fileConfiguration, "swordsSlot", 11);

        axesItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "axesItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_AXE, "&6Axes", List.of("&7View axe skills", "&7Click to select")), true);
        axesSlot = ConfigurationEngine.setInt(fileConfiguration, "axesSlot", 12);

        bowsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "bowsItem",
                ItemStackGenerator.generateItemStack(Material.BOW, "&6Bows", List.of("&7View bow skills", "&7Click to select")), true);
        bowsSlot = ConfigurationEngine.setInt(fileConfiguration, "bowsSlot", 13);

        crossbowsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "crossbowsItem",
                ItemStackGenerator.generateItemStack(Material.CROSSBOW, "&6Crossbows", List.of("&7View crossbow skills", "&7Click to select")), true);
        crossbowsSlot = ConfigurationEngine.setInt(fileConfiguration, "crossbowsSlot", 14);

        tridentsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "tridentsItem",
                ItemStackGenerator.generateItemStack(Material.TRIDENT, "&6Tridents", List.of("&7View trident skills", "&7Click to select")), true);
        tridentsSlot = ConfigurationEngine.setInt(fileConfiguration, "tridentsSlot", 15);

        hoesItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "hoesItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_HOE, "&6Hoes (Scythes)", List.of("&7View hoe/scythe skills", "&7Click to select")), true);
        hoesSlot = ConfigurationEngine.setInt(fileConfiguration, "hoesSlot", 20);

        // Maces (1.21+) - use IRON_BLOCK as fallback for older versions
        Material macesMaterial;
        try {
            macesMaterial = Material.MACE;
        } catch (NoSuchFieldError e) {
            macesMaterial = Material.IRON_BLOCK;
        }
        macesItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "macesItem",
                ItemStackGenerator.generateItemStack(macesMaterial, "&6Maces", List.of("&7View mace skills", "&7Click to select")), true);
        macesSlot = ConfigurationEngine.setInt(fileConfiguration, "macesSlot", 21);

        // Spears (1.21.11+) - use IRON_SWORD as fallback for older versions
        Material spearsMaterial;
        try {
            spearsMaterial = Material.IRON_SPEAR;
        } catch (NoSuchFieldError e) {
            spearsMaterial = Material.IRON_SWORD;
        }
        spearsItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "spearsItem",
                ItemStackGenerator.generateItemStack(spearsMaterial, "&6Spears", List.of("&7View spear skills", "&7Click to select")), true);
        spearsSlot = ConfigurationEngine.setInt(fileConfiguration, "spearsSlot", 22);

        armorItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "armorItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_CHESTPLATE, "&6Armor", List.of("&7View armor skills", "&7Click to select")), true);
        armorSlot = ConfigurationEngine.setInt(fileConfiguration, "armorSlot", 23);

        // Navigation items
        backItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "backItem",
                ItemStackGenerator.generateItemStack(Material.ARROW, "&cBack", List.of("&7Return to weapon selection")), true);
        backSlot = ConfigurationEngine.setInt(fileConfiguration, "backSlot", 45);

        infoItem = ConfigurationEngine.setItemStack(file, fileConfiguration, "infoItem",
                ItemStackGenerator.generateItemStack(Material.BOOK, "&eSkill Info", List.of(
                        "&7You can have up to &63 active skills",
                        "&7per weapon type.",
                        "",
                        "&7Skills unlock at levels:",
                        "&a Tier 1: &fLevel 10",
                        "&e Tier 2: &fLevel 25",
                        "&6 Tier 3: &fLevel 50",
                        "&c Tier 4: &fLevel 75"
                )), true);
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoSlot", 49);

        // Status text
        lockedPrefix = ConfigurationEngine.setString(file, fileConfiguration, "lockedPrefix", "&8[LOCKED] ", true);
        unlockedPrefix = ConfigurationEngine.setString(file, fileConfiguration, "unlockedPrefix", "&a[UNLOCKED] ", true);
        activePrefix = ConfigurationEngine.setString(file, fileConfiguration, "activePrefix", "&6[ACTIVE] ", true);
        lockedLore = ConfigurationEngine.setString(file, fileConfiguration, "lockedLore", "&cRequires level %level%", true);
        unlockedLore = ConfigurationEngine.setString(file, fileConfiguration, "unlockedLore", "&aClick to activate", true);
        activeLore = ConfigurationEngine.setString(file, fileConfiguration, "activeLore", "&eClick to deactivate", true);
        clickToActivate = ConfigurationEngine.setString(file, fileConfiguration, "clickToActivate", "&aClick to activate this skill", true);
        clickToDeactivate = ConfigurationEngine.setString(file, fileConfiguration, "clickToDeactivate", "&eClick to deactivate this skill", true);
        maxActiveSkillsReached = ConfigurationEngine.setString(file, fileConfiguration, "maxActiveSkillsReached", "&cYou already have 3 active skills! Deactivate one first.", true);
    }
}
