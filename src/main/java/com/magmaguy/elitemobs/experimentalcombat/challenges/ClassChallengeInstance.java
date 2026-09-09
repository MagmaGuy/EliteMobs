package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.instanced.InstancePlayerManager;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

/** One solo run borrowing a league's physical container, without its waves or rewards. */
public final class ClassChallengeInstance extends MatchInstance implements Listener {
    private final java.util.UUID runId = java.util.UUID.randomUUID();
    private final ArenaContainer container;
    private final Player challenger;
    private final ClassTrialDefinition trial;
    private final double quotedFee;
    private CustomBossEntity instructor;
    private BukkitTask combatTask;
    private BukkitTask cleanupTask;
    private boolean ending;
    private boolean closing;
    private int elapsedTicks;
    private boolean feeCharged;
    private boolean classUnlocked;

    public java.util.UUID runId() { return runId; }

    private ClassChallengeInstance(ArenaContainer container, Player player,
                                   ClassTrialDefinition trial, double quotedFee) {
        super(container.start(), container.exit(), 1, 1);
        this.container = container;
        this.challenger = player;
        this.trial = trial;
        this.quotedFee = quotedFee;
        world = container.start().getWorld();
        lobbyLocation = container.start();
    }

    public static double fee(ClassFormDefinition form) {
        if (form.band() == com.magmaguy.elitemobs.experimentalcombat.classes.ClassBand.STARTER) return 1D;
        return 100D * Math.max(0, ItemLootShower.getCurrencyAmount(form.requiredFoundationSkillLevel()));
    }

    public static void admit(Player player, String formId, double quotedFee) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Trial admission requires the server thread");
        var module = ExperimentalCombatModule.get();
        if (!module.canChallenge(player, formId)) {
            tell(player, "&cMeet this class's training requirements before challenging its instructor.");
            return;
        }
        var form = module.catalog().require(formId);
        if (Double.compare(quotedFee, fee(form)) != 0) {
            tell(player, "&eThe entry fee changed. Reopen the class menu for the current price.");
            return;
        }
        ArenaInstance league = ArenaInstance.getArenaInstances().get("wood_league.yml");
        if (league == null || league.isDefunct()) {
            tell(player, "&cThe Wood League arena is not available.");
            return;
        }
        if (league.getContainer().occupied()) {
            tell(player, "&eThe arena is in use. Wait for the current group or class trial to finish.");
            return;
        }
        if (EconomyHandler.checkCurrency(player.getUniqueId()) < quotedFee) {
            tell(player, "&cThis attempt costs " + EconomyHandler.formatCurrency(quotedFee) + " coins.");
            return;
        }
        ClassTrialDefinition trial;
        try {
            trial = ClassTrialDefinition.forForm(formId);
        } catch (RuntimeException failure) {
            Logger.warn("Could not prepare class trial " + formId + ": " + failure.getMessage());
            tell(player, "&cThis instructor's trial is unavailable. Ask an administrator to check its required content. You were not charged.");
            return;
        }
        ClassChallengeInstance run = new ClassChallengeInstance(league.getContainer(), player, trial, quotedFee);
        try {
            if (run.isDefunct() || !InstancePlayerManager.addNewPlayers(java.util.List.of(player), run,
                    () -> module.canChallenge(player, formId))) {
                run.destroyMatch();
                return;
            }
            Bukkit.getPluginManager().registerEvents(run, MetadataHandler.PLUGIN);
            player.closeInventory();
            run.countdownMatch();
        } catch (RuntimeException failure) {
            run.destroyMatch();
            Logger.warn("Could not admit class trial " + formId + ": " + failure.getMessage());
            tell(player, "&cThe trial could not begin. You were not charged.");
        }
    }

    @Override protected boolean isInRegion(Location location) { return container.contains(location); }
    @Override protected Location participantExitLocation(Player player) { return previousLocationOrExit(player); }
    @Override public boolean isAcceptingNewPlayers() {
        return !closing && super.isAcceptingNewPlayers() && container.availableTo(this);
    }
    @Override protected boolean reserveAdmission() { return !closing && container.acquire(this); }
    @Override protected void abortAdmission() { if (players.isEmpty()) container.release(this); }
    @Override protected boolean isAcceptingSpectator(Player player, boolean wasPlayer) { return false; }

    @Override protected void startMatch() {
        if (closing || !challenger.isOnline() || !players.contains(challenger)
                || !container.contains(challenger.getLocation())) {
            destroyMatch();
            return;
        }
        try {
            instructor = new CustomBossEntity(trial.boss());
            Location spawn = container.spawnPoint("north");
            if (spawn == null || !container.contains(spawn))
                throw new IllegalStateException("Trial arena requires an interior north spawn point");
            instructor.spawn(spawn, true);
            if (!instructor.exists()) throw new IllegalStateException("Instructor spawn was rejected");
            requireActivePowers();
            if (!EconomyHandler.tryWithdraw(challenger.getUniqueId(), quotedFee)) {
                tell(challenger, "&cThe entry fee could not be paid. The trial was cancelled.");
                destroyMatch();
                return;
            }
            feeCharged = true;
            super.startMatch();
            if (instructor.getLivingEntity() instanceof Mob mob) mob.setTarget(challenger);
            combatTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::tickCombat, 5, 5);
        } catch (RuntimeException failure) {
            Logger.warn("Class trial " + trial.form().id() + " failed: " + failure.getMessage());
            tell(challenger, "&cThe instructor could not complete this trial.");
            refund();
            endMatch();
        }
    }

    private void tickCombat() {
        if (closing || state != InstancedRegionState.ONGOING) return;
        if (!instructor.exists()) {
            // Despawns, reloads and other removals never count as completing training.
            tell(challenger, "&cThe instructor disappeared; no class was unlocked.");
            refund();
            endMatch();
            return;
        }
        try {
            requireActivePowers();
        } catch (RuntimeException failure) {
            Logger.warn("Class trial combat failed for " + trial.form().id() + ": " + failure.getMessage());
            refund();
            endMatch();
            return;
        }
        if ((elapsedTicks += 5) >= 6000 && state == InstancedRegionState.ONGOING) {
            tell(challenger, "&eThe trial time limit was reached.");
            defeat();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectTrial(EliteMobDamagedByPlayerEvent event) {
        if (!owns(event.getEliteMobEntity())) return;
        if (state != InstancedRegionState.ONGOING || !event.getPlayer().equals(challenger)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectBystanders(PlayerDamagedByEliteMobEvent event) {
        if (!owns(event.getEliteMobEntity())) return;
        if (state != InstancedRegionState.ONGOING || !event.getPlayer().equals(challenger))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void instructorDefeated(EliteMobDeathEvent event) {
        if (event.getEliteEntity() != instructor || closing || state != InstancedRegionState.ONGOING
                || !challenger.isOnline() || !players.contains(challenger)) return;
        if (ExperimentalCombatModule.get().completeChallenge(challenger, trial.form().id())) {
            classUnlocked = true;
            victory();
        } else {
            tell(challenger, "&cYour class data became unavailable. Contact an administrator about this victory.");
            refund();
            endMatch();
        }
    }


    @Override protected void endMatch() {
        if (closing || ending) return;
        ending = true;
        if (combatTask != null) combatTask.cancel();
        super.endMatch();
        // Let the canonical death/leave callback finish before evacuating its participants.
        cleanupTask = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, this::destroyMatch);
    }

    @Override protected void destroyMatch() {
        if (closing) return;
        closing = true;
        instances.remove(this);
        cancelScheduledTasks();
        if (combatTask != null) combatTask.cancel();
        if (cleanupTask != null) cleanupTask.cancel();
        HandlerList.unregisterAll(this);
        try {
            if (feeCharged && state == InstancedRegionState.ONGOING) refund();
            if (instructor != null) instructor.remove(RemovalReason.ARENA_RESET);
            super.destroyMatch();
        } finally {
            container.release(this);
        }
        if (classUnlocked && challenger.isOnline() && ExperimentalCombatModule.isInitialized()) {
            ExperimentalCombatModule.get().onClassTrialEnded(challenger);
            tell(challenger, "&aUnlocked and activated " + trial.form().displayName() + "!");
            challenger.sendTitle(ChatColorConverter.convert("&6Class Unlocked!"),
                    ChatColorConverter.convert("&f" + trial.form().displayName() + " &ais now active"),
                    10, 70, 20);
            challenger.playSound(challenger.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }

    private boolean owns(com.magmaguy.elitemobs.mobconstructor.EliteEntity entity) {
        var visited = new java.util.HashSet<com.magmaguy.elitemobs.mobconstructor.EliteEntity>();
        while (entity != null && visited.add(entity)) {
            if (entity == instructor) return true;
            entity = entity.getSummoningEntity();
        }
        return false;
    }

    private void requireActivePowers() {
        var powers = instructor.getElitePowers().stream()
                .filter(com.magmaguy.elitemobs.powers.lua.LuaElitePower.class::isInstance)
                .map(com.magmaguy.elitemobs.powers.lua.LuaElitePower.class::cast).toList();
        if (powers.isEmpty() || powers.stream().anyMatch(power -> !power.isRuntimeActive()))
            throw new IllegalStateException("An instructor's required Lua power is unavailable");
    }

    private void refund() {
        if (!feeCharged) return;
        feeCharged = false;
        if (EconomyHandler.refundPayment(challenger.getUniqueId(), quotedFee))
            tell(challenger, "&eYour trial entry fee was refunded.");
        else Logger.warn("Trial refund failed for " + challenger.getUniqueId() + ": " + quotedFee);
    }

    private static void tell(Player player, String text) { if (player.isOnline()) player.sendMessage(ChatColorConverter.convert(text)); }
}

