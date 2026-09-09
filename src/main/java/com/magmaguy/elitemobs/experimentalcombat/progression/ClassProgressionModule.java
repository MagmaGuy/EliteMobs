package com.magmaguy.elitemobs.experimentalcombat.progression;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe cache and domain Module for Experimental Combat progression.
 *
 * <p>All store access runs through one serial executor. Reads, awards and selections use only
 * immutable catalog data and the loaded cache, so gameplay callers never perform JDBC work.</p>
 */
public final class ClassProgressionModule {


    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger();

    private final ClassCatalog catalog;
    private final int catalogVersion;
    private final FoundationLevels foundationLevels;
    private final ClassContentAvailability contentAvailability;
    private final ClassProgressionStore store;
    private final SerialExecutor persistenceExecutor;
    private final ExecutorService ownedExecutor;
    private final Map<UUID, CachedPlayer> players = new ConcurrentHashMap<>();
    private final Map<UUID, LockedRun> runSelections = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicReference<Throwable> persistenceFailure = new AtomicReference<>();

    private CompletableFuture<Void> closeFuture;

    /** Creates a Module with its own daemon persistence thread. */
    public ClassProgressionModule(
            ClassCatalog catalog,
            FoundationLevels foundationLevels,
            ClassProgressionStore store) {
        this(catalog, foundationLevels, store, ClassContentAvailability.ALL_AVAILABLE);
    }

    public ClassProgressionModule(
            ClassCatalog catalog,
            FoundationLevels foundationLevels,
            ClassProgressionStore store,
            ClassContentAvailability contentAvailability) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.catalogVersion = catalog.persistenceVersion();
        this.foundationLevels = Objects.requireNonNull(foundationLevels, "foundationLevels");
        this.contentAvailability = Objects.requireNonNull(contentAvailability, "contentAvailability");
        this.store = Objects.requireNonNull(store, "store");
        this.ownedExecutor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task,
                    "EliteMobs-ClassProgression-" + THREAD_SEQUENCE.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        this.persistenceExecutor = new SerialExecutor(ownedExecutor);
    }

    /**
     * Creates a Module over a supplied asynchronous executor; calls are serialized even if it
     * is a pool. The supplied executor must never dispatch work onto the server's main thread.
     */
    public ClassProgressionModule(
            ClassCatalog catalog,
            FoundationLevels foundationLevels,
            ClassProgressionStore store,
            Executor persistenceExecutor) {
        this(catalog, foundationLevels, store, persistenceExecutor,
                ClassContentAvailability.ALL_AVAILABLE);
    }

    public ClassProgressionModule(
            ClassCatalog catalog,
            FoundationLevels foundationLevels,
            ClassProgressionStore store,
            Executor persistenceExecutor,
            ClassContentAvailability contentAvailability) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.catalogVersion = catalog.persistenceVersion();
        this.foundationLevels = Objects.requireNonNull(foundationLevels, "foundationLevels");
        this.contentAvailability = Objects.requireNonNull(contentAvailability, "contentAvailability");
        this.store = Objects.requireNonNull(store, "store");
        this.ownedExecutor = null;
        this.persistenceExecutor = new SerialExecutor(
                Objects.requireNonNull(persistenceExecutor, "persistenceExecutor"));
    }

    public static ClassProgressionModule builtIn(
            FoundationLevels foundationLevels,
            ClassProgressionStore store) {
        return new ClassProgressionModule(
                BuiltInClassContent.catalog(), foundationLevels, store);
    }

    public static ClassProgressionModule builtIn(
            FoundationLevels foundationLevels,
            ClassProgressionStore store,
            Executor persistenceExecutor) {
        return new ClassProgressionModule(
                BuiltInClassContent.catalog(), foundationLevels, store, persistenceExecutor);
    }

    /**
     * Loads, validates and publishes one player's complete profile without blocking the caller.
     * The injected FoundationLevels source must already have that player's skill data available.
     */
    public CompletableFuture<ProfileSnapshot> load(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        if (closed.get())
            return CompletableFuture.failedFuture(new IllegalStateException("Progression Module is closed"));

        CachedPlayer candidate = new CachedPlayer();
        CachedPlayer state = players.putIfAbsent(playerId, candidate);
        if (state != null) {
            synchronized (state.monitor) {
                if (state.readiness == ProgressionReadiness.READY)
                    return CompletableFuture.completedFuture(
                            snapshotLocked(playerId, state, captureFoundationLevels(playerId)));
                if (state.readiness == ProgressionReadiness.FAILED)
                    return CompletableFuture.failedFuture(state.failure == null
                            ? new IllegalStateException("Player progression is unavailable")
                            : state.failure);
                return state.loadFuture;
            }
        }
        state = candidate;

        CachedPlayer loadingState = state;
        submitPersistence(() -> hydrate(playerId, loadingState)).whenComplete((snapshot, failure) -> {
            if (failure == null) {
                loadingState.loadFuture.complete(snapshot);
                return;
            }
            Throwable cause = unwrap(failure);
            if (cause instanceof SQLException) persistenceFailure.compareAndSet(null, cause);
            fail(playerId, loadingState, cause);
            loadingState.loadFuture.completeExceptionally(cause);
        });
        return state.loadFuture;
    }

    public ProgressionReadiness readiness(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        if (closed.get()) return ProgressionReadiness.CLOSED;
        CachedPlayer state = players.get(playerId);
        return state == null ? ProgressionReadiness.UNLOADED : state.readiness;
    }

    public boolean isReady(UUID playerId) {
        return readiness(playerId) == ProgressionReadiness.READY;
    }

    public Optional<Throwable> failure(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        CachedPlayer state = players.get(playerId);
        return state == null ? Optional.empty() : Optional.ofNullable(state.failure);
    }

    /** Returns an immutable current view only after successful hydration. */
    public Optional<ProfileSnapshot> snapshot(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        CachedPlayer state = players.get(playerId);
        if (closed.get() || state == null) return Optional.empty();
        synchronized (state.monitor) {
            if (state.readiness != ProgressionReadiness.READY) return Optional.empty();
            return Optional.of(snapshotLocked(playerId, state, captureFoundationLevels(playerId)));
        }
    }

    public Optional<SkillTutorialProgress> tutorialProgress(UUID playerId) {
        CachedPlayer state = readyState(playerId);
        if (state == null) return Optional.empty();
        synchronized (state.monitor) {
            if (!isReady(state)) return Optional.empty();
            return Optional.of(new SkillTutorialProgress(state.profile.tutorialSkillsUsed()));
        }
    }

    /** Records a successful cast once, using the same ordered persistence queue as class selections. */
    public Optional<SkillTutorialProgress> recordTutorialSkill(UUID playerId, AbilitySlot slot) {
        CachedPlayer state = readyState(playerId);
        if (state == null) return Optional.empty();
        synchronized (state.monitor) {
            if (!isReady(state)) return Optional.empty();
            SkillTutorialProgress current = new SkillTutorialProgress(state.profile.tutorialSkillsUsed());
            if (current.hasUsed(slot)) return Optional.empty();
            SkillTutorialProgress updated = current.withUsed(slot);
            state.profile = new StoredClassProfile(playerId, state.profile.selectedFormId(),
                    state.profile.selectedInputId(), catalogVersion, updated.usedSkills());
            enqueueProfileSave(playerId, state);
            return Optional.of(updated);
        }
    }

    /** Drops loaded profile state. The run lock remains until its matching instance unlocks it. */
    public void unload(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        players.computeIfPresent(playerId, (ignored, state) -> {
            synchronized (state.monitor) {
                state.readiness = ProgressionReadiness.UNLOADED;
                if (!state.loadFuture.isDone())
                    state.loadFuture.completeExceptionally(
                            new CancellationException("Player progression unloaded"));
            }
            return null;
        });
    }

    public SelectionResult selectForm(UUID playerId, String formId) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(formId, "formId");
        CachedPlayer state = readyState(playerId);
        if (state == null) return new SelectionResult(SelectionResult.Status.NOT_READY, null);

        synchronized (state.monitor) {
            if (!isReady(state)) return new SelectionResult(SelectionResult.Status.NOT_READY, null);
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            Optional<ClassFormDefinition> candidate = catalog.find(formId);
            if (candidate.isEmpty())
                return new SelectionResult(SelectionResult.Status.UNKNOWN_FORM,
                        snapshotLocked(playerId, state, levels));
            if (!isUnlocked(candidate.get(), state.progressXp, state.challenges, levels, new HashMap<>()))
                return new SelectionResult(SelectionResult.Status.LOCKED_FORM,
                        snapshotLocked(playerId, state, levels));
            if (formId.equals(state.profile.selectedFormId()))
                return new SelectionResult(SelectionResult.Status.UNCHANGED,
                        snapshotLocked(playerId, state, levels));

            state.profile = new StoredClassProfile(
                    playerId,
                    formId,
                    state.profile.selectedInputId(),
                    catalogVersion, state.profile.tutorialSkillsUsed());
            enqueueProfileSave(playerId, state);
            return new SelectionResult(SelectionResult.Status.APPLIED,
                    snapshotLocked(playerId, state, levels));
        }
    }

    public SelectionResult clearSelectedForm(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        CachedPlayer state = readyState(playerId);
        if (state == null) return new SelectionResult(SelectionResult.Status.NOT_READY, null);

        synchronized (state.monitor) {
            if (!isReady(state)) return new SelectionResult(SelectionResult.Status.NOT_READY, null);
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            if (state.profile.selectedFormId() == null)
                return new SelectionResult(SelectionResult.Status.UNCHANGED,
                        snapshotLocked(playerId, state, levels));
            state.profile = new StoredClassProfile(
                    playerId,
                    null,
                    state.profile.selectedInputId(),
                    catalogVersion, state.profile.tutorialSkillsUsed());
            enqueueProfileSave(playerId, state);
            return new SelectionResult(SelectionResult.Status.APPLIED,
                    snapshotLocked(playerId, state, levels));
        }
    }

    public SelectionResult selectInputProfile(UUID playerId, InputProfile inputProfile) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(inputProfile, "inputProfile");
        CachedPlayer state = readyState(playerId);
        if (state == null) return new SelectionResult(SelectionResult.Status.NOT_READY, null);

        synchronized (state.monitor) {
            if (!isReady(state)) return new SelectionResult(SelectionResult.Status.NOT_READY, null);
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            if (inputProfile.storedId().equals(state.profile.selectedInputId()))
                return new SelectionResult(SelectionResult.Status.UNCHANGED,
                        snapshotLocked(playerId, state, levels));
            state.profile = new StoredClassProfile(
                    playerId,
                    state.profile.selectedFormId(),
                    inputProfile.storedId(),
                    catalogVersion, state.profile.tutorialSkillsUsed());
            enqueueProfileSave(playerId, state);
            return new SelectionResult(SelectionResult.Status.APPLIED,
                    snapshotLocked(playerId, state, levels));
        }
    }

    /** Locks the selected class and its controls for this run. */
    public RunLockResult lockRun(UUID playerId, UUID runId) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(runId, "runId");
        if (closed.get()) return new RunLockResult(RunLockResult.Status.NOT_READY, runId, null);
        LockedRun existing = runSelections.get(playerId);
        if (existing != null) {
            RunLockResult.Status status = existing.runId().equals(runId)
                    ? RunLockResult.Status.ALREADY_LOCKED
                    : RunLockResult.Status.LOCKED_BY_ANOTHER_RUN;
            return new RunLockResult(status, existing.runId(), existing.selection());
        }

        CachedPlayer state = readyState(playerId);
        if (state == null) return new RunLockResult(RunLockResult.Status.NOT_READY, runId, null);
        synchronized (state.monitor) {
            if (!isReady(state)) return new RunLockResult(RunLockResult.Status.NOT_READY, runId, null);
            String formId = state.profile.selectedFormId();
            if (formId == null)
                return new RunLockResult(RunLockResult.Status.NO_SELECTED_FORM, runId, null);
            ClassFormDefinition form = catalog.find(formId).orElse(null);
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            if (form == null || !isUnlocked(form, state.progressXp, state.challenges, levels, new HashMap<>()))
                return new RunLockResult(RunLockResult.Status.FORM_LOCKED, runId, null);

            RunSelection selection = new RunSelection(formId, selectedInputProfile(state.profile));
            LockedRun lockedRun = new LockedRun(runId, selection);
            LockedRun raced = runSelections.putIfAbsent(playerId, lockedRun);
            if (raced != null) {
                RunLockResult.Status status = raced.runId().equals(runId)
                        ? RunLockResult.Status.ALREADY_LOCKED
                        : RunLockResult.Status.LOCKED_BY_ANOTHER_RUN;
                return new RunLockResult(status, raced.runId(), raced.selection());
            }
            return new RunLockResult(RunLockResult.Status.LOCKED, runId, selection);
        }
    }

    public Optional<RunSelection> runSelection(UUID playerId, UUID runId) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(runId, "runId");
        if (closed.get()) return Optional.empty();
        LockedRun lockedRun = runSelections.get(playerId);
        if (lockedRun == null || !lockedRun.runId().equals(runId)) return Optional.empty();
        return Optional.of(lockedRun.selection());
    }

    public Optional<RunSelection> activeRunSelection(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        if (closed.get()) return Optional.empty();
        LockedRun lockedRun = runSelections.get(playerId);
        return lockedRun == null ? Optional.empty() : Optional.of(lockedRun.selection());
    }

    /** A stale run token cannot clear a newer instance's lock. */
    public boolean unlockRun(UUID playerId, UUID runId) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(runId, "runId");
        CachedPlayer state = players.get(playerId);
        if (state == null) {
            LockedRun lockedRun = runSelections.get(playerId);
            return lockedRun != null
                    && lockedRun.runId().equals(runId)
                    && runSelections.remove(playerId, lockedRun);
        }
        synchronized (state.monitor) {
            LockedRun lockedRun = runSelections.get(playerId);
            return lockedRun != null
                    && lockedRun.runId().equals(runId)
                    && runSelections.remove(playerId, lockedRun);
        }
    }

    /** Awards the selected form, or the memory-locked run form when one exists. */
    public AwardResult award(UUID playerId, long requestedXp) {
        Objects.requireNonNull(playerId, "playerId");
        if (requestedXp <= 0)
            return emptyAward(AwardResult.Status.INVALID_AMOUNT, requestedXp);

        CachedPlayer state = readyState(playerId);
        if (state == null) return emptyAward(AwardResult.Status.NOT_READY, requestedXp);
        synchronized (state.monitor) {
            if (!isReady(state)) return emptyAward(AwardResult.Status.NOT_READY, requestedXp);
            LockedRun lockedRun = runSelections.get(playerId);
            String formId = lockedRun == null
                    ? state.profile.selectedFormId()
                    : lockedRun.selection().formId();
            if (formId == null) return emptyAward(AwardResult.Status.NO_SELECTED_FORM, requestedXp);

            ClassFormDefinition form = catalog.find(formId).orElse(null);
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            if (form == null || !isUnlocked(form, state.progressXp, state.challenges, levels, new HashMap<>()))
                return lockedAward(formId, requestedXp, form, state.progressXp, levels);

            int localCap = form.localProgressionCap(levels::get);
            int effectiveCap = form.effectiveProgressionCap(levels::get);
            ProgressionCapReason capReason = capReason(form, levels);
            long xpAtCap = xpAtLocalCap(form, localCap);
            long storedXp = state.progressXp.getOrDefault(formId, 0L);
            long previousXp = Math.min(storedXp, xpAtCap);
            long headroom = xpAtCap - previousXp;
            long appliedXp = Math.min(requestedXp, headroom);
            long currentXp = previousXp + appliedXp;
            long discardedXp = requestedXp - appliedXp;

            // A lower foundation-skill level temporarily lowers the visible/earnable cap. It must
            // not erase XP the player already earned while the cap was higher. If there is
            // headroom then storedXp cannot be above the current cap, so only an applied award
            // needs persistence here.
            if (appliedXp > 0) {
                state.progressXp.put(formId, currentXp);
                enqueueProgressSave(playerId, state, formId, currentXp);
            }

            int previousLocalLevel = localLevelFromXp(form, previousXp);
            int currentLocalLevel = localLevelFromXp(form, currentXp);
            AwardResult.Status status;
            if (appliedXp == 0) status = AwardResult.Status.AT_CAP;
            else if (discardedXp > 0) status = AwardResult.Status.PARTIALLY_APPLIED;
            else status = AwardResult.Status.APPLIED;

            return new AwardResult(
                    status,
                    formId,
                    requestedXp,
                    appliedXp,
                    discardedXp,
                    previousXp,
                    currentXp,
                    previousLocalLevel,
                    currentLocalLevel,
                    form.band().toEffectiveLevel(currentLocalLevel),
                    localCap,
                    effectiveCap,
                    capReason,
                    limitingSkills(form, levels, capReason));
        }
    }

    /**
     * Permission-gated tester fixture: completes every ancestor band, sets the requested form to
     * an exact visible level, and selects it. Foundation requirements remain authoritative.
     *
     * <p>This is deliberately a domain operation instead of direct command-side persistence so
     * testers cannot create an impossible branch state. It is rejected while a dungeon run owns
     * the player's selection.</p>
     */
    public ClassProgressionSetResult setLineageForAdministration(
            UUID playerId,
            String formId,
            int effectiveLevel) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(formId, "formId");
        ClassFormDefinition requestedForm = catalog.find(formId).orElse(null);
        if (requestedForm == null)
            return progressionSetResult(
                    ClassProgressionSetResult.Status.UNKNOWN_FORM,
                    formId, effectiveLevel, null, 0, List.of(), null);
        if (!requestedForm.band().containsEffectiveLevel(effectiveLevel))
            return progressionSetResult(
                    ClassProgressionSetResult.Status.LEVEL_OUTSIDE_FORM_BAND,
                    formId,
                    effectiveLevel,
                    requestedForm.id(),
                    requestedForm.band().effectiveEnd(),
                    List.of(),
                    snapshot(playerId).orElse(null));
        if (runSelections.containsKey(playerId))
            return progressionSetResult(
                    ClassProgressionSetResult.Status.RUN_LOCKED,
                    formId, effectiveLevel, null, 0, List.of(), snapshot(playerId).orElse(null));

        CachedPlayer state = readyState(playerId);
        if (state == null)
            return progressionSetResult(
                    ClassProgressionSetResult.Status.NOT_READY,
                    formId, effectiveLevel, null, 0, List.of(), null);
        synchronized (state.monitor) {
            if (!isReady(state))
                return progressionSetResult(
                        ClassProgressionSetResult.Status.NOT_READY,
                        formId, effectiveLevel, null, 0, List.of(), null);
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            List<ClassFormDefinition> progressionPath = catalog.progressionPathOf(formId);
            for (ClassFormDefinition lineageForm : progressionPath) {
                int neededLevel = lineageForm.id().equals(formId)
                        ? effectiveLevel
                        : lineageForm.band().effectiveEnd();
                int effectiveCap = lineageForm.effectiveProgressionCap(levels::get);
                if (neededLevel > effectiveCap)
                    return progressionSetResult(
                            ClassProgressionSetResult.Status.FOUNDATION_SKILL_CAP,
                            formId,
                            effectiveLevel,
                            lineageForm.id(),
                            effectiveCap,
                            lineageForm.foundationSkills().limitingSkills(levels::get),
                            snapshotLocked(playerId, state, levels));
            }

            List<StoredClassProgress> persistedRows = new ArrayList<>();
            for (ClassFormDefinition lineageForm : progressionPath) {
                int visibleLevel = lineageForm.id().equals(formId)
                        ? effectiveLevel
                        : lineageForm.band().effectiveEnd();
                int localLevel = lineageForm.band().toLocalLevel(visibleLevel);
                long xp = xpAtLocalCap(lineageForm, localLevel);
                state.progressXp.put(lineageForm.id(), xp);
                state.challenges.add(lineageForm.id());
                persistedRows.add(new StoredClassProgress(
                        playerId, lineageForm.id(), xp, catalogVersion, true));
            }
            state.profile = new StoredClassProfile(
                    playerId,
                    formId,
                    state.profile.selectedInputId(),
                    catalogVersion, state.profile.tutorialSkillsUsed());
            StoredClassProfile persistedProfile = state.profile;
            trackPersistence(playerId, state, submitInternal(() -> {
                store.savePlayerAggregate(persistedProfile, persistedRows);
                return null;
            }));
            return progressionSetResult(
                    ClassProgressionSetResult.Status.APPLIED,
                    formId,
                    effectiveLevel,
                    null,
                    effectiveLevel,
                    List.of(),
                    snapshotLocked(playerId, state, levels));
        }
    }

    /** Revokes a form and all descendants, preserving its ancestors and sibling branches. */
    public ClassProgressionForgetResult forgetForAdministration(UUID playerId, String formId) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(formId, "formId");
        if (catalog.find(formId).isEmpty())
            return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.UNKNOWN_FORM, 0, false);
        CachedPlayer state = readyState(playerId);
        if (state == null)
            return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.NOT_READY, 0, false);
        synchronized (state.monitor) {
            if (!isReady(state))
                return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.NOT_READY, 0, false);
            if (runSelections.containsKey(playerId))
                return new ClassProgressionForgetResult(ClassProgressionForgetResult.Status.RUN_LOCKED, 0, false);

            Set<String> forgotten = new HashSet<>();
            List<StoredClassProgress> rows = new ArrayList<>();
            for (ClassFormDefinition form : catalog.forms()) {
                if (catalog.progressionPathOf(form.id()).stream().noneMatch(ancestor -> ancestor.id().equals(formId)))
                    continue;
                forgotten.add(form.id());
                state.progressXp.put(form.id(), 0L);
                state.challenges.remove(form.id());
                // Explicit false rows also override legacy databases whose column defaults to true.
                rows.add(new StoredClassProgress(playerId, form.id(), 0L, catalogVersion, false));
            }
            boolean selectionCleared = forgotten.contains(state.profile.selectedFormId());
            if (selectionCleared)
                state.profile = new StoredClassProfile(playerId, null, state.profile.selectedInputId(),
                        catalogVersion, state.profile.tutorialSkillsUsed());
            StoredClassProfile profile = state.profile;
            List<StoredClassProgress> persistedRows = List.copyOf(rows);
            trackPersistence(playerId, state, submitInternal(() -> {
                store.savePlayerAggregate(profile, persistedRows);
                return null;
            }));
            return new ClassProgressionForgetResult(
                    ClassProgressionForgetResult.Status.APPLIED, forgotten.size(), selectionCleared);
        }
    }

    /**
     * Permission-gated tester fixture backing {@code /em loot debug}: scales every class tree to
     * one effective level. Forms whose band contains the level are set to it exactly, their
     * ancestors complete, and deeper forms reset, so the whole catalog reflects the requested
     * point of progression and advanced branches stay reachable through their completed ancestors.
     * The selection is kept while it remains valid and cleared otherwise so the tester can pick
     * any branch. Foundation caps clamp rather than reject.
     *
     * @return the number of forms now sitting exactly at the requested level
     */
    public int setAllLineagesForAdministration(UUID playerId, int effectiveLevel) {
        Objects.requireNonNull(playerId, "playerId");
        if (runSelections.containsKey(playerId)) return 0;
        CachedPlayer state = readyState(playerId);
        if (state == null) return 0;
        synchronized (state.monitor) {
            if (!isReady(state)) return 0;
            Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
            int formsAtLevel = 0;
            List<StoredClassProgress> persistedRows = new ArrayList<>();
            for (ClassFormDefinition form : catalog.forms()) {
                long xp;
                if (form.band().effectiveStart() > effectiveLevel) {
                    xp = 0L; // deeper band than the requested point: reset
                } else {
                    int visibleLevel = form.band().containsEffectiveLevel(effectiveLevel)
                            ? effectiveLevel
                            : form.band().effectiveEnd();
                    visibleLevel = Math.min(visibleLevel, form.effectiveProgressionCap(levels::get));
                    if (visibleLevel < form.band().effectiveStart()) {
                        xp = 0L; // foundation cap keeps this band locked
                    } else {
                        if (visibleLevel == effectiveLevel) formsAtLevel++;
                        xp = xpAtLocalCap(form, form.band().toLocalLevel(visibleLevel));
                    }
                }
                state.progressXp.put(form.id(), xp);
                boolean granted = form.band().effectiveStart() <= effectiveLevel;
                if (granted) state.challenges.add(form.id()); else state.challenges.remove(form.id());
                persistedRows.add(new StoredClassProgress(playerId, form.id(), xp, catalogVersion, granted));
            }
            String selectedFormId = state.profile.selectedFormId();
            boolean selectionInvalid = selectedFormId != null
                    && catalog.find(selectedFormId)
                    .map(selected -> selected.band().effectiveStart() > effectiveLevel)
                    .orElse(true);
            if (selectionInvalid) {
                state.profile = new StoredClassProfile(
                        playerId,
                        null,
                        state.profile.selectedInputId(),
                        catalogVersion, state.profile.tutorialSkillsUsed());
            }
            StoredClassProfile persistedProfile = state.profile;
            List<StoredClassProgress> rows = List.copyOf(persistedRows);
            trackPersistence(playerId, state, submitInternal(() -> {
                store.savePlayerAggregate(persistedProfile, rows);
                return null;
            }));
            return formsAtLevel;
        }
    }

    /** Completes after every operation submitted before this call has left the store boundary. */
    public CompletableFuture<Void> flush() {
        if (closed.get()) return closeAsync();
        return this.<Void>submitPersistence(() -> null)
                .thenCompose(ignored -> persistenceOutcome());
    }

    /** Same global ordering barrier, additionally surfacing this player's recorded failure. */
    public CompletableFuture<Void> flush(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        CachedPlayer state = players.get(playerId);
        CompletableFuture<?> tail;
        if (state == null) return flush();
        else {
            synchronized (state.monitor) {
                tail = state.readiness == ProgressionReadiness.LOADING
                        ? state.loadFuture
                        : state.persistenceTail;
            }
        }
        return tail.<Void>handle((ignored, tailFailure) -> {
            Throwable recordedFailure = state.persistenceFailure != null
                    ? state.persistenceFailure
                    : state.failure;
            if (recordedFailure != null) throw new java.util.concurrent.CompletionException(recordedFailure);
            if (tailFailure != null) throw new java.util.concurrent.CompletionException(unwrap(tailFailure));
            return null;
        });
    }

    public synchronized CompletableFuture<Void> closeAsync() {
        if (closeFuture != null) return closeFuture;
        closed.set(true);
        // Let cache mutations which already passed their readiness check enqueue their writes
        // before placing the final persistence barrier.
        for (CachedPlayer state : players.values()) {
            synchronized (state.monitor) {
                // Acquiring and releasing the per-player monitor is the barrier.
            }
        }
        closeFuture = this.<Void>submitInternal(() -> null)
                .thenCompose(ignored -> persistenceOutcome())
                .whenComplete((ignored, failure) -> {
                    players.clear();
                    runSelections.clear();
                    if (ownedExecutor != null) ownedExecutor.shutdown();
                });
        return closeFuture;
    }

    private ProfileSnapshot hydrate(UUID playerId, CachedPlayer state) throws SQLException {
        StoredClassProfile storedProfile = store.loadOrCreateProfile(playerId, catalogVersion);
        if (!playerId.equals(storedProfile.playerId()))
            throw new IllegalStateException("Progression store returned another player's profile");
        requireSupportedCatalogVersion(storedProfile.catalogVersion(), "profile");
        List<StoredClassProgress> storedProgress = store.loadAllProgress(playerId);
        Map<SkillType, Integer> levels = captureFoundationLevels(playerId);
        Map<String, Long> progressXp = new HashMap<>();
        Set<String> challenges = new HashSet<>();
        List<StoredClassProgress> repairedProgress = new ArrayList<>();

        for (StoredClassProgress progress : storedProgress) {
            if (!playerId.equals(progress.playerId()))
                throw new IllegalStateException("Progression store returned another player's form progress");
            requireSupportedCatalogVersion(progress.catalogVersion(), "form " + progress.formId());
            ClassFormDefinition form = catalog.find(progress.formId()).orElse(null);
            if (form == null) {
                String classification = catalog.retiredFormIds().contains(progress.formId())
                        ? "retired"
                        : "unknown";
                throw new IllegalStateException("Stored class progress references " + classification
                        + " form id " + progress.formId() + " without a catalog migration");
            }
            if (progressXp.putIfAbsent(form.id(), progress.xp()) != null)
                throw new IllegalStateException("Progression store returned duplicate form " + form.id());

            if (progress.challengeCompleted()) challenges.add(form.id());
            long normalizedXp = normalizePersistedXp(form, progress.xp());
            progressXp.put(form.id(), normalizedXp);
            if (normalizedXp != progress.xp())
                repairedProgress.add(new StoredClassProgress(
                        playerId, form.id(), normalizedXp, catalogVersion, progress.challengeCompleted()));
        }

        StoredClassProfile repairedProfile = repairProfile(playerId, storedProfile, progressXp, challenges, levels);
        if (!repairedProgress.isEmpty() || !repairedProfile.equals(storedProfile))
            store.savePlayerAggregate(repairedProfile, repairedProgress);

        synchronized (state.monitor) {
            if (players.get(playerId) != state)
                throw new CancellationException("Player progression was unloaded while loading");
            state.profile = repairedProfile;
            state.progressXp.clear();
            state.progressXp.putAll(progressXp);
            state.challenges.clear();
            state.challenges.addAll(challenges);
            state.readiness = ProgressionReadiness.READY;
            return snapshotLocked(playerId, state, levels);
        }
    }

    private StoredClassProfile repairProfile(
            UUID playerId,
            StoredClassProfile stored,
            Map<String, Long> progressXp,
            Set<String> challenges,
            Map<SkillType, Integer> levels) {
        String selectedFormId = stored.selectedFormId();
        ClassFormDefinition selectedForm = null;
        if (selectedFormId != null) {
            selectedForm = catalog.find(selectedFormId).orElse(null);
            if (selectedForm == null) {
                String classification = catalog.retiredFormIds().contains(selectedFormId)
                        ? "retired"
                        : "unknown";
                throw new IllegalStateException("Stored class profile selects " + classification
                        + " form id " + selectedFormId + " without a catalog migration");
            }
            if (!isUnlocked(selectedForm, progressXp, challenges, levels, new HashMap<>())) selectedFormId = null;
        }

        InputProfile inputProfile = InputProfile.fromStoredId(stored.selectedInputId())
                .orElse(InputProfile.DEFAULT);
        return new StoredClassProfile(
                playerId,
                selectedFormId,
                inputProfile.storedId(),
                catalogVersion, stored.tutorialSkillsUsed());
    }

    private void requireSupportedCatalogVersion(int storedVersion, String recordName) {
        if (storedVersion == catalogVersion) return;
        if (storedVersion > catalogVersion)
            throw new IllegalStateException("Stored class " + recordName + " uses future catalog version "
                    + storedVersion + "; this build supports " + catalogVersion);
        throw new IllegalStateException("Stored class " + recordName + " uses catalog version "
                + storedVersion + ", but no migration to " + catalogVersion + " is registered");
    }

    private ProfileSnapshot snapshotLocked(
            UUID playerId,
            CachedPlayer state,
        Map<SkillType, Integer> levels) {
        Map<String, FormProgressSnapshot> forms = new LinkedHashMap<>();
        Map<String, List<UnlockBlocker>> unlocks = new HashMap<>();
        for (ClassFormDefinition form : catalog.forms()) {
            List<UnlockBlocker> unlockBlockers = unlockBlockers(
                    form, state.progressXp, state.challenges, levels, unlocks);
            boolean unlocked = unlockBlockers.isEmpty();
            int localCap = unlocked ? form.localProgressionCap(levels::get) : 0;
            int effectiveCap = unlocked ? form.effectiveProgressionCap(levels::get) : 0;
            long xpAtCap = unlocked ? xpAtLocalCap(form, localCap) : 0L;
            long xp = unlocked
                    ? Math.min(state.progressXp.getOrDefault(form.id(), 0L), xpAtCap)
                    : 0L;
            int localLevel = unlocked ? localLevelFromXp(form, xp) : 0;
            int effectiveLevel = unlocked ? form.band().toEffectiveLevel(localLevel) : 0;
            ProgressionCapReason capReason = unlocked
                    ? capReason(form, levels)
                    : ProgressionCapReason.NOT_APPLICABLE;
            forms.put(form.id(), new FormProgressSnapshot(
                    form.id(),
                    unlocked,
                    unlockBlockers,
                    xp,
                    localLevel,
                    effectiveLevel,
                    localCap,
                    effectiveCap,
                    xpAtCap,
                    capReason,
                    unlocked ? limitingSkills(form, levels, capReason) : List.of()));
        }

        LockedRun lockedRun = runSelections.get(playerId);
        RunSelection runSelection = lockedRun == null ? null : lockedRun.selection();
        String activeFormId = runSelection == null ? state.profile.selectedFormId() : runSelection.formId();
        ActiveLineageSnapshot activeLineage = activeLineage(activeFormId, forms);
        return new ProfileSnapshot(
                playerId,
                state.profile.selectedFormId(),
                selectedInputProfile(state.profile),
                catalogVersion,
                lockedRun == null ? null : lockedRun.runId(),
                runSelection,
                activeLineage,
                forms);
    }

    private ActiveLineageSnapshot activeLineage(
            String activeFormId,
            Map<String, FormProgressSnapshot> forms) {
        if (activeFormId == null) return null;
        FormProgressSnapshot active = forms.get(activeFormId);
        if (active == null || !active.unlocked()) return null;
        ClassLineage lineage = catalog.lineageOf(activeFormId);
        return new ActiveLineageSnapshot(
                activeFormId,
                active.localLevel(),
                active.effectiveLevel(),
                lineage.formIds(),
                lineage.passiveContributionLevels(active.effectiveLevel()));
    }

    private boolean isUnlocked(
            ClassFormDefinition form,
            Map<String, Long> progressXp,
            Set<String> challenges,
            Map<SkillType, Integer> levels,
            Map<String, List<UnlockBlocker>> memo) {
        return unlockBlockers(form, progressXp, challenges, levels, memo).isEmpty();
    }

    private List<UnlockBlocker> unlockBlockers(
            ClassFormDefinition form,
            Map<String, Long> progressXp,
            Set<String> challenges,
            Map<SkillType, Integer> levels,
            Map<String, List<UnlockBlocker>> memo) {
        List<UnlockBlocker> known = memo.get(form.id());
        if (known != null) return known;

        List<UnlockBlocker> blockers = new ArrayList<>();
        if (!challenges.contains(form.id()))
            blockers.add(UnlockBlocker.classChallenge(form.id()));
        if (form.band().isRoot()) {
            contentAvailability.unavailableReason(form.id())
                    .map(reason -> UnlockBlocker.contentRequirement(form.id(), reason))
                    .ifPresent(blockers::add);
        }
        int requiredFoundationLevel = form.requiredFoundationSkillLevel();
        for (SkillType skillType : form.foundationSkills().asList()) {
            int currentLevel = levels.get(skillType);
            if (currentLevel < requiredFoundationLevel)
                blockers.add(UnlockBlocker.foundationSkill(
                        form.id(), skillType, currentLevel, requiredFoundationLevel));
        }

        if (form.parentId() != null) {
            ClassFormDefinition parent = catalog.require(form.parentId());
            blockers.addAll(unlockBlockers(parent, progressXp, challenges, levels, memo));
            long parentXpAtCap = xpAtLocalCap(parent, parent.localProgressionCap(levels::get));
            int parentLocalLevel = localLevelFromXp(parent,
                    Math.min(progressXp.getOrDefault(parent.id(), 0L), parentXpAtCap));
            int completedParentLevel = parent.band().toLocalLevel(parent.band().effectiveEnd());
            if (parentLocalLevel < completedParentLevel)
                blockers.add(UnlockBlocker.parentLocalLevel(parent.id(), parentLocalLevel, completedParentLevel));
        }

        List<UnlockBlocker> result = List.copyOf(blockers);
        memo.put(form.id(), result);
        return result;
    }

    private Map<SkillType, Integer> captureFoundationLevels(UUID playerId) {
        FoundationLevelSnapshot snapshot = foundationLevels.snapshot(playerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Foundation levels are not loaded for " + playerId));
        if (!playerId.equals(snapshot.playerId()))
            throw new IllegalStateException("Foundation adapter returned another player's levels");
        return snapshot.levels();
    }

    /** Eligibility excludes only this form's trial, never its parent's requirements. */
    public boolean canChallenge(UUID playerId, String formId) {
        CachedPlayer state = readyState(playerId);
        ClassFormDefinition form = catalog.find(formId).orElse(null);
        if (state == null || form == null) return false;
        synchronized (state.monitor) {
            if (!isReady(state) || state.challenges.contains(formId) || runSelections.containsKey(playerId))
                return false;
            return unlockBlockers(form, state.progressXp, state.challenges,
                    captureFoundationLevels(playerId), new HashMap<>()).stream()
                    .allMatch(blocker -> blocker.kind() == UnlockBlocker.Kind.CLASS_CHALLENGE
                            && blocker.formId().equals(formId));
        }
    }

    /** Unlocks and selects the form after the owning trial's instructor actually dies. */
    public boolean completeChallenge(UUID playerId, String formId) {
        CachedPlayer state = readyState(playerId);
        if (state == null || catalog.find(formId).isEmpty()) return false;
        synchronized (state.monitor) {
            if (!isReady(state)) return false;
            if (state.challenges.add(formId))
                enqueueProgressSave(playerId, state, formId, state.progressXp.getOrDefault(formId, 0L));
            if (!formId.equals(state.profile.selectedFormId())) {
                state.profile = new StoredClassProfile(playerId, formId, state.profile.selectedInputId(),
                        catalogVersion, state.profile.tutorialSkillsUsed());
                enqueueProfileSave(playerId, state);
            }
            return true;
        }
    }

    private void enqueueProfileSave(UUID playerId, CachedPlayer state) {
        StoredClassProfile profile = state.profile;
        trackPersistence(playerId, state, submitInternal(() -> {
            store.saveProfile(profile);
            return null;
        }));
    }

    private void enqueueProgressSave(UUID playerId, CachedPlayer state, String formId, long xp) {
        StoredClassProgress progress = new StoredClassProgress(playerId, formId, xp, catalogVersion, state.challenges.contains(formId));
        trackPersistence(playerId, state, submitInternal(() -> {
            store.saveProgress(progress);
            return null;
        }));
    }

    private void trackPersistence(
            UUID playerId,
            CachedPlayer state,
            CompletableFuture<Void> persistence) {
        state.persistenceTail = persistence;
        persistence.whenComplete((ignored, failure) -> {
            if (failure == null) return;
            Throwable cause = unwrap(failure);
            state.persistenceFailure = cause;
            persistenceFailure.compareAndSet(null, cause);
            fail(playerId, state, cause);
        });
    }

    private CompletableFuture<Void> persistenceOutcome() {
        Throwable failure = persistenceFailure.get();
        return failure == null
                ? CompletableFuture.completedFuture(null)
                : CompletableFuture.failedFuture(failure);
    }

    private void fail(UUID playerId, CachedPlayer state, Throwable failure) {
        if (players.get(playerId) != state) return;
        synchronized (state.monitor) {
            if (players.get(playerId) != state) return;
            state.failure = failure;
            state.readiness = ProgressionReadiness.FAILED;
        }
    }

    private CachedPlayer readyState(UUID playerId) {
        if (closed.get()) return null;
        CachedPlayer state = players.get(playerId);
        return state != null && state.readiness == ProgressionReadiness.READY ? state : null;
    }

    private boolean isReady(CachedPlayer state) {
        return !closed.get() && state.readiness == ProgressionReadiness.READY;
    }

    private InputProfile selectedInputProfile(StoredClassProfile profile) {
        return InputProfile.fromStoredId(profile.selectedInputId()).orElse(InputProfile.DEFAULT);
    }

    private AwardResult lockedAward(
            String formId,
            long requestedXp,
            ClassFormDefinition form,
            Map<String, Long> progressXp,
            Map<SkillType, Integer> levels) {
        if (form == null)
            return emptyAward(AwardResult.Status.FORM_LOCKED, requestedXp, formId);
        int localCap = form.localProgressionCap(levels::get);
        ProgressionCapReason capReason = capReason(form, levels);
        long xpAtCap = xpAtLocalCap(form, localCap);
        long xp = Math.min(progressXp.getOrDefault(formId, 0L), xpAtCap);
        int localLevel = localLevelFromXp(form, xp);
        return new AwardResult(
                AwardResult.Status.FORM_LOCKED,
                formId,
                requestedXp,
                0,
                requestedXp,
                xp,
                xp,
                localLevel,
                localLevel,
                form.band().toEffectiveLevel(localLevel),
                localCap,
                form.effectiveProgressionCap(levels::get),
                capReason,
                limitingSkills(form, levels, capReason));
    }

    private static AwardResult emptyAward(AwardResult.Status status, long requestedXp) {
        return emptyAward(status, requestedXp, null);
    }

    private static AwardResult emptyAward(
            AwardResult.Status status,
            long requestedXp,
            String formId) {
        return new AwardResult(
                status, formId, requestedXp, 0, Math.max(requestedXp, 0),
                0, 0, 0, 0, 0, 0, 0, ProgressionCapReason.NOT_APPLICABLE, List.of());
    }

    private static ProgressionCapReason capReason(
            ClassFormDefinition form,
            Map<SkillType, Integer> levels) {
        if (!form.band().isTerminal()
                && form.effectiveSkillCap(levels::get) >= form.band().effectiveEnd())
            return ProgressionCapReason.BAND_COMPLETE;
        return ProgressionCapReason.FOUNDATION_SKILLS;
    }

    private static List<SkillType> limitingSkills(
            ClassFormDefinition form,
            Map<SkillType, Integer> levels,
            ProgressionCapReason capReason) {
        return capReason == ProgressionCapReason.FOUNDATION_SKILLS
                ? form.foundationSkills().limitingSkills(levels::get)
                : List.of();
    }

    /**
     * Returns branch-local XP at a local level while pricing each step at its visible effective
     * class level. A level-31 specialization therefore starts at zero stored XP, but advancing it
     * to effective level 32 costs {@code xpToNextLevel(31)} rather than {@code xpToNextLevel(1)}.
     */
    private static long xpAtLocalCap(ClassFormDefinition form, int localCap) {
        if (localCap <= 0) return 0;
        int effectiveLevel = form.band().toEffectiveLevel(localCap);
        long baselineXp = SkillXPCalculator.totalXPForLevel(form.band().effectiveStart());
        long effectiveXp = SkillXPCalculator.totalXPForLevel(effectiveLevel);
        if (baselineXp < 0 || effectiveXp < baselineXp)
            throw new ArithmeticException("Class XP cap overflow at effective level " + effectiveLevel);
        return effectiveXp - baselineXp;
    }

    private static ClassProgressionSetResult progressionSetResult(
            ClassProgressionSetResult.Status status,
            String formId,
            int requestedEffectiveLevel,
            String blockingFormId,
            int effectiveCap,
            List<SkillType> limitingSkills,
            ProfileSnapshot snapshot) {
        return new ClassProgressionSetResult(
                status,
                formId,
                requestedEffectiveLevel,
                blockingFormId,
                effectiveCap,
                limitingSkills,
                snapshot);
    }

    /**
     * Repairs only values that can never be valid for this catalog. Foundation skill levels are a
     * temporary progression gate, not an ownership boundary for already-earned class XP.
     */
    private static long normalizePersistedXp(ClassFormDefinition form, long persistedXp) {
        if (persistedXp <= 0) return 0;
        if (form.band().isTerminal()) return persistedXp;
        int maximumLocalLevel = form.band().toLocalLevel(form.band().effectiveEnd());
        return Math.min(persistedXp, xpAtLocalCap(form, maximumLocalLevel));
    }

    /** Inverse of {@link #xpAtLocalCap(ClassFormDefinition, int)} for normalized stored XP. */
    private static int localLevelFromXp(ClassFormDefinition form, long branchXp) {
        if (branchXp < 0) throw new IllegalArgumentException("Class XP must not be negative");
        long baselineXp = SkillXPCalculator.totalXPForLevel(form.band().effectiveStart());
        long effectiveTotalXp = Math.addExact(baselineXp, branchXp);
        int effectiveLevel = SkillXPCalculator.levelFromTotalXP(effectiveTotalXp);
        if (!form.band().containsEffectiveLevel(effectiveLevel))
            throw new IllegalArgumentException("Class XP resolves outside the " + form.band()
                    + " band at effective level " + effectiveLevel);
        return form.band().toLocalLevel(effectiveLevel);
    }

    private <T> CompletableFuture<T> submitPersistence(Callable<T> task) {
        if (closed.get())
            return CompletableFuture.failedFuture(new IllegalStateException("Progression Module is closed"));
        return submitInternal(task);
    }

    private <T> CompletableFuture<T> submitInternal(Callable<T> task) {
        return persistenceExecutor.submit(task);
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable cause = failure;
        while ((cause instanceof java.util.concurrent.CompletionException
                || cause instanceof java.util.concurrent.ExecutionException)
                && cause.getCause() != null)
            cause = cause.getCause();
        return cause;
    }

    private static final class CachedPlayer {
        private final Object monitor = new Object();
        private final CompletableFuture<ProfileSnapshot> loadFuture = new CompletableFuture<>();
        private final Map<String, Long> progressXp = new HashMap<>();
        private final Set<String> challenges = new HashSet<>();
        private volatile ProgressionReadiness readiness = ProgressionReadiness.LOADING;
        private volatile Throwable failure;
        // A later successful tail must never hide an earlier failed save from flush(playerId).
        private volatile Throwable persistenceFailure;
        private StoredClassProfile profile;
        // The serial executor makes the latest submitted save a barrier for every earlier save.
        private CompletableFuture<Void> persistenceTail = CompletableFuture.completedFuture(null);
    }

    private record LockedRun(UUID runId, RunSelection selection) {
        private LockedRun {
            Objects.requireNonNull(runId, "runId");
            Objects.requireNonNull(selection, "selection");
        }
    }

    /** FIFO adapter which completes every submitted future, including rejected dispatches. */
    private static final class SerialExecutor {
        private final ArrayDeque<QueuedTask<?>> tasks = new ArrayDeque<>();
        private final Executor executor;
        private QueuedTask<?> active;

        private SerialExecutor(Executor executor) {
            this.executor = executor;
        }

        private <T> CompletableFuture<T> submit(Callable<T> command) {
            Objects.requireNonNull(command, "command");
            QueuedTask<T> task = new QueuedTask<>(command);
            synchronized (this) {
                tasks.addLast(task);
            }
            dispatchNext();
            return task.future;
        }

        private void dispatchNext() {
            while (true) {
                QueuedTask<?> next;
                synchronized (this) {
                    if (active != null) return;
                    next = tasks.pollFirst();
                    if (next == null) return;
                    active = next;
                }
                try {
                    executor.execute(() -> run(next));
                    return;
                } catch (Throwable failure) {
                    next.future.completeExceptionally(failure);
                    synchronized (this) {
                        if (active == next) active = null;
                    }
                }
            }
        }

        private void run(QueuedTask<?> task) {
            try {
                task.complete();
            } finally {
                synchronized (this) {
                    if (active == task) active = null;
                }
                dispatchNext();
            }
        }
    }

    private static final class QueuedTask<T> {
        private final Callable<T> command;
        private final CompletableFuture<T> future = new CompletableFuture<>();

        private QueuedTask(Callable<T> command) {
            this.command = command;
        }

        private void complete() {
            try {
                future.complete(command.call());
            } catch (Throwable failure) {
                future.completeExceptionally(failure);
            }
        }
    }
}
