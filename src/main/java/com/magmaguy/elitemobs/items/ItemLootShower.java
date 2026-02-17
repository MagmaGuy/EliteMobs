package com.magmaguy.elitemobs.items;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.MetadataHandler;
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
import org.bukkit.entity.Display;
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

/**
 * Handles the visual coin shower effect when players defeat elite mobs.
 * On 1.21.4+, uses packet-based FakeItem entities that cannot be picked up by hoppers or other plugins.
 * On older versions, falls back to real Item entities with pickup protection.
 */
public class ItemLootShower {

    private static final HashMap<UUID, Double> playerCurrencyPickup = new HashMap<>();
    // For legacy (pre-1.21.4) real item system
    public static HashMap<UUID, LegacyCoin> coinValues = new HashMap<>();
    // Cached version check result
    private static Boolean useFakeItems = null;

    public ItemLootShower(double itemLevel, double mobLevel, Location location, Player player) {

        this.player = player;

        if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) == null) return;

        if (!EconomySettingsConfig.isEnableCurrencyShower())
            return;

        // Legacy system requires soulbind to be enabled
        if (!useFakeItems() && !SoulbindEnchantment.isEnabled)
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
                            ItemSettingsConfig.getLevelRangeTooDifferent()
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

    public static void shutdown() {
        playerCurrencyPickup.clear();
        coinValues.clear();
        useFakeItems = null;
    }

    private final Player player;

    /**
     * Check if we should use FakeItem (1.21.4+) or real Items (older versions)
     */
    private static boolean useFakeItems() {
        if (useFakeItems == null) {
            // Check if the NMS adapter supports FakeItems
            useFakeItems = NMSManager.getAdapter().supportsFakeItems();
        }
        return useFakeItems;
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

                player.sendMessage(EconomySettingsConfig.getChatCurrencyShowerMessage()
                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                        .replace("$amount", playerCurrencyPickup.get(playerUUID) + ""));

                playerCurrencyPickup.remove(playerUUID);
                sendAdventurersGuildNotification(player);

                cancel();

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 40);

    }

    private static void sendAdventurersGuildNotification(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(EconomySettingsConfig.getAdventurersGuildNotificationMessage()));
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
        return (int) (eliteMobTier / 2D * EconomySettingsConfig.getCurrencyShowerMultiplier());
    }

    private void addDirectly(double eliteMobTier) {
        EconomyHandler.addCurrency(player.getUniqueId(), getCurrencyAmount(eliteMobTier));
        player.sendMessage(EconomySettingsConfig.getChatCurrencyShowerMessage()
                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                .replace("$amount", getCurrencyAmount(eliteMobTier) + ""));
    }

    /**
     * Generate currency item - uses FakeItem on 1.21.4+ or real Item on older versions
     */
    private void generateCurrencyItem(Material material, Location location, double value, String colorCode) {
        if (useFakeItems()) {
            generateFakeCurrencyItem(material, location, value, colorCode);
        } else {
            generateLegacyCurrencyItem(material, location, value, colorCode);
        }
    }

    /**
     * Modern implementation using packet-based FakeItem (1.21.4+)
     */
    private void generateFakeCurrencyItem(Material material, Location location, double value, String colorCode) {
        ItemStack currencyItemStack = ItemStackGenerator.generateItemStack(material, "",
                new ArrayList<>(List.of("EliteMobsCurrencyItem", value + "", ThreadLocalRandom.current().nextDouble() + "")));

        String model = null;
        try {
            model = EconomySettingsConfig.getThisConfiguration().getString("lootShowerDataV2." + (int) value);
        } catch (Exception ex) {
            Logger.warn("Failed to get coin model for value " + value + " !");
            ex.printStackTrace();
        }

        if (model == null) Logger.warn("No model found for value " + value + " !");
        else setCoinModel(currencyItemStack, model);

        // Create packet-based FakeItem - only visible to the target player
        Location spawnLocation = location.clone().add(0, 1, 0);
        FakeItem fakeItem = NMSManager.getAdapter().fakeItemBuilder()
                .itemStack(currencyItemStack)
                .billboard(Display.Billboard.CENTER)
                .scale(0.6f)
                .customName(ChatColorConverter.convert(colorCode + (int) value + " " + EconomySettingsConfig.getCurrencyName()))
                .customNameVisible(true)
                .build(spawnLocation);

        // Show only to the target player
        fakeItem.displayTo(player);

        // Start the coin animation
        new FakeCoin(value, player, fakeItem, spawnLocation);
    }

    /**
     * Legacy implementation using real Item entities (pre-1.21.4)
     */
    private void generateLegacyCurrencyItem(Material material, Location location, double value, String colorCode) {
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

        currencyItem.setCustomName(ChatColorConverter.convert(colorCode + (int) value + " " + EconomySettingsConfig.getCurrencyName()));
        currencyItem.setCustomNameVisible(true);

        new LegacyCoin(value, player, currencyItem);
    }

    private void dropOne(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial1()), location, 1, "&7");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 1 is invalid. Defaulting to gold nugget.");
            generateCurrencyItem(Material.GOLD_NUGGET, location, 1, "&7");
        }
    }

    private void dropFive(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial5()), location, 5, "&f");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 5 is invalid. Defaulting to gold ingot.");
            generateCurrencyItem(Material.GOLD_INGOT, location, 5, "&f");
        }
    }

    private void dropTen(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial10()), location, 10, "&a");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 10 is invalid. Defaulting to Gold block.");
            generateCurrencyItem(Material.GOLD_BLOCK, location, 10, "&a");
        }
    }

    private void dropTwenty(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial20()), location, 20, "&2");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 20 is invalid. Defaulting to emerald.");
            generateCurrencyItem(Material.EMERALD, location, 20, "&2");
        }
    }

    private void dropFifty(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial50()), location, 50, "&2");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 50 is invalid. Defaulting to emerald block.");
            generateCurrencyItem(Material.EMERALD_BLOCK, location, 50, "&2");
        }
    }

    private void dropOneHundred(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial100()), location, 100, "&2");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 100 is invalid. Defaulting to diamond.");
            generateCurrencyItem(Material.DIAMOND, location, 100, "&2");
        }
    }

    private void dropFiveHundred(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial500()), location, 500, "&2");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 500 is invalid. Defaulting to diamond block.");
            generateCurrencyItem(Material.DIAMOND_BLOCK, location, 500, "&2");
        }
    }

    private void dropOneThousand(Location location) {
        try {
            generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial1000()), location, 1000, "&2");
        } catch (Exception ex) {
            Logger.warn("Material for EliteMob shower 1000 is invalid. Defaulting to nether star.");
            generateCurrencyItem(Material.NETHER_STAR, location, 1000, "&2");
        }
    }

    private ItemStack setCoinModel(ItemStack itemStack, String data) {
        CustomModelAdder.addCustomModel(itemStack, data);
        return itemStack;
    }

    /**
     * Event listeners for legacy (pre-1.21.4) real item pickup prevention
     */
    public static class ItemLootShowerEvents implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public static void onItemPickup(EntityPickupItemEvent event) {
            // Coins are soulbound so if someone can pick them up they can have it
            if (!coinValues.containsKey(event.getItem().getUniqueId())) return;
            event.setCancelled(true);
            if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;

            LegacyCoin coin = coinValues.get(event.getItem().getUniqueId());
            if (!coin.pickupable)
                return;

            coinValues.remove(event.getItem().getUniqueId());
            double amountIncremented = coin.value;
            Player player = (Player) event.getEntity();
            event.getItem().remove();
            EconomyHandler.addCurrency(player.getUniqueId(), amountIncremented);
            sendCurrencyNotification(player);

            // Cache for counting how much coin they're getting over a short amount of time
            UUID playerUUID = player.getUniqueId();
            if (playerCurrencyPickup.containsKey(playerUUID))
                playerCurrencyPickup.put(playerUUID, playerCurrencyPickup.get(playerUUID) + amountIncremented);
            else
                playerCurrencyPickup.put(playerUUID, amountIncremented);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                    .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(playerUUID)) + "")));
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public static void onItemPickup(InventoryPickupItemEvent event) {
            if (!coinValues.containsKey(event.getItem().getUniqueId())) return;
            event.setCancelled(true);
        }
    }

    /**
     * Packet-based coin that floats toward the player and awards currency on "pickup".
     * Since this is packet-based, no other plugins or hoppers can interact with it.
     * Used on 1.21.4+
     */
    private class FakeCoin {
        private final UUID playerUUID;
        private final double value;
        private final FakeItem fakeItem;
        private Location currentLocation;
        private Vector velocity;

        public FakeCoin(double value, Player player, FakeItem fakeItem, Location spawnLocation) {
            this.playerUUID = player.getUniqueId();
            this.value = value;
            this.fakeItem = fakeItem;
            this.currentLocation = spawnLocation.clone();

            // Initial upward + random horizontal velocity
            this.velocity = new Vector(
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3,
                    0.4,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3
            );

            // Remove after 5 minutes if not collected
            new BukkitRunnable() {
                @Override
                public void run() {
                    fakeItem.remove();
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * 5);

            // Animation task
            new BukkitRunnable() {
                int counter = 0;
                final int LAUNCH_TICKS = 15; // Initial launch phase
                final float ROTATION_PER_TICK = 9f; // 360 degrees / 40 ticks = 1 full turn per 2 seconds
                boolean attracting = false;
                float rotation = 0; // Y-axis rotation in degrees

                @Override
                public void run() {
                    Player targetPlayer = player.isOnline() ? player : null;

                    // Player disconnected or invalid
                    if (targetPlayer == null || !targetPlayer.isValid()) {
                        fakeItem.remove();
                        cancel();
                        return;
                    }

                    // Different world
                    if (!targetPlayer.getWorld().equals(currentLocation.getWorld())) {
                        fakeItem.remove();
                        cancel();
                        return;
                    }

                    // Check if close enough to "collect"
                    double distanceSquared = targetPlayer.getLocation().add(0, 1, 0).distanceSquared(currentLocation);
                    if (distanceSquared <= 1.5) {
                        collectCoin(targetPlayer);
                        cancel();
                        return;
                    }

                    // Too far away - give up after some time
                    if (counter > 20 * 10 && distanceSquared > 900) {
                        fakeItem.remove();
                        cancel();
                        return;
                    }

                    // Update rotation - 1 full turn every 2 seconds
                    rotation += ROTATION_PER_TICK;
                    if (rotation >= 360) rotation -= 360;
                    fakeItem.setYawRotation(rotation);

                    // Launch phase - apply gravity and initial velocity
                    if (counter < LAUNCH_TICKS) {
                        velocity.setY(velocity.getY() - 0.04); // Gravity
                        currentLocation.add(velocity);
                        fakeItem.teleport(currentLocation);
                    }
                    // Attract phase - float toward player
                    else {
                        if (!attracting) {
                            attracting = true;
                            velocity = new Vector(0, 0, 0);
                        }

                        Location targetLoc = targetPlayer.getLocation().add(0, 1, 0);
                        Vector direction = targetLoc.clone().subtract(currentLocation).toVector();

                        if (direction.lengthSquared() > 0.01) {
                            direction.normalize().multiply(0.35);
                            currentLocation.add(direction);
                            fakeItem.teleport(currentLocation);
                        }
                    }

                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
        }

        private void collectCoin(Player targetPlayer) {
            fakeItem.remove();
            EconomyHandler.addCurrency(targetPlayer.getUniqueId(), value);
            sendCurrencyNotification(targetPlayer);

            // Cache for counting how much coin they're getting over a short amount of time
            if (playerCurrencyPickup.containsKey(playerUUID))
                playerCurrencyPickup.put(playerUUID, playerCurrencyPickup.get(playerUUID) + value);
            else
                playerCurrencyPickup.put(playerUUID, value);

            targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(
                            EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                                    .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                    .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(playerUUID)) + "")));
        }
    }

    /**
     * Legacy coin using real Item entities (pre-1.21.4)
     * Coins are soulbound and protected from hopper/entity pickup via events
     */
    private class LegacyCoin {
        UUID player;
        UUID item;
        double value;
        boolean pickupable;

        public LegacyCoin(double value, Player player, Item item) {
            this.player = player.getUniqueId();
            this.value = value;
            this.item = item.getUniqueId();
            coinValues.put(item.getUniqueId(), this);
            pickupable = false;
            item.setGravity(false);

            // Remove after 5 minutes if not collected
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

            // Animation/attraction task
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

                        // Cache for counting how much coin they're getting over a short amount of time
                        UUID coinPlayerUUID = player.getUniqueId();
                        if (playerCurrencyPickup.containsKey(coinPlayerUUID))
                            playerCurrencyPickup.put(coinPlayerUUID, playerCurrencyPickup.get(coinPlayerUUID) + value);
                        else
                            playerCurrencyPickup.put(coinPlayerUUID, value);

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(
                                        EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                                                .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                                                .replace("$amount", Round.twoDecimalPlaces(playerCurrencyPickup.get(coinPlayerUUID)) + "")));
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
