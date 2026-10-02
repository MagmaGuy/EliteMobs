package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Death's Embrace (COOLDOWN) - Cheat death once per cooldown.
 * A fatal hit from an elite while holding a hoe is cancelled and leaves you at 10% health.
 * Tier 4 unlock.
 * <p>
 * Balance is hardcoded; the config only supplies presentation. The old config curve saved the
 * player every 30-38 seconds at 20% health and added +250% hoe damage at level 75 (a baseValue of
 * 1.0 read as a bonus fraction). Saves now sit beside Last Stand (1 HP, 120s) and Divine Shield
 * (120s, 60s floor): 10% health, 90s at unlock and 80s at level 100.
 */
public class DeathsEmbraceSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "hoes_deaths_embrace";
    private static final long BASE_COOLDOWN = 120; // 120 seconds, minus 0.4s per level
    private static final long MINIMUM_COOLDOWN = 60;
    private static final double HEAL_PERCENT = 0.10; // Heal to 10% HP
    private static final double BASE_PASSIVE_BONUS = 0.05; // 5% passive damage

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public DeathsEmbraceSkill() {
        super(SkillType.HOES, 75, "Death's Embrace",
              "Cheat death once per cooldown, reviving with 10% health.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        long reduction = (long) (skillLevel * 0.4);
        return Math.max(MINIMUM_COOLDOWN, BASE_COOLDOWN - reduction);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return false;

        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000;
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return 0;

        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Attempts to prevent death for the player.
     * Returns true if death was prevented, false if on cooldown.
     */
    public static boolean preventDeath(Player player) {
        if (!activePlayers.contains(player.getUniqueId())) return false;

        // Only works when holding a hoe
        String mainHandName = player.getInventory().getItemInMainHand().getType().name();
        if (!mainHandName.endsWith("_HOE")) return false;

        // Get the instance from registry
        SkillBonus skill = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getSkillById(SKILL_ID);
        if (!(skill instanceof DeathsEmbraceSkill deathsEmbrace)) return false;

        if (deathsEmbrace.isOnCooldown(player)) {
            return false;
        }

        int skillLevel = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry
            .getPlayerSkillLevel(player, SkillType.HOES);

        // Prevent death and heal
        player.setHealth(player.getMaxHealth() * HEAL_PERCENT);

        // Visual and sound effects
        player.getWorld().spawnParticle(Particle.SOUL,
            player.getLocation(), 50, 1, 1, 1, 0.1);
        player.getWorld().playSound(player.getLocation(),
            Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);

        // Start cooldown
        deathsEmbrace.startCooldown(player, skillLevel);
        deathsEmbrace.incrementProcCount(player);

        return true;
    }

    public double getPassiveDamageBonus(int skillLevel) {
        return scaled(BASE_PASSIVE_BONUS, 0.001, skillLevel); // +12.5% at unlock, +15% at level 100
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        endCooldown(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        endCooldown(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "healPercent", String.format("%.0f", HEAL_PERCENT * 100),
                "passiveBonus", String.format("%.1f", getPassiveDamageBonus(skillLevel) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getPassiveDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "passiveBonus", String.format("%.1f", getPassiveDamageBonus(skillLevel) * 100),
                "healPercent", String.format("%.0f", HEAL_PERCENT * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public boolean affectsDamage() {
        return false; // Passive damage is separate from the defensive cooldown activation.
    }

    @Override
    public boolean triggersOnOffensiveHit() {
        return false;
    }

    @Override
    public void shutdown() {
        cooldowns.clear();
        activePlayers.clear();
    }
}
