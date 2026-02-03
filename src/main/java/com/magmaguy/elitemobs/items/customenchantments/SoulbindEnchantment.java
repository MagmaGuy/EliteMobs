package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.SoulbindConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;


public class SoulbindEnchantment extends CustomEnchantment {

    public static final NamespacedKey PRESTIGE_KEY = new NamespacedKey(MetadataHandler.PLUGIN, "prestige");
    public static String key = "soulbind";
    public static final NamespacedKey SOULBIND_KEY = new NamespacedKey(MetadataHandler.PLUGIN, key);
    public static boolean isEnabled;

    public SoulbindEnchantment() {
        super(key, true);
    }

    public static ItemStack addEnchantment(ItemStack itemStack, Player player) {
        isEnabled = EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled();
        if (!isEnabled) return null;
        if (itemStack == null || player == null) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(SOULBIND_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
        setPrestigeLevel(itemMeta, 0);
        itemStack.setItemMeta(itemMeta);
        new EliteItemLore(itemStack, true);
        return itemStack;
    }

    public static void removeEnchantment(ItemStack itemStack) {
        if (itemStack == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().remove(SOULBIND_KEY);
        itemStack.setItemMeta(itemMeta);
        //update item lore display
        new EliteItemLore(itemStack, false);
    }

    public static void addPhysicalDisplay(Item item, Player player) {
        if (player == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (item == null)
                    return;
                FakeText fakeText = VisualDisplay.generateFakeText(item.getLocation().clone().add(new Vector(0, -50, 0)), ChatColorConverter.convert(
                        SoulbindConfig.hologramStrings.replace("$playerName", player.getName()).replace("$player", player.getDisplayName())), 30);
                if (fakeText == null) return;

                new BukkitRunnable() {
                    final Location lastLocation = item.getLocation().clone();
                    int counter = 0;

                    @Override
                    public void run() {
                        counter++;
                        if (counter > 20 * 60 * 5 || !item.isValid()) {
                            cancel();
                            fakeText.remove();
                            return;
                        }
                        if (!lastLocation.equals(item.getLocation()))
                            fakeText.teleport(item.getLocation().clone().add(new Vector(0, 0.5, 0)));
                        if (counter == 1)
                            fakeText.teleport(item.getLocation().clone().add(new Vector(0, 0.5, 0)));
                    }
                }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);
    }

    public static boolean isValidSoulbindUser(ItemMeta itemMeta, Player player) {
        if (!isEnabled) return true;
        if (player.hasPermission("elitemobs.soulbind.bypass")) return true;
        if (itemMeta == null)
            return true;
        if (!itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING))
            return true;
        return itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING).equals(player.getUniqueId().toString());
    }

    public static boolean isSoulboundItem(ItemMeta itemMeta) {
        if (!isEnabled) return false;
        if (itemMeta == null)
            return false;
        return !itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING);
    }

    public static Player getSoulboundPlayer(ItemMeta itemMeta) {
        if (!itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING))
            return null;
        try {
            UUID uuid = UUID.fromString(itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING));
            return Bukkit.getPlayer(uuid);
        } catch (Exception ex) {

        }
        return null;
        //return Bukkit.getPlayer(UUID.fromString(itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING)));
    }

    public static boolean itemHasSoulbindEnchantment(ItemMeta itemMeta) {
        return itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING);
    }

    private static void setPrestigeLevel(ItemMeta itemMeta, int prestigeLevel) {
        itemMeta.getPersistentDataContainer().set(PRESTIGE_KEY, PersistentDataType.INTEGER, prestigeLevel);
    }

    public static int getPrestigeLevel(ItemMeta itemMeta) {
        if (!itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING))
            return 0;
        int prestige = 0;
        if (itemMeta.getPersistentDataContainer().get(PRESTIGE_KEY, PersistentDataType.INTEGER) != null)
            prestige = itemMeta.getPersistentDataContainer().get(PRESTIGE_KEY, PersistentDataType.INTEGER);
        return prestige;
    }

    public static class SoulbindEnchantmentEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPickup(PlayerPickupItemEvent event) {
            if (isValidSoulbindUser(event.getItem().getItemStack().getItemMeta(), event.getPlayer())) return;
            event.setCancelled(true);
        }
    }

}
