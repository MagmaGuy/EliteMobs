package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Dead Eye (COOLDOWN) - Massive damage on critical shots.
 * Tier 4 unlock.
 */
public class DeadEyeSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "bows_dead_eye";
    private static final long BASE_COOLDOWN = 45; // 45 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 2.5; // 250% damage

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Set<UUID> onCooldown = new HashSet<>();
    private static final Map<UUID, Long> cooldownEndTimes = new HashMap<>();

    public DeadEyeSkill() {
        super(SkillType.BOWS, 75, "Dead Eye",
              "Critical shots deal massive bonus damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(20, BASE_COOLDOWN - (skillLevel / 5)); // 45s base, min 20s
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return onCooldown.contains(player.getUniqueId());
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        UUID uuid = player.getUniqueId();
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000L;
        onCooldown.add(uuid);
        cooldownEndTimes.put(uuid, System.currentTimeMillis() + cooldownMs);

        new BukkitRunnable() {
            @Override
            public void run() {
                endCooldown(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, getCooldownSeconds(skillLevel) * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long endTime = cooldownEndTimes.get(player.getUniqueId());
        if (endTime == null) return 0;
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    @Override
    public void endCooldown(Player player) {
        onCooldown.remove(player.getUniqueId());
        cooldownEndTimes.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        if (!(event instanceof EliteMobDamagedByPlayerEvent damageEvent)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
        double multiplier = getDamageMultiplier(skillLevel);

        damageEvent.setDamage(damageEvent.getDamage() * multiplier);

        // Visual effect
        if (damageEvent.getEliteMobEntity().getLivingEntity() != null) {
            damageEvent.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                    Particle.ENCHANTED_HIT, damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.2);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c§lDEAD EYE!"));
    }

    private double getDamageMultiplier(int skillLevel) {
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02); // 250% base + 2% per level
    }

    @Override
    public void applyBonus(Player player, int skillLevel) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }
    @Override
    public void onActivate(Player player) { activePlayers.add(player.getUniqueId()); }
    @Override
    public void onDeactivate(Player player) {
        activePlayers.remove(player.getUniqueId());
        endCooldown(player);
    }
    @Override
    public boolean isActive(Player player) { return activePlayers.contains(player.getUniqueId()); }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return List.of(
                "&7Damage Multiplier: &f" + String.format("%.1f", getDamageMultiplier(skillLevel)) + "x",
                "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s",
                "&7Triggers on critical hits"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getDamageMultiplier(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.1fx critical damage", getDamageMultiplier(skillLevel)); }
    @Override
    public void shutdown() {
        activePlayers.clear();
        onCooldown.clear();
        cooldownEndTimes.clear();
    }
}
