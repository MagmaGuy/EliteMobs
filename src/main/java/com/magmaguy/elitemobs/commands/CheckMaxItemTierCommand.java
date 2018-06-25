package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.config.ItemsProceduralSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.command.CommandSender;

public class CheckMaxItemTierCommand {

    public static void checkMaxItemTier(CommandSender commandSender) {

        commandSender.sendMessage("The current maximum item tier is tier " + ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.MAXIMUM_LOOT_TIER));
        commandSender.sendMessage("The current maximum power enchantment is " + ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.ARROW_DAMAGE_MAX_LEVEL));
        commandSender.sendMessage("The current maximum sharpness enchantment is " + ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.DAMAGE_ALL_MAX_LEVEL));
        commandSender.sendMessage("The current maximum protection enchantment is " + ConfigValues.itemsProceduralSettingsConfig.getString(ItemsProceduralSettingsConfig.PROTECTION_ENVIRONMENTAL_MAX_LEVEL));
        commandSender.sendMessage("The current maximum natural mob level is " + ConfigValues.mobCombatSettingsConfig.getString(MobCombatSettingsConfig.NATURAL_ELITEMOB_LEVEL_CAP));

    }

}
