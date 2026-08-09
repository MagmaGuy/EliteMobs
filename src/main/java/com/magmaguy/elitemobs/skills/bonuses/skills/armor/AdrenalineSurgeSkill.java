package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tier 2 ARMOR skill - Adrenaline Surge
 * Grants buffs when health drops below threshold
 */
public class AdrenalineSurgeSkill extends SkillBonus implements CooldownSkill {

    /**
     * Strength and length of the burst of buffs.
     * <p>
     * Pinned to the values the old config scaling produced at the reference skill level of 50, so
     * nothing changes at that level — this removes balance config from the skill and stops the
     * amplifier climbing to Strength XI at very high levels.
     */
    private static final int BUFF_AMPLIFIER = 2;
    private static final int BUFF_DURATION_TICKS = 180; // 9 seconds

    /**
     * Damage reduction granted for the duration of the surge.
     * <p>
     * This used to be a {@link PotionEffectType#RESISTANCE} effect. That put it outside the reach
     * of {@code PlayerDamagedByEliteMobEvent.MAX_AGGREGATE_DEFENSIVE_REDUCTION}: potion resistance
     * is applied by {@code PotionCombatModifierCalculator} at step 6 of the damage formula, before
     * the event is even constructed, so it was folded into the <i>incoming</i> damage the ceiling
     * measures against. The ceiling then capped the skill pipeline at 85% of an already reduced
     * number, and a full defensive stack reached ~88% total reduction instead of 85%.
     * <p>
     * 0.20 is exactly what RESISTANCE I was worth at the stock {@code resistanceDamageMultiplierV2}
     * of 0.2, so at default config this is a containment fix with no intended power change — the
     * reduction is simply applied inside the skill pipeline now, where the aggregate ceiling can
     * see it. Resistance a player drinks (or gets from any other source) is untouched: this skill
     * no longer grants the potion effect at all, so the potion path keeps working normally for
     * everything that legitimately uses it.
     * <p>
     * Sustained value {@code E = uptime * reduction = (9 / 45) * 0.20 = 0.04}, comfortably inside
     * the 0.20 defensive budget and deliberately so: the surge also hands out Strength III and
     * Speed III, and it only fires below 30% health, so its real duty cycle is well under 9s/45s.
     */
    private static final double SURGE_DAMAGE_REDUCTION = 0.20;

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
    /**
     * Player UUID to the epoch millisecond the surge's damage reduction stops applying.
     * Mirrors the lifetime the RESISTANCE potion effect used to have.
     */
    private static final Map<UUID, Long> surgeActiveMap = new ConcurrentHashMap<>();
    private static final double HEALTH_THRESHOLD = 0.30; // 30% health

    public AdrenalineSurgeSkill() {
        super(
            SkillType.ARMOR,
            25,
            "Adrenaline Surge",
            "Gain buffs when health drops below 30%",
            SkillBonusType.COOLDOWN,
            2,
            "armor_adrenaline_surge"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        // The surge now carries a damage reduction, so an in-flight window has to be closed when
        // the skill is unslotted - otherwise it would keep reducing damage for a skill the player
        // no longer has.
        surgeActiveMap.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        cooldownMap.remove(player.getUniqueId());
        surgeActiveMap.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "duration", String.format("%.1fs", getDuration(skillLevel) / 20.0),
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    /**
     * The defensive quantity this skill contributes, matching every other armor skill:
     * the damage reduction applied while the surge is up.
     * <p>
     * This used to report the potion level granted (3, for amplifier 2). That was a display value
     * masquerading as a bonus value, and it read as a 300% reduction to anything that treats
     * {@code getBonusValue} as a reduction the way the generic defensive handler does.
     */
    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageReduction(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "cooldown", String.format("%ds", getCooldownSeconds(skillLevel))));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldownMap.clear();
        surgeActiveMap.clear();
    }

    // CooldownSkill interface methods

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        // Base 45 seconds cooldown
        return 45;
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long cooldownEnd = cooldownMap.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return false;
        }
        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldownMap.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000L;
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long cooldownEnd = cooldownMap.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        int skillLevel = getPlayerSkillLevel(player);

        // Grant adrenaline buffs
        int duration = getDuration(skillLevel);
        int amplifier = configFields == null
                ? BUFF_AMPLIFIER
                : Math.max(0, (int) Math.floor(configFields.calculateValue(skillLevel)));

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amplifier));
        // No RESISTANCE effect: see SURGE_DAMAGE_REDUCTION. The reduction is opened as a timed
        // window here and applied inside the skill pipeline, where the aggregate defensive ceiling
        // can bound it.
        surgeActiveMap.put(player.getUniqueId(), System.currentTimeMillis() + duration * 50L);

        // Visual effect
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
            player.getLocation(), 10, 0.5, 0.5, 0.5, 0);

        // Start cooldown
        startCooldown(player, skillLevel);
    }

    /**
     * Whether the surge's damage reduction window is currently open for a player.
     *
     * @param player The player to check
     * @return true while the surge is running
     */
    public boolean isSurgeActive(Player player) {
        Long activeUntil = surgeActiveMap.get(player.getUniqueId());
        if (activeUntil == null) return false;
        if (System.currentTimeMillis() > activeUntil) {
            surgeActiveMap.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    /**
     * The damage reduction the surge applies while it is running.
     * Clamped so this path can never invert the {@code damage * (1 - reduction)} formula.
     *
     * @param skillLevel The player's skill level
     * @return The reduction, in the range [0, {@link SkillBonus#MAX_DEFENSIVE_REDUCTION}]
     */
    public double getDamageReduction(int skillLevel) {
        return clampDefensiveReduction(SURGE_DAMAGE_REDUCTION);
    }

    /**
     * Applies the surge's damage reduction to an incoming hit.
     * Called from the armor skill pipeline in
     * {@code PlayerDamagedByEliteMobEvent.applyDefensiveSkillBonuses()} so the result is covered by
     * {@code PlayerDamagedByEliteMobEvent.MAX_AGGREGATE_DEFENSIVE_REDUCTION}.
     *
     * @param player         The player taking damage
     * @param originalDamage The damage as it stands at this point in the pipeline
     * @return The reduced damage, or the original if the surge is not running
     */
    public double modifyIncomingDamage(Player player, double originalDamage) {
        if (!isSurgeActive(player)) return originalDamage;
        return originalDamage * (1 - getDamageReduction(getPlayerSkillLevel(player)));
    }

    /**
     * Checks if the skill should trigger based on health threshold.
     * Called from damage event handler.
     *
     * @param player The player taking damage
     * @param newHealthPercent The player's new health percentage (0.0 to 1.0)
     */
    public void checkTrigger(Player player, double newHealthPercent) {
        if (!isActive(player) || isOnCooldown(player)) {
            return;
        }

        if (newHealthPercent <= HEALTH_THRESHOLD) {
            onActivate(player, null);
            incrementProcCount(player);
        }
    }

    /**
     * Gets the buff duration in ticks.
     *
     * @param skillLevel The player's skill level
     * @return Duration in ticks
     */
    private int getDuration(int skillLevel) {
        if (configFields == null) return BUFF_DURATION_TICKS;
        return 100 + (int) (configFields.calculateValue(skillLevel) * 40);
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
