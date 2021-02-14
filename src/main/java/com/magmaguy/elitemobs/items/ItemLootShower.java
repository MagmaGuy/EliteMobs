package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.Round;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ItemLootShower implements Listener {

    private final Player player;
    public static HashMap<UUID, Coin> coinValues = new HashMap<>();

    private class Coin {
        UUID player;
        UUID item;
        double value;
        boolean pickupable;

        public Coin(double value, Player player, Item item) {
            this.player = player.getUniqueId();
            this.value = value;
            this.item = item.getUniqueId();
            coinValues.put(item.getUniqueId(), this);
            pickupable = false;
            item.setGravity(false);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (coinValues.containsKey(item.getUniqueId())) {
                        if (Bukkit.getEntity(item.getUniqueId()) != null)
                            Bukkit.getEntity(item.getUniqueId()).remove();
                        coinValues.remove(item.getUniqueId());
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * 5);

            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {

                    if (!item.isValid() ||
                            !player.isValid() ||
                            !player.getWorld().equals(item.getWorld()) ||
                            counter > 20 * 4 ||
                            item.getLocation().distanceSquared(player.getLocation()) > 900) {
                        cancel();
                        pickupable = true;
                        item.setGravity(true);
                        return;
                    }

                    item.setVelocity(player.getLocation().clone().subtract(item.getLocation()).toVector().normalize().multiply(0.2));

                    if (player.getLocation().distanceSquared(item.getLocation()) <= 1) {
                        item.remove();
                        EconomyHandler.addCurrency(player.getUniqueId(), value);
                        sendCurrencyNotification(player);

                        //cache for counting how much coin they're getting over a short amount of time
                        if (playerCurrencyPickup.containsKey(player))
                            playerCurrencyPickup.put(player, playerCurrencyPickup.get(player) + value);
                        else
                            playerCurrencyPickup.put(player, value);

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(
                                        ChatColorConverter.convert(EconomySettingsConfig.actionBarCurrencyShowerMessage
                                                .replace("$currency_name", EconomySettingsConfig.currencyName)
                                                .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(player)) + ""))));
                        coinValues.remove(item.getUniqueId());
                        cancel();
                        return;
                    }

                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
        }
    }

    public ItemLootShower(double eliteMobTier, Location location, Player player) {

        this.player = player;

        if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) == null) return;

        if (!EconomySettingsConfig.enableCurrencyShower || !SoulbindEnchantment.isEnabled)
            return;

        if (eliteMobTier - ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false) < -20) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    ChatColorConverter.convert("&8EM] &4You are too well equipped to get coins for killing this Elite!")));
            return;
        }

        new BukkitRunnable() {

            int currencyAmount = (int) (eliteMobTier / 2 * EconomySettingsConfig.currencyShowerMultiplier *
                    GuildRank.currencyBonusMultiplier(GuildRank.getGuildPrestigeRank(player)));

            @Override
            public void run() {

                if (currencyAmount <= 0) {
                    cancel();
                    return;
                }

                if (currencyAmount >= 1000) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropOneThousand(location);
                        currencyAmount -= 1000;
                        return;
                    }
                }

                if (currencyAmount >= 500) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFiveHundred(location);
                        currencyAmount -= 500;
                        return;
                    }
                }

                if (currencyAmount >= 100) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropOneHundred(location);
                        currencyAmount -= 100;
                        return;
                    }

                } else if (currencyAmount >= 50) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFifty(location);
                        currencyAmount -= 50;
                        return;
                    }

                } else if (currencyAmount >= 20) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTwenty(location);
                        currencyAmount -= 20;
                        return;
                    }

                } else if (currencyAmount >= 10) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTen(location);
                        currencyAmount -= 10;
                        return;
                    }

                } else if (currencyAmount >= 5) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFive(location);
                        currencyAmount -= 5;
                        return;
                    }

                } else {
                    dropOne(location);
                    currencyAmount--;
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 2, 2);

    }

    private Item generateCurrencyItem(Material material, Location location, double value) {

        ItemStack currencyItemStack = SoulbindEnchantment.addEnchantment(ItemStackGenerator.generateItemStack(material, "",
                Arrays.asList("EliteMobsCurrencyItem", value + "", ThreadLocalRandom.current().nextDouble() + "")), player);
        Item currencyItem = location.getWorld().dropItem(location.clone().add(new Vector(0, 1, 0)), currencyItemStack);
        EntityTracker.registerItemVisualEffects(currencyItem);

        currencyItem.setVelocity(new Vector(
                (ThreadLocalRandom.current().nextDouble() - 0.5) / 2,
                0.5,
                (ThreadLocalRandom.current().nextDouble() - 0.5) / 2));

        SoulbindEnchantment.addPhysicalDisplay(currencyItem, this.player);

        new Coin(value, player, currencyItem);

        return currencyItem;

    }

    private void dropOne(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial1), location, 1);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 1 is invalid. Defaulting to gold nugget.");
            currencyItem = generateCurrencyItem(Material.GOLD_NUGGET, location, 1);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&7" + 1 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);

    }

    private void dropFive(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial5), location, 5);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 5 is invalid. Defaulting to gold ingot.");
            currencyItem = generateCurrencyItem(Material.GOLD_INGOT, location, 5);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&f" + 5 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropTen(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial10), location, 10);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 10 is invalid. Defaulting to Gold block.");
            currencyItem = generateCurrencyItem(Material.GOLD_BLOCK, location, 10);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&a" + 10 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropTwenty(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial20), location, 20);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 20 is invalid. Defaulting to emerald.");
            currencyItem = generateCurrencyItem(Material.EMERALD, location, 20);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 20 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropFifty(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial50), location, 50);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 50 is invalid. Defaulting to emerald block.");
            currencyItem = generateCurrencyItem(Material.EMERALD_BLOCK, location, 50);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 50 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropOneHundred(Location location) {
        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial100), location, 100);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 100 is invalid. Defaulting to diamond.");
            currencyItem = generateCurrencyItem(Material.DIAMOND, location, 100);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 100 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropFiveHundred(Location location) {
        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial500), location, 500);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 500 is invalid. Defaulting to diamond block.");
            currencyItem = generateCurrencyItem(Material.DIAMOND_BLOCK, location, 500);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 500 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropOneThousand(Location location) {
        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.lootShowerMaterial1000), location, 1000);
        } catch (Exception ex) {
            new WarningMessage("Material for EliteMob shower 1000 is invalid. Defaulting to nether star.");
            currencyItem = generateCurrencyItem(Material.NETHER_STAR, location, 1000);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 1000 + " " + EconomySettingsConfig.currencyName));
        currencyItem.setCustomNameVisible(true);
    }


    private static final HashMap<Player, Double> playerCurrencyPickup = new HashMap<>();

    /**
     * Currency pickup event
     */
    public static class ItemLootShowerEvents implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public static void onItemPickup(EntityPickupItemEvent event) {
            //coins are soulbound so if someone can pick them up they can have it
            if (!coinValues.containsKey(event.getItem().getUniqueId())) return;
            event.setCancelled(true);
            if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;

            Coin coin = coinValues.get(event.getItem().getUniqueId());
            if (!coin.pickupable)
                return;

            coinValues.remove(event.getItem().getUniqueId());
            double amountIncremented = coin.value;
            Player player = (Player) event.getEntity();
            event.getItem().remove();
            EconomyHandler.addCurrency(player.getUniqueId(), amountIncremented);
            sendCurrencyNotification(player);

            //cache for counting how much coin they're getting over a short amount of time
            if (playerCurrencyPickup.containsKey(player))
                playerCurrencyPickup.put(player, playerCurrencyPickup.get(player) + amountIncremented);
            else
                playerCurrencyPickup.put(player, amountIncremented);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            ChatColorConverter.convert(EconomySettingsConfig.actionBarCurrencyShowerMessage
                                    .replace("$currency_name", EconomySettingsConfig.currencyName)
                                    .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(player)) + ""))));
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public static void onItemPickup(InventoryPickupItemEvent event) {
            if (!coinValues.containsKey(event.getItem().getUniqueId())) return;
            event.setCancelled(true);
        }
    }


    private static void sendCurrencyNotification(Player player) {
        if (playerCurrencyPickup.containsKey(player)) return;

        new BukkitRunnable() {
            double oldAmount = 0;

            @Override
            public void run() {

                if (!playerCurrencyPickup.containsKey(player)) {
                    playerCurrencyPickup.put(player, 0.0);
                    return;
                }

                if (oldAmount != playerCurrencyPickup.get(player)) {
                    oldAmount = playerCurrencyPickup.get(player);
                    return;
                }

                player.sendMessage(ChatColorConverter.convert(EconomySettingsConfig.chatCurrencyShowerMessage
                        .replace("$currency_name", EconomySettingsConfig.currencyName)
                        .replace("$amount", playerCurrencyPickup.get(player) + "")));

                playerCurrencyPickup.remove(player);
                sendAdventurersGuildNotification(player);

                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 40);

    }

    private static void sendAdventurersGuildNotification(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert(EconomySettingsConfig.adventurersGuildNotificationMessage)));
    }

}
