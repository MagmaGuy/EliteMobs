package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.events.mobs.ZombieKing;
import com.magmaguy.elitemobs.utils.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class FlamethrowerEnchantment extends CustomEnchantment implements Listener {

    public String setKey() {
        return "FLAMETHROWER";
    }

    private static ArrayList<Player> playersUsingFlamethrower = new ArrayList<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)))
            return;

        if (playersUsingFlamethrower.contains(player)) return;

        ItemStack zombieKingAxe = player.getInventory().getItemInMainHand();

        if (hasCustomEnchantment(zombieKingAxe)) {

            if (zombieKingAxe.getDurability() + 4 > zombieKingAxe.getType().getMaxDurability()) {
                return;
            } else zombieKingAxe.setDurability((short) (zombieKingAxe.getDurability() + 4));

            ZombieKing.initializeFlamethrower(player.getLocation(), player.getLocation().getDirection(), player, true);
            Cooldown.initialize(playersUsingFlamethrower, player, 3 * 60);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 5));

        }

    }

}
