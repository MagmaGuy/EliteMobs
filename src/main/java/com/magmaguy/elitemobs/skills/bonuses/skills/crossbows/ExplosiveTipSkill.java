package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Explosive Tip (PROC) - Chance for bolts to explode on impact.
 * Tier 2 unlock.
 */
public class ExplosiveTipSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "crossbows_explosive_tip";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% chance
    private static final double EXPLOSION_RADIUS = 3.0;
    private static final double BASE_EXPLOSION_DAMAGE = 0.50; // 50% of original

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ExplosiveTipSkill() {
        super(SkillType.CROSSBOWS, 25, "Explosive Tip",
              "Bolts have a chance to explode on impact.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;

        LivingEntity target = event.getEliteMobEntity().getLivingEntity();
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        double explosionDamage = event.getDamage() * getExplosionDamageMultiplier(skillLevel);

        // Create explosion effect
        target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);

        // Damage nearby enemies
        // Use bypass to prevent recursive skill processing
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.getNearbyEntities(EXPLOSION_RADIUS, EXPLOSION_RADIUS, EXPLOSION_RADIUS).stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                    .forEach(e -> ((LivingEntity) e).damage(explosionDamage, player));
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6EXPLOSIVE!"));
    }

    private double getExplosionDamageMultiplier(int skillLevel) {
        return BASE_EXPLOSION_DAMAGE + (skillLevel * 0.005); // 50% base + 0.5% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) { activePlayers.remove(player.getUniqueId()); }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&7Proc Chance: &f" + String.format("%.1f", getProcChance(skillLevel) * 100) + "%",
                "&7AoE Damage: &f" + String.format("%.0f", getExplosionDamageMultiplier(skillLevel) * 100) + "%",
                "&7Radius: &f" + (int)EXPLOSION_RADIUS + " blocks"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getExplosionDamageMultiplier(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.0f%% AoE damage", getExplosionDamageMultiplier(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
