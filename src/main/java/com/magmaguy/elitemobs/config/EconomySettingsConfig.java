package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Created by MagmaGuy on 17/06/2017.
 */
public class EconomySettingsConfig {

    public static boolean enableEconomy;
    public static double resaleValue;
    public static String currencyName;
    public static boolean useVault;
    public static boolean enableCurrencyShower;
    public static double currencyShowerMultiplier;
    public static String chatCurrencyShowerMessage;
    public static String actionBarCurrencyShowerMessage;
    public static String lootShowerMaterial1, lootShowerMaterial5, lootShowerMaterial10, lootShowerMaterial20,
            lootShowerMaterial50, lootShowerMaterial100, lootShowerMaterial500, lootShowerMaterial1000;
    public static String adventurersGuildNotificationMessage;
    public static double defaultMaterialWorth;
    private static FileConfiguration thisConfiguration;
    public static double playerToPlayerTaxes;

    public static void initializeConfig() {

        File file = ConfigurationEngine.fileCreator("EconomySettings.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        double netheriteLevel = CombatSystem.NETHERITE_TIER_LEVEL + 10;
        double tridentLevel = CombatSystem.TRIDENT_TIER_LEVEL + 10;
        double diamondLevel = CombatSystem.DIAMOND_TIER_LEVEL + 10;
        double ironLevel = CombatSystem.IRON_TIER_LEVEL + 10;
        double stoneChainLevel = CombatSystem.STONE_CHAIN_TIER_LEVEL + 10;
        double goldWoodLeatherLevel = CombatSystem.GOLD_WOOD_LEATHER_TIER_LEVEL + 10;

        enableEconomy = ConfigurationEngine.setBoolean(fileConfiguration, "enableEconomy", true);
        resaleValue = ConfigurationEngine.setDouble(fileConfiguration, "itemResaleValue", 5);
        currencyName = ConfigurationEngine.setString(fileConfiguration, "currencyName", "Elite Coins");
        useVault = ConfigurationEngine.setBoolean(fileConfiguration, "useVault - not recommended", false);
        enableCurrencyShower = ConfigurationEngine.setBoolean(fileConfiguration, "enableCurrencyShower", true);
        currencyShowerMultiplier = ConfigurationEngine.setDouble(fileConfiguration, "currencyShowerTierMultiplier", 1D);
        chatCurrencyShowerMessage = ConfigurationEngine.setString(fileConfiguration, "chatCurrencyShowerMessage", "&7[EM] You've picked up &a$amount $currency_name!");
        actionBarCurrencyShowerMessage = ConfigurationEngine.setString(fileConfiguration, "actionbarCurrencyShowerMessage", "&7[EM] You've picked up &a$amount $currency_name!");
        lootShowerMaterial1 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.1", Material.GOLD_NUGGET.name());
        lootShowerMaterial5 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.5", Material.GOLD_INGOT.name());
        lootShowerMaterial10 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.10", Material.GOLD_BLOCK.name());
        lootShowerMaterial20 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.20", Material.EMERALD.name());
        lootShowerMaterial50 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.50", Material.EMERALD_BLOCK.name());
        lootShowerMaterial100 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.100", Material.DIAMOND.name());
        lootShowerMaterial500 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.500", Material.DIAMOND_BLOCK.name());
        lootShowerMaterial1000 = ConfigurationEngine.setString(fileConfiguration, "lootShowerMaterial.1000", Material.NETHER_STAR.name());
        adventurersGuildNotificationMessage = ConfigurationEngine.setString(fileConfiguration, "adventurersGuildNotificationMessages", "&7[EM] Extra spending money? Try &a/em !");

        addMaterial(fileConfiguration, Material.DIAMOND_AXE, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_BOOTS, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_CHESTPLATE, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_LEGGINGS, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_HELMET, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_PICKAXE, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_SHOVEL, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_SWORD, diamondLevel);
        addMaterial(fileConfiguration, Material.DIAMOND_HOE, diamondLevel);

        addMaterial(fileConfiguration, Material.IRON_AXE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_BOOTS, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_LEGGINGS, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_CHESTPLATE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_HELMET, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_PICKAXE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_SHOVEL, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_HOE, ironLevel);
        addMaterial(fileConfiguration, Material.IRON_SWORD, ironLevel);
        addMaterial(fileConfiguration, Material.SHIELD, ironLevel);
        addMaterial(fileConfiguration, Material.BOW, ironLevel);

        addMaterial(fileConfiguration, Material.CHAINMAIL_BOOTS, stoneChainLevel);
        addMaterial(fileConfiguration, Material.CHAINMAIL_LEGGINGS, stoneChainLevel);
        addMaterial(fileConfiguration, Material.CHAINMAIL_CHESTPLATE, stoneChainLevel);
        addMaterial(fileConfiguration, Material.CHAINMAIL_HELMET, stoneChainLevel);

        addMaterial(fileConfiguration, Material.STONE_SWORD, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_AXE, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_PICKAXE, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_SHOVEL, stoneChainLevel);
        addMaterial(fileConfiguration, Material.STONE_HOE, stoneChainLevel);

        addMaterial(fileConfiguration, Material.GOLDEN_AXE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_BOOTS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_LEGGINGS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_CHESTPLATE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_HELMET, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_SWORD, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_SHOVEL, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_PICKAXE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_HOE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.GOLDEN_APPLE, diamondLevel);
        addMaterial(fileConfiguration, Material.ENCHANTED_GOLDEN_APPLE, diamondLevel);

        addMaterial(fileConfiguration, Material.LEATHER_BOOTS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.LEATHER_LEGGINGS, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.LEATHER_CHESTPLATE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.LEATHER_HELMET, goldWoodLeatherLevel);

        addMaterial(fileConfiguration, Material.WOODEN_SWORD, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.WOODEN_AXE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.WOODEN_HOE, goldWoodLeatherLevel);
        addMaterial(fileConfiguration, Material.WOODEN_PICKAXE, goldWoodLeatherLevel);

        addMaterial(fileConfiguration, Material.TRIDENT, tridentLevel);
        addMaterial(fileConfiguration, Material.ELYTRA, diamondLevel);
        addMaterial(fileConfiguration, Material.TURTLE_HELMET, goldWoodLeatherLevel);

        if (!VersionChecker.currentVersionIsUnder(16, 0)) {
            addMaterial(fileConfiguration, Material.NETHERITE_AXE, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_PICKAXE, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_SHOVEL, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_HOE, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_SWORD, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_HELMET, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_CHESTPLATE, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_LEGGINGS, netheriteLevel);
            addMaterial(fileConfiguration, Material.NETHERITE_BOOTS, netheriteLevel);
        }

        defaultMaterialWorth = ConfigurationEngine.setDouble(fileConfiguration, "materialWorth.defaultMaterialWorth", 1);
        playerToPlayerTaxes = ConfigurationEngine.setDouble(fileConfiguration, "playerToPlayerPaymentTaxes", 0.2);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        thisConfiguration = fileConfiguration;

    }

    private static void addMaterial(FileConfiguration fileConfiguration, Material material, double value) {
        ConfigurationEngine.setDouble(fileConfiguration, "materialWorth." + material.name(), value);
    }

    public static double getMaterialWorth(Material material) {
        try {
            return thisConfiguration.getDouble("materialWorth." + material.name());
        } catch (Exception ex) {
            return defaultMaterialWorth;
        }
    }

}
