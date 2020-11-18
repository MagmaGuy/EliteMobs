package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class SummonMerchantEnchantment extends CustomEnchantment implements Listener {

    public static String key = "summon_merchant";

    private static final String merchantMessage = EnchantmentsConfig.getEnchantment("summon_merchant.yml").getFileConfiguration().getString("message");

    public SummonMerchantEnchantment() {
        super(key, false);
    }

    /**
     * If the value is 0, it means that the enchantment isn't present
     */
    private static int getEnchantment(ItemMeta itemMeta) {
        return ItemTagger.getEnchantment(itemMeta, key);
    }

    /**
     * Summons the merchant
     *
     * @param player
     */
    public static void doSummonMerchant(Player player, boolean fromMessage, ItemStack itemStack) {
        if (itemStack != null)
            itemStack.setAmount(itemStack.getAmount() - 1);
        if (!fromMessage) {
            new NPCEntity(player.getLocation());
            if (!merchantMessage.isEmpty())
                player.chat(ChatColorConverter.convert(merchantMessage));
            return;
        }
        //pass to a sync task
        new BukkitRunnable() {
            @Override
            public void run() {
                new NPCEntity(player.getLocation());
            }
        }.runTask(MetadataHandler.PLUGIN);
    }

    public static class SummonMerchantEvents implements Listener {
        private static final ArrayList<Player> playerCooldowns = new ArrayList<>();

        @EventHandler
        public void onItemInteract(PlayerInteractEvent event) {
            if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
                return;
            if (getEnchantment(event.getPlayer().getInventory().getItemInMainHand().getItemMeta()) < 1)
                return;
            if (playerCooldowns.contains(event.getPlayer())) return;
            playerCooldowns.add(event.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    playerCooldowns.remove(event.getPlayer());
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60);
            doSummonMerchant(event.getPlayer(), false, event.getPlayer().getInventory().getItemInMainHand());
        }

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (merchantMessage.isEmpty()) return;
            if (event.getMessage().equalsIgnoreCase(merchantMessage)) {
                if (playerCooldowns.contains(event.getPlayer())) return;
                playerCooldowns.add(event.getPlayer());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerCooldowns.remove(event.getPlayer());
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60);
                for (ItemStack itemStack : event.getPlayer().getInventory())
                    if (itemStack != null)
                        if (getEnchantment(itemStack.getItemMeta()) > 0) {
                            doSummonMerchant(event.getPlayer(), true, itemStack);
                            return;
                        }
            }
        }
    }

}
