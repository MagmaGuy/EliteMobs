package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Impaler (COOLDOWN) - Pin an enemy in place, dealing massive damage.
 * Tier 4 unlock.
 */
public class ImpalerSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "spears_impaler";
    private static final long BASE_COOLDOWN_SECONDS = 45;
    private static final int PIN_DURATION_TICKS = 60; // 3 seconds
    private static final double BASE_DAMAGE_MULTIPLIER = 4.0; // 400% damage

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Set<UUID> pinnedEntities = new HashSet<>();

    public ImpalerSkill() {
        super(SkillType.SPEARS, 75, "Impaler",
              "Pin an enemy in place, dealing massive damage.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(25, BASE_COOLDOWN_SECONDS - (skillLevel / 4));
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
        if (event instanceof EliteMobDamagedByPlayerEvent dmgEvent) {
            checkAndApply(player, dmgEvent);
        }
    }

    /**
     * Attempts to impale the target.
     * Returns the damage multiplier if successful, 1.0 otherwise.
     */
    public double checkAndApply(Player player, EliteMobDamagedByPlayerEvent event) {
        if (!isActive(player) || isOnCooldown(player)) return 1.0;

        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return 1.0;

        // Don't impale if already pinned
        if (pinnedEntities.contains(eliteEntity.getLivingEntity().getUniqueId())) return 1.0;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
        double multiplier = getDamageMultiplier(skillLevel);

        // Execute the impale
        activateImpale(player, eliteEntity, skillLevel);

        return multiplier;
    }

    private void activateImpale(Player player, EliteEntity target, int skillLevel) {
        LivingEntity living = target.getLivingEntity();
        if (living == null) return;

        UUID targetUUID = living.getUniqueId();
        pinnedEntities.add(targetUUID);

        // Apply root effect (extreme slowness)
        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PIN_DURATION_TICKS, 127));

        // Visual and sound effects
        living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);
        living.getWorld().playSound(living.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.5f);

        // Impale particle effect
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= PIN_DURATION_TICKS || living.isDead()) {
                    pinnedEntities.remove(targetUUID);
                    cancel();
                    return;
                }

                // Visual effect - spear through target
                living.getWorld().spawnParticle(Particle.END_ROD,
                    living.getLocation().add(0, 1, 0), 5, 0.2, 0.5, 0.2, 0.02);

                if (ticks % 10 == 0) {
                    living.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        living.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.1);
                }

                ticks++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText("\u00A7b\u00A7l\u2694 IMPALED! \u2694"));

        startCooldown(player, skillLevel);
        incrementProcCount(player);
    }

    public double getDamageMultiplier(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel);
        }
        return BASE_DAMAGE_MULTIPLIER + (skillLevel * 0.05);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        activePlayers.remove(player.getUniqueId());
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
            "&7Damage: &f" + String.format("%.0f", getDamageMultiplier(skillLevel) * 100) + "%",
            "&7Pin Duration: &f3 seconds",
            "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageMultiplier(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("%.0f%% + Pin (CD: %ds)", getDamageMultiplier(skillLevel) * 100, getCooldownSeconds(skillLevel));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        cooldowns.clear();
        pinnedEntities.clear();
    }
}
