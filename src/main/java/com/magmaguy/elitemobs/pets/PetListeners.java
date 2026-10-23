package com.magmaguy.elitemobs.pets;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.config.PetsConfig;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityRightClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/** Routes clicks, damage, deaths and player sessions to {@link PetManager}. */
public final class PetListeners implements Listener {
    private static PetManager manager() {
        return PetManager.get();
    }

    private static Player attacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event instanceof PlayerInteractAtEntityEvent)) click(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractAt(PlayerInteractAtEntityEvent event) {
        click(event);
    }

    /**
     * A right-click on a pet body. Whatever the manager makes of it, the vanilla mob underneath never gets the click:
     * an allay would pocket the food it was offered. A lead still goes through.
     */
    private static void click(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        PetManager manager = manager();
        if (manager == null) return;
        PetControl pet = manager.pet(event.getRightClicked());
        if (pet == null) return;
        boolean consumed = manager.interact(event.getPlayer(), pet);
        if (consumed || event.getPlayer().getInventory().getItemInMainHand().getType() != Material.LEAD) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        PetManager manager = manager();
        if (manager == null) return;
        PetControl pet = manager.pet(event.getEntity());
        if (pet == null || pet.isWild()) return;
        Player attacker = attacker(event.getDamager());
        if (attacker == null) return;
        // Owners never hurt their own pets; in COSMETIC mode nobody does.
        if (pet.isOwnedBy(attacker) || PetsConfig.getMode() == PetsConfig.Mode.COSMETIC || PetsConfig.isPetsInvulnerableToPlayers())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTargeted(EntityTargetLivingEntityEvent event) {
        PetManager manager = manager();
        if (manager == null || event.getTarget() == null) return;
        // The owner's own pet is never a target for the owner's other pets or for pets as such.
        if (manager.pet(event.getEntity()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        PetManager manager = manager();
        if (manager == null) return;
        // Breeding is the manager's, from the species' food, not the vanilla animal's.
        if (manager.pet(event.getMother()) != null || manager.pet(event.getFather()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEliteDeath(EliteMobDeathEvent event) {
        PetManager manager = manager();
        if (manager == null) return;
        PetControl pet = manager.pet(event.getEliteEntity());
        if (pet != null) manager.bodyGone(pet, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteRemove(EliteMobRemoveEvent event) {
        PetManager manager = manager();
        if (manager == null) return;
        PetControl pet = manager.pet(event.getEliteMobEntity());
        if (pet != null) manager.bodyGone(pet, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        PetManager manager = manager();
        if (manager != null) manager.playerJoined(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        PetManager manager = manager();
        if (manager != null) manager.playerLeft(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // The watchdog respawns equipped pets in the new world; bodies left behind are removed now.
        PetManager manager = manager();
        if (manager == null) return;
        for (PetControl control : new java.util.ArrayList<>(manager.pets()))
            if (control.isOwnedBy(event.getPlayer())) manager.despawn(control, com.magmaguy.elitemobs.api.internal.RemovalReason.OTHER);
    }

    /** Clicks on a pet's FreeMinecraftModels model, which hides the body from clients. Registered only with FMM. */
    public static final class ModelClicks implements Listener {
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onModelRightClick(ModeledEntityRightClickEvent event) {
            PetManager manager = manager();
            if (manager == null) return;
            Entity body = event.getEntity().getUnderlyingEntity();
            PetControl pet = body == null ? null : manager.pet(body);
            if (pet != null && manager.interact(event.getPlayer(), pet)) event.setCancelled(true);
        }
    }
}
