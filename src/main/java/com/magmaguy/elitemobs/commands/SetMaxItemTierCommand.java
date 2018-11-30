package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

public class SetMaxItemTierCommand {

    public static void setMaxItemTier(double tier, CommandSender commandSender) {

        CustomConfigLoader customConfigLoader = new CustomConfigLoader();
        Configuration itemsDropSettingsConfig = customConfigLoader.getCustomConfig(ItemsDropSettingsConfig.CONFIG_NAME);

        itemsDropSettingsConfig.set(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER, tier);
        customConfigLoader.saveCustomConfig(ItemsDropSettingsConfig.CONFIG_NAME);

        CustomConfigLoader customConfigLoader1 = new CustomConfigLoader();
        Configuration itemsProceduralSettingsConfig = customConfigLoader1.getCustomConfig(ItemsProceduralSettingsConfig.CONFIG_NAME);

        itemsProceduralSettingsConfig.set(ItemsProceduralSettingsConfig.ARROW_DAMAGE_MAX_LEVEL, tier-1);
        itemsProceduralSettingsConfig.set(ItemsProceduralSettingsConfig.DAMAGE_ALL_MAX_LEVEL, tier-1);
        itemsProceduralSettingsConfig.set(ItemsProceduralSettingsConfig.PROTECTION_ENVIRONMENTAL_MAX_LEVEL, tier-1);
        customConfigLoader1.saveCustomConfig(ItemsProceduralSettingsConfig.CONFIG_NAME);


        int maxMobLevel = (int) (tier * ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.PER_TIER_LEVEL_INCREASE) * 3);

        CustomConfigLoader customConfigLoader2 = new CustomConfigLoader();
        Configuration mobCombatSettingsConfig = customConfigLoader2.getCustomConfig(MobCombatSettingsConfig.CONFIG_NAME);
        mobCombatSettingsConfig.set(MobCombatSettingsConfig.NATURAL_ELITEMOB_LEVEL_CAP, maxMobLevel);
        customConfigLoader2.saveCustomConfig(MobCombatSettingsConfig.CONFIG_NAME);

        ConfigValues.initializeCachedConfigurations();

        commandSender.sendMessage(ChatColorConverter.convert(
                "Warning: You have set the max tier to " + tier + "!"));
        commandSender.sendMessage(ChatColorConverter.convert(
                "The max protection enchantment has been increased to " + (tier - 1)));
        commandSender.sendMessage(ChatColorConverter.convert(
                "The max sharpness enchantment has been increased to " + (tier- 1)));
        commandSender.sendMessage(ChatColorConverter.convert(
                "The max power enchantment has been increased to " + (tier - 1)));
        commandSender.sendMessage(ChatColorConverter.convert(
                "The max mob level has been increased to " + maxMobLevel));
        commandSender.sendMessage(ChatColorConverter.convert(
                "You can run this command again to tweak the values or change them in the ItemProceduralSettings.yml file," +
                        " as well as the ItemsDropSettings and the MobCombatSettings"));

    }

}
