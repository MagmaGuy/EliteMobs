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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Riposte (COOLDOWN) - After blocking, your next attack deals bonus damage.
 * Has a cooldown between activations.
 * Tier 2 unlock.
 */
public class RiposteSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "swords_riposte";
    private static final double BASE_COOLDOWN = 10.0; // 10 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 1.5; // 50% bonus damage

    private static final Set<UUID> playersOnCooldown = new HashSet<>();
    private static final Set<UUID> playersWithRiposteReady = new HashSet<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public RiposteSkill() {
        super(SkillType.SWORDS, 25, "Riposte",
              "After blocking an attack, your next attack deals bonus damage.",
              SkillBonusType.COOLDOWN, 2, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        // Reduce cooldown by 0.5% per level, min 5 seconds
        double reduction = 1.0 - (skillLevel * 0.005);
        return (long) Math.max(5.0, BASE_COOLDOWN * reduction);
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
            }
        }.runTaskLater(MetadataHandler.PLUGIN, seconds * 20L);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        // This would require tracking start times - simplified for now
        return 0;
    }

    @Override
    public void endCooldown(Player player) {
        playersOnCooldown.remove(player.getUniqueId());
    }

    public void onProc(Player player, Object context) {
        if (!(context instanceof EliteMobDamagedByPlayerEvent event)) return;

        // Check if riposte is ready
        if (!playersWithRiposteReady.contains(player.getUniqueId())) return;

        // Apply bonus damage
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
        double multiplier = getDamageMultiplier(skillLevel);
        event.setDamage(event.getDamage() * multiplier);

        // Consume riposte
        playersWithRiposteReady.remove(player.getUniqueId());

        // Start cooldown
        startCooldown(player, skillLevel);

        // Visual feedback
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Riposte!"));
    }

    /**
     * Called when a player successfully blocks an attack.
     */
    public static void onPlayerBlock(Player player) {
        UUID uuid = player.getUniqueId();
        if (!activePlayers.contains(uuid)) return;
        if (playersOnCooldown.contains(uuid)) return;

        playersWithRiposteReady.add(uuid);

        // Riposte window expires after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                playersWithRiposteReady.remove(uuid);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 60L);
    }

    /**
     * Checks if a player has riposte ready.
     */
    public static boolean hasRiposteReady(UUID playerUUID) {
        return playersWithRiposteReady.contains(playerUUID);
    }

    private double getDamageMultiplier(int skillLevel) {
        // Base 50% bonus + 1% per level
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.01);
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
        playersWithRiposteReady.remove(uuid);
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
        double multiplier = (getDamageMultiplier(skillLevel) - 1) * 100;
        double cooldown = getCooldownSeconds(skillLevel);
        return List.of(
                "&7Bonus Damage: &f+" + String.format("%.0f", multiplier) + "%",
                "&7Cooldown: &f" + String.format("%.1f", cooldown) + "s",
                "&7Block to activate, then attack within 3s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.0f%% Riposte Damage", (getDamageMultiplier(skillLevel) - 1) * 100);
    }

    @Override
    public boolean affectsDamage() {
        // Riposte handles its own damage via onProc() when player blocks then attacks
        return false;
    }

    @Override
    public void shutdown() {
        playersOnCooldown.clear();
        playersWithRiposteReady.clear();
        activePlayers.clear();
    }
}
