package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stoke the Flames (STACKING) - Each fireball that hits builds heat for the next ones.
 * Tier 2 unlock.
 * <p>
 * One stack per fireball, not per target, so every enemy caught in a blast takes the same bonus.
 * Stacks drop after 6 seconds without a fireball hit, three fireball cycles.
 * Power budget: ~50% uptime on the ramp, so full stacks are worth +40% at level 50
 * (E = 0.50 * 0.40 = 0.20), the same band as Barrage.
 */
public class StokeTheFlamesSkill extends MagicWeaponSkill implements StackingSkill {

    public static final String SKILL_ID = "staves_stoke_the_flames";
    private static final int MAX_STACKS = 5;
    private static final long DECAY_MILLIS = 6000;

    private final Map<UUID, Heat> heat = new ConcurrentHashMap<>();

    private static final class Heat {
        private UUID attackId;
        private int stacksBeforeAttack;
        private int stacks;
        private long lastFireball;
        private boolean stackPending;
    }

    public StokeTheFlamesSkill() {
        super(SkillType.STAVES, 2, "Stoke the Flames", "Each fireball that hits builds heat for the next.",
                SkillBonusType.STACKING, SKILL_ID);
    }

    @Override
    public boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
        if (event.getMagicStrike() != MagicStrike.STAFF_FIREBALL) return false;
        Heat state = heat.computeIfAbsent(event.getPlayer().getUniqueId(), id -> new Heat());
        UUID attackId = event.getMagicHit().attackId();
        if (!attackId.equals(state.attackId)) {
            if (System.currentTimeMillis() - state.lastFireball > DECAY_MILLIS) state.stacks = 0;
            state.attackId = attackId;
            state.stacksBeforeAttack = state.stacks;
            state.stackPending = true;
        }
        return true;
    }

    @Override
    public int getMaxStacks() {
        return MAX_STACKS;
    }

    /** Stacks as they stood before the current fireball, shared by every target it hits. */
    @Override
    public int getCurrentStacks(Player player) {
        Heat state = heat.get(player.getUniqueId());
        return state == null ? 0 : state.stacksBeforeAttack;
    }

    @Override
    public void addStack(Player player) {
        Heat state = heat.get(player.getUniqueId());
        if (state == null || !state.stackPending) return;
        state.stackPending = false;
        state.stacks = Math.min(MAX_STACKS, state.stacksBeforeAttack + 1);
        state.lastFireball = System.currentTimeMillis();
    }

    @Override
    public void resetStacks(Player player) {
        heat.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return scaled(0.05, 0.0006, skillLevel); // 8% at level 50
    }

    @Override
    protected void clearPlayer(UUID playerId) {
        heat.remove(playerId);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "bonusPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxStacks", String.valueOf(MAX_STACKS)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel) * MAX_STACKS;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonusPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100),
                "maxStacks", String.valueOf(MAX_STACKS)));
    }

    @Override
    public void shutdown() {
        heat.clear();
        super.shutdown();
    }
}
