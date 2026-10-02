package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Echo Bolt (PROC) - Missiles can echo, striking the target again half a second later.
 * Tier 3 unlock.
 * <p>
 * Power budget: 20% at level 50 for a full repeat of the hit (E = 0.20 * 1.0 = 0.20).
 */
public class EchoBoltSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "wands_echo_bolt";
    private static final long ECHO_DELAY_TICKS = 10;

    private final Set<BukkitRunnable> echoes = ConcurrentHashMap.newKeySet();

    public EchoBoltSkill() {
        super(SkillType.WANDS, 3, "Echo Bolt", "Missiles can echo and strike again.", SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        return strike(context, MagicStrike.WAND_MISSILE) != null;
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.10, 0.002, 0.40, skillLevel); // 20% at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        double damage = event.getDamage();
        BukkitRunnable echo = new BukkitRunnable() {
            @Override
            public void run() {
                echoes.remove(this);
                if (!target.isValid() || target.isDead()) return;
                target.getWorld().spawnParticle(Particle.END_ROD, chest(target), 10, 0.2, 0.3, 0.2, 0.05);
                sideDamage(player, target, damage);
            }
        };
        echoes.add(echo);
        echo.runTaskLater(MetadataHandler.PLUGIN, ECHO_DELAY_TICKS);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of("procChance", String.format("%.1f", getProcChance(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getProcChance(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("procChance", String.format("%.1f", getProcChance(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The echo is a separate hit.
    }

    @Override
    public void shutdown() {
        echoes.forEach(BukkitRunnable::cancel);
        echoes.clear();
        super.shutdown();
    }
}
