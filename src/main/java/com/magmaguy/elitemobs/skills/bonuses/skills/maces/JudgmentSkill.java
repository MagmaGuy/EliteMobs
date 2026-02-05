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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Judgment (PROC) - Mark enemies for judgment, causing them to take bonus damage.
 * Tier 2 unlock.
 */
public class JudgmentSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "maces_judgment";
    private static final double BASE_PROC_CHANCE = 0.20;
    private static final long MARK_DURATION = 10000; // 10 seconds
    private static final double BASE_DAMAGE_BONUS = 0.40; // 40% extra damage

    private static final Map<UUID, UUID> judgedTargets = new HashMap<>(); // EntityUUID -> PlayerUUID
    private static final Map<UUID, Long> markExpiry = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public JudgmentSkill() {
        super(SkillType.MACES, 25, "Judgment",
              "Mark enemies for judgment, causing them to take bonus damage.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.45, BASE_PROC_CHANCE + (skillLevel * 0.005));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

        LivingEntity target = eliteEntity.getLivingEntity();
        UUID entityUUID = target.getUniqueId();

        // Apply judgment mark
        judgedTargets.put(entityUUID, player.getUniqueId());
        markExpiry.put(entityUUID, System.currentTimeMillis() + MARK_DURATION);

        // Visual effects - golden glow
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int)(MARK_DURATION / 50), 0));
        target.getWorld().spawnParticle(Particle.END_ROD,
            target.getLocation().add(0, 1.5, 0), 20, 0.5, 0.5, 0.5, 0.05);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7e\u00A7lJUDGMENT!"));

        incrementProcCount(player);
    }

    /**
     * Checks if a target is judged by this player.
     */
    public static boolean isJudged(LivingEntity target, Player player) {
        UUID entityUUID = target.getUniqueId();
        UUID judgerUUID = judgedTargets.get(entityUUID);

        if (judgerUUID == null) return false;

        // Check if mark has expired
        Long expiry = markExpiry.get(entityUUID);
        if (expiry == null || System.currentTimeMillis() > expiry) {
            judgedTargets.remove(entityUUID);
            markExpiry.remove(entityUUID);
            return false;
        }

        return judgerUUID.equals(player.getUniqueId());
    }

    public double getJudgmentDamageBonus(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BONUS + (skillLevel * 0.005);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        // Clean up marks created by this player
        UUID playerUUID = player.getUniqueId();
        judgedTargets.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        removeBonus(player);
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double procChance = getProcChance(skillLevel) * 100;
        double damageBonus = getJudgmentDamageBonus(skillLevel) * 100;
        return List.of(
                "&7Chance: &f" + String.format("%.1f", procChance) + "%",
                "&7Judgment Damage: &f+" + String.format("%.0f", damageBonus) + "%",
                "&7Duration: &f10 seconds",
                "&7Judged enemies glow"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getJudgmentDamageBonus(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% vs Judged", getJudgmentDamageBonus(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        judgedTargets.clear();
        markExpiry.clear();
        activePlayers.clear();
    }
}
