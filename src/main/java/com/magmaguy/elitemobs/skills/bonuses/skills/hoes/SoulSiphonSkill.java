package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Soul Siphon (STACKING) - Gain stacking damage bonus on kills.
 * Stacks decay after 30 seconds without a kill.
 * Tier 2 unlock.
 */
public class SoulSiphonSkill extends SkillBonus implements StackingSkill {

    public static final String SKILL_ID = "hoes_soul_siphon";
    private static final int MAX_STACKS = 10;
    /**
     * Stacks fall off after 12s without a kill. The old 30s window meant any sustained farming
     * pace kept the stack bar permanently full, so the bonus behaved as a flat passive instead
     * of the intermittent ramp its per-stack value is priced for.
     */
    private static final long STACK_DECAY_TIME = 12000;
    private static final double BASE_BONUS_PER_STACK = 0.02; // 2% per stack

    private static final Map<UUID, Integer> soulStacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastKillTime = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitRunnable> decayTasks = new ConcurrentHashMap<>();
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

    /**
     * The elite-death listener is registered lazily, the first time any player actually selects
     * this skill, and torn down on shutdown. Servers where nobody runs Soul Siphon never pay for
     * an extra handler on every elite death.
     */
    private static final AtomicBoolean killListenerRegistered = new AtomicBoolean(false);
    private static SoulSiphonKillListener killListener = null;

    public SoulSiphonSkill() {
        super(SkillType.HOES, 25, "Soul Siphon",
              "Each kill grants a stacking damage bonus that decays over time.",
              SkillBonusType.STACKING, 2, SKILL_ID);
    }

    @Override
    public int getMaxStacks() {
        return MAX_STACKS;
    }

    /**
     * Souls are banked on kills by the elite-death listener, not by the on-hit dispatcher; hits
     * only read the current stacks so the bonus applies to every swing while the stacks are alive.
     */
    @Override
    public boolean banksStacksExternally() {
        return true;
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
        if (configFields != null) return configFields.calculateValue(skillLevel) / getMaxStacks();
        return scaled(BASE_BONUS_PER_STACK, 0.0004, skillLevel); // 2% base + 0.04% per level
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
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(DungeonsConfig.getSoulSiphonDecayMessage()));
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

    /**
     * Registers the elite-death listener the first time a player picks this skill up.
     */
    private static void ensureKillListenerRegistered() {
        if (MetadataHandler.PLUGIN == null) return;
        if (!killListenerRegistered.compareAndSet(false, true)) return;
        killListener = new SoulSiphonKillListener();
        Bukkit.getPluginManager().registerEvents(killListener, MetadataHandler.PLUGIN);
    }

    @Override
    public void applyBonus(Player player, int skillLevel) {
        ensureKillListenerRegistered();
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void removeBonus(Player player) {
        resetStacks(player);
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public void onActivate(Player player) {
        ensureKillListenerRegistered();
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
        return applyLoreTemplates(Map.of(
                "bonusPerStack", String.format("%.1f", bonusPerStack),
                "maxStacks", String.valueOf(MAX_STACKS),
                "maxBonus", String.format("%.1f", maxBonus)
        ));
    }

    @Override
    public double getBonusValue(int skillLevel) {
        return getBonusPerStack(skillLevel);
    }

    @Override
    public String getFormattedBonus(int skillLevel) {
        return applyFormattedBonusTemplate(Map.of(
                "bonusPerStack", String.format("%.1f", getBonusPerStack(skillLevel) * 100)
        ));
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
        if (killListener != null) {
            HandlerList.unregisterAll(killListener);
            killListener = null;
        }
        killListenerRegistered.set(false);
    }

    /**
     * Grants a soul stack when a player finishes off an elite with a hoe.
     * <p>
     * The stack used to be granted by the generic STACKING hit path in
     * {@link com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent}, which meant every swing
     * banked a soul and the 12 second decay window never lapsed - the bar sat permanently full
     * and the skill paid its full-stack bonus as if it were a flat passive.
     */
    public static class SoulSiphonKillListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onEliteMobDeath(EliteMobDeathEvent event) {
            if (activePlayers.isEmpty()) return;
            EntityDeathEvent entityDeathEvent = event.getEntityDeathEvent();
            if (entityDeathEvent == null || entityDeathEvent.getEntity() == null) return;
            Player killer = entityDeathEvent.getEntity().getKiller();
            if (killer == null) return;
            // Souls are reaped with a scythe: only hoe kills bank a stack, matching the weapon
            // the bonus can actually be spent with.
            if (SkillType.fromMaterial(killer.getInventory().getItemInMainHand().getType()) != SkillType.HOES) return;
            onKill(killer);
        }
    }
}
