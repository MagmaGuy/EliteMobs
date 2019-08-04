package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class GuildRankMenuConfig extends MenusConfigFields {
    public GuildRankMenuConfig() {
        super("guild_rank_selector_menu");
    }

    public static String menuName;
    public static String lowTierWarning, normalTierWarning;
    public static ItemStack unlockedButton, lockedButton, currentButton, nextButton;
    public static String notEnoughCurrencyMessage, unlockMessage, broadcastMessage, failedMessage;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        menuName = ConfigurationEngine.setString(fileConfiguration, "questTierSelectorMenuTitle", "[EM] Guild Rank Selection");
        lowTierWarning = ConfigurationEngine.setString(fileConfiguration, "lowTierWarning", "&cElites can't drop better loot!");
        normalTierWarning = ConfigurationEngine.setString(fileConfiguration, "normalTierWarning", "&aElite can drop better loot!");

        ItemStackSerializer.serialize(
                "unlockedButton",
                ItemStackGenerator.generateItemStack(Material.GRAY_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&aThis rank is unlocked!",
                                "&fYou can select it.",
                                "$tierWarning",
                                "&f&m-------------------------------",
                                "$lootBonus",
                                "$mobSpawning",
                                "$difficultyBonus")), fileConfiguration);
        unlockedButton = ItemStackSerializer.deserialize("unlockedButton", fileConfiguration);

        ItemStackSerializer.serialize(
                "lockedButton",
                ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&aThis rank is locked!",
                                "&cYou need the $previousRank rank first!",
                                "$tierWarning",
                                "&f&m-------------------------------",
                                "$lootBonus",
                                "$mobSpawning",
                                "$difficultyBonus")), fileConfiguration);
        lockedButton = ItemStackSerializer.deserialize("lockedButton", fileConfiguration);

        ItemStackSerializer.serialize(
                "currentButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&aThis is your current rank!",
                                "$tierWarning",
                                "&f&m-------------------------------",
                                "$lootBonus",
                                "$mobSpawning",
                                "$difficultyBonus")), fileConfiguration);
        currentButton = ItemStackSerializer.deserialize("currentButton", fileConfiguration);

        ItemStackSerializer.serialize(
                "nextButton",
                ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&6This is the next rank you can unlock",
                                "&aSelect it when you're ready!",
                                "&6Costs $price &6$currencyName",
                                "&fYou have &a$currentCurrency $currencyName",
                                "$tierWarning",
                                "&f&m-------------------------------",
                                "$lootBonus",
                                "$mobSpawning",
                                "$difficultyBonus")), fileConfiguration);
        nextButton = ItemStackSerializer.deserialize("nextButton", fileConfiguration);

        notEnoughCurrencyMessage = ConfigurationEngine.setString(fileConfiguration, "notEnoughCurrencyMessage",
                "&7[EliteMobs] &4You don't have enough Elite Coins ($neededAmount $currencyName)! Sell some Elite Mob loot to [/em shop]! " +
                        "You have $currentAmount $currencyName.");

        unlockMessage = ConfigurationEngine.setString(fileConfiguration, "unlockMessage", "&7[EliteMobs] &aYou have unlocked the $rankName &arank for $price $currencyName. \n&6Happy hunting!");

        broadcastMessage = ConfigurationEngine.setString(fileConfiguration, "questCompleteBroadcastMessage", "&7[EliteMobs] &a$player &ahas reached the $rankName &aguild rank!");

        failedMessage = ConfigurationEngine.setString(fileConfiguration, "failedMessage", "&7[EliteMobs] &cYou need to unlock other ranks first!");

    }

}
