package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Riposte (COOLDOWN) - After blocking, your next attack deals bonus damage.
 * Has a cooldown between activations.
 * Tier 2 unlock.
 */
public class RiposteSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "swords_riposte";
    private static final double BASE_COOLDOWN = 10.0; // 10 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 1.5; // 50% bonus damage

    private static final Set<UUID> playersOnCooldown = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> playersWithRiposteReady = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

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

    @Override
    public void onActivate(Player player, Object event) {
        // Riposte is triggered from the damage bonus integration, not the generic handler
        // Do nothing here - riposte ready check + damage is handled in onProc()
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
        incrementProcCount(player);
        SkillBonus.sendSkillActionBar(player, this);
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
        // Base 50% bonus + 1% per level, capped at 2.5x
        return Math.min(2.5, BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.01));
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
        return applyLoreTemplates(Map.of(
                "damage", String.format("%.0f", multiplier),
                "cooldown", String.format("%.1f", cooldown)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of("damage", String.format("%.0f", (getDamageMultiplier(skillLevel) - 1) * 100)));
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
