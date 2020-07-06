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
    public static ItemStack unlockedButton, lockedButton, currentButton, nextButton, prestigeLockedButton, prestigeNextUnlockButton;
    public static String notEnoughCurrencyMessage, unlockMessage, broadcastMessage, failedMessage;
    public static String spawnRateModifierMessage, lootModifierMessage, difficultyModifierMessage, lootTierMessage, currencyBonusMessage,
            healthBonusMessage, critBonusMessage, dodgeBonusMessage;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        menuName = ConfigurationEngine.setString(fileConfiguration, "questTierSelectorMenuTitle", "[EM] Guild Rank Selection");
        lowTierWarning = ConfigurationEngine.setString(fileConfiguration, "lowTierWarning", "&cElites can't drop better loot!");
        normalTierWarning = ConfigurationEngine.setString(fileConfiguration, "normalTierWarning", "&aElite can drop better loot!");

        ItemStackSerializer.serialize(
                "unlockedButtons",
                ItemStackGenerator.generateItemStack(Material.GRAY_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&aThis rank is unlocked!",
                                "&fYou can select it.",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), fileConfiguration);
        unlockedButton = ItemStackSerializer.deserialize("unlockedButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "lockedButtons",
                ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&aThis rank is locked!",
                                "&cYou need the $previousRank rank first!",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), fileConfiguration);
        lockedButton = ItemStackSerializer.deserialize("lockedButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "currentButtons",
                ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                        "$rankName",
                        Arrays.asList("&f&m-------------------------------",
                                "&aThis is your current rank!",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), fileConfiguration);
        currentButton = ItemStackSerializer.deserialize("currentButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "nextButtons",
                ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "Prestige $prestigeNumber",
                        Arrays.asList(
                                "&6This is the next rank you can unlock",
                                "&aSelect it when you're ready!",
                                "&6Costs $price &6$currencyName",
                                "&fYou have &a$currentCurrency $currencyName",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), fileConfiguration);
        nextButton = ItemStackSerializer.deserialize("nextButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "prestigeLockedButtons",
                ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS,
                        "Prestige $rank",
                        Arrays.asList("&f&m-------------------------------",
                                "&4You must unlock all guild ranks first!",
                                "&f&m-------------------------------")), fileConfiguration);
        prestigeLockedButton = ItemStackSerializer.deserialize("prestigeLockedButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "prestigeNextUnlockButtons",
                ItemStackGenerator.generateItemStack(Material.GOLD_BLOCK,
                        "Prestige $rank",
                        Arrays.asList("&f&m-------------------------------",
                                "&2Prestige unlock is ready!",
                                "&6Costs $price &6$currencyName",
                                "&cBuying prestige will reset guild ranks",
                                "&abut will allow you to unlock:",
                                "&a- Access to higher guild tiers",
                                "&a- Access to higher tier loot",
                                "&a- More coins from kills and quests",
                                "&a- Higher max health",
                                "&a- Base chance to crit",
                                "&a- Base chance to dodge",
                                "&4Warning: this resets you current",
                                "&4EliteMobs ITEMS AND CURRENCY!",
                                "&f&m-------------------------------")), fileConfiguration);
        prestigeNextUnlockButton = ItemStackSerializer.deserialize("prestigeNextUnlockButtons", fileConfiguration);

        notEnoughCurrencyMessage = ConfigurationEngine.setString(fileConfiguration, "notEnoughCurrencyMessages",
                "&7[EliteMobs] &4You don't have enough Elite Coins ($neededAmount $currencyName)! Sell some Elite Mob loot to [/em shop]! " +
                        "You have $currentAmount $currencyName.");

        unlockMessage = ConfigurationEngine.setString(fileConfiguration, "unlockMessages", "&7[EliteMobs] &aYou have unlocked the $rankName &arank for $price $currencyName. \n&6Happy hunting!");

        broadcastMessage = ConfigurationEngine.setString(fileConfiguration, "questCompleteBroadcastMessages", "&7[EliteMobs] &a$player &ahas reached the $rankName &aguild rank!");

        failedMessage = ConfigurationEngine.setString(fileConfiguration, "failedMessages", "&7[EliteMobs] &cYou need to unlock other ranks first!");

        spawnRateModifierMessage = ConfigurationEngine.setString(fileConfiguration, "spawnRateModifierMessages", "&fElite Mob spawn rate modifier: &c$modifier%");
        lootModifierMessage = ConfigurationEngine.setString(fileConfiguration, "lootModifierMessages", "&fElite Mob loot modifier: &a$modifier%");
        difficultyModifierMessage = ConfigurationEngine.setString(fileConfiguration, "difficultyModifierMessages", "&fElite Mob difficulty modifier: &c$modifier%");

        lootTierMessage = ConfigurationEngine.setString(fileConfiguration, "lootTierMessages", "&2Elites can drop up to tier $tier loot!");
        currencyBonusMessage = ConfigurationEngine.setString(fileConfiguration, "currencyBonusMessages", "&a$amount% coins from drops and quests!");
        healthBonusMessage = ConfigurationEngine.setString(fileConfiguration, "healthBonusMessages", "&cAdds $amount ❤ of max health");
        critBonusMessage = ConfigurationEngine.setString(fileConfiguration, "critBonusMessages", "&eAdds $amount% chance to crit");
        dodgeBonusMessage = ConfigurationEngine.setString(fileConfiguration, "dodgeBonusMessages", "&8Adds $amount% chance to dodge");

    }

}
