package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Arrow Rain (COOLDOWN) - Rain arrows on target location.
 * Tier 4 unlock.
 */
public class ArrowRainSkill extends SkillBonus implements CooldownSkill {

    public static final String SKILL_ID = "crossbows_arrow_rain";
    private static final long BASE_COOLDOWN = 30; // 30 seconds
    private static final double BASE_ARROW_DAMAGE = 0.30; // 30% of original

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Set<UUID> onCooldown = new HashSet<>();
    private static final Map<UUID, Long> cooldownEndTimes = new HashMap<>();

    public ArrowRainSkill() {
        super(SkillType.CROSSBOWS, 75, "Arrow Rain",
              "Rain arrows on the target location.",
              SkillBonusType.COOLDOWN, 4, SKILL_ID);
    }

    @Override
    public long getCooldownSeconds(int skillLevel) {
        return Math.max(15, BASE_COOLDOWN - (skillLevel / 5)); // 30s base, min 15s
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
        if (damageEvent.getEliteMobEntity().getLivingEntity() == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
        double arrowDamage = damageEvent.getDamage() * getArrowDamageMultiplier(skillLevel);
        Location targetLoc = damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0, 10, 0);

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 5) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 3; i++) {
                    Location spawnLoc = targetLoc.clone().add(
                            ThreadLocalRandom.current().nextDouble(-2, 2),
                            0,
                            ThreadLocalRandom.current().nextDouble(-2, 2)
                    );
                    Arrow arrow = player.getWorld().spawn(spawnLoc, Arrow.class);
                    arrow.setShooter(player);
                    arrow.setVelocity(new Vector(0, -2, 0));
                    arrow.setDamage(arrowDamage);
                    arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                }
                count++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§b§lARROW RAIN!"));
    }

    private double getArrowDamageMultiplier(int skillLevel) {
        return BASE_ARROW_DAMAGE + (skillLevel * 0.005); // 30% base + 0.5% per level
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
                "&7Arrow Damage: &f" + String.format("%.0f", getArrowDamageMultiplier(skillLevel) * 100) + "%",
                "&7Arrows: &f15 total (3x5 waves)",
                "&7Cooldown: &f" + getCooldownSeconds(skillLevel) + "s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) { return getArrowDamageMultiplier(skillLevel); }
    @Override
    public String getFormattedBonus(int skillLevel) { return String.format("%.0f%% arrow damage", getArrowDamageMultiplier(skillLevel) * 100); }
    @Override
    public void shutdown() {
        activePlayers.clear();
        onCooldown.clear();
        cooldownEndTimes.clear();
    }
}
