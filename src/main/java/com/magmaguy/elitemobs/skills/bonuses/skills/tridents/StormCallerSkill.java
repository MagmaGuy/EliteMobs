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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Storm Caller (PROC) - Trident attacks have a chance to strike lightning at the target.
 * Tier 2 unlock.
 */
public class StormCallerSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "tridents_storm_caller";
    private static final double BASE_PROC_CHANCE = 0.15; // 15% base chance

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public StormCallerSkill() {
        super(SkillType.TRIDENTS, 25, "Storm Caller",
              "Trident attacks have a chance to strike lightning at the target.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.15% per level, capped at 35%
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.0015));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = getPlayerSkillLevel(player);

        // Strike lightning effect at target
        target.getWorld().strikeLightningEffect(target.getLocation());

        // Deal additional lightning damage
        // Use bypass to prevent recursive skill processing
        double lightningDamage = calculateLightningDamage(skillLevel);
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.damage(event.getDamage() * lightningDamage, player);
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }

        // Send feedback to player
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§eSTORM STRIKE!"));
    }

    private double calculateLightningDamage(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return 0.5 + (skillLevel * 0.01);
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
        double damage = calculateLightningDamage(skillLevel);
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Lightning Damage: &f" + String.format("%.1f", damage * 100) + "%",
                "&7Strikes lightning effect"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateLightningDamage(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% Lightning Damage", calculateLightningDamage(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
