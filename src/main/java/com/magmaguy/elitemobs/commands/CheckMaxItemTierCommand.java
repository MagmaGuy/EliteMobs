package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

public class CheckMaxItemTierCommand {

    public static void checkMaxItemTier(CommandSender commandSender) {

        commandSender.sendMessage("The current maximum item tier is tier " + ItemSettingsConfig.maximumLootTier);
        commandSender.sendMessage("The current maximum power enchantment is " + EnchantmentsConfig.getEnchantment(Enchantment.ARROW_DAMAGE).getMaxLevel());
        commandSender.sendMessage("The current maximum sharpness enchantment is " + EnchantmentsConfig.getEnchantment(Enchantment.DAMAGE_ALL).getMaxLevel());
        commandSender.sendMessage("The current maximum protection enchantment is " + EnchantmentsConfig.getEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getMaxLevel());
        commandSender.sendMessage("The current maximum natural mob level is " + MobCombatSettingsConfig.naturalElitemobLevelCap);

    }

}
