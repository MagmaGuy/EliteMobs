package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Undertow (PROC) - Trident attacks have a chance to pull targets toward the player.
 * Tier 3 unlock.
 */
public class UndertowSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "tridents_undertow";
    private static final double BASE_PROC_CHANCE = 0.20; // 20% base chance
    private static final double BASE_PULL_STRENGTH = 1.0;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public UndertowSkill() {
        super(SkillType.TRIDENTS, 50, "Undertow",
              "Trident attacks have a chance to pull targets toward the player.",
              SkillBonusType.PROC, 3, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.2% per level, capped at 40%
        return Math.min(0.40, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = getPlayerSkillLevel(player);

        // Pull target toward player
        double pullStrength = calculatePullStrength(skillLevel);
        Vector direction = player.getLocation().toVector()
                .subtract(target.getLocation().toVector());
        // Check for zero/near-zero vector to avoid NaN from normalize()
        if (direction.lengthSquared() < 0.001) {
            // Player and target at same location - skip pull
            return;
        }
        direction.normalize().multiply(pullStrength);
        direction.setY(0.2);
        target.setVelocity(direction);

        // Send feedback to player
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§3UNDERTOW!"));
    }

    private double calculatePullStrength(int skillLevel) {
        if (configFields != null) {
            return BASE_PULL_STRENGTH + configFields.calculateValue(skillLevel);
        }
        return BASE_PULL_STRENGTH + (skillLevel * 0.01);
    }

    private int getPlayerSkillLevel(Player player) {
        return com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double procChance = getProcChance(skillLevel) * 100;
        double pullStrength = calculatePullStrength(skillLevel);
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Pull Strength: &f" + String.format("%.1f", pullStrength),
                "&7Pulls targets toward you"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculatePullStrength(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f Pull Strength", calculatePullStrength(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
