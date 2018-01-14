package com.magmaguy.elitemobs.commands.guiconfig.configurers;

import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobSpawningAndLoot implements Listener {

    private static Boolean aggressiveStacking, spawnerStacking, naturalStacking, customLoot, mmorpgColors, mmorpgColorsCustom,
            creepersFarmDamage, spawnerVanillaLootBonus, naturalVisualEffect, spawnerVisualEffect, telegraphedVisualEffects,
            permissionMissingTitles, customItemRank, scoreboards;

    private static double conversionPercentage, aggressiveStackingCap, naturalLevelCap, passiveStackAmount, flatLootDropRate,
            levelDropRate, mmorpgRankChange, lifeMultiplier, damageMultiplier, lootMultiplier, explosionMultiplier;

    private static void configStatusChecker(){

        //get boolean values
        aggressiveStacking = boolValueGrabber("Aggressive mob stacking");
        spawnerStacking = boolValueGrabber("Stack aggressive spawner mobs");
        naturalStacking = boolValueGrabber("Stack aggressive natural mobs");
        customLoot = boolValueGrabber("Aggressive EliteMobs can drop custom loot");
        mmorpgColors = boolValueGrabber("Use MMORPG colors for item ranks");
        mmorpgColorsCustom = boolValueGrabber("Use MMORPG colors for custom items");
        creepersFarmDamage = boolValueGrabber("Prevent creepers from killing passive mobs");
        spawnerVanillaLootBonus = boolValueGrabber("Drop multiplied default loot from aggressive elite mobs spawned in spawners");
        naturalVisualEffect = boolValueGrabber("Turn on visual effects for natural or plugin-spawned EliteMobs");
        spawnerVisualEffect = boolValueGrabber("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs");
        telegraphedVisualEffects = boolValueGrabber("Turn on visual effects that indicate an attack is about to happen");
        permissionMissingTitles = boolValueGrabber("Use titles to warn players they are missing a permission");
        customItemRank = boolValueGrabber("Show item rank on custom item drops");
        scoreboards = boolValueGrabber("Use scoreboards (requires permission)");

        //get double values
        conversionPercentage = doubleValueGrabber("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn");
        aggressiveStackingCap = doubleValueGrabber("Aggressive mob stacking cap");
        naturalLevelCap = doubleValueGrabber("Natural elite mob level cap");
        passiveStackAmount = doubleValueGrabber("Passive EliteMob stack amount");
        levelDropRate = doubleValueGrabber("Aggressive EliteMobs flat loot drop rate %");
        mmorpgRankChange = doubleValueGrabber("Increase MMORPG color rank every X levels X=");
        lifeMultiplier = doubleValueGrabber("Aggressive EliteMob life multiplier");
        damageMultiplier = doubleValueGrabber("Aggressive EliteMob damage multiplier");
        lootMultiplier = doubleValueGrabber("Aggressive EliteMob default loot multiplier");
        explosionMultiplier = doubleValueGrabber("SuperCreeper explosion nerf multiplier");

    }

    private static Boolean boolValueGrabber (String string) {

        return ConfigValues.defaultConfig.getBoolean(string);

    }

    private static double doubleValueGrabber (String string) {

        return ConfigValues.defaultConfig.getDouble(string);

    }

    public static void mobSpawningAndLootPopulator (Inventory inventory){

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = GUIConfigHandler.itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

    }

    private static void populateMobSpawningAndLoot(Inventory inventory) {

        configStatusChecker();

        //This lists the settings in config.yml in no particular order, with the exception of valid mobs and worlds
        //Valid chest slots start at slot 18
        //ItemStack conversionPercentageItem = itemStackConstructor() TODO: Continue from here

    }

    private static ItemStack itemStackConstructor (String title, List<String> lore, Boolean currentSetting) {



    }


    
}
