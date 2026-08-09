package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Avatar of Judgment (COOLDOWN) - Massive damage boost with visual effects for 10 seconds.
 * Tier 4 unlock.
 */
public class AvatarOfJudgmentSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "maces_avatar_of_judgment";
    private static final long BASE_COOLDOWN_SECONDS = 90;
    private static final int BUFF_DURATION_TICKS = 200; // 10 seconds

    /**
     * Skill level every balance figure on this class is quoted at, matching the rest of the
     * rebalance.
     */
    public static final int REFERENCE_SKILL_LEVEL = 50;

    /**
     * Total damage multiplier while the Avatar buff is up. Quoted at {@link #REFERENCE_SKILL_LEVEL}.
     * <p>
     * Priced on the shared sustained-damage budget {@code E = trigger_rate * (multiplier - 1) = 0.20}.
     * Unlike every other offensive skill the trigger rate here is not a per-swing proc but a duty
     * cycle: the buff covers a {@value #BUFF_DURATION_TICKS} tick (10 second) window out of each
     * cooldown, so {@code trigger_rate = 10 / getCooldownSeconds(50) = 10 / 74 = 0.135}.
     * <p>
     * At the old 5.0x the skill was worth {@code 0.135 * (5.0 - 1) = 0.541}, 2.7x its budget.
     * Solving for budget at the unchanged cooldown gives {@code multiplier - 1 = 0.20 * 74 / 10 =
     * 1.48}, hence 2.48x. Buying the same correction from the cooldown instead would need
     * {@code 10 * 4.0 / 0.20 = 200} seconds, which is not a tier 4 cooldown any player would slot.
     * <p>
     * Flat rather than {@code base + level * rate}: the cooldown already shortens with level
     * (74s at 50, 60s at 100), so a multiplier that also climbed with level would compound two
     * ramps and put the skill back over budget. Flat and hardcoded also matches how the rest of
     * the rebalance settled - balance values no longer come from config, only presentation does.
     * <p>
     * Read together with {@code EliteMobDamagedByPlayerEvent.applySkillBonuses()}, which merges
     * {@link #getDamageBoost(int)} as the multiplier itself (not as {@code 1.0 + x} the way the
     * mark/debuff bonuses are merged). The budgeted quantity is therefore {@code multiplier - 1}.
     */
    private static final double DAMAGE_BOOST_MULTIPLIER = 2.48;

    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> buffedPlayers = ConcurrentHashMap.newKeySet();

    public AvatarOfJudgmentSkill() {
        super(SkillType.MACES, 75, "Avatar of Judgment",
              "Become an avatar of divine wrath. Massive damage boost for 10 seconds.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        if (configFields != null && configFields.getCooldownSeconds() > 0)
            return Math.max(1L, Math.round(configFields.calculateCooldown(skillLevel)));
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

        // Particle aura while active
        new BukkitRunnable() {
            int ticksRemaining = BUFF_DURATION_TICKS;

            @Override
            public void run() {
                if (ticksRemaining <= 0 || !player.isOnline() || !buffedPlayers.contains(player.getUniqueId())) {
                    buffedPlayers.remove(player.getUniqueId());
                    if (player.isOnline()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(DungeonsConfig.getAvatarFadesMessage()));
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

    /**
     * The total damage multiplier applied while the Avatar buff is up.
     * <p>
     * This is the multiplier, not a bonus fraction - see {@link #DAMAGE_BOOST_MULTIPLIER} for how
     * it is merged and why it is 2.48x. Do not shorten the cooldown alongside it: the two together
     * are what keep the sustained value on budget.
     */
    public double getDamageBoost(int skillLevel) {
        if (configFields != null) return configFields.calculateValue(skillLevel);
        return DAMAGE_BOOST_MULTIPLIER;
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
    public boolean affectsDamage() {
        return false; // Avatar triggers as a side effect; damage boost is applied via hasAvatarBuff() check
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        return applyLoreTemplates(Map.of(
                "damageBoost", String.format("%.0f", (getDamageBoost(skillLevel) - 1) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getDamageBoost(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "damageBoost", String.format("%.0f", (getDamageBoost(skillLevel) - 1) * 100),
                "cooldown", String.valueOf(getCooldownSeconds(skillLevel))
        ));
    }

    @Override
    public void shutdown() {
        activePlayers.clear();
        buffedPlayers.clear();
        cooldowns.clear();
    }
}
