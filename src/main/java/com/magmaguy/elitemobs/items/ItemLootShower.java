package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.Round;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ItemLootShower implements Listener {

    private final Player player;

    public ItemLootShower(double eliteMobTier, Location location, Player player) {

        this.player = player;

        if (!EconomySettingsConfig.enableCurrencyShower)
            return;

        new BukkitRunnable() {
            int currencyAmount = (int) (eliteMobTier / 2 * EconomySettingsConfig.currencyShowerMultiplier *
                    GuildRank.currencyBonusMultiplier(GuildRank.getGuildPrestigeRank(player)));

            @Override
            public void run() {

                if (currencyAmount == 0) {
                    cancel();
                    return;
                }

                if (currencyAmount >= 50) {
                    if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                        dropFifty(location);
                        currencyAmount -= 50;
                        return;
                    }

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

                } else if (currencyAmount >= 20) {
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

    private Item generateCurrencyItem(Material material, Location location, double value) {

        ItemStack currencyItemStack = ItemStackGenerator.generateItemStack(material, "",
                Arrays.asList("EliteMobsCurrencyItem", value + "", ThreadLocalRandom.current().nextDouble() + ""));
        Item currencyItem = location.getWorld().dropItem(location.clone().add(new Vector(0, 1, 0)), currencyItemStack);
        EntityTracker.registerItemVisualEffects(currencyItem);

        currencyItem.setVelocity(new Vector(
                (ThreadLocalRandom.current().nextDouble() - 0.5) / 2,
                0.5,
                (ThreadLocalRandom.current().nextDouble() - 0.5) / 2));

        SoulbindEnchantment.addEnchantment(currencyItem, this.player);

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

    private static final HashMap<Player, Double> playerCurrencyPickup = new HashMap<>();

    /**
     * Currency pickup event
     */
    public static class ItemLootShowerEvents implements Listener {
        @EventHandler(priority = EventPriority.LOW)
        public static void onItemPickup(PlayerPickupItemEvent event) {
            if (event.isCancelled()) return;

            if (event.getItem() == null ||
                    !event.getItem().getItemStack().hasItemMeta() ||
                    !event.getItem().getItemStack().getItemMeta().hasLore() ||
                    event.getItem().getItemStack().getItemMeta().getLore().get(0) == null ||
                    event.getItem().getItemStack().getItemMeta().getLore().get(0).isEmpty() ||
                    !event.getItem().getItemStack().getItemMeta().getLore().get(0).equalsIgnoreCase("EliteMobsCurrencyItem"))
                return;

            event.setCancelled(true);

            Player player = event.getPlayer();

            double amountIncremented = Double.valueOf(event.getItem().getItemStack().getItemMeta().getLore().get(1));

            event.getItem().remove();

            EconomyHandler.addCurrency(player.getUniqueId(), amountIncremented);
            sendCurrencyNotification(player);

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
