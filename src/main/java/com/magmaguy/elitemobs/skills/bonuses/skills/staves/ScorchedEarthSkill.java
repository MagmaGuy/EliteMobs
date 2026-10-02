package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scorched Earth (PROC) - Fireballs can leave the ground burning under the target.
 * Tier 3 unlock.
 * <p>
 * The patch burns for 4 seconds and hits every enemy inside it for 25% of the triggering hit each
 * second at level 50. At most one patch per fireball, so a blast that procs on several targets
 * does not stack patches. Power budget: 20% * 100% for a target that stays inside (E = 0.20).
 */
public class ScorchedEarthSkill extends MagicWeaponSkill implements ProcSkill {

    public static final String SKILL_ID = "staves_scorched_earth";
    private static final double RADIUS = 2.5;
    private static final int PULSES = 4; // one per second

    private final Map<UUID, UUID> lastPatchAttack = new ConcurrentHashMap<>();
    private final Set<BukkitRunnable> patches = ConcurrentHashMap.newKeySet();

    public ScorchedEarthSkill() {
        super(SkillType.STAVES, 3, "Scorched Earth", "Fireballs can leave the ground burning.",
                SkillBonusType.PROC, SKILL_ID);
    }

    @Override
    public boolean canProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
        return event != null && !event.getMagicHit().attackId().equals(lastPatchAttack.get(player.getUniqueId()));
    }

    @Override
    public double getProcChance(int skillLevel) {
        return scaled(0.10, 0.002, 0.40, skillLevel); // 20% at level 50
    }

    private double pulseFraction(int skillLevel) {
        return scaled(0.15, 0.002, skillLevel); // 25% per second at level 50
    }

    @Override
    public void onProc(Player player, Object context) {
        EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
        if (event == null || event.getEliteMobEntity().getLivingEntity() == null) return;
        lastPatchAttack.put(player.getUniqueId(), event.getMagicHit().attackId());
        Location center = event.getEliteMobEntity().getLivingEntity().getLocation();
        double pulseDamage = event.getDamageWithoutCriticalStrike() * pulseFraction(skillLevel(player));
        BukkitRunnable patch = new BukkitRunnable() {
            private int remaining = PULSES;

            @Override
            public void run() {
                if (remaining-- <= 0 || !player.isOnline()) {
                    patches.remove(this);
                    cancel();
                    return;
                }
                burnGround(center);
                for (LivingEntity enemy : enemiesNear(player, center, RADIUS, null))
                    sideDamage(player, enemy, pulseDamage);
            }
        };
        patches.add(patch);
        burnGround(center);
        patch.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
    }

    private static void burnGround(Location center) {
        World world = center.getWorld();
        if (world == null) return;
        for (int point = 0; point < 16; point++) {
            double angle = 2D * Math.PI * point / 16D;
            world.spawnParticle(Particle.FLAME, center.clone().add(Math.cos(angle) * RADIUS, 0.1,
                    Math.sin(angle) * RADIUS), 1, 0, 0, 0, 0);
        }
        world.spawnParticle(Particle.LAVA, center.clone().add(0, 0.1, 0), 3, RADIUS / 2, 0, RADIUS / 2, 0);
    }

    @Override
    protected void clearPlayer(UUID playerId) {
        lastPatchAttack.remove(playerId);
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "procChance", String.format("%.1f", getProcChance(skillLevel) * 100),
                "pulsePercent", String.format("%.0f", pulseFraction(skillLevel) * 100)));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return pulseFraction(skillLevel) * PULSES;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("pulsePercent", String.format("%.0f", pulseFraction(skillLevel) * 100)));
    }

    @Override
    public boolean affectsDamage() {
        return false; // The patch deals its own damage; the fireball hit is unchanged.
    }

    @Override
    public void shutdown() {
        patches.forEach(BukkitRunnable::cancel);
        patches.clear();
        lastPatchAttack.clear();
        super.shutdown();
    }
}
