package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Concussion (PROC) - Chance to daze enemies, reducing their damage.
 * Tier 1 unlock.
 */
public class ConcussionSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "maces_concussion";
    private static final double BASE_PROC_CHANCE = 0.15;
    private static final int DAZE_DURATION_TICKS = 60; // 3 seconds

    private static final Set<UUID> activePlayers = new HashSet<>();

    public ConcussionSkill() {
        super(SkillType.MACES, 10, "Concussion",
              "Chance to daze enemies, reducing their damage output.",
              SkillBonusType.PROC, 1, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.35, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);

        // Apply weakness (reduces damage dealt by mob)
        int amplifier = Math.min(2, skillLevel / 25);
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, DAZE_DURATION_TICKS, amplifier));

        // Visual effect
        target.getWorld().spawnParticle(Particle.CRIT,
            target.getLocation().add(0, 1.5, 0), 15, 0.3, 0.3, 0.3, 0.1);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7e\u00A7lCONCUSSION!"));

        incrementProcCount(player);
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
        return List.of(
            "&7Proc Chance: &f" + String.format("%.1f", getProcChance(skillLevel) * 100) + "%",
            "&7Duration: &f3 seconds",
            "&7Applies Weakness"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getProcChance(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% Daze Chance", getProcChance(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }
}
