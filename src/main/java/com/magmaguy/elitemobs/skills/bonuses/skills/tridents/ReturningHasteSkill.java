package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Returning Haste (STACKING) - Build stacks with consecutive trident throws.
 * Each stack increases damage. Stacks decay after 5 seconds of no throws.
 * Tier 3 unlock.
 */
public class ReturningHasteSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "tridents_returning_haste";
    private static final int BASE_MAX_STACKS = 5;
    /**
     * Stacks fall off after 3s without a throw. The old 5s window was longer than a
     * throw-and-return cycle, so the ramp never decayed and the bonus sat at max permanently.
     */
    private static final long STACK_DECAY_TIME = 3000;
    private static final double BASE_BONUS_PER_STACK = 0.05;

    // Track player stacks: PlayerUUID -> Stack count
    private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
    // Track last throw time: PlayerUUID -> Timestamp
    private static final Map<UUID, Long> lastThrowTime = new ConcurrentHashMap<>();
    // Track which players have this skill active
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    public ReturningHasteSkill() {
        super(SkillType.TRIDENTS, 50, "Returning Haste",
              "Build stacks with consecutive trident throws. Each stack increases damage.",
              SkillBonusType.STACKING, 3, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        if (configFields != null && configFields.getMaxStacks() > 0) return configFields.getMaxStacks();
        return BASE_MAX_STACKS;
    }

    @Override
    public int getCurrentStacks(Player player) {
        Long lastThrow = lastThrowTime.get(player.getUniqueId());
        if (lastThrow != null && System.currentTimeMillis() - lastThrow > STACK_DECAY_TIME) {
            resetStacks(player);
        }
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        int current = getCurrentStacks(player);
        if (current < getMaxStacks()) {
            playerStacks.put(player.getUniqueId(), current + 1);
        }
        lastThrowTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Only projectile hits (thrown trident) build stacks; melee trident hits do not. In test mode
     * (rangedAttack flag set without a real Bukkit event) stacking is allowed.
     */
    @Override
    public boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
        if (event.getEntityDamageByEntityEvent() == null) return event.isRangedAttack();
        return event.getEntityDamageByEntityEvent().getCause() == EntityDamageEvent.DamageCause.PROJECTILE;
    }

    @Override
    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        lastThrowTime.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        if (configFields != null)
            return BASE_BONUS_PER_STACK + (configFields.calculateValue(skillLevel) * 0.01);
        return scaled(BASE_BONUS_PER_STACK, 0.0006, skillLevel); // 5% base + 0.06% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        resetStacks(player);
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        resetStacks(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    /**
     * Simulates having stacks for testing. Pre-sets the stack count and timestamp
     * so the stacking bonus is active during test attacks.
     */
    public static void simulateStacks(UUID uuid, int stacks) {
        playerStacks.put(uuid, stacks);
        lastThrowTime.put(uuid, System.currentTimeMillis());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonusPerStack = getBonusPerStack(skillLevel) * 100;
        double maxBonus = bonusPerStack * getMaxStacks();
        return applyLoreTemplates(Map.of(
                "maxStacks", String.valueOf(getMaxStacks()),
                "bonusPerStack", String.format("%.1f", bonusPerStack),
                "maxBonus", String.format("%.1f", maxBonus)
        ));
    }

    @Override
    public TestStrategy getTestStrategy() { return TestStrategy.CONDITION_SETUP; }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonusPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100)
        ));
    }

    @Override
    public void shutdown() {
        playerStacks.clear();
        lastThrowTime.clear();
        activePlayers.clear();
    }
}
