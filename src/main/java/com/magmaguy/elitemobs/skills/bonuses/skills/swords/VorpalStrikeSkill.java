package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Vorpal Strike (COOLDOWN) - Critical attacks have a chance to deal massive bonus damage.
 * Only triggers on critical hits.
 * Tier 4 unlock.
 */
public class VorpalStrikeSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "swords_vorpal_strike";
    private static final double BASE_COOLDOWN = 30.0; // 30 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 3.0; // Triple damage

    private static final Set<UUID> playersOnCooldown = new HashSet<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public VorpalStrikeSkill() {
        super(SkillType.SWORDS, 75, "Vorpal Strike",
              "Critical hits can deal devastating bonus damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Reduce cooldown by 0.3% per level, min 15 seconds
        double reduction = 1.0 - (skillLevel * 0.003);
        return (long) Math.max(15.0, BASE_COOLDOWN * reduction);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return playersOnCooldown.contains(player.getUniqueId());
    }

    @Override
    public void startCooldown(Player player, int skillLevel) {
        UUID uuid = player.getUniqueId();
        playersOnCooldown.add(uuid);
        long seconds = getCooldownSeconds(skillLevel);

        new BukkitRunnable() {
            @Override
            public void run() {
                playersOnCooldown.remove(uuid);
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendMessage("§6Vorpal Strike §aready!");
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, seconds * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        return 0; // Simplified
    }

    @Override
    public void endCooldown(Player player) {
        playersOnCooldown.remove(player.getUniqueId());
    }

    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        // Only triggers on critical hits
        if (!event.isCriticalStrike()) return;

        if (isOnCooldown(player)) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double multiplier = getDamageMultiplier(skillLevel);

        event.setDamage(event.getDamage() * multiplier);

        // Visual effects
        if (event.getEliteMobEntity().getLivingEntity() != null) {
            event.getEliteMobEntity().getLivingEntity().getWorld().spawnParticle(
                    Particle.CRIT,
                    event.getEliteMobEntity().getLivingEntity().getLocation().add(0, 1, 0),
                    30, 0.5, 0.5, 0.5, 0.3
            );
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§4§lVORPAL STRIKE!"));

        startCooldown(player, skillLevel);
    }

    private double getDamageMultiplier(int skillLevel) {
        // Base 3x + 0.02x per level
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.02);
    }

    /**
     * Checks if this skill should proc on a critical hit.
     */
    public static boolean shouldProc(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!activePlayers.contains(player.getUniqueId())) return false;
        if (!event.isCriticalStrike()) return false;
        return !playersOnCooldown.contains(player.getUniqueId());
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        playersOnCooldown.remove(uuid);
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
        double multiplier = getDamageMultiplier(skillLevel);
        double cooldown = getCooldownSeconds(skillLevel);
        return List.of(
                "&7Damage Multiplier: &f" + String.format("%.1f", multiplier) + "x",
                "&7Cooldown: &f" + String.format("%.1f", cooldown) + "s",
                "&7Only triggers on critical hits"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.1fx Critical Damage", getDamageMultiplier(skillLevel));
    }

    @Override
    public void shutdown() {
        playersOnCooldown.clear();
        activePlayers.clear();
    }
}
