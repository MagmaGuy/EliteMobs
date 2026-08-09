package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vorpal Strike (COOLDOWN) - Critical attacks have a chance to deal massive bonus damage.
 * Only triggers on critical hits.
 * Tier 4 unlock.
 */
public class VorpalStrikeSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "swords_vorpal_strike";
    private static final double BASE_COOLDOWN = 8.0; // 8 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 1.45; // 45% bonus damage

    private static final Set<UUID> playersOnCooldown = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public VorpalStrikeSkill() {
        super(SkillType.SWORDS, 75, "Vorpal Strike",
              "Critical hits can deal devastating bonus damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        // Reduce cooldown by 0.3% per level, min 4 seconds
        double reduction = 1.0 - (skillLevel * 0.003);
        return (long) Math.max(4.0, BASE_COOLDOWN * reduction);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return playersOnCooldown.contains(player.getUniqueId());
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        UUID uuid = player.getUniqueId();
        playersOnCooldown.add(uuid);
        long seconds = getCooldownSeconds(skillLevel);

        new BukkitRunnable() {
            @Override
            public void run() {
                playersOnCooldown.remove(uuid);
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendMessage(DungeonsConfig.getVorpalStrikeReadyMessage());
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, seconds * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        return 0; // Simplified
    }

    @Override
    public void endCooldown(Player player) {
        playersOnCooldown.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;

        // Only triggers on critical hits
        if (!damageEvent.isCriticalStrike()) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double multiplier = getDamageMultiplier(skillLevel);

        damageEvent.setDamage(damageEvent.getDamage() * multiplier);

        // Visual effects
        if (damageEvent.getEliteMobEntity().getLivingEntity() != null) {
            damageEvent.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                    Particle.CRIT,
                    damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0, 1, 0),
                    30, 0.5, 0.5, 0.5, 0.3
            );
        }

        startCooldown(player, skillLevel);
        incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, this);
    }

    /**
     * Power budget: the target rate for this skill is the standard proc band, so it is now a
     * frequent crit follow-up (a 1.75x hit at level 50, E = 0.267 * 0.75 = 0.20) rather than a
     * 25 second nuke. The cooldown above was cut to match; at the old 25s cooldown a 1.75x
     * payoff would have been strictly worse than any tier 1 skill.
     */
    private double getDamageMultiplier(int skillLevel) {
        // Base 1.45x + 0.006x per level, capped at 2.5x
        return scaled(BASE_DAMAGE_MULTIPLIER, 0.006, 2.5, skillLevel);
    }

    /**
     * Vorpal Strike only fires on critical hits, so the gate lives here: on a non-crit this
     * returns false without starting the cooldown, and the dispatcher consumes neither the
     * cooldown nor the proc feedback. On success the cooldown is started inside
     * {@link #onActivate(Player, Object)}, never by the dispatcher — the side-effect COOLDOWN
     * branch verifies the skill went on cooldown to detect that it actually fired.
     */
    @Override
    public boolean tryActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return false;
        if (!damageEvent.isCriticalStrike()) return false;
        if (isOnCooldown(player)) return false;
        onActivate(player, damageEvent);
        return true;
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        playersOnCooldown.remove(uuid);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double multiplier = getDamageMultiplier(skillLevel);
        double cooldown = getCooldownSeconds(skillLevel);
        return applyLoreTemplates(Map.of(
                "multiplier", String.format("%.1f", multiplier),
                "cooldown", String.format("%.1f", cooldown)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "multiplier", String.format("%.1f", getDamageMultiplier(skillLevel)),
                "cooldown", String.format("%d", getCooldownSeconds(skillLevel))));
    }

    @Override
    public boolean affectsDamage() {
        // Only triggers on critical hits via onProc, not via generic damage multiplier
        return false;
    }

    @Override
    public void shutdown() {
        playersOnCooldown.clear();
        activePlayers.clear();
    }
}
