package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Impale (PROC) - Trident attacks have a chance to deal massive bonus damage that ignores armor.
 * Tier 1 unlock.
 */
public class ImpaleSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "tridents_impale";
    private static final double BASE_PROC_CHANCE = 0.20; // 20% base chance
    private static final double BASE_DAMAGE_MULTIPLIER = 1.5;

    // Track which players have this skill active
    private static final Set<UUID> activePlayers = new HashSet<>();

    public ImpaleSkill() {
        super(SkillType.TRIDENTS, 10, "Impale",
              "Trident attacks have a chance to deal massive bonus damage that ignores armor.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        // Base chance + 0.2% per level, capped at 40%
        return Math.min(0.40, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        // Send feedback to player
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cIMPALE!"));
    }

    private double calculateDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return BASE_DAMAGE_MULTIPLIER + configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02);
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
        double multiplier = calculateDamageMultiplier(skillLevel);
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Damage Multiplier: &f" + String.format("%.1f", multiplier) + "x",
                "&7Ignores armor"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return calculateDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1fx Damage", calculateDamageMultiplier(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
