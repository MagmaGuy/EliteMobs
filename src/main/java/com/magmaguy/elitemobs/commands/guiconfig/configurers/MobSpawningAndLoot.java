package com.magmaguy.elitemobs.commands.guiconfig.configurers;

import org.bukkit.event.Listener;

public class MobSpawningAndLoot implements Listener {

//    private static Boolean aggressiveStacking, spawnerStacking, naturalStacking, customLoot, mmorpgColors, mmorpgColorsCustom,
//            creepersFarmDamage, spawnerVanillaLootBonus, naturalVisualEffect, spawnerVisualEffects, telegraphedVisualEffects,
//            permissionMissingTitles, customItemRank, scoreboards;
//
//    private static double conversionPercentage, aggressiveStackingCap, naturalLevelCap, passiveStackAmount, flatLootDropRate,
//            levelDropRate, lifeMultiplier, damageMultiplier, lootMultiplier, explosionMultiplier;
//
//    private static void configStatusChecker() {
//
//        //get boolean values
//        aggressiveStacking = boolValueGrabber(DefaultConfig.AGGRESSIVE_MOB_STACKING);
//        spawnerStacking = boolValueGrabber(DefaultConfig.STACK_AGGRESSIVE_SPAWNER_MOBS);
//        naturalStacking = boolValueGrabber(DefaultConfig.STACK_AGGRESSIVE_NATURAL_MOBS);
//        customLoot = boolValueGrabber(DefaultConfig.DROP_CUSTOM_ITEMS);
//        mmorpgColors = boolValueGrabber(DefaultConfig.MMORPG_COLORS);
//        mmorpgColorsCustom = boolValueGrabber(DefaultConfig.MMORPG_COLORS_FOR_CUSTOM_ITEMS);
//        creepersFarmDamage = boolValueGrabber(DefaultConfig.CREEPER_PASSIVE_DAMAGE_PREVENTER);
//        spawnerVanillaLootBonus = boolValueGrabber(DefaultConfig.SPAWNER_DEFAULT_LOOT_MULTIPLIER);
//        naturalVisualEffect = boolValueGrabber(DefaultConfig.ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS);
//        spawnerVisualEffects = boolValueGrabber(DefaultConfig.DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS);
//        telegraphedVisualEffects = boolValueGrabber(DefaultConfig.ENABLE_WARNING_VISUAL_EFFECTS);
//        permissionMissingTitles = boolValueGrabber(DefaultConfig.ENABLE_PERMISSION_TITLES);
//        scoreboards = boolValueGrabber(DefaultConfig.ENABLE_POWER_SCOREBOARDS);
//
//        //get double values
//        conversionPercentage = doubleValueGrabber(DefaultConfig.AGGRESSIVE_MOB_CONVERSION_PERCENTAGE);
//        aggressiveStackingCap = doubleValueGrabber(DefaultConfig.ELITEMOB_STACKING_CAP);
//        naturalLevelCap = doubleValueGrabber(DefaultConfig.NATURAL_ELITEMOB_LEVEL_CAP);
//        passiveStackAmount = doubleValueGrabber(DefaultConfig.SUPERMOB_STACK_AMOUNT);
//        flatLootDropRate = doubleValueGrabber(DefaultConfig.ELITE_ITEM_FLAT_DROP_RATE);
//        levelDropRate = doubleValueGrabber(DefaultConfig.ELITE_ITEM_FLAT_DROP_RATE);
//        lifeMultiplier = doubleValueGrabber(DefaultConfig.LIFE_MULTIPLIER);
//        damageMultiplier = doubleValueGrabber(DefaultConfig.DAMAGE_MULTIPLIER);
//        lootMultiplier = doubleValueGrabber(DefaultConfig.DEFAULT_LOOT_MULTIPLIER);
//        explosionMultiplier = doubleValueGrabber(DefaultConfig.ELITE_CREEPER_EXPLOSION_MULTIPLIER);
//
//    }
//
//    private static Boolean boolValueGrabber(String string) {
//
//        return ConfigValues.defaultConfig.getBoolean(string);
//
//    }
//
//    private static double doubleValueGrabber(String string) {
//
//        return ConfigValues.defaultConfig.getDouble(string);
//
//    }
//
//    public static void mobSpawningAndLootPopulator(Inventory inventory) {
//
//        List<String> backToMainLore = new ArrayList<>(Arrays.asList("Back To Main Menu"));
//        ItemStack backToMain = GUIConfigHandler.itemInitializer(Material.BARRIER, "Back To Main Menu", backToMainLore);
//        inventory.setItem(0, backToMain);
//
//        populateMobSpawningAndLoot(inventory);
//
//    }
//
//    private static void populateMobSpawningAndLoot(Inventory inventory) {
//
//        configStatusChecker();
//
//        //This lists the settings in config.yml in no particular order, with the exception of valid mobs and worlds
//        List<ItemStack> optionsList = new ArrayList<>();
//
//        List<String> conversionPercentageLore = new ArrayList<>(Arrays.asList(
//                "Changes the chance that a freshly",
//                "spawned mob will become an Elite Mob"));
//        ItemStack conversionPercentageItem = percentageDoubleItemStackConstructor(DefaultConfig.AGGRESSIVE_MOB_CONVERSION_PERCENTAGE,
//                conversionPercentageLore, conversionPercentage);
//        optionsList.add(conversionPercentageItem);
//
//        List<String> aggressiveStackingLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not mobs can merge",
//                "creating a new, higher level mob."));
//        ItemStack aggressiveStackingItem = boolItemStackConstructor(DefaultConfig.AGGRESSIVE_MOB_STACKING,
//                aggressiveStackingLore, aggressiveStacking);
//        optionsList.add(aggressiveStackingItem);
//
//        List<String> aggressiveStackingCapLore = new ArrayList<>(Arrays.asList(
//                "Changes the maximum level Elite Mobs",
//                "can reach through stacking"));
//        ItemStack aggressiveStackingCapItem = doubleItemStackConstructor(DefaultConfig.ELITEMOB_STACKING_CAP,
//                aggressiveStackingCapLore, aggressiveStackingCap);
//        optionsList.add(aggressiveStackingCapItem);
//
//        List<String> naturalLevelCapLore = new ArrayList<>(Arrays.asList(
//                "Sets the maximum level Elite Mobs",
//                "can naturally spawn at."));
//        ItemStack naturalLevelCapItem = doubleItemStackConstructor(DefaultConfig.NATURAL_ELITEMOB_LEVEL_CAP,
//                naturalLevelCapLore, naturalLevelCap);
//        optionsList.add(naturalLevelCapItem);
//
//        List<String> spawnerStackingLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not mobs spawned in",
//                "mob spawners can merge"));
//        ItemStack spawnerStackingItem = boolItemStackConstructor(DefaultConfig.STACK_AGGRESSIVE_SPAWNER_MOBS,
//                spawnerStackingLore, spawnerStacking);
//        optionsList.add(spawnerStackingItem);
//
//        List<String> naturalStackingLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not mobs spawned",
//                "naturally can merge "));
//        ItemStack naturalStackingItem = boolItemStackConstructor(DefaultConfig.STACK_AGGRESSIVE_NATURAL_MOBS,
//                naturalStackingLore, naturalStacking);
//        optionsList.add(naturalStackingItem);
//
//        List<String> passiveStackAmountLore = new ArrayList<>(Arrays.asList(
//                "Sets how many passive mobs need to",
//                "be together to merge into a Super mob"));
//        ItemStack passiveStackAmountItem = doubleItemStackConstructor(DefaultConfig.SUPERMOB_STACK_AMOUNT,
//                passiveStackAmountLore, passiveStackAmount);
//        optionsList.add(passiveStackAmountItem);
//
//        List<String> customLootLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not naturally spawned",
//                "Elite Mobs can drop the loot in ItemsCustomLootList.yml"));
//        ItemStack customLootItem = boolItemStackConstructor(DefaultConfig.DROP_CUSTOM_ITEMS,
//                customLootLore, customLoot);
//        optionsList.add(customLootItem);
//
//        List<String> flatLootDropRateLore = new ArrayList<>(Arrays.asList(
//                "Sets the base drop rate for special items",
//                "(custom and procedurally generated) dropped",
//                "by Elite Mobs"));
//        ItemStack flatLootDropRateItem = percentageDoubleItemStackConstructor(DefaultConfig.ELITE_ITEM_FLAT_DROP_RATE,
//                flatLootDropRateLore, flatLootDropRate);
//        optionsList.add(flatLootDropRateItem);
//
//        List<String> levelDropRateLore = new ArrayList<>(Arrays.asList(
//                "Sets the additional percentual chance",
//                "Elite Mobs have of dropping special items",
//                "with each additional level"));
//        ItemStack levelDropRateItem = percentageDoubleItemStackConstructor(DefaultConfig.ELITE_ITEM_LEVEL_DROP_RATE,
//                levelDropRateLore, levelDropRate);
//        optionsList.add(levelDropRateItem);
//
//        List<String> mmorpgColorsLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not items will be",
//                "colored based on their power following",
//                "the World of Warcraft rarity format"));
//        ItemStack mmorpgColorsItem = boolItemStackConstructor(DefaultConfig.MMORPG_COLORS,
//                mmorpgColorsLore, mmorpgColors);
//        optionsList.add(mmorpgColorsItem);
//
//        List<String> mmorpgColorsCustomLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not items in ItemsCustomLootList.yml",
//                "will have their color codes overwritten",
//                "to follow the WoW item power format"));
//        ItemStack mmorpgColorsCustomItem = boolItemStackConstructor(DefaultConfig.MMORPG_COLORS_FOR_CUSTOM_ITEMS,
//                mmorpgColorsCustomLore, mmorpgColorsCustom);
//        optionsList.add(mmorpgColorsCustomItem);
//
//        List<String> creepersFarmDamageLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not creepers can damage",
//                "passive mobs. Prevents Elite Creepers",
//                "from nuking animal farms"));
//        ItemStack creepersFarmDamageItem = boolItemStackConstructor(DefaultConfig.CREEPER_PASSIVE_DAMAGE_PREVENTER,
//                creepersFarmDamageLore, creepersFarmDamage);
//        optionsList.add(creepersFarmDamageItem);
//
//        List<String> lifeMultiplierLore = new ArrayList<>(Arrays.asList(
//                "Multiplies life. Values between 0.1 and 0.99",
//                "nerf, values above 1 boost"));
//        ItemStack lifeMultiplierItem = doubleItemStackConstructor(DefaultConfig.LIFE_MULTIPLIER,
//                lifeMultiplierLore, lifeMultiplier);
//        optionsList.add(lifeMultiplierItem);
//
//        List<String> damageMultiplierLore = new ArrayList<>(Arrays.asList(
//                "Multiplies damage. Values between 0.1",
//                "and 0.99 nerf, values above 1 boost"));
//        ItemStack damageMultiplierItem = doubleItemStackConstructor(DefaultConfig.DAMAGE_MULTIPLIER,
//                damageMultiplierLore, damageMultiplier);
//        optionsList.add(damageMultiplierItem);
//
//        List<String> lootMultiplierLore = new ArrayList<>(Arrays.asList(
//                "Multiplies vanilla loot drops from Elite Mobs",
//                "Values between 0.1 and 0.99 nerf, values above",
//                "1 boost"));
//        ItemStack lootMultiplierItem = doubleItemStackConstructor(DefaultConfig.DEFAULT_LOOT_MULTIPLIER,
//                lootMultiplierLore, lootMultiplier);
//        optionsList.add(lootMultiplierItem);
//
//        List<String> spawnerVanillaLootBonusLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not Elite Mobs spawned",
//                "through mob spawners drop more loot than",
//                "non-elite mobs"));
//        ItemStack spawnerVanillaLootBonusItem = boolItemStackConstructor(DefaultConfig.SPAWNER_DEFAULT_LOOT_MULTIPLIER,
//                spawnerVanillaLootBonusLore, spawnerVanillaLootBonus);
//        optionsList.add(spawnerVanillaLootBonusItem);
//
//        List<String> explosionMultiplierLore = new ArrayList<>(Arrays.asList(
//                "Multiplies explosion range and damage",
//                "from Elite Creepers. Values between 0.1",
//                "and 0.99 nerf, values above 1 boost."));
//        ItemStack explosionMultiplierItem = doubleItemStackConstructor(DefaultConfig.ELITE_CREEPER_EXPLOSION_MULTIPLIER,
//                explosionMultiplierLore, explosionMultiplier);
//        optionsList.add(explosionMultiplierItem);
//
//        List<String> naturalVisualEffectsLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not Elite Mobs have",
//                "visual effects floating around them",
//                "indicating the powers they have"));
//        ItemStack naturalVisualEffectItem = boolItemStackConstructor(DefaultConfig.ENABLE_VISUAL_EFFECTS_FOR_NATURAL_MOBS,
//                naturalVisualEffectsLore, naturalVisualEffect);
//        optionsList.add(naturalVisualEffectItem);
//
//        List<String> spawnerVisualEffectsLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not Elite Mobs spawned",
//                "in mob spawners will have visual effects",
//                "around them indicating the powers they have"));
//        ItemStack spawnerVisualEffectsItem = boolItemStackConstructor(DefaultConfig.DISABLE_VISUAL_EFFECTS_FOR_SPAWNER_MOBS,
//                spawnerVisualEffectsLore, spawnerVisualEffects);
//        optionsList.add(spawnerVisualEffectsItem);
//
//        List<String> telegraphedVisualEffectsLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or no Elite Mobs will",
//                "display special visual effects when",
//                "in special attack phases"));
//        ItemStack telegraphedVisualEffectsItem = boolItemStackConstructor(DefaultConfig.ENABLE_WARNING_VISUAL_EFFECTS,
//                telegraphedVisualEffectsLore, telegraphedVisualEffects);
//        optionsList.add(telegraphedVisualEffectsItem);
//
//        List<String> permissionMissingTitlesLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not titles will",
//                "be used to display permission missing",
//                "messages (instead of chat text)"));
//        ItemStack permissionMissingTitlesItem = boolItemStackConstructor(DefaultConfig.ENABLE_PERMISSION_TITLES,
//                permissionMissingTitlesLore, permissionMissingTitles);
//        optionsList.add(permissionMissingTitlesItem);
//
//        List<String> scoreboardsLore = new ArrayList<>(Arrays.asList(
//                "Sets whether or not scoreboards can be",
//                "used to display health and powers that",
//                "an Elite Mob may have (requires permission)"));
//        ItemStack scoreboardsItem = boolItemStackConstructor(DefaultConfig.ENABLE_POWER_SCOREBOARDS,
//                scoreboardsLore, scoreboards);
//        optionsList.add(scoreboardsItem);
//
//
//        //Valid chest slots start at slot 18
//        int slot = 18;
//        for (ItemStack itemStack : optionsList) {
//
//            inventory.setItem(slot, itemStack);
//            slot++;
//
//        }
//
//    }
//
//    private static ItemStack boolItemStackConstructor(String title, List<String> lore, Boolean currentSetting) {
//
//        Material itemMaterial = currentSetting ? Material.PAPER : Material.BARRIER;
//
//        title += currentSetting ? ChatColorConverter.chatColorConverter(": &2true") :
//                ChatColorConverter.chatColorConverter(": &4false");
//
//        String loteInstructions = currentSetting ? ChatColorConverter.chatColorConverter("&4Click to disable!") :
//                ChatColorConverter.chatColorConverter("&aClick to enable!");
//
//        lore.add(loteInstructions);
//
//        ItemStack itemStack = new ItemStack(itemMaterial, 1);
//
//        return itemStackAssembler(title, lore, itemStack);
//
//    }
//
//    private static ItemStack doubleItemStackConstructor(String title, List<String> lore, double currentSetting) {
//
//        Material itemMaterial = Material.PAPER;
//
//        title += ": " + currentSetting;
//
//        String loreInstructions = "Click to change!";
//
//        lore.add(loreInstructions);
//
//        ItemStack itemStack = new ItemStack(itemMaterial, 1);
//
//        return itemStackAssembler(title, lore, itemStack);
//
//    }
//
//    private static ItemStack percentageDoubleItemStackConstructor(String title, List<String> lore, double currentSetting) {
//
//        Material itemMaterial = Material.PAPER;
//
//        title += ": " + currentSetting + "%";
//
//        String loreInstructions = "Click to change!";
//
//        lore.add(loreInstructions);
//
//        ItemStack itemStack = new ItemStack(itemMaterial, 1);
//
//        return itemStackAssembler(title, lore, itemStack);
//
//    }
//
//    private static ItemStack itemStackAssembler(String title, List<String> lore, ItemStack itemStack) {
//
//        ItemMeta itemMeta = itemStack.getItemMeta();
//        itemMeta.setDisplayName(title);
//        itemMeta.setLore(lore);
//        itemStack.setItemMeta(itemMeta);
//
//        return itemStack;
//
//    }

}
