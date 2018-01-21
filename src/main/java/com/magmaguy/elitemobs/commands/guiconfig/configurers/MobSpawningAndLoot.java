package com.magmaguy.elitemobs.commands.guiconfig.configurers;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobSpawningAndLoot implements Listener {

    private static Boolean aggressiveStacking, spawnerStacking, naturalStacking, customLoot, mmorpgColors, mmorpgColorsCustom,
            creepersFarmDamage, spawnerVanillaLootBonus, naturalVisualEffect, spawnerVisualEffects, telegraphedVisualEffects,
            permissionMissingTitles, customItemRank, scoreboards;

    private static double conversionPercentage, aggressiveStackingCap, naturalLevelCap, passiveStackAmount, flatLootDropRate,
            levelDropRate, mmorpgRankChange, lifeMultiplier, damageMultiplier, lootMultiplier, explosionMultiplier;

    private static void configStatusChecker() {

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
        spawnerVisualEffects = boolValueGrabber("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs");
        telegraphedVisualEffects = boolValueGrabber("Turn on visual effects that indicate an attack is about to happen");
        permissionMissingTitles = boolValueGrabber("Use titles to warn players they are missing a permission");
        customItemRank = boolValueGrabber("Show item rank on custom item drops");
        scoreboards = boolValueGrabber("Use scoreboards (requires permission)");

        //get double values
        conversionPercentage = doubleValueGrabber("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn");
        aggressiveStackingCap = doubleValueGrabber("Aggressive mob stacking cap");
        naturalLevelCap = doubleValueGrabber("Natural elite mob level cap");
        passiveStackAmount = doubleValueGrabber("Passive EliteMob stack amount");
        flatLootDropRate = doubleValueGrabber("Aggressive EliteMobs flat loot drop rate %");
        levelDropRate = doubleValueGrabber("Aggressive EliteMobs flat loot drop rate %");
        mmorpgRankChange = doubleValueGrabber("Increase MMORPG color rank every X levels X=");
        lifeMultiplier = doubleValueGrabber("Aggressive EliteMob life multiplier");
        damageMultiplier = doubleValueGrabber("Aggressive EliteMob damage multiplier");
        lootMultiplier = doubleValueGrabber("Aggressive EliteMob default loot multiplier");
        explosionMultiplier = doubleValueGrabber("SuperCreeper explosion nerf multiplier");

    }

    private static Boolean boolValueGrabber(String string) {

        return ConfigValues.defaultConfig.getBoolean(string);

    }

    private static double doubleValueGrabber(String string) {

        return ConfigValues.defaultConfig.getDouble(string);

    }

    public static void mobSpawningAndLootPopulator(Inventory inventory) {

        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
        ItemStack backToMain = GUIConfigHandler.itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
        inventory.setItem(0, backToMain);

        populateMobSpawningAndLoot(inventory);

    }

    private static void populateMobSpawningAndLoot(Inventory inventory) {

        configStatusChecker();

        //This lists the settings in config.yml in no particular order, with the exception of valid mobs and worlds
        List<ItemStack> optionsList = new ArrayList<>();

        List<String> conversionPercentageLore = new ArrayList<>(Arrays.asList(
                "Changes the chance that a freshly",
                "spawned mob will become an Elite Mob"));
        ItemStack conversionPercentageItem = percentageDoubleItemStackConstructor("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn",
                conversionPercentageLore, conversionPercentage);
        optionsList.add(conversionPercentageItem);

        List<String> aggressiveStackingLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not mobs can merge",
                "creating a new, higher level mob."));
        ItemStack aggressiveStackingItem = boolItemStackConstructor("Aggressive mob stacking",
                aggressiveStackingLore, aggressiveStacking);
        optionsList.add(aggressiveStackingItem);

        List<String> aggressiveStackingCapLore = new ArrayList<>(Arrays.asList(
                "Changes the maximum level Elite Mobs",
                "can reach through stacking"));
        ItemStack aggressiveStackingCapItem = doubleItemStackConstructor("Aggressive mob stacking cap",
                aggressiveStackingCapLore, aggressiveStackingCap);
        optionsList.add(aggressiveStackingCapItem);

        List<String> naturalLevelCapLore = new ArrayList<>(Arrays.asList(
                "Sets the maximum level Elite Mobs",
                "can naturally spawn at."));
        ItemStack naturalLevelCapItem = doubleItemStackConstructor("Natural elite mob level cap",
                naturalLevelCapLore, naturalLevelCap);
        optionsList.add(naturalLevelCapItem);

        List<String> spawnerStackingLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not mobs spawned in",
                "mob spawners can merge"));
        ItemStack spawnerStackingItem = boolItemStackConstructor("Stack aggressive spawner mobs",
                spawnerStackingLore, spawnerStacking);
        optionsList.add(spawnerStackingItem);

        List<String> naturalStackingLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not mobs spawned",
                "naturally can merge "));
        ItemStack naturalStackingItem = boolItemStackConstructor("Stack aggressive natural mobs",
                naturalStackingLore, naturalStacking);
        optionsList.add(naturalStackingItem);

        List<String> passiveStackAmountLore = new ArrayList<>(Arrays.asList(
                "Sets how many passive mobs need to",
                "be together to merge into a Super mob"));
        ItemStack passiveStackAmountItem = doubleItemStackConstructor("Passive EliteMob stack amount",
                passiveStackAmountLore, passiveStackAmount);
        optionsList.add(passiveStackAmountItem);

        List<String> customLootLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not naturally spawned",
                "Elite Mobs can drop the loot in loot.yml"));
        ItemStack customLootItem = boolItemStackConstructor("Aggressive EliteMobs can drop custom loot",
                customLootLore, customLoot);
        optionsList.add(customLootItem);

        List<String> flatLootDropRateLore = new ArrayList<>(Arrays.asList(
                "Sets the base drop rate for special items",
                "(custom and procedurally generated) dropped",
                "by Elite Mobs"));
        ItemStack flatLootDropRateItem = percentageDoubleItemStackConstructor("Aggressive EliteMobs flat loot drop rate %",
                flatLootDropRateLore, flatLootDropRate);
        optionsList.add(flatLootDropRateItem);

        List<String> levelDropRateLore = new ArrayList<>(Arrays.asList(
                "Sets the additional percentual chance",
                "Elite Mobs have of dropping special items",
                "with each additional level"));
        ItemStack levelDropRateItem = percentageDoubleItemStackConstructor("Aggressive EliteMobs drop rate % increase per mob level",
                levelDropRateLore, levelDropRate);
        optionsList.add(levelDropRateItem);

        List<String> mmorpgColorsLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not items will be",
                "colored based on their power following",
                "the World of Warcraft rarity format"));
        ItemStack mmorpgColorsItem = boolItemStackConstructor("Use MMORPG colors for item ranks",
                mmorpgColorsLore, mmorpgColors);
        optionsList.add(mmorpgColorsItem);

        List<String> mmorpgColorsCustomLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not items in loot.yml",
                "will have their color codes overwritten",
                "to follow the WoW item power format"));
        ItemStack mmorpgColorsCustomItem = boolItemStackConstructor("Use MMORPG colors for custom items",
                mmorpgColorsCustomLore, mmorpgColorsCustom);
        optionsList.add(mmorpgColorsCustomItem);

        List<String> mmorpgRankChangeLore = new ArrayList<>(Arrays.asList(
                "Sets how many levels of enchantments and",
                "potions it takes for the rarity rank of",
                "items to go up"));
        ItemStack mmorpgRankChangeItem = doubleItemStackConstructor("Increase MMORPG color rank every X levels X=",
                mmorpgRankChangeLore, mmorpgRankChange);
        optionsList.add(mmorpgRankChangeItem);

        List<String> creepersFarmDamageLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not creepers can damage",
                "passive mobs. Prevents Elite Creepers",
                "from nuking animal farms"));
        ItemStack creepersFarmDamageItem = boolItemStackConstructor("Prevent creepers from killing passive mobs",
                creepersFarmDamageLore, creepersFarmDamage);
        optionsList.add(creepersFarmDamageItem);

        List<String> lifeMultiplierLore = new ArrayList<>(Arrays.asList(
                "Multiplies life. Values between 0.1 and 0.99",
                "nerf, values above 1 boost"));
        ItemStack lifeMultiplierItem = doubleItemStackConstructor("Aggressive EliteMob life multiplier",
                lifeMultiplierLore, lifeMultiplier);
        optionsList.add(lifeMultiplierItem);

        List<String> damageMultiplierLore = new ArrayList<>(Arrays.asList(
                "Multiplies damage. Values between 0.1",
                "and 0.99 nerf, values above 1 boost"));
        ItemStack damageMultiplierItem = doubleItemStackConstructor("Aggressive EliteMob damage multiplier",
                damageMultiplierLore, damageMultiplier);
        optionsList.add(damageMultiplierItem);

        List<String> lootMultiplierLore = new ArrayList<>(Arrays.asList(
                "Multiplies vanilla loot drops from Elite Mobs",
                "Values between 0.1 and 0.99 nerf, values above",
                "1 boost"));
        ItemStack lootMultiplierItem = doubleItemStackConstructor("Aggressive EliteMob default loot multiplier",
                lootMultiplierLore, lootMultiplier);
        optionsList.add(lootMultiplierItem);

        List<String> spawnerVanillaLootBonusLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not Elite Mobs spawned",
                "through mob spawners drop more loot than",
                "non-elite mobs"));
        ItemStack spawnerVanillaLootBonusItem = boolItemStackConstructor("Drop multiplied default loot from aggressive elite mobs spawned in spawners",
                spawnerVanillaLootBonusLore, spawnerVanillaLootBonus);
        optionsList.add(spawnerVanillaLootBonusItem);

        List<String> explosionMultiplierLore = new ArrayList<>(Arrays.asList(
                "Multiplies explosion range and damage",
                "from Elite Creepers. Values between 0.1",
                "and 0.99 nerf, values above 1 boost."));
        ItemStack explosionMultiplierItem = doubleItemStackConstructor("SuperCreeper explosion nerf multiplier",
                explosionMultiplierLore, explosionMultiplier);
        optionsList.add(explosionMultiplierItem);

        List<String> naturalVisualEffectsLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not Elite Mobs have",
                "visual effects floating around them",
                "indicating the powers they have"));
        ItemStack naturalVisualEffectItem = boolItemStackConstructor("Turn on visual effects for natural or plugin-spawned EliteMobs",
                naturalVisualEffectsLore, naturalVisualEffect);
        optionsList.add(naturalVisualEffectItem);

        List<String> spawnerVisualEffectsLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not Elite Mobs spawned",
                "in mob spawners will have visual effects",
                "around them indicating the powers they have"));
        ItemStack spawnerVisualEffectsItem = boolItemStackConstructor("Turn off visual effects for non-natural or non-plugin-spawned EliteMobs",
                spawnerVisualEffectsLore, spawnerVisualEffects);
        optionsList.add(spawnerVisualEffectsItem);

        List<String> telegraphedVisualEffectsLore = new ArrayList<>(Arrays.asList(
                "Sets whether or no Elite Mobs will",
                "display special visual effects when",
                "in special attack phases"));
        ItemStack telegraphedVisualEffectsItem = boolItemStackConstructor("Turn on visual effects that indicate an attack is about to happen",
                telegraphedVisualEffectsLore, telegraphedVisualEffects);
        optionsList.add(telegraphedVisualEffectsItem);

        List<String> permissionMissingTitlesLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not titles will",
                "be used to display permission missing",
                "messages (instead of chat text)"));
        ItemStack permissionMissingTitlesItem = boolItemStackConstructor("Use titles to warn players they are missing a permission",
                permissionMissingTitlesLore, permissionMissingTitles);
        optionsList.add(permissionMissingTitlesItem);

        List<String> customItemRankLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not items in loot.yml",
                "will display item rank info"));
        ItemStack customItemRankItem = boolItemStackConstructor("Show item rank on custom item drops",
                customItemRankLore, customItemRank);
        optionsList.add(customItemRankItem);

        List<String> scoreboardsLore = new ArrayList<>(Arrays.asList(
                "Sets whether or not scoreboards can be",
                "used to display health and powers that",
                "an Elite Mob may have (requires permission)"));
        ItemStack scoreboardsItem = boolItemStackConstructor("Use scoreboards (requires permission)",
                scoreboardsLore, scoreboards);
        optionsList.add(scoreboardsItem);


        //Valid chest slots start at slot 18
        int slot = 18;
        for (ItemStack itemStack : optionsList) {

            inventory.setItem(slot, itemStack);
            slot++;

        }

    }

    private static ItemStack boolItemStackConstructor(String title, List<String> lore, Boolean currentSetting) {

        Material itemMaterial = currentSetting ? Material.PAPER : Material.BARRIER;

        title += currentSetting ? ChatColorConverter.chatColorConverter(": &2true") :
                ChatColorConverter.chatColorConverter(": &4false");

        String loteInstructions = currentSetting ? ChatColorConverter.chatColorConverter("&4Click to disable!") :
                ChatColorConverter.chatColorConverter("&aClick to enable!");

        lore.add(loteInstructions);

        ItemStack itemStack = new ItemStack(itemMaterial, 1);

        return itemStackAssembler(title, lore, itemStack);

    }

    private static ItemStack doubleItemStackConstructor(String title, List<String> lore, double currentSetting) {

        Material itemMaterial = Material.PAPER;

        title += ": " + currentSetting;

        String loreInstructions = "Click to change!";

        lore.add(loreInstructions);

        ItemStack itemStack = new ItemStack(itemMaterial, 1);

        return itemStackAssembler(title, lore, itemStack);

    }

    private static ItemStack percentageDoubleItemStackConstructor(String title, List<String> lore, double currentSetting) {

        Material itemMaterial = Material.PAPER;

        title += ": " + currentSetting + "%";

        String loreInstructions = "Click to change!";

        lore.add(loreInstructions);

        ItemStack itemStack = new ItemStack(itemMaterial, 1);

        return itemStackAssembler(title, lore, itemStack);

    }

    private static ItemStack itemStackAssembler(String title, List<String> lore, ItemStack itemStack) {

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(title);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

}
