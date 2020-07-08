package com.magmaguy.elitemobs.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class AdvancedAggroManager implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEliteMobEntity().hasDamagers()) return;
        if (event.getEliteMobEntity().getDamagers().size() < 2) return;
        if (!(event.getEliteMobEntity().getLivingEntity() instanceof Mob)) return;

        ArrayList<Player> nearbyPlayers = new ArrayList<>();
        for (Entity entity : event.getEliteMobEntity().getLivingEntity().getNearbyEntities(35, 35, 35))
            if (entity.getType().equals(EntityType.PLAYER))
                nearbyPlayers.add((Player) entity);

        Player player = null;
        double damage = 0;
        for (Player iteratedPlayer : event.getEliteMobEntity().getDamagers().keySet()) {
            if (event.getEliteMobEntity().getDamagers().get(iteratedPlayer) > damage &&
                    nearbyPlayers.contains(iteratedPlayer)) {
                player = iteratedPlayer;
                damage = event.getEliteMobEntity().getDamagers().get(iteratedPlayer);
            }
        }

        if (player != null) {
            if (((Mob) event.getEliteMobEntity().getLivingEntity()).getTarget() == null ||
                    !((Mob) event.getEliteMobEntity().getLivingEntity()).getTarget().getUniqueId().equals(player.getUniqueId())) {
                ((Mob) event.getEliteMobEntity().getLivingEntity()).setTarget(player);
            }

        }

    }

}
