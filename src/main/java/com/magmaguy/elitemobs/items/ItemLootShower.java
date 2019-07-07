package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemsDropSettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ItemLootShower implements Listener {

    public static void runShower(double eliteMobTier, Location location) {

        if (!ConfigValues.economyConfig.getBoolean(EconomySettingsConfig.ENABLE_CURRENCY_SHOWER))
            return;

        new BukkitRunnable() {
            int currencyAmount = (int) (eliteMobTier / 2 * ConfigValues.economyConfig.getDouble(EconomySettingsConfig.CURRENCY_SHOWER_MULTIPLIER));

            @Override
            public void run() {

                if (currencyAmount == 0) {
                    cancel();
                    return;
                }

                if (currencyAmount >= 20) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTwenty(location);
                        currencyAmount -= 20;
                        return;
                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTen(location);
                        currencyAmount -= 10;
                        return;
                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFive(location);
                        currencyAmount -= 5;
                        return;
                    }

                    dropOne(location);
                    currencyAmount--;
                    return;

                } else if (currencyAmount >= 10) {

                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTen(location);
                        currencyAmount -= 10;
                        return;
                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFive(location);
                        currencyAmount -= 5;
                        return;
                    }

                    dropOne(location);
                    currencyAmount--;
                    return;

                } else if (currencyAmount >= 5) {

                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFive(location);
                        currencyAmount -= 5;
                        return;
                    }

                    dropOne(location);
                    currencyAmount--;
                    return;

                } else {
                    dropOne(location);
                    currencyAmount--;
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 2, 2);

    }

    private static Item generateCurrencyItem(Material material, Location location) {

        ItemStack currencyItemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = currencyItemStack.getItemMeta();
        itemMeta.setLore(Arrays.asList("EliteMobsCurrencyItem", ThreadLocalRandom.current().nextDouble() + ""));
        currencyItemStack.setItemMeta(itemMeta);
        Item currencyItem = location.getWorld().dropItem(location, currencyItemStack);
        EntityTracker.registerItemVisualEffects(currencyItem);

        currencyItem.setVelocity(new Vector(
                (ThreadLocalRandom.current().nextDouble() - 0.5) / 2,
                0.5,
                (ThreadLocalRandom.current().nextDouble() - 0.5) / 2));

        return currencyItem;

    }

    private static void dropOne(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.valueOf(ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.LOOT_SHOWER_ITEM_ONE)), location);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 1 is invalid. Defaulting to gold nugget.");
            currencyItem = generateCurrencyItem(Material.GOLD_NUGGET, location);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&7" + 1 + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));
        currencyItem.setCustomNameVisible(true);

    }

    private static void dropFive(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.valueOf(ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.LOOT_SHOWER_ITEM_FIVE)), location);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 5 is invalid. Defaulting to gold ingot.");
            currencyItem = generateCurrencyItem(Material.GOLD_INGOT, location);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&f" + 5 + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));
        currencyItem.setCustomNameVisible(true);
    }

    private static void dropTen(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.valueOf(ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.LOOT_SHOWER_ITEM_TEN)), location);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 1 is invalid. Defaulting to emerald.");
            currencyItem = generateCurrencyItem(Material.EMERALD, location);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&a" + 10 + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));
        currencyItem.setCustomNameVisible(true);
    }

    private static void dropTwenty(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.valueOf(ConfigValues.itemsDropSettingsConfig.getString(ItemsDropSettingsConfig.LOOT_SHOWER_ITEM_TWENTY)), location);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 1 is invalid. Defaulting to emerald block.");
            currencyItem = generateCurrencyItem(Material.EMERALD_BLOCK, location);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 20 + " " + ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)));
        currencyItem.setCustomNameVisible(true);
    }

    private static HashMap<Player, Double> playerCurrencyPickup = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onItemPickup(PlayerPickupItemEvent event) {

        if (event.getItem() == null ||
                !event.getItem().getItemStack().hasItemMeta() ||
                !event.getItem().getItemStack().getItemMeta().hasLore() ||
                event.getItem().getItemStack().getItemMeta().getLore().get(0) == null ||
                event.getItem().getItemStack().getItemMeta().getLore().get(0).isEmpty() ||
                !event.getItem().getItemStack().getItemMeta().getLore().get(0).equalsIgnoreCase("EliteMobsCurrencyItem"))
            return;

        Player player = event.getPlayer();

        if (event.getItem().getItemStack().getType().equals(Material.GOLD_NUGGET)) {
            sendCurrencyNotification(player);
            EconomyHandler.addCurrency(player.getUniqueId(), 1);
            sendActionBarMessage(ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_SHOWER_MESSAGE_1), player);
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (event.getItem().getItemStack().getType().equals(Material.GOLD_INGOT)) {
            sendCurrencyNotification(player);
            EconomyHandler.addCurrency(player.getUniqueId(), 5);
            sendActionBarMessage(ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_SHOWER_MESSAGE_5), player);
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (event.getItem().getItemStack().getType().equals(Material.EMERALD)) {
            sendCurrencyNotification(player);
            EconomyHandler.addCurrency(player.getUniqueId(), 10);
            sendActionBarMessage(ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_SHOWER_MESSAGE_10), player);
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (event.getItem().getItemStack().getType().equals(Material.EMERALD_BLOCK)) {
            sendCurrencyNotification(player);
            EconomyHandler.addCurrency(player.getUniqueId(), 20);
            sendActionBarMessage(ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_SHOWER_MESSAGE_20), player);
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

    }

    private static void sendActionBarMessage(String string, Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(
                        ChatColorConverter.convert(string.replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME)))));
    }

    private static void sendCurrencyNotification(Player player) {
        if (playerCurrencyPickup.containsKey(player)) return;

        new BukkitRunnable() {
            double originalAmount = EconomyHandler.checkCurrency(player.getUniqueId());

            @Override
            public void run() {

                if (!playerCurrencyPickup.containsKey(player)) {
                    playerCurrencyPickup.put(player, 0.0);
                    return;
                }

                if (originalAmount + playerCurrencyPickup.get(player) != EconomyHandler.checkCurrency(player.getUniqueId())) {

                    playerCurrencyPickup.put(player, EconomyHandler.checkCurrency(player.getUniqueId()) - originalAmount);
                    return;

                }

                player.sendMessage(ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_LOOT_MESSAGE)
                        .replace("$currency_name", ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_NAME))
                        .replace("$amount", playerCurrencyPickup.get(player) + "")));

                playerCurrencyPickup.remove(player);
                sendAdventurersGuildNotification(player);

                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);

    }

    private static void sendAdventurersGuildNotification(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert(ConfigValues.economyConfig.getString(EconomySettingsConfig.CURRENCY_AG_NOTIFICATION))));
    }

}
