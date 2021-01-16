package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SummonWolfEnchantment extends CustomEnchantment {

    public static final String key = "summon_wolf";

    public SummonWolfEnchantment() {
        super(key, false);
    }

    public static void summonWolf(Player player, ItemStack itemStack) {
        Wolf wolf;
        if (itemStack != null)
            itemStack.setAmount(itemStack.getAmount() - 1);
        if (ThreadLocalRandom.current().nextDouble() < 1D / 1000D)
            wolf = summonSnoopy(player);
        else
            wolf = summonGenericWolf(player);
        if (wolf == null) return;
        wolf.setAngry(false);
        wolf.setOwner(player);
    }

    private static Wolf summonGenericWolf(Player player) {
        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss("summonable_wolf.yml",
                player.getLocation(),
                ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true));
        if (customBossEntity.getLivingEntity().getType() != EntityType.WOLF) {
            new WarningMessage("snoopy.yml boss file was not set to a wolf entity type! It must be a wolf for the summon mechanic to work correctly!");
            return null;
        }
        return (Wolf) customBossEntity.getLivingEntity();
    }

    private static Wolf summonSnoopy(Player player) {
        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss("snoopy.yml",
                player.getLocation(),
                ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true));
        if (customBossEntity.getLivingEntity().getType() != EntityType.WOLF) {
            new WarningMessage("snoopy.yml boss file was not set to a wolf entity type! It must be a wolf for the summon mechanic to work correctly!");
            return null;
        }
        return (Wolf) customBossEntity.getLivingEntity();
    }

    private static int getEnchantment(ItemMeta itemMeta) {
        return ItemTagger.getEnchantment(itemMeta, key);
    }

    public static class SummonWolfEnchantmentEvent implements Listener {
        private static final ArrayList<Player> playerCooldowns = new ArrayList<>();

        @EventHandler(ignoreCancelled = true)
        public void onRightClick(PlayerInteractEvent event) {
            if (playerCooldowns.contains(event.getPlayer())) return;
            playerCooldowns.add(event.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    playerCooldowns.remove(event.getPlayer());
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60);
            if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
                return;
            if (getEnchantment(event.getPlayer().getInventory().getItemInMainHand().getItemMeta()) < 1)
                return;
            summonWolf(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
        }
    }

}
