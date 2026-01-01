package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Multishot (PROC) - Chance to fire additional arrows on hit.
 * Tier 2 unlock.
 */
public class MultishotSkill extends SkillBonus implements ProcSkill {

    public static final String SKILL_ID = "bows_multishot";
    private static final double BASE_PROC_CHANCE = 0.20; // 20% chance

    private static final Set<UUID> activePlayers = new HashSet<>();

    public MultishotSkill() {
        super(SkillType.BOWS, 25, "Multishot",
              "Chance to fire additional arrows when attacking.",
              SkillBonusType.PROC, 2, SKILL_ID);
    }

    @Override
    public double getProcChance(int skillLevel) {
        return Math.min(0.40, BASE_PROC_CHANCE + (skillLevel * 0.002));
    }

    @Override
    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
        double arrowDamage = event.getDamage() * 0.5;

        // Spawn additional arrows
        Vector direction = player.getLocation().getDirection();
        double spread = 0.15;

        int extraArrows = 2 + (skillLevel / 40); // 2-4 extra arrows based on level
        for (int i = 0; i < extraArrows; i++) {
            Vector offset = new Vector(
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * spread,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * spread,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * spread
            );
            Arrow arrow = player.launchProjectile(Arrow.class, direction.clone().add(offset).normalize().multiply(2));
            arrow.setDamage(arrowDamage);
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aMULTISHOT!"));
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
        int extraArrows = 2 + (skillLevel / 40);
        return List.of(
                "&7Proc Chance: &f" + String.format("%.1f", getProcChance(skillLevel) * 100) + "%",
                "&7Extra Arrows: &f" + extraArrows,
                "&7Arrow Damage: &f50% of original"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getProcChance(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.0f%% chance for extra arrows", getProcChance(skillLevel) * 100); }
    @Override
    public void shutdown() { activePlayers.clear(); }
}
