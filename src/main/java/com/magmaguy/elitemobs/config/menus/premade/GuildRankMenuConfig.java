package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GuildRankMenuConfig extends MenusConfigFields {
    @Getter
    private static String menuName;
    @Getter
    private static String lowTierWarning;
    @Getter
    private static String normalTierWarning;
    @Getter
    private static ItemStack unlockedButton;
    @Getter
    private static ItemStack lockedButton;
    @Getter
    private static ItemStack currentButton;
    @Getter
    private static ItemStack nextButton;
    @Getter
    private static ItemStack prestigeLockedButton;
    @Getter
    private static ItemStack prestigeNextUnlockButton;
    @Getter
    private static String notEnoughCurrencyMessage;
    @Getter
    private static String unlockMessage;
    @Getter
    private static String broadcastMessage;
    @Getter
    private static String failedMessage;
    @Getter
    private static String spawnRateModifierMessage;
    @Getter
    private static String lootModifierMessage;
    @Getter
    private static String difficultyModifierMessage;
    @Getter
    private static String lootTierMessage;
    @Getter
    private static String currencyBonusMessage;
    @Getter
    private static String healthBonusMessage;
    @Getter
    private static String critBonusMessage;
    @Getter
    private static String dodgeBonusMessage;

    public GuildRankMenuConfig() {
        super("guild_rank_selector_menu", true);
    }

    @Override
    public void processAdditionalFields() {

        menuName = ConfigurationEngine.setString(file, fileConfiguration, "questTierSelectorMenuTitle", "[EM] Guild Rank Selection", true);
        lowTierWarning = ConfigurationEngine.setString(file, fileConfiguration, "lowTierWarning", "&cElites can't drop better loot!", true);
        normalTierWarning = ConfigurationEngine.setString(file, fileConfiguration, "normalTierWarning", "&aElite can drop better loot!", true);

        ItemStackSerializer.serialize(
                "unlockedButtons",
                ItemStackGenerator.generateItemStack(Material.GRAY_STAINED_GLASS_PANE,
                        "$rankName",
                        new ArrayList<>(List.of("&f&m-------------------------------",
                                "&aThis rank is unlocked!",
                                "&fYou can select it.",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), MetadataHandler.signatureID), fileConfiguration);
        unlockedButton = ItemStackSerializer.deserialize("unlockedButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "lockedButtons",
                ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE,
                        "$rankName",
                        new ArrayList<>(List.of("&f&m-------------------------------",
                                "&aThis rank is locked!",
                                "&cYou need the $previousRank rank first!",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), MetadataHandler.signatureID), fileConfiguration);
        lockedButton = ItemStackSerializer.deserialize("lockedButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "currentButtons",
                ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                        "$rankName",
                        new ArrayList<>(List.of("&f&m-------------------------------",
                                "&aThis is your current rank!",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), MetadataHandler.signatureID), fileConfiguration);
        currentButton = ItemStackSerializer.deserialize("currentButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "nextButtons",
                ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "Prestige $prestigeNumber",
                        new ArrayList<>(List.of(
                                "&6This is the next rank you can unlock",
                                "&aSelect it when you're ready!",
                                "&6Costs $price &6$currencyName",
                                "&fYou have &a$currentCurrency $currencyName",
                                "&f&m-------------------------------",
                                "$lootTier",
                                "$currencyBonusMessage",
                                "$maxHealthIncrease",
                                "$chanceToCrit",
                                "$chanceToDodge")), MetadataHandler.signatureID), fileConfiguration);
        nextButton = ItemStackSerializer.deserialize("nextButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "prestigeLockedButtons",
                ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS,
                        "Prestige $rank",
                        new ArrayList<>(List.of("&f&m-------------------------------",
                                "&4You must unlock all guild ranks first!",
                                "&f&m-------------------------------")), MetadataHandler.signatureID), fileConfiguration);
        prestigeLockedButton = ItemStackSerializer.deserialize("prestigeLockedButtons", fileConfiguration);

        ItemStackSerializer.serialize(
                "prestigeNextUnlockButtons",
                ItemStackGenerator.generateItemStack(Material.GOLD_BLOCK,
                        "Prestige $rank",
                        new ArrayList<>(List.of("&f&m-------------------------------",
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
                                "&f&m-------------------------------")), MetadataHandler.signatureID), fileConfiguration);
        prestigeNextUnlockButton = ItemStackSerializer.deserialize("prestigeNextUnlockButtons", fileConfiguration);

        notEnoughCurrencyMessage = ConfigurationEngine.setString(file, fileConfiguration, "notEnoughCurrencyMessages",
                "&7[EliteMobs] &4You don't have enough Elite Coins ($neededAmount $currencyName)! Sell some Elite Mob loot to [/em shop]! " +
                        "You have $currentAmount $currencyName.", true);

        unlockMessage = ConfigurationEngine.setString(file, fileConfiguration, "unlockMessages", "&7[EliteMobs] &aYou have unlocked the $rankName &arank for $price $currencyName. \n&6Happy hunting!", true);

        broadcastMessage = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteBroadcastMessages", "&7[EliteMobs] &a$player &ahas reached the $rankName &aguild rank!", true);

        failedMessage = ConfigurationEngine.setString(file, fileConfiguration, "failedMessages", "&7[EliteMobs] &cYou need to unlock other ranks first!", true);

        spawnRateModifierMessage = ConfigurationEngine.setString(file, fileConfiguration, "spawnRateModifierMessages", "&fElite Mob spawn rate modifier: &c$modifier%", true);
        lootModifierMessage = ConfigurationEngine.setString(file, fileConfiguration, "lootModifierMessages", "&fElite Mob loot modifier: &a$modifier%", true);
        difficultyModifierMessage = ConfigurationEngine.setString(file, fileConfiguration, "difficultyModifierMessages", "&fElite Mob difficulty modifier: &c$modifier%", true);

        lootTierMessage = ConfigurationEngine.setString(file, fileConfiguration, "lootTierMessages", "&2Elites can drop up to tier $tier loot!", true);
        currencyBonusMessage = ConfigurationEngine.setString(file, fileConfiguration, "currencyBonusMessages", "&a$amountx coins from drops and quests!", true);
        healthBonusMessage = ConfigurationEngine.setString(file, fileConfiguration, "healthBonusMessages", "&cAdds $amount ❤ of max health", true);
        critBonusMessage = ConfigurationEngine.setString(file, fileConfiguration, "critBonusMessages", "&eAdds $amount% chance to crit", true);
        dodgeBonusMessage = ConfigurationEngine.setString(file, fileConfiguration, "dodgeBonusMessages", "&8Adds $amount% chance to dodge", true);

    }

}
