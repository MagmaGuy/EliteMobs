package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

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
 * Spellweave (STACKING) - Consecutive missiles on the same target weave stronger spells.
 * Tier 2 unlock.
 * <p>
 * Switching targets or 3 seconds without a hit unravels the weave. A cast moves the weave only when
 * none of its missiles reaches the woven target, so Multicast missiles that also strike a nearby
 * enemy do not break the weave on the one being focused. Missiles land in any order, so a cast that
 * has only hit other targets so far waits for the next cast to settle it. Power budget: ~50% uptime
 * on the ramp, so full stacks are worth +40% at level 50 (E = 0.50 * 0.40 = 0.20), the same band as
 * Ranger's Focus.
 */
public class SpellweaveSkill extends MagicWeaponSkill implements StackingSkill {

    public static final String SKILL_ID = "wands_spellweave";
    private static final int MAX_STACKS = 8;
    private static final long DECAY_MILLIS = 3000;

    private final Map<UUID, Weave> weaves = new ConcurrentHashMap<>();

    private static final class Weave {
        private UUID target;
        private int stacks;
        private long lastHit;
        // The latest cast that reached the woven target.
        private UUID wovenCast;
        // A newer cast that has so far only hit another target, and that target.
        private UUID strayCast;
        private UUID strayTarget;

        private void weaveOnto(UUID newTarget) {
            target = newTarget;
            stacks = 0;
            wovenCast = null;
            strayCast = null;
            strayTarget = null;
        }
    }

    public SpellweaveSkill() {
        super(SkillType.WANDS, 2, "Spellweave", "Consecutive missiles on one target build damage.",
                SkillBonusType.STACKING, SKILL_ID);
    }

    @Override
    public boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
        if (event.getMagicStrike() != MagicStrike.WAND_MISSILE || event.getEliteMobEntity().getLivingEntity() == null)
            return false;
        Weave weave = weaves.computeIfAbsent(event.getPlayer().getUniqueId(), id -> new Weave());
        UUID target = event.getEliteMobEntity().getLivingEntity().getUniqueId();
        UUID cast = event.getMagicHit().attackId();
        // A newer cast has arrived, so the stray one never reached the woven target.
        if (weave.strayCast != null && !weave.strayCast.equals(cast)) weave.weaveOnto(weave.strayTarget);
        if (weave.target == null) weave.weaveOnto(target);
        if (target.equals(weave.target)) {
            weave.wovenCast = cast;
            if (cast.equals(weave.strayCast)) weave.strayCast = null;
            return true;
        }
        if (!cast.equals(weave.wovenCast) && weave.strayCast == null) {
            weave.strayCast = cast;
            weave.strayTarget = target;
        }
        return false;
    }

    @Override
    public int getMaxStacks() {
        return MAX_STACKS;
    }

    @Override
    public int getCurrentStacks(Player player) {
        Weave weave = weaves.get(player.getUniqueId());
        if (weave == null) return 0;
        if (System.currentTimeMillis() - weave.lastHit > DECAY_MILLIS) weave.stacks = 0;
        return weave.stacks;
    }

    @Override
    public void addStack(Player player) {
        Weave weave = weaves.computeIfAbsent(player.getUniqueId(), id -> new Weave());
        weave.stacks = Math.min(MAX_STACKS, getCurrentStacks(player) + 1);
        weave.lastHit = System.currentTimeMillis();
    }

    @Override
    public void resetStacks(Player player) {
        weaves.remove(player.getUniqueId());
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        return scaled(0.03, 0.0004, skillLevel); // 5% at level 50
    }

    @Override
    protected void clearPlayer(UUID playerId) {
        weaves.remove(playerId);
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
        weaves.clear();
        super.shutdown();
    }
}
