package com.magmaguy.elitemobs.pets;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * The live state of one pet body: who owns it, whether it is told to stay, where it idles, and the timers taming,
 * breeding and growing up run on. Owned by {@link PetManager}; the Mind behaviors read it every tick on the server
 * thread, so nothing here needs synchronization.
 */
public final class PetControl {
    private final PetSpecies species;
    private final UUID owner;
    private final PetRecord record;
    private final String model;
    private final boolean baby;
    private CustomBossEntity actor;
    private boolean stay;
    private Location stayAnchor;
    // Where a wild pet drifts around; an owned pet drifts around its owner instead.
    private Location home;
    private long loveUntilTick;
    private long breedCooldownUntilTick;
    private long lastInteractionTick;

    PetControl(PetSpecies species, UUID owner, PetRecord record, String model, boolean baby, Location home) {
        this.species = species;
        this.owner = owner;
        this.record = record;
        this.model = model;
        this.baby = baby;
        this.home = home == null ? null : home.clone();
    }

    public PetSpecies species() {
        return species;
    }

    /** The owning player's id, or null for a wild pet. */
    public UUID ownerId() {
        return owner;
    }

    public boolean isWild() {
        return owner == null;
    }

    public boolean isOwnedBy(Player player) {
        return owner != null && owner.equals(player.getUniqueId());
    }

    public Optional<Player> owner() {
        if (owner == null) return Optional.empty();
        Player player = Bukkit.getPlayer(owner);
        return player != null && player.isOnline() && !player.isDead() ? Optional.of(player) : Optional.empty();
    }

    /** The collection entry behind an owned pet; null for a wild one. */
    public PetRecord record() {
        return record;
    }

    public String model() {
        return model;
    }

    public boolean isBaby() {
        return baby;
    }

    public CustomBossEntity actor() {
        return actor;
    }

    void setActor(CustomBossEntity actor) {
        this.actor = actor;
    }

    public LivingEntity body() {
        return actor == null ? null : actor.getLivingEntity();
    }

    public boolean isStaying() {
        return stay;
    }

    public void setStay(boolean stay, Location at) {
        this.stay = stay;
        this.stayAnchor = stay && at != null ? at.clone() : null;
    }

    public Location stayAnchor() {
        return stayAnchor == null ? null : stayAnchor.clone();
    }

    public Location home() {
        return home == null ? null : home.clone();
    }

    void setHome(Location home) {
        this.home = home == null ? null : home.clone();
    }

    /** Distance to the owner, or infinity when the owner is away or in another world. */
    public double ownerDistance(LivingEntity body) {
        Player player = owner().orElse(null);
        if (player == null || body == null || player.getWorld() != body.getWorld()) return Double.POSITIVE_INFINITY;
        return player.getLocation().distance(body.getLocation());
    }

    public boolean isInLove(long tick) {
        return tick < loveUntilTick;
    }

    public void setLoveUntil(long tick) {
        this.loveUntilTick = tick;
    }

    public void endLove() {
        this.loveUntilTick = 0;
    }

    public boolean canBreed(long tick) {
        return !baby && tick >= breedCooldownUntilTick;
    }

    public void setBreedCooldownUntil(long tick) {
        this.breedCooldownUntilTick = tick;
    }

    public long lastInteractionTick() {
        return lastInteractionTick;
    }

    public void setLastInteractionTick(long tick) {
        this.lastInteractionTick = tick;
    }
}
