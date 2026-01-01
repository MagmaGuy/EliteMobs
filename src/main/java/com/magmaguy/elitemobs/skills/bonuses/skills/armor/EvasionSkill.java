package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tier 1 ARMOR skill - Evasion
 * Chance to completely evade incoming attacks
 */
public class EvasionSkill extends SkillBonus implements ProcSkill {

    private static final HashSet<UUID> activePlayers = new HashSet<>();

    public EvasionSkill() {
        super(
            SkillType.ARMOR,
            10,
            "Evasion",
            "Chance to completely dodge incoming attacks",
            SkillBonusType.PROC,
            1,
            "evasion"
        );
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        // Evasion is checked on damage event, no persistent bonus to apply
    }

    @Override
    public void removeBonus(Player player) {
        // No persistent bonus to remove
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
        List<String> lore = new ArrayList<>();
        lore.add("Chance to completely dodge incoming attacks");
        lore.add(String.format("Evasion Chance: %.1f%%", getProcChance(skillLevel) * 100));
        return lore;
    }

    @Override
    public double getBonusValue(int skillLevel) {
        // Returns 1.0 for full damage negation when evasion procs
        // The proc chance is handled separately via getProcChance()
        return 1.0;
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1f%% Evasion Chance", getProcChance(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
    }

    // ProcSkill interface methods

    @Override
    public double getProcChance(int skillLevel) {
        // Base 10% + 5% per scaled value
        return 0.10 + getScaledValue(skillLevel) * 0.05;
    }

    @Override
    public void onProc(Player player, Object context) {
        // Visual effect for dodge
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.3, 0.5, 0.3, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.5f);

        // Send action bar message
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Evaded!"));
    }

    /**
     * Attempts to evade an incoming attack.
     * Called from damage event handler.
     *
     * @param player The player being attacked
     * @param context The PlayerDamagedByEliteMobEvent
     * @return true if the attack was evaded
     */
    public boolean tryEvade(Player player, Object context) {
        if (!isActive(player)) {
            return false;
        }

        int skillLevel = getPlayerSkillLevel(player);
        if (ThreadLocalRandom.current().nextDouble() < getProcChance(skillLevel)) {
            onProc(player, context);
            return true;
        }
        return false;
    }

    private int getPlayerSkillLevel(Player player) {
        return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
    }
}
