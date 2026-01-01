package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Soul Siphon (STACKING) - Gain stacking damage bonus on kills.
 * Stacks decay after 30 seconds without a kill.
 * Tier 2 unlock.
 */
public class SoulSiphonSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "hoes_soul_siphon";
    private static final int MAX_STACKS = 10;
    private static final long STACK_DECAY_TIME = 30000; // 30 seconds
    private static final double BASE_BONUS_PER_STACK = 0.05; // 5% per stack

    private static final Map<UUID, Integer> soulStacks = new HashMap<>();
    private static final Map<UUID, Long> lastKillTime = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> decayTasks = new HashMap<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    public SoulSiphonSkill() {
        super(SkillType.HOES, 25, "Soul Siphon",
              "Each kill grants a stacking damage bonus that decays over time.",
              SkillBonusType.STACKING, 2, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return MAX_STACKS;
    }

    @Override
    public int getCurrentStacks(Player player) {
        checkAndDecayStacks(player);
        return soulStacks.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public void addStack(Player player) {
        UUID uuid = player.getUniqueId();
        int current = getCurrentStacks(player);

        if (current < MAX_STACKS) {
            soulStacks.put(uuid, current + 1);
            player.getWorld().spawnParticle(Particle.SOUL,
                player.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.02);

            // Send stack count feedback
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§5Soul Siphon: " + (current + 1) + "/" + MAX_STACKS));
        }

        lastKillTime.put(uuid, System.currentTimeMillis());
        scheduleDecay(player);
    }

    @Override
    public void resetStacks(Player player) {
        UUID uuid = player.getUniqueId();
        soulStacks.remove(uuid);
        lastKillTime.remove(uuid);

        // Cancel decay task
        BukkitRunnable task = decayTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public double getBonusPerStack(int skillLevel) {
        if (configFields != null) {
            return configFields.calculateValue(skillLevel) / MAX_STACKS;
        }
        // Base 5% per stack + 0.1% per level
        return BASE_BONUS_PER_STACK + (skillLevel * 0.001);
    }

    private void checkAndDecayStacks(Player player) {
        Long lastKill = lastKillTime.get(player.getUniqueId());
        if (lastKill != null && System.currentTimeMillis() - lastKill > STACK_DECAY_TIME) {
            resetStacks(player);
        }
    }

    private void scheduleDecay(Player player) {
        UUID uuid = player.getUniqueId();

        // Cancel existing decay task
        BukkitRunnable oldTask = decayTasks.remove(uuid);
        if (oldTask != null) {
            oldTask.cancel();
        }

        // Schedule new decay
        BukkitRunnable decayTask = new BukkitRunnable() {
            @Override
            public void run() {
                resetStacks(player);
                decayTasks.remove(uuid);
                if (player.isOnline()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§7Soul Siphon stacks decayed"));
                }
            }
        };

        decayTask.runTaskLater(MetadataHandler.PLUGIN, STACK_DECAY_TIME / 50); // Convert ms to ticks
        decayTasks.put(uuid, decayTask);
    }

    /**
     * Called when a player kills an elite mob.
     */
    public static void onKill(Player player) {
        if (!activePlayers.contains(player.getUniqueId())) return;

        // Get the instance from registry to call addStack
        SkillBonus skill = com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry.getSkillById(SKILL_ID);
        if (skill instanceof SoulSiphonSkill soulSiphon) {
            soulSiphon.addStack(player);
        }
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        resetStacks(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void onDeactivate(Player player) {
        resetStacks(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public List<String> getLoreDescription(int skillLevel) {
        double bonusPerStack = getBonusPerStack(skillLevel) * 100;
        double maxBonus = bonusPerStack * MAX_STACKS;
        return List.of(
                "&7Bonus per Stack: &f+" + String.format("%.1f", bonusPerStack) + "%",
                "&7Max Stacks: &f" + MAX_STACKS,
                "&7Max Bonus: &f+" + String.format("%.1f", maxBonus) + "%",
                "&7Stacks decay after 30s"
        );
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return String.format("+%.1f%% per Soul Stack", getBonusPerStack(skillLevel) * 100);
    }

    @Override
    public void shutdown() {
        for (BukkitRunnable task : decayTasks.values()) {
            task.cancel();
        }
        decayTasks.clear();
        soulStacks.clear();
        lastKillTime.clear();
        activePlayers.clear();
    }
}
