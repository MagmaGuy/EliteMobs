package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.utils.VisualArmorStand;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
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

import java.util.ArrayList;
import java.util.List;

public class SoulbindEnchantment extends CustomEnchantment {

    public static String key = "soulbind";

    public SoulbindEnchantment() {
        super(key);
    }

    public static void addEnchantment(Item item, Player player) {
        if (!EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled()) return;
        if (item == null) return;
        addEnchantment(item.getItemStack(), player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isValid())
                    return;

                ArmorStand soulboundPlayer = VisualArmorStand.VisualArmorStand(item.getLocation().clone().add(new Vector(0, -50, 0)), ChatColorConverter.convert(
                        EnchantmentsConfig.getEnchantment("soulbind.yml")
                                .getFileConfiguration().getString("hologramString").replace("$player", player.getDisplayName())));
                new BukkitRunnable() {
                    int counter = 0;
                    final Location lastLocation = item.getLocation().clone();

                    @Override
                    public void run() {
                        counter++;
                        if (counter > 20 * 60 * 5 || !item.isValid()) {
                            cancel();
                            EntityTracker.unregisterArmorStand(soulboundPlayer);
                            return;
                        }
                        if (!lastLocation.equals(item.getLocation()))
                            soulboundPlayer.teleport(item.getLocation().clone().add(new Vector(0, 0.5, 0)));
                        if (counter == 1)
                            soulboundPlayer.teleport(item.getLocation().clone().add(new Vector(0, 0.5, 0)));
                    }
                }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

    }

    private static final NamespacedKey namespacedKey = new NamespacedKey(MetadataHandler.PLUGIN, key);

    private static void addEnchantment(ItemStack itemStack, Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        //for legacy reasons, prestige 0 gets no prestige tier id
        if (GuildRank.getGuildPrestigeRank(player) == 0)
            itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, player.getUniqueId().toString());
        else
            itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, player.getUniqueId().toString() + GuildRank.getGuildPrestigeRank(player));
        itemStack.setItemMeta(itemMeta);
        List<String> lore = itemStack.getItemMeta().getLore();
        if (lore == null)
            lore = new ArrayList<>();
        lore.add(
                ChatColorConverter.convert(
                        EnchantmentsConfig.getEnchantment("soulbind.yml")
                                .getFileConfiguration().getString("loreString").replace("$player", player.getDisplayName())));
        if (GuildRank.getGuildPrestigeRank(player) > 0)
            lore.add(ChatColorConverter.convert("Prestige " + GuildRank.getGuildPrestigeRank(player)));

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    public static boolean isValidSoulbindUser(ItemMeta itemMeta, Player player) {
        if (itemMeta == null)
            return true;
        //for legacy items
        //if (GuildRank.getGuildPrestigeRank(player) == 0)
        //    if (itemMeta.getCustomTagContainer().hasCustomTag(namespacedKey, ItemTagType.STRING))
        //        return itemMeta.getCustomTagContainer().getCustomTag(new NamespacedKey(MetadataHandler.PLUGIN, key), ItemTagType.STRING).equals(player.getUniqueId().toString());
        if (!itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING))
            return true;
        if (GuildRank.getGuildPrestigeRank(player) == 0)
            return itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING).equals(player.getUniqueId().toString());
        else
            return (itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING)).equals(player.getUniqueId().toString() + GuildRank.getGuildPrestigeRank(player));
    }

    public static class SoulbindEnchantmentEvents implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPickup(PlayerPickupItemEvent event) {
            if (isValidSoulbindUser(event.getItem().getItemStack().getItemMeta(), event.getPlayer())) return;
            event.setCancelled(true);
        }
    }

}
