package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.DebugMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.utils.WeightedProbability.pickWeighedProbability;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class LootTables implements Listener {

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {

        if (!event.getEliteMobEntity().getHasSpecialLoot()) return;
        if (event.getEliteMobEntity().getLevel() < 1) return;
        if (event.getEliteMobEntity().getDamagers().isEmpty()) return;

        if (!event.getEliteMobEntity().hasVanillaLoot())
            event.getEntityDeathEvent().getDrops().clear();

        generatePlayerLoot(event.getEliteMobEntity());

    }

    public static void generatePlayerLoot(EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity.getTriggeredAntiExploit())
            return;
        for (Player player : eliteMobEntity.getDamagers().keySet()) {

            if (eliteMobEntity.getDamagers().get(player) / eliteMobEntity.getMaxHealth() < 0.1)
                return;

            new ItemLootShower(eliteMobEntity.getTier(), eliteMobEntity.getLivingEntity().getLocation(), player);

            Item item = null;

            if (AdventurersGuildConfig.guildLootLimiter) {
                double itemTier = setItemTier((int) eliteMobEntity.getTier());
                if (itemTier > GuildRank.getActiveGuildRank(player) * 10) {
                    itemTier = GuildRank.getActiveGuildRank(player) * 10;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(AdventurersGuildConfig.lootLimiterMessage));
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10);
                }
                item = generateLoot((int) Math.floor(itemTier), eliteMobEntity, player);
            } else
                item = generateLoot(eliteMobEntity, player);

            if (item != null &&
                    item.getItemStack() != null &&
                    item.getItemStack().hasItemMeta() &&
                    item.getItemStack().getItemMeta().hasDisplayName()) {
                item.setCustomName(item.getItemStack().getItemMeta().getDisplayName());
                item.setCustomNameVisible(true);
            }

            SoulbindEnchantment.addPhysicalDisplay(item, player);

            if (item == null) return;

            RareDropEffect.runEffect(item);
        }
    }

    private static final boolean proceduralItemsOn = ProceduralItemGenerationSettingsConfig.doProceduralItemDrops;
    private static final boolean customItemsOn = ItemSettingsConfig.doEliteMobsLoot && !CustomItem.getCustomItemStackList().isEmpty();
    private static final boolean weighedItemsExist = CustomItem.getWeighedFixedItems() != null && !CustomItem.getWeighedFixedItems().isEmpty();
    private static final boolean fixedItemsExist = CustomItem.getFixedItems() != null && !CustomItem.getFixedItems().isEmpty();
    private static final boolean limitedItemsExist = CustomItem.getLimitedItem() != null && !CustomItem.getLimitedItem().isEmpty();
    private static final boolean scalableItemsExist = CustomItem.getScalableItems() != null && !CustomItem.getScalableItems().isEmpty();

    private static Item generateLoot(EliteMobEntity eliteMobEntity, Player player) {

        int mobTier = (int) MobTierCalculator.findMobTier(eliteMobEntity);

        /*
        Add some wiggle room to avoid making obtaining loot too linear
         */
        int itemTier = (int) setItemTier(mobTier);

        return generateLoot(itemTier, eliteMobEntity, player);

    }

    public static Item generateLoot(int itemTier, EliteMobEntity eliteMobEntity, Player player) {

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ItemSettingsConfig.flatDropRate;
        double dropChanceBonus = ItemSettingsConfig.tierIncreaseDropRate * itemTier;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus)
            return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn)
            weightedProbability.put("procedural", ItemSettingsConfig.proceduralItemWeight);
        if (customItemsOn) {
            if (weighedItemsExist)
                weightedProbability.put("weighed", ItemSettingsConfig.weighedItemWeight);
            if (fixedItemsExist)
                if (CustomItem.getFixedItems().containsKey(itemTier))
                    weightedProbability.put("fixed", ItemSettingsConfig.fixedItemWeight);
            if (limitedItemsExist)
                weightedProbability.put("limited", ItemSettingsConfig.limitedItemWeight);
            if (scalableItemsExist)
                weightedProbability.put("scalable", ItemSettingsConfig.scalableItemWeight);
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "procedural":
                return dropProcedurallyGeneratedItem(itemTier, eliteMobEntity, player);
            case "weighed":
                return dropWeighedFixedItem(eliteMobEntity.getLivingEntity().getLocation(), player);
            case "fixed":
                return dropFixedItem(eliteMobEntity.getLivingEntity().getLocation(), itemTier, player);
            case "limited":
                return dropLimitedItem(eliteMobEntity.getLivingEntity().getLocation(), itemTier, player);
            case "scalable":
                return dropScalableItem(eliteMobEntity.getLivingEntity().getLocation(), itemTier, player);
        }

        return null;

    }

    /**
     * Used for "fake" drops. Currently this means it's used by the SimLootHandler, reason will default to shop.
     *
     * @param itemTier
     * @param location
     * @param player
     * @return
     */
    public static Item generateLoot(int itemTier, Location location, Player player) {

         /*
        Handle the odds of an item dropping
         */
        double baseChance = ItemSettingsConfig.flatDropRate;
        double dropChanceBonus = ItemSettingsConfig.tierIncreaseDropRate * itemTier;

        if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus)
            return null;

        HashMap<String, Double> weightedProbability = new HashMap<>();
        if (proceduralItemsOn)
            weightedProbability.put("procedural", ItemSettingsConfig.proceduralItemWeight);
        if (customItemsOn) {
            if (weighedItemsExist)
                weightedProbability.put("weighed", ItemSettingsConfig.weighedItemWeight);
            if (fixedItemsExist)
                if (CustomItem.getFixedItems().containsKey(itemTier))
                    weightedProbability.put("fixed", ItemSettingsConfig.fixedItemWeight);
            if (limitedItemsExist)
                weightedProbability.put("limited", ItemSettingsConfig.limitedItemWeight);
            if (scalableItemsExist)
                weightedProbability.put("scalable", ItemSettingsConfig.scalableItemWeight);
        }

        String selectedLootSystem = pickWeighedProbability(weightedProbability);

        switch (selectedLootSystem) {
            case "procedural":
                return dropProcedurallyGeneratedItem(itemTier, location, player);
            case "weighed":
                return dropWeighedFixedItem(location, player);
            case "fixed":
                return dropFixedItem(location, itemTier, player);
            case "limited":
                return dropLimitedItem(location, itemTier, player);
            case "scalable":
                return dropScalableItem(location, itemTier, player);
        }

        return null;

    }

    public static double setItemTier(int mobTier) {

        double chanceToUpgradeTier = 10 / (double) mobTier * ItemSettingsConfig.maximumLootTier;

        if (ThreadLocalRandom.current().nextDouble() * 100 < chanceToUpgradeTier)
            return mobTier + 1;


        double diceRoll = ThreadLocalRandom.current().nextDouble();

        /*
        10% of the time, give an item a tier below what the player is wearing
        40% of the time, give an item of the same tier as what the player is wearing
        50% of the time, give an item better than what the player is wearing
        If you're wondering why this isn't configurable, wonder instead why no one has noticed it isn't before you reading this
         */
        if (diceRoll < 0.10)
            mobTier -= 2;
        else if (diceRoll < 0.50)
            mobTier -= 1;

        if (mobTier < 0) mobTier = 0;

        return mobTier;

    }


    private static Item dropWeighedFixedItem(Location location, Player player) {

        double totalWeight = 0;


        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet())
            try {
                totalWeight += CustomItem.getWeighedFixedItems().get(itemStack);
            } catch (NullPointerException ex) {
                new DebugMessage("Error generating loot");
                if (itemStack != null)
                    new DebugMessage("itemStack is " + itemStack.getItemMeta().getDisplayName());
                new DebugMessage("Weight value is null: " + (CustomItem.getWeighedFixedItems().get(itemStack) == null));
                ex.printStackTrace();
            }


        ItemStack generatedItemStack = null;
        double random = Math.random() * totalWeight;

        for (ItemStack itemStack : CustomItem.getWeighedFixedItems().keySet()) {
            random -= CustomItem.getWeighedFixedItems().get(itemStack);
            if (random <= 0) {
                generatedItemStack = itemStack;
                break;
            }
        }

        SoulbindEnchantment.addEnchantment(generatedItemStack, player);

        return location.getWorld().dropItem(location, generatedItemStack);

    }

    private static Item dropProcedurallyGeneratedItem(int tierLevel, EliteMobEntity eliteMobEntity, Player player) {
        ItemStack randomLoot = ItemConstructor.constructItem(tierLevel, eliteMobEntity, player, false);
        return eliteMobEntity.getLivingEntity().getWorld().dropItem(eliteMobEntity.getLivingEntity().getLocation(), randomLoot);
    }

    private static Item dropProcedurallyGeneratedItem(int tierLevel, Location location, Player player) {
        ItemStack randomLoot = ItemConstructor.constructItem(tierLevel, null, player, false);
        return location.getWorld().dropItem(location, randomLoot);
    }

    private static Item dropScalableItem(Location location, int itemTier, Player player) {
        return location.getWorld().dropItem(location, ScalableItemConstructor.randomizeScalableItem(itemTier, player));
    }

    private static Item dropLimitedItem(Location location, int itemTier, Player player) {
        return location.getWorld().dropItem(location, ScalableItemConstructor.randomizeLimitedItem(itemTier, player));
    }

    private static Item dropFixedItem(Location location, int itemTier, Player player) {
        return location.getWorld().dropItem(location, CustomItem.getFixedItems().get(itemTier).get(ThreadLocalRandom.current().nextInt(CustomItem.getFixedItems().get(itemTier).size())).generateDefaultsItemStack(player, false));
    }

}
