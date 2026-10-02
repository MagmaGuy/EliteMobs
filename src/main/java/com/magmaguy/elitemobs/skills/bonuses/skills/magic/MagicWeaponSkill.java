package com.magmaguy.elitemobs.skills.bonuses.skills.magic;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatEnemyAuthorization;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared lifecycle and combat helpers for staff and wand skills.
 * <p>
 * Staff and wand hits reach skills as {@link EliteMobDamagedByPlayerEvent#getMagicHit() magic hits}:
 * custom damage that still runs weapon skills. Everything a skill deals on top of that hit goes
 * through {@link #sideDamage}, which cannot trigger skills again.
 */
public abstract class MagicWeaponSkill extends SkillBonus {
    private final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> cooldownEnds = new ConcurrentHashMap<>();

    protected MagicWeaponSkill(SkillType skillType, int unlockTier, String name, String description,
                               SkillBonusType bonusType, String skillId) {
        super(skillType, getLevelForTier(unlockTier), name, description, bonusType, unlockTier, skillId);
    }

    @Override public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override public void removeBonus(Player player) { deactivate(player); }
    @Override public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override public void onDeactivate(Player player) { deactivate(player); }
    @Override public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    private void deactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        clearPlayer(player.getUniqueId());
    }

    /** Drops per-player combat state when the skill is deactivated. Cooldowns are kept on purpose. */
    protected void clearPlayer(UUID playerId) {
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldownEnds.clear();
    }

    protected int skillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, skillType);
    }

    /** The hit in this context when it is the primary hit of the given staff or wand attack. */
    protected static EliteMobDamagedByPlayerEvent strike(Object context, MagicStrike strike) {
        return context instanceof EliteMobDamagedByPlayerEvent event && event.getMagicStrike() == strike ? event : null;
    }

    /** True while the player holds this skill's weapon with the skill active and unlocked. */
    protected boolean wieldedBy(Player player) {
        if (!isEnabled() || !isActive(player)) return false;
        if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) != skillType) return false;
        return meetsLevelRequirement(skillLevel(player));
    }

    /** Elites a magic weapon may hit within a radius of a point, nearest first. */
    protected static List<LivingEntity> enemiesNear(Player player, Location center, double radius, LivingEntity exclude) {
        World world = center.getWorld();
        if (world == null) return List.of();
        List<LivingEntity> enemies = new ArrayList<>();
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || living.equals(exclude) || living.isDead()) continue;
            if (living.getLocation().distanceSquared(center) > radius * radius) continue;
            EliteEntity elite = EntityTracker.getEliteMobEntity(living);
            if (elite == null || !elite.isValid()) continue;
            if (!AdvancedCombatEnemyAuthorization.canTargetWithMagicWeapon(player, living)) continue;
            enemies.add(living);
        }
        enemies.sort(Comparator.comparingDouble(living -> living.getLocation().distanceSquared(center)));
        return enemies;
    }

    /** Skill damage on top of the weapon's hit. It cannot trigger weapon skills again. */
    protected static void sideDamage(Player player, LivingEntity target, double damage) {
        if (!player.isOnline() || target.isDead() || !target.isValid() || !Double.isFinite(damage) || damage <= 0) return;
        CombatDamageContext.runPlayerToEliteBypass(() -> target.damage(damage, player));
    }

    protected static Location chest(LivingEntity entity) {
        return entity.getLocation().add(0, entity.getHeight() * 0.6, 0);
    }

    /** A straight particle line, capped so long beams stay cheap. */
    protected static void beam(Location from, Location to, Particle particle) {
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) return;
        Vector step = to.toVector().subtract(from.toVector());
        double length = step.length();
        if (length < 0.01) return;
        int points = (int) Math.min(40, Math.ceil(length / 0.4));
        step.multiply(1D / points);
        Location point = from.clone();
        for (int i = 0; i <= points; i++) {
            world.spawnParticle(particle, point, 1, 0, 0, 0, 0);
            point.add(step);
        }
    }

    // Cooldown storage for the skills that implement CooldownSkill.

    public long getCooldownSeconds(int skillLevel) {
        return 0;
    }

    public boolean isOnCooldown(Player player) {
        Long end = cooldownEnds.get(player.getUniqueId());
        if (end == null) return false;
        if (System.currentTimeMillis() < end) return true;
        cooldownEnds.remove(player.getUniqueId());
        return false;
    }

    public void startCooldown(Player player, int skillLevel) {
        cooldownEnds.put(player.getUniqueId(), System.currentTimeMillis() + getCooldownSeconds(skillLevel) * 1000L);
    }

    public long getRemainingCooldown(Player player) {
        Long end = cooldownEnds.get(player.getUniqueId());
        return end == null ? 0 : Math.max(0, (end - System.currentTimeMillis()) / 1000);
    }

    public void endCooldown(Player player) {
        cooldownEnds.remove(player.getUniqueId());
    }
}
