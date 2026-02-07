package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Avatar of Judgment (COOLDOWN) - Massive damage boost with visual effects for 10 seconds.
 * Tier 4 unlock.
 */
public class AvatarOfJudgmentSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "maces_avatar_of_judgment";
    private static final long BASE_COOLDOWN_SECONDS = 90;
    private static final int BUFF_DURATION_TICKS = 200; // 10 seconds
    private static final double BASE_DAMAGE_BOOST = 2.0; // 200% damage

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Set<UUID> buffedPlayers = new HashSet<>();

    public AvatarOfJudgmentSkill() {
        super(SkillType.MACES, 75, "Avatar of Judgment",
              "Become an avatar of divine wrath. Massive damage boost for 10 seconds.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(60, BASE_COOLDOWN_SECONDS - (skillLevel / 3));
    }

    @Override
    public boolean isOnCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return false;
        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        long cooldownMs = getCooldownSeconds(skillLevel) * 1000L;
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) return 0;
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
        return Math.max(0, remaining);
    }

    @Override
    public void endCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player, Object event) {
        activateAvatar(player);
    }

    /**
     * Activates the Avatar of Judgment transformation.
     */
    public void activateAvatar(Player player) {
        if (!isActive(player) || isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);

        // Add to buffed players
        buffedPlayers.add(player.getUniqueId());

        // Apply visual buff effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, BUFF_DURATION_TICKS, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION_TICKS, 0));

        // Initial transformation effect
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.END_ROD,
            player.getLocation().add(0, 1, 0), 100, 1, 1, 1, 0.3);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7e\u00A7l\u2728 AVATAR OF JUDGMENT! \u2728"));

        // Particle aura while active
        new BukkitRunnable() {
            int ticksRemaining = BUFF_DURATION_TICKS;

            @Override
            public void run() {
                if (ticksRemaining <= 0 || !player.isOnline() || !buffedPlayers.contains(player.getUniqueId())) {
                    buffedPlayers.remove(player.getUniqueId());
                    if (player.isOnline()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText("\u00A77Avatar fades..."));
                    }
                    cancel();
                    return;
                }

                // Aura particles
                if (ticksRemaining % 5 == 0) {
                    double angle = (BUFF_DURATION_TICKS - ticksRemaining) * 0.3;
                    for (int i = 0; i < 2; i++) {
                        double x = Math.cos(angle + i * Math.PI) * 0.8;
                        double z = Math.sin(angle + i * Math.PI) * 0.8;
                        player.getWorld().spawnParticle(Particle.END_ROD,
                            player.getLocation().add(x, 1 + Math.sin(angle * 0.5) * 0.3, z),
                            1, 0, 0, 0, 0);
                    }
                }

                ticksRemaining--;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        startCooldown(player, skillLevel);
        incrementProcCount(player);
    }

    /**
     * Checks if the player currently has the Avatar buff active.
     */
    public static boolean hasAvatarBuff(Player player) {
        return buffedPlayers.contains(player.getUniqueId());
    }

    public double getDamageBoost(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_BOOST + (skillLevel * 0.03);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
        buffedPlayers.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
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
        return List.of(
            "&7Damage Boost: &f+" + String.format("%.0f", (getDamageBoost(skillLevel) - 1) * 100) + "%",
            "&7Duration: &f10 seconds",
            "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBoost(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% for 10s (CD: %ds)", (getDamageBoost(skillLevel) - 1) * 100, getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        buffedPlayers.clear();
        cooldowns.clear();
    }
}
