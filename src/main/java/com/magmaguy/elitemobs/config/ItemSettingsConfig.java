package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.ChatColorConverter;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ItemSettingsConfig {
    @Getter
    private static String shopItemSource;
    @Getter
    private static boolean doEliteMobsLoot;
    @Getter
    private static boolean doMmorpgColors;
    @Getter
    private static boolean preventCustomItemPlacement;
    @Getter
    private static List<String> loreStructure;
    @Getter
    private static String mobItemSource;
    @Getter
    private static String loreWorth;
    @Getter
    private static String loreResale;
    @Getter
    private static double flatDropRate;
    @Getter
    private static double regionalBossNonUniqueDropRate;
    @Getter
    private static boolean regionalBossesDropVanillaLoot;
    @Getter
    private static double levelIncreaseDropRate;
    @Getter
    private static double proceduralItemWeight;
    @Getter
    private static double weighedItemWeight;
    @Getter
    private static double fixedItemWeight;
    @Getter
    private static double limitedItemWeight;
    @Getter
    private static double scalableItemWeight;
    @Getter
    private static double defaultLootMultiplier;
    @Getter
    private static double defaultExperienceMultiplier;
    @Getter
    private static String potionEffectOnHitTargetLore;
    @Getter
    private static int maxLevelForDefaultLootMultiplier;
    @Getter
    private static int maximumItemLevel;
    @Getter
    private static boolean useEliteEnchantments;
    @Getter
    private static String eliteEnchantLoreString;
    @Getter
    private static boolean useHoesAsWeapons;
    @Getter
    private static boolean enableRareItemParticleEffects;
    @Getter
    private static String potionEffectOnHitSelfLore;
    @Getter
    private static String potionEffectContinuousLore;
    @Getter
    private static String scrapSucceededMessage;
    @Getter
    private static String eliteEnchantmentColor;
    @Getter
    private static String vanillaEnchantmentColor;
    @Getter
    private static String customEnchantmentColor;
    @Getter
    private static String potionEffectColor;
    @Getter
    private static String noSoulbindLore;
    @Getter
    private static boolean preventEliteItemEnchantment;
    @Getter
    private static boolean preventEliteItemDisenchantment;
    @Getter
    private static String preventEliteItemDisenchantmentMessage;
    @Getter
    private static boolean preventEliteItemDiamondToNetheriteUpgrade;
    @Getter
    private static boolean eliteDurability;
    @Getter
    private static double eliteDurabilityMultiplier;
    @Getter
    private static String scrapFailedMessage;
    @Getter
    private static boolean putLootDirectlyIntoPlayerInventory;
    @Getter
    private static int lootLevelDifferenceLockout;
    @Getter
    private static boolean preventEliteItemsFromBreaking;
    @Getter
    private static String lowArmorDurabilityItemDropMessage;
    @Getter
    private static String lowWeaponDurabilityItemDropMessage;
    @Getter
    private static int minimumProcedurallyGeneratedDiamondLootLevelPlusSeven;
    @Getter
    private static String simlootMessageSuccess;
    @Getter
    private static String simlootMessageFailure;
    @Getter
    private static String directDropCustomLootMessage;
    @Getter
    private static String directDropMinecraftLootMessage;
    @Getter
    private static String directDropCoinMessage;
    @Getter
    private static boolean hideAttributes;
    @Getter
    private static String weaponEntry;
    @Getter
    private static String armorEntry;
    @Getter
    private static String tooWellEquippedMessage;

    private ItemSettingsConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("ItemSettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        doEliteMobsLoot = ConfigurationEngine.setBoolean(
                List.of("Sets if any EliteMobs loot will drop.",
                        "Includes Elite Coins, Custom Items, Procedurally Generate Items - everything!",
                        "Not recommended, makes mmorpg progression impossible."),
                fileConfiguration, "doEliteMobsLoot", true);
        doMmorpgColors = ConfigurationEngine.setBoolean(
                List.of("Sets if procedurally generated loot will have different colors based on the quality of the item."),
                fileConfiguration, "doMMORPGColorsForItems", true);
        preventCustomItemPlacement = ConfigurationEngine.setBoolean(
                List.of("Sets if the placement of custom items, like banners or blocks, is prevented.", "This is recommended - custom items break when they are placed and can not be recovered!")
                , fileConfiguration, "preventCustomItemPlacement", true);
        loreStructure = ConfigurationEngine.setList(
                List.of("Sets the format for the lore of custom all EliteMobs items!",
                        "The following are valid placeholders:",
                        "$itemLevel - shows the item level",
                        "$prestigeLevel - shows the prestige level",
                        "$weaponOrArmorStats - shows the elite DPS or elite armor stats, depending on the item",
                        "$soulbindInfo - shows who, if anyone, the item is soulbound to",
                        "$itemSource - shows where the item came from, like a mob or a shop",
                        "$ifLore - makes a line only appear if the item has custom lore. Only applies for custom items",
                        "$customLore - shows the entirety of the custom lore. Only applies for custom items",
                        "$ifEnchantments - makes a line only appear if the item has any enchantments",
                        "$enchantments - shows the enchantments on the item",
                        "$eliteEnchantments - shows the elite enchantments on the item",
                        "$ifCustomEnchantments - shows the custom enchantments on the item",
                        "$customEnchantments - shows the custom enchantments on the item",
                        "$ifPotionEffects - only shows the line if the item has potion effects",
                        "$potionEffects - shows the potion effects on the item",
                        "$loreResaleValue - shows the value of the item. Might show the purchase or sell price depending on where it is viewed",
                        "Important: Several of the placeholders can be further customized by the configuration settings further below"),
                file, fileConfiguration, "itemLoreStructureV2", Arrays.asList(
                        ChatColorConverter.convert("&7&m&l---------&7<&lEquip Info&7>&m&l---------"),
                        ChatColorConverter.convert("&7Item level: &f$itemLevel &7Prestige &6$prestigeLevel"),
                        ChatColorConverter.convert("$weaponOrArmorStats"),
                        ChatColorConverter.convert("$soulbindInfo"),
                        ChatColorConverter.convert("$itemSource"),
                        ChatColorConverter.convert("$ifLore&7&m&l-----------&7< &f&lLore&7 >&m&l-----------"),
                        ChatColorConverter.convert("$customLore"),
                        ChatColorConverter.convert("$ifEnchantments&7&m&l--------&7<&9&lEnchantments&7>&m&l--------"),
                        ChatColorConverter.convert("$enchantments"),
                        ChatColorConverter.convert("$eliteEnchantments"),
                        ChatColorConverter.convert("$ifCustomEnchantments&7&m&l------&7< &3&lCustom Enchants&7 >&m&l------"),
                        ChatColorConverter.convert("$customEnchantments"),
                        ChatColorConverter.convert("$ifPotionEffects&7&m&l----------&7< &5&lEffects&7 >&m&l----------"),
                        ChatColorConverter.convert("$potionEffect"),
                        ChatColorConverter.convert("&7&l&m-----------------------------"),
                        ChatColorConverter.convert("$loreResaleValue")
                ), true);
        shopItemSource = ConfigurationEngine.setString(
                List.of("Sets the shop source lore for store purchased"),
                file, fileConfiguration, "shopSourceItemLores", "&7Purchased from a store", true);
        mobItemSource = ConfigurationEngine.setString(
                List.of("Sets the shop source lore for items looted from bosses"),
                file, fileConfiguration, "mobSourceItemLores", "&7Looted from $mob", true);
        loreWorth = ConfigurationEngine.setString(
                List.of("Sets the item worth lore"),
                file, fileConfiguration, "loreWorths", "&7Worth $worth $currencyName", true);
        loreResale = ConfigurationEngine.setString(
                List.of("Sets the item resale value lore"),
                file, fileConfiguration, "loreResaleValues", "&7Sells for $resale $currencyName", true);
        flatDropRate = ConfigurationEngine.setDouble(
                List.of("Sets the base chance for any elite item to drop from elite mobs"),
                fileConfiguration, "flatDropRateV3", 0.20);
        regionalBossNonUniqueDropRate = ConfigurationEngine.setDouble(
                List.of("Sets the base chance for any elite item to drop from regional bosses"),
                fileConfiguration, "regionalBossNonUniqueDropRate", 0.05);
        regionalBossesDropVanillaLoot = ConfigurationEngine.setBoolean(
                List.of("Sets if the regional bosses can drop vanilla loot"),
                fileConfiguration, "regionalBossesDropVanillaLoot", false);
        levelIncreaseDropRate = ConfigurationEngine.setDouble(
                List.of("Sets how much the chance of an elite item dropping increases based on the level of the boss.",
                        "The level of the boss is multiplied by this value and added to the base chance.",
                        "It is no longer recommended to have this above 0.0!"),
                fileConfiguration, "levelIncreaseDropRateV2", 0.00);
        proceduralItemWeight = ConfigurationEngine.setDouble(
                List.of("Sets the weighed chance of a procedurally generated item dropping.",
                        "This system uses weighted probabilities! Look that up on google if you don't know what that is."),
                fileConfiguration, "proceduralItemDropWeight", 90);
        weighedItemWeight = ConfigurationEngine.setDouble(
                List.of("Sets the relative chance of a weighed item dropping.",
                        "Weighed items are custom items that don't have a dynamic weight, like charms.")
                , fileConfiguration, "weighedItemDropWeight", 1);
        fixedItemWeight = ConfigurationEngine.setDouble(
                List.of("Sets the relative chance of a fixed item dropping. These are custom items that do not scale."),
                fileConfiguration, "fixedItemDropWeight", 10);
        limitedItemWeight = ConfigurationEngine.setDouble(
                List.of("Sets the relative chance of a limited item dropping. These are custom items that scale up to a specific level"),
                fileConfiguration, "limitedItemDropWeight", 3);
        scalableItemWeight = ConfigurationEngine.setDouble(
                List.of("Sets the relative chance of a scalable item dropping. These are custom items that can scale to any level, and are the most common in the plugin."),
                fileConfiguration, "scalableItemDropWeight", 6);
        defaultLootMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the multiplier for the vanilla loot of the mob, based on the level of the mob."),
                fileConfiguration, "defaultLootMultiplier", 0);
        maxLevelForDefaultLootMultiplier = ConfigurationEngine.setInt(
                List.of("Sets the maximum level for the default loot multiplier."),
                fileConfiguration, "levelCapForDefaultLootMultiplier", 200);
        defaultExperienceMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the vanilla Minecraft experience dropped multiplier for the boss, based on the level of the boss."),
                fileConfiguration, "defaultExperienceMultiplier", 1);
        maximumItemLevel = ConfigurationEngine.setInt(
                List.of("Sets the level of the maximum loot that will be dropped by EliteMobs. Strongly recommended to leave it at 200."),
                fileConfiguration, "maximumItemLevel", 200);
        useEliteEnchantments = ConfigurationEngine.setBoolean(
                List.of("Sets if elite enchantments will be used. ",
                        "Elite enchantments replace vanilla enchantments when elite items get enchantment levels that go beyond vanilla limits.",
                        "Example: if an elite sword is supposed to have sharpness 10, since the Minecraft limit is level 5, it will have sharpness 5 and elite sharpness 5.",
                        "Elite sharpness only affects mobs spawned by EliteMobs. This is done so PVP and vanilla combat don't become unbalanced."),
                fileConfiguration, "useEliteEnchantments", true);
        eliteEnchantLoreString = ConfigurationEngine.setString(
                List.of("Sets the display name that will be used for the elite enchantments on item lore."),
                file, fileConfiguration, "eliteEnchantmentLoreStrings", "Elite", true);
        useHoesAsWeapons = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will see hoes as valid weapons for the damage calculations."),
                fileConfiguration, "useHoesAsWeapons", false);
        enableRareItemParticleEffects = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will spawn special particles over dropped items of high quality."),
                fileConfiguration, "enableRareItemParticleEffects", true);
        potionEffectOnHitTargetLore = ConfigurationEngine.setString(
                List.of("Sets the symbols that will be used in item lore to show that a potion effect applies on hit to the entity that gets hit."),
                file, fileConfiguration, "potionEffectOnHitTargetLore", "&4⚔☠", false);
        potionEffectOnHitSelfLore = ConfigurationEngine.setString(
                List.of("Sets the symbols that will be used in item lore to show that a potion effect applies on hit to the player doing the hitting."),
                file, fileConfiguration, "potionEffectOnHitSelfLore", "&9⚔🛡", false);
        potionEffectContinuousLore = ConfigurationEngine.setString(
                List.of("Sets the symbols that will be used in item lore to show that a potion effect will keep reapplying for as long as the player wields it."),
                file, fileConfiguration, "potionEffectContinuousLore", "&6⟲", false);
        eliteEnchantmentColor = ConfigurationEngine.setString(
                List.of("Sets the characters prefixed to elite enchantments in item lore."),
                file, fileConfiguration, "eliteEnchantmentLoreColor", "&9◇", false);
        vanillaEnchantmentColor = ConfigurationEngine.setString(
                List.of("Sets the characters prefixed to vanilla enchantments in item lore."),
                file, fileConfiguration, "vanillaEnchantmentLoreColor", "&7◇", false);
        customEnchantmentColor = ConfigurationEngine.setString(
                List.of("Sets the characters prefixed to custom enchantments in item lore."),
                file, fileConfiguration, "customEnchantmentColor", "&3◇", false);
        potionEffectColor = ConfigurationEngine.setString(
                List.of("Sets the characters prefixed to potion effects in item lore."),
                file, fileConfiguration, "potionEffectLoreColor", "&5◇", false);
        noSoulbindLore = ConfigurationEngine.setString(
                List.of("Sets the text that will appear on the item if the item is not soulbound."),
                file, fileConfiguration, "noSoulbindLore", "&7Not Soulbound!", true);
        preventEliteItemEnchantment = ConfigurationEngine.setBoolean(
                List.of("Sets if elite item can be enchanted by vanilla means. This is not recommended as EliteMobs has its own custom enchantments system  with its own balance!"),
                fileConfiguration, "preventEliteItemEnchantment", true);
        preventEliteItemDisenchantment = ConfigurationEngine.setBoolean(
                List.of("Sets if elite items can be disenchanted by vanilla means."),
                fileConfiguration, "preventEliteItemDisenchantment", true);
        preventEliteItemDisenchantmentMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears for players when they attempt to disenchant an item and that is not allowed."),
                file, fileConfiguration, "preventEliteItemDisenchantmentMessage", "&c[EliteMobs] Can't disenchant Elite Items!", true);
        preventEliteItemDiamondToNetheriteUpgrade = ConfigurationEngine.setBoolean(
                List.of("Sets whether elite items can be upgraded from diamond to netherite by vanilla means. Not recommended!"),
                fileConfiguration, "preventEliteItemDiamondToNetheriteUpgrade", true);
        eliteDurability = ConfigurationEngine.setBoolean(
                List.of("Sets whether elite items will only lose durability on death.",
                        "This is an important system for EliteMobs, and it is strongly recommended as high level fights are nearly impossible without it!"),
                fileConfiguration, "eliteItemsDurabilityLossOnlyOnDeath", true);
        eliteDurabilityMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the durability loss multiplier for elite items if it is set to lose durability on death.",
                        "Values between 0.0 and 1.0 lower the durability loss and values above 1.0 increase it.",
                        "Example: 0.5 deals 50% of the durability loss, 2.0 deals 200% of the durability loss."),
                fileConfiguration, "eliteItemsDurabilityLossMultiplier", 1d);
        scrapSucceededMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when item scrapping is successful."),
                file, fileConfiguration, "scrapSucceededMessageV2", "&8[EliteMobs] &2Scrapping succeeded $amount times!", true);
        scrapFailedMessage = ConfigurationEngine.setString(
                List.of("Sets message that appears when item scrapping fails."),
                file, fileConfiguration, "scrapFailedMessageV2", "&8[EliteMobs] &cScrapping failed $amount times!", true);
        putLootDirectlyIntoPlayerInventory = ConfigurationEngine.setBoolean(
                List.of("Sets if elite loot should be placed directly into players' inventories."),
                fileConfiguration, "putLootDirectlyIntoPlayerInventory", false);
        lootLevelDifferenceLockout = ConfigurationEngine.setInt(
                List.of("Sets maximum level difference players can have before they can no longer loot items that are too low level.",
                        "This is calculated based on the average level of the loot the player is wearing.",
                        "As an example, if it is set to 10 and a player has level 50 gear, they will not be able to farm level 39 bosses."),
                fileConfiguration, "lootLevelDifferenceLockout", 10);
        preventEliteItemsFromBreaking = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will prevent Elite Items from breaking when using the durability loss on death system.",
                        "Players will not be able to use items with no durability left anyway, this is simply to prevent the accidental loss of high level but low durability items."),
                fileConfiguration, "preventEliteItemsFromBreaking", true);
        lowArmorDurabilityItemDropMessage = ConfigurationEngine.setString(
                List.of("Sets the message that will be sent to players if the durability left on an item is too low to be used in combat."),
                file, fileConfiguration, "lowDurabilityItemDropMessage", "&8[EliteMobs] &cDropped armor due to low durability! &8Repair it at the NPC with scrap to use it!", true);
        lowWeaponDurabilityItemDropMessage = ConfigurationEngine.setString(
                List.of("Sets the characters prefixed to vanilla enchantments in item lore."),
                file, fileConfiguration, "lowWeaponItemDropMessage", "&8[EliteMobs] &cDropped weapon due to low durability! &8Repair it at the NPC with scrap to use it!", true);
        minimumProcedurallyGeneratedDiamondLootLevelPlusSeven = ConfigurationEngine.setInt(
                List.of("Sets the minimum level, +7, of bosses that can procedurally generated drop diamond gear in EliteMobs.",
                        "There is no procedurally generated netherite gear in EliteMobs, only custom loot."),
                fileConfiguration, "minimumProcedurallyGeneratedDiamondLootLevelPlusSeven", 10);
        simlootMessageSuccess = ConfigurationEngine.setString(
                List.of("Sets the message show in chat when successfully rolling for loot through the /em simloot <level> <times> command."),
                file, fileConfiguration, "simlootMessageSuccess", "&8[EliteMobs] &2Rolled for loot and got $itemName &2!", true);
        simlootMessageFailure = ConfigurationEngine.setString(
                List.of("Sets the message show in chat when failing to roll for loot through the /em simloot <level> <times> command."),
                file, fileConfiguration, "simlootMessageFailure", "&8[EliteMobs] &cRolled for loot and got nothing!", true);
        directDropCustomLootMessage = ConfigurationEngine.setString(
                List.of("Sets the message that players get when elite loot is deposited directly into their inventories."),
                file, fileConfiguration, "directDropCustomLootMessage", "&8[EliteMobs] &2Obtained $itemName &2!", true);
        directDropMinecraftLootMessage = ConfigurationEngine.setString(
                List.of("Sets the message that players get when vanilla loot is deposited directly into their inventories."),
                file, fileConfiguration, "directDropMinecraftLootMessage", "&8[EliteMobs] &aObtained $itemName &a!", true);
        directDropCoinMessage = ConfigurationEngine.setString(
                List.of("Sets the message that players get when elite coins are deposited directly into their inventories."),
                file, fileConfiguration, "directDropCoinMessage", "&8[EliteMobs] &aObtained &2$amount $currencyName &a!", true);
        hideAttributes = ConfigurationEngine.setBoolean(
                List.of("Sets if EliteMobs will hide vanilla Minecraft attributes."),
                fileConfiguration, "hideItemAttributes", true);
        weaponEntry = ConfigurationEngine.setString(
                List.of("Sets the weapon-specific lore entry on an elite item. The $EDPS placeholder gets replaced with the elite DPS (damage per second) of the weapon."),
                file, fileConfiguration, "weaponEntry", "&7Elite DPS: &2$EDPS", true);
        armorEntry = ConfigurationEngine.setString(
                List.of("Sets the armor-specific lore entry on an elite item. The $EDEF placeholder gets replaced with the elite DEF (defense) of the weapon."),
                file, fileConfiguration, "armorEntry", "&7Elite Armor: &2$EDEF", true);
        tooWellEquippedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a played kills a boss but has too high level gear to get loot"),
                file, fileConfiguration, "tooWellEquipped", "&8EM] &4You are too well equipped to get coins for killing this Elite!", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
