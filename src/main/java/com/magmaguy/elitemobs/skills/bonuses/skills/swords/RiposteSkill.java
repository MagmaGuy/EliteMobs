package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Riposte (COOLDOWN) - After blocking, your next attack deals bonus damage.
 * Has a cooldown between activations.
 * Tier 2 unlock.
 */
public class RiposteSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "swords_riposte";
    private static final double BASE_COOLDOWN = 10.0; // 10 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 1.83; // 83% bonus damage

    private static final Map<UUID, Long> cooldownEnds = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> readyEnds = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public RiposteSkill() {
        super(SkillType.SWORDS, 25, "Riposte",
              "After blocking an attack, your next attack deals bonus damage.",
              SkillBonusType.COOLDOWN, 2, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
        // Reduce cooldown by 0.5% per level, min 5 seconds
        double reduction = 1.0 - (skillLevel * 0.005);
        return (long) Math.max(5.0, BASE_COOLDOWN * reduction);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return getRemainingCooldown(player) > 0L;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        cooldownEnds.put(player.getUniqueId(), GameClock.getCurrentTick() + getCooldownSeconds(skillLevel) * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long end = cooldownEnds.get(player.getUniqueId());
        if (end == null) return 0L;
        long ticks = end - GameClock.getCurrentTick();
        if (ticks <= 0L) cooldownEnds.remove(player.getUniqueId(), end);
        return Math.max(0L, (ticks + 19L) / 20L);
    }

    @Override
    public void endCooldown(Player player) {
        cooldownEnds.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        // Riposte is triggered from the damage bonus integration, not the generic handler
        // Do nothing here - riposte ready check + damage is handled in onProc()
    }

    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        // Check if riposte is ready
        if (!hasRiposteReady(player.getUniqueId())) return;

        // Apply bonus damage
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double multiplier = getDamageMultiplier(skillLevel);
        event.setDamage(event.getDamage() * multiplier);

        // Consume riposte
        readyEnds.remove(player.getUniqueId());

        // Start cooldown
        startCooldown(player, skillLevel);
        incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, this);
    }

    /**
     * Called when a player successfully blocks an attack.
     */
    public static void onPlayerBlock(Player player) {
        UUID uuid = player.getUniqueId();
        if (!activePlayers.contains(uuid)) return;
        if (cooldownEnds.getOrDefault(uuid, 0L) > GameClock.getCurrentTick()) return;
        // Another block refreshes this player's one readiness window.
        readyEnds.put(uuid, GameClock.getCurrentTick() + 60L);
    }

    /**
     * Checks if a player has riposte ready.
     */
    public static boolean hasRiposteReady(UUID playerUUID) {
        Long end = readyEnds.get(playerUUID);
        if (end == null) return false;
        if (end > GameClock.getCurrentTick()) return true;
        readyEnds.remove(playerUUID, end);
        return false;
    }

    private double getDamageMultiplier(int skillLevel) {
        // Power budget: block-then-strike lands on roughly 15% of hits, which earns a 2.33x
        // hit at level 50 (E = 0.15 * 1.33 = 0.20). Base 83% bonus + 1% per level, capped at 3x.
        return scaled(BASE_DAMAGE_MULTIPLIER, 0.01, 3.0, skillLevel);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        cooldownEnds.remove(uuid);
        readyEnds.remove(uuid);
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
        double multiplier = (getDamageMultiplier(skillLevel) - 1) * 100;
        double cooldown = getCooldownSeconds(skillLevel);
        return applyLoreTemplates(Map.of(
                "damage", String.format("%.0f", multiplier),
                "cooldown", String.format("%.1f", cooldown)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("damage", String.format("%.0f", (getDamageMultiplier(skillLevel) - 1) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        // Riposte handles its own damage via onProc() when player blocks then attacks
        return false;
    }

    @Override
    public void shutdown() {
        cooldownEnds.clear();
        readyEnds.clear();
        activePlayers.clear();
    }
}
