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
    private static final double BASE_COOLDOWN = 30.0; // 30 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 3.0; // Triple damage

    private static final Set<UUID> playersOnCooldown = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public VorpalStrikeSkill() {
        super(SkillType.SWORDS, 75, "Vorpal Strike",
              "Critical hits can deal devastating bonus damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Reduce cooldown by 0.3% per level, min 15 seconds
        double reduction = 1.0 - (skillLevel * 0.003);
        return (long) Math.max(15.0, BASE_COOLDOWN * reduction);
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

    private double getDamageMultiplier(int skillLevel) {
        // Base 3x + 0.02x per level, capped at 4.0x
        return Math.min(4.0, BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02));
    }

    /**
     * Checks if this skill should proc on a critical hit.
     */
    public static boolean shouldProc(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!activePlayers.contains(player.getUniqueId())) return false;
        if (!event.isCriticalStrike()) return false;
        return !playersOnCooldown.contains(player.getUniqueId());
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
