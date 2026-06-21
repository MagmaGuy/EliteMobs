package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

/**
 * Prevents players from taming EliteMob entities (e.g. WOLF-type bosses such as Snoopy).
 * <p>
 * Wolf-type elites are kept angry/targeted by {@link KeepNeutralsAngry}, but vanilla still
 * allows a player to tame a Wolf with a bone during the brief window in which the wolf has no
 * target (e.g. right after it kills its current target, before {@code KeepNeutralsAngry}'s
 * 1-second poll re-asserts a target). Taming fires {@link EntityTameEvent}, which nothing else
 * cancels, so the boss would become a player pet.
 * <p>
 * This is safe for the Summon Wolf enchantment: summoned wolves are made owned via
 * {@code Wolf.setOwner(...)}, which does NOT fire {@link EntityTameEvent}. Only the
 * player-feeds-a-bone tame path fires this event, which is exactly what we want to block.
 */
public class TamePrevention implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        if (!EntityTracker.isEliteMob(event.getEntity())) return;
        event.setCancelled(true);
        //Re-assert anger so the neutral window that allowed the tame attempt is closed immediately
        if (event.getEntity() instanceof Wolf wolf)
            wolf.setAngry(true);
    }
}
