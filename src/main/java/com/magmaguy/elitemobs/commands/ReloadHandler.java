package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.items.CustomItemConstructor;
import com.magmaguy.elitemobs.items.UniqueItemConstructor;
import com.magmaguy.elitemobs.items.uniqueitems.UniqueItemInitializer;
import org.bukkit.command.CommandSender;

public class ReloadHandler {

    public static void reloadCommand(CommandSender commandSender, String[] args) {

        ConfigValues.initializeConfigValues();
        CustomItemConstructor.customItemList.clear();
        CustomItemConstructor.staticCustomItemHashMap.clear();
        CustomItemConstructor.dynamicRankedItemStacks.clear();
        UniqueItemConstructor.uniqueItems.clear();
        CustomItemConstructor customItemConstructor = new CustomItemConstructor();
        customItemConstructor.superDropParser();
//        UniqueItemConstructor uniqueItemConstructor = new UniqueItemConstructor();
//        uniqueItemConstructor.intializeUniqueItems();
        UniqueItemInitializer.initialize();
        commandSender.sendMessage("EliteMobs reloaded!");

    }

}
