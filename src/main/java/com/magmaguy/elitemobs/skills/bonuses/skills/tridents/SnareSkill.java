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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Snare (PROC) - Trident attacks have a chance to heavily slow and ground targets.
 * Tier 1 unlock.
 */
public class SnareSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "tridents_snare";
    private static final double BASE_PROC_CHANCE = 0.25; // 25% base chance
    private static final int SNARE_DURATION = 60; // 3 seconds

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public SnareSkill() {
        super(SkillType.TRIDENTS, 10, "Snare",
              "Trident attacks have a chance to heavily slow and ground targets.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.2% per level, capped at 45%
        return Math.min(0.45, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = getPlayerSkillLevel(player);
        int amplifier = calculateAmplifier(skillLevel);

        // Apply slowness and prevent jumping
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SNARE_DURATION, 3 + amplifier));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, SNARE_DURATION, 128)); // Negative jump

        // Send feedback to player
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§bSNARED!"));
    }

    private int calculateAmplifier(int skillLevel) {
        if (configFields != null) {
            return (int) Math.floor(configFields.calculateValue(skillLevel));
        }
        return (int) Math.floor(skillLevel * 0.05);
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
        int amplifier = calculateAmplifier(skillLevel);
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Slowness Level: &f" + (4 + amplifier),
                "&7Duration: &f3 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateAmplifier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("Slowness %d", 4 + calculateAmplifier(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
