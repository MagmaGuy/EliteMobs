package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Suppressing Fire (PROC) - Chance to weaken and slow enemies.
 * Tier 3 unlock.
 */
public class SuppressingFireSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "crossbows_suppressing_fire";
    private static final double BASE_PROC_CHANCE = 0.20; // 20% chance
    private static final int DEBUFF_DURATION = 80; // 4 seconds

    private static final Set<UUID> activePlayers = new HashSet<>();

    public SuppressingFireSkill() {
        super(SkillType.CROSSBOWS, 50, "Suppressing Fire",
              "Chance to weaken and slow enemies on hit.",
              SkillBonusType.PROC, 3, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.40, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;
        if (event.getEliteMobEntity().getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        int amplifier = Math.min(2, skillLevel / 30);

        event.getEliteMobEntity().getLivingEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.WEAKNESS, DEBUFF_DURATION, amplifier));
        event.getEliteMobEntity().getLivingEntity().addPotionEffect(
                new PotionEffect(PotionEffectType.SLOWNESS, DEBUFF_DURATION, 1));

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§7SUPPRESSED!"));
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
                "&7Applies: &8Weakness &7and &bSlowness",
                "&7Duration: &f4 seconds"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getProcChance(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.0f%% suppress chance", getProcChance(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
