package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.Round;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ItemLootShower implements Listener {

    private static final HashMap<UUID, Double> playerCurrencyPickup = new HashMap<>();
    public static HashMap<UUID, Coin> coinValues = new HashMap<>();

    public static void shutdown() {
        playerCurrencyPickup.clear();
        coinValues.clear();
    }
    private final Player player;

    public ItemLootShower(double itemLevel, double mobLevel, Location location, Player player) {

        this.player = player;

        if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) == null) return;

        if (!EconomySettingsConfig.isEnableCurrencyShower() || !SoulbindEnchantment.isEnabled)
            return;

        if (Math.abs(mobLevel - ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false))
                > ItemSettingsConfig.getLootLevelDifferenceLockout()) {
            new BukkitRunnable() {
                int counter = 0;

                @Override
                public void run() {
                    counter++;
                    if (!player.isValid() || counter > 20 * 5) {
                        cancel();
                        return;
                    }
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                            ChatColorConverter.convert(ItemSettingsConfig.getLevelRangeTooDifferent())
                                    .replace("$playerLevel", ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false) + "")
                                    .replace("$bossLevel", (int) mobLevel + "")));
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
            return;
        }
        if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory())
            addDirectly(getCurrencyAmount(itemLevel));
        else
            addIndirectly(location, getCurrencyAmount(itemLevel));
    }

    public ItemLootShower(Location location, Player player, int amount) {
        this.player = player;
        addIndirectly(location, amount);
    }

    private static void sendCurrencyNotification(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (playerCurrencyPickup.containsKey(playerUUID)) return;

        new BukkitRunnable() {
            double oldAmount = 0;

            @Override
            public void run() {

                if (!playerCurrencyPickup.containsKey(playerUUID)) {
                    playerCurrencyPickup.put(playerUUID, 0.0);
                    return;
                }

                if (oldAmount != playerCurrencyPickup.get(playerUUID)) {
                    oldAmount = playerCurrencyPickup.get(playerUUID);
                    return;
                }

                if (!player.isOnline()) {
                    playerCurrencyPickup.remove(playerUUID);
                    cancel();
                    return;
                }

                player.sendMessage(ChatColorConverter.convert(EconomySettingsConfig.getChatCurrencyShowerMessage()
                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                        .replace("$amount", playerCurrencyPickup.get(playerUUID) + "")));

                playerCurrencyPickup.remove(playerUUID);
                sendAdventurersGuildNotification(player);

                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 40);

    }

    private static void sendAdventurersGuildNotification(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert(EconomySettingsConfig.getAdventurersGuildNotificationMessage())));
    }

    private void addIndirectly(Location location, int currencyAmount2) {
        new BukkitRunnable() {
            int currencyAmount = currencyAmount2;

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
                    }

                } else if (currencyAmount >= 50) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFifty(location);
                        currencyAmount -= 50;
                    }

                } else if (currencyAmount >= 20) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTwenty(location);
                        currencyAmount -= 20;
                    }

                } else if (currencyAmount >= 10) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropTen(location);
                        currencyAmount -= 10;
                    }

                } else if (currencyAmount >= 5) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFive(location);
                        currencyAmount -= 5;
                    }

                } else {
                    dropOne(location);
                    currencyAmount--;
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 2, 2);
    }

    private int getCurrencyAmount(double eliteMobTier) {
        return (int) (eliteMobTier / 2D * EconomySettingsConfig.getCurrencyShowerMultiplier() *
                GuildRank.currencyBonusMultiplier(GuildRank.getGuildPrestigeRank(player)));
    }

    private void addDirectly(double eliteMobTier) {
        EconomyHandler.addCurrency(player.getUniqueId(), getCurrencyAmount(eliteMobTier));
        player.sendMessage(ChatColorConverter.convert(EconomySettingsConfig.getChatCurrencyShowerMessage()
                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                .replace("$amount", getCurrencyAmount(eliteMobTier) + "")));
    }

    private Item generateCurrencyItem(Material material, Location location, double value) {

        ItemStack currencyItemStack = SoulbindEnchantment.addEnchantment(ItemStackGenerator.generateItemStack(material, "",
                new ArrayList<>(List.of("EliteMobsCurrencyItem", value + "", ThreadLocalRandom.current().nextDouble() + ""))), player);
        String model = null;
        try {
            model = EconomySettingsConfig.getThisConfiguration().getString("lootShowerDataV2." + (int) value);
        } catch (Exception ex) {
            Logger.warn("Failed to get coin model for value " + value + " !");
            ex.printStackTrace();
        }

        if (model == null) Logger.warn("No model found for value " + value + " !");
        else setCoinModel(currencyItemStack, model);
        Item currencyItem = location.getWorld().dropItem(location.clone().add(new Vector(0, 1, 0)), currencyItemStack);
        EntityTracker.registerVisualEffects(currencyItem);
        currencyItem.setInvulnerable(true);

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
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial1()), location, 1);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 1 is invalid. Defaulting to gold nugget.");
            currencyItem = generateCurrencyItem(Material.GOLD_NUGGET, location, 1);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&7" + 1 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);

    }

    private void dropFive(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial5()), location, 5);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 5 is invalid. Defaulting to gold ingot.");
            currencyItem = generateCurrencyItem(Material.GOLD_INGOT, location, 5);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&f" + 5 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropTen(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial10()), location, 10);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 10 is invalid. Defaulting to Gold block.");
            currencyItem = generateCurrencyItem(Material.GOLD_BLOCK, location, 10);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&a" + 10 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropTwenty(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial20()), location, 20);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 20 is invalid. Defaulting to emerald.");
            currencyItem = generateCurrencyItem(Material.EMERALD, location, 20);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 20 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropFifty(Location location) {

        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial50()), location, 50);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 50 is invalid. Defaulting to emerald block.");
            currencyItem = generateCurrencyItem(Material.EMERALD_BLOCK, location, 50);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 50 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropOneHundred(Location location) {
        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial100()), location, 100);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 100 is invalid. Defaulting to diamond.");
            currencyItem = generateCurrencyItem(Material.DIAMOND, location, 100);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 100 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropFiveHundred(Location location) {
        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial500()), location, 500);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 500 is invalid. Defaulting to diamond block.");
            currencyItem = generateCurrencyItem(Material.DIAMOND_BLOCK, location, 500);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 500 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private void dropOneThousand(Location location) {
        Item currencyItem;
        try {
            currencyItem = generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial1000()), location, 1000);
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 1000 is invalid. Defaulting to nether star.");
            currencyItem = generateCurrencyItem(Material.NETHER_STAR, location, 1000);
        }

        currencyItem.setCustomName(ChatColorConverter.convert("&2" + 1000 + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);
    }

    private ItemStack setCoinModel(ItemStack itemStack, String data) {
        CustomModelAdder.addCustomModel(itemStack, data);
        return itemStack;
    }

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
            UUID playerUUID = player.getUniqueId();
            if (playerCurrencyPickup.containsKey(playerUUID))
                playerCurrencyPickup.put(playerUUID, playerCurrencyPickup.get(playerUUID) + amountIncremented);
            else
                playerCurrencyPickup.put(playerUUID, amountIncremented);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            ChatColorConverter.convert(EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                    .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(playerUUID)) + ""))));
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public static void onItemPickup(InventoryPickupItemEvent event) {
            if (!coinValues.containsKey(event.getItem().getUniqueId())) return;
            event.setCancelled(true);
        }
    }

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
                        UUID coinPlayerUUID = player.getUniqueId();
                        if (playerCurrencyPickup.containsKey(coinPlayerUUID))
                            playerCurrencyPickup.put(coinPlayerUUID, playerCurrencyPickup.get(coinPlayerUUID) + value);
                        else
                            playerCurrencyPickup.put(coinPlayerUUID, value);

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(
                                        ChatColorConverter.convert(EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                                .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(coinPlayerUUID)) + ""))));
                        coinValues.remove(item.getUniqueId());
                        cancel();
                        return;
                    }

                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
        }
    }

}
