package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class SpecialItemSystemsConfig {

    @Getter
    private static final HashMap<CustomItem, Double> specialValues = new HashMap<>();
    @Getter
    private static boolean dropSpecialLoot;
    @Getter
    private static double bossChanceToDrop;
    @Getter
    private static double nonEliteChanceToDrop;
    @Getter
    private static double luckyTicketMultiplier;
    @Getter
    private static double criticalFailureChance;
    @Getter
    private static double challengeChance;
    @Getter
    private static double criticalFailureChanceDuringChallengeChance;
    @Getter
    private static String insufficientFundsMessage;
    @Getter
    private static String newFundsMessage;
    @Getter
    private static boolean announceImportantEnchantments;
    @Getter
    private static String successAnnouncement;
    @Getter
    private static String criticalFailureAnnouncement;
    @Getter
    private static String challengeAnnouncement;

    public static void initializeConfig() {
        specialValues.clear();

        File file = ConfigurationEngine.fileCreator("SpecialItemSystems.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        dropSpecialLoot = ConfigurationEngine.setBoolean(
                List.of("Sets if special loot will drop."),
                fileConfiguration, "dropSpecialLoot", true);

        bossChanceToDrop = ConfigurationEngine.setDouble(
                List.of("Sets the chance of special loot dropping from any custom boss that can drop elite loot with a health multiplier over 1.0.", "This means all bosses, minibosses and marginally hard bosses."),
                fileConfiguration, "bossChanceToDrop", 0.1);
        nonEliteChanceToDrop = ConfigurationEngine.setDouble(
                List.of("Sets the chance of special loot dropping custom boss that can drop elite loot.", "Normal elites are excluded to avoid encouraging mob farms."),
                fileConfiguration, "normalChanceToDrop", 0.01);

        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_arrow_damage", 200);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_arrow_fire", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_arrow_infinite", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_arrow_knockback", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_channeling", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_critical_strikes", 1);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_damage_all", 200);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_damage_arthropods", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_damage_undead", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_depth_strider", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_dig_speed", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_drilling", 1);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_durability", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_earthquake", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_flamethrower", 0);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_frost_walker", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_hunter", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_ice_breaker", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_impaling", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_knockback", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_lightning", 0);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_loot_bonus_blocks", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_loot_bonus_mobs", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_loud_strikes", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_loyalty", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_luck", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_lure", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_mending", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_meteor_shower", 0);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_multishot", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_oxygen", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_piercing", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_plasma_boots", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_protection_environmental", 600);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_protection_explosions", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_protection_fall", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_protection_fire", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_protection_projectile", 100);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_quick_charge", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_riptide", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_silk_touch", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_soul_speed", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_sweeping_edge", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_thorns", 10);
        addDefaultEnchantmentBook(fileConfiguration, "enchanted_book_water_worker", 10);

        addDefaultEnchantmentBook(fileConfiguration, "elite_lucky_ticket", 100);

        addDefaultEnchantmentBook(fileConfiguration, "elite_scrap_huge", 100);

        if (!VersionChecker.serverVersionOlderThan(18, 2))
            fileConfiguration.setComments("enchantedBookWeightedDropChance",
                    List.of("Sets the chance of a special item dropping over another special item.",
                            "The higher the value, the higher the chance of that item getting picked over other items.",
                            "Keep in mind that if values get too high, things with low values will become almost impossible to obtain."));

        luckyTicketMultiplier = ConfigurationEngine.setDouble(
                List.of("Multiplier for the lucky ticket success chance in enchantments. 2.0 = 2x"),
                fileConfiguration, "luckyTicketMultiplier", 2.00);

        criticalFailureChance = ConfigurationEngine.setDouble(
                List.of("Chance of an item being lost if an item enchantment fails. This is a percentage of the base failure chance."),
                fileConfiguration, "criticalFailureChanceV2", 0.01);

        challengeChance = ConfigurationEngine.setDouble(
                List.of("Chance of a player being teleported to a challenge arena if an item enchantment fails. This is a percentage of the base failure chance.", "If the player defeats the arena, their item is upgraded correctly. If the lose, either it is not enchanted or, if they're unlucky, they may lose the item."),
                fileConfiguration, "challengeChance", 0.30);

        criticalFailureChanceDuringChallengeChance = ConfigurationEngine.setDouble(
                List.of("Chance of an item being lost if a player loses the enchantment challenge. This is a percentage of the base failure chance."),
                fileConfiguration, "criticalFailureChanceDuringChallengeChance", 0.10);

        insufficientFundsMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when players do not have enough elite coins to enchant an item"),
                file, fileConfiguration, "insufficientFundsMessage", "&8[EliteMobs] &c$price $currencyName is required to enchant $itemName, but you only have $currentAmount!", true);

        newFundsMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when players after deducting the funds"),
                file, fileConfiguration, "newFundsMessage", "&8[EliteMobs] &fYou just spent $price $currencyName! You now have $currentAmount $currencyName.", true);

        announceImportantEnchantments = ConfigurationEngine.setBoolean(
                List.of("Sets if enchantments of items with more than 10 enchantments in them will be announced"),
                fileConfiguration, "successAnnouncement", true);

        successAnnouncement = ConfigurationEngine.setString(
                List.of("Sets the message announced to all players when successfully enchanting an item with more than 10 enchantments"),
                file, fileConfiguration, "successAnnouncement", "&8[EliteMobs] $player &2has successfully enchanted $itemName&2!", true);

        challengeAnnouncement = ConfigurationEngine.setString(
                List.of("Sets the message announced to all players when they get a boss fight while trying to enchant an item with more than 10 enchantments"),
                file, fileConfiguration, "challengeAnnouncement", "&8[EliteMobs] $player &6is challenging a boss to enchant $itemName&6!", true);

        criticalFailureAnnouncement = ConfigurationEngine.setString(
                List.of("Sets the message announced to all players when losing an item while trying to enchant it if the item has more than 10 enchantments"),
                file, fileConfiguration, "criticalFailureAnnouncement", "&8[EliteMobs] $player &clost $itemName &cwhile trying to enchant it!", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    private static void addDefaultEnchantmentBook(FileConfiguration fileConfiguration, String configFilename, double chance) {
        String key = "enchantedBookWeightedDropChance." + configFilename;
        fileConfiguration.addDefault(key, chance);
        CustomItem customItem = CustomItem.getCustomItem(configFilename + ".yml");
        if (customItem == null) {
            new WarningMessage("Failed to get custom item " + configFilename + ".yml for the special loot list!");
            return;
        }
        specialValues.put(customItem, fileConfiguration.getDouble(key));
    }
}
