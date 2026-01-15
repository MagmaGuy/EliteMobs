package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import lombok.Getter;

/**
 * Configuration for the Arrow Shop menu.
 * Allows players to purchase arrows for Elite Coins.
 */
public class ArrowShopMenuConfig extends MenusConfigFields {

    @Getter
    private static String shopName;
    @Getter
    private static String insufficientFundsMessage;
    @Getter
    private static String purchaseSuccessMessage;
    @Getter
    private static String inventoryFullMessage;

    // Arrow items and prices
    @Getter
    private static int arrowSmallStackPrice;
    @Getter
    private static int arrowSmallStackAmount;
    @Getter
    private static int arrowLargeStackPrice;
    @Getter
    private static int arrowLargeStackAmount;
    @Getter
    private static int spectralArrowPrice;
    @Getter
    private static int spectralArrowAmount;
    @Getter
    private static int tippedArrowPoisonPrice;
    @Getter
    private static int tippedArrowPoisonAmount;
    @Getter
    private static int tippedArrowSlownessPrice;
    @Getter
    private static int tippedArrowSlownessAmount;
    @Getter
    private static int tippedArrowWeaknessPrice;
    @Getter
    private static int tippedArrowWeaknessAmount;
    @Getter
    private static int tippedArrowHarmingPrice;
    @Getter
    private static int tippedArrowHarmingAmount;
    @Getter
    private static int tippedArrowHealingPrice;
    @Getter
    private static int tippedArrowHealingAmount;

    public ArrowShopMenuConfig() {
        super("arrow_shop_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "shopName",
                "&2&lFletcher's Arrow Shop", true);
        insufficientFundsMessage = ConfigurationEngine.setString(file, fileConfiguration, "insufficientFundsMessage",
                "&c[EliteMobs] &7You don't have enough Elite Coins! You need &f%price% &7coins.", true);
        purchaseSuccessMessage = ConfigurationEngine.setString(file, fileConfiguration, "purchaseSuccessMessage",
                "&a[EliteMobs] &7Purchased &f%amount%x %item% &7for &f%price% &7Elite Coins!", true);
        inventoryFullMessage = ConfigurationEngine.setString(file, fileConfiguration, "inventoryFullMessage",
                "&c[EliteMobs] &7Your inventory is full!", true);

        // Regular arrows
        arrowSmallStackPrice = ConfigurationEngine.setInt(fileConfiguration, "arrowSmallStackPrice", 10);
        arrowSmallStackAmount = ConfigurationEngine.setInt(fileConfiguration, "arrowSmallStackAmount", 16);
        arrowLargeStackPrice = ConfigurationEngine.setInt(fileConfiguration, "arrowLargeStackPrice", 35);
        arrowLargeStackAmount = ConfigurationEngine.setInt(fileConfiguration, "arrowLargeStackAmount", 64);

        // Spectral arrows
        spectralArrowPrice = ConfigurationEngine.setInt(fileConfiguration, "spectralArrowPrice", 40);
        spectralArrowAmount = ConfigurationEngine.setInt(fileConfiguration, "spectralArrowAmount", 16);

        // Tipped arrows
        tippedArrowPoisonPrice = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowPoisonPrice", 50);
        tippedArrowPoisonAmount = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowPoisonAmount", 8);
        tippedArrowSlownessPrice = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowSlownessPrice", 50);
        tippedArrowSlownessAmount = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowSlownessAmount", 8);
        tippedArrowWeaknessPrice = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowWeaknessPrice", 50);
        tippedArrowWeaknessAmount = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowWeaknessAmount", 8);
        tippedArrowHarmingPrice = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowHarmingPrice", 75);
        tippedArrowHarmingAmount = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowHarmingAmount", 8);
        tippedArrowHealingPrice = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowHealingPrice", 75);
        tippedArrowHealingAmount = ConfigurationEngine.setInt(fileConfiguration, "tippedArrowHealingAmount", 8);
    }
}
