package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swift Strikes (PASSIVE) - Increases movement speed while wielding a sword.
 * Always active when selected.
 * Tier 1 unlock.
 */
public class SwiftStrikesSkill extends SkillBonus {

    public static final String SKILL_ID = "swords_swift_strikes";
    private static final double BASE_SPEED_BONUS = 0.05; // 5% movement speed

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, SpeedLease> appliedSpeeds = new ConcurrentHashMap<>();

    public SwiftStrikesSkill() {
        super(SkillType.SWORDS, 10, "Swift Strikes",
              "Move faster while wielding a sword.",
              SkillBonusType.PASSIVE, 1, SKILL_ID);
    }

    /**
     * Gets the movement speed bonus for a skill level.
     */
    public static double getSpeedBonus(int skillLevel) {
        // Base 5% + 0.1% per level, max 15%
        return scaled(BASE_SPEED_BONUS, 0.001, 0.15, skillLevel);
    }

    /**
     * Checks if a player has this skill active.
     */
    public static boolean hasActiveSkill(UUID playerUUID) {
        return activePlayers.contains(playerUUID);
    }

    /**
     * Applies the speed bonus to the player's walk speed attribute.
     */
    public static void applySpeedBonus(Player player, int skillLevel) {
        if (!hasActiveSkill(player.getUniqueId())) return;
        SpeedLease current = appliedSpeeds.get(player.getUniqueId());
        float actual = player.getWalkSpeed();
        float baseline = current != null && current.player() == player
                && Float.compare(actual, current.applied()) == 0 ? current.baseline() : actual;
        float applied = Math.min(1.0f, baseline + (float) getSpeedBonus(skillLevel));
        if (Float.compare(actual, applied) != 0) player.setWalkSpeed(applied);
        appliedSpeeds.put(player.getUniqueId(), new SpeedLease(player, baseline, applied));
    }

    /**
     * Restores this activation's baseline only while its write still owns the value.
     */
    public static void removeSpeedBonus(Player player) {
        SpeedLease lease = appliedSpeeds.get(player.getUniqueId());
        if (lease == null || lease.player() != player) return;
        appliedSpeeds.remove(player.getUniqueId(), lease);
        if (Float.compare(player.getWalkSpeed(), lease.applied()) == 0)
            player.setWalkSpeed(lease.baseline());
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
        if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) == SkillType.SWORDS)
            applySpeedBonus(player, skillLevel);
        else removeSpeedBonus(player);
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeSpeedBonus(player);
    }

    @Override
    public void onActivate(Player player) {
        applyBonus(player, SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS));
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        removeSpeedBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double speed = getSpeedBonus(skillLevel) * 100;
        return applyLoreTemplates(Map.of("value", String.format("%.1f", speed)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getSpeedBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getSpeedBonus(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // Movement speed skill doesn't affect damage
    }

    @Override
    public TestStrategy getTestStrategy() {
        return TestStrategy.ATTRIBUTE_CHECK;
    }

    @Override
    public void shutdown() {
        for (SpeedLease lease : List.copyOf(appliedSpeeds.values())) removeSpeedBonus(lease.player());
        activePlayers.clear();
    }

    private record SpeedLease(Player player, float baseline, float applied) {}
}
