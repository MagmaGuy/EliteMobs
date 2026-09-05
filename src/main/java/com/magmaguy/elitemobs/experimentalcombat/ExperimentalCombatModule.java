package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.ExperimentalCombatConfig;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityContribution;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityCommitEffects;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityFailureReason;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityMechanicModifiers;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityResult;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityRuntimeSignal;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityRuntimeEvidenceLedger;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityRuntimeObservation;
import com.magmaguy.elitemobs.experimentalcombat.abilities.BukkitClassAbilityEngine;
import com.magmaguy.elitemobs.experimentalcombat.abilities.ClassAbilityEngine;
import com.magmaguy.elitemobs.experimentalcombat.abilities.EliteMobsAbilitySemantics;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.experimentalcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityInput;
import com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityInputRouter;
import com.magmaguy.elitemobs.experimentalcombat.minions.ClassMinionManager;
import com.magmaguy.elitemobs.experimentalcombat.participation.ClassParticipationTracker;
import com.magmaguy.elitemobs.experimentalcombat.passives.ClassPassiveRuntime;
import com.magmaguy.elitemobs.experimentalcombat.passives.FixedPassiveRegistry;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveAggregate;
import com.magmaguy.elitemobs.experimentalcombat.passives.PassiveMechanics;
import com.magmaguy.elitemobs.experimentalcombat.progression.ActiveLineageSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.AwardResult;
import com.magmaguy.elitemobs.experimentalcombat.progression.ClassProgressionModule;
import com.magmaguy.elitemobs.experimentalcombat.progression.ClassContentAvailability;
import com.magmaguy.elitemobs.experimentalcombat.progression.ClassProgressionSetResult;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;
import com.magmaguy.elitemobs.experimentalcombat.progression.FoundationLevelSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProgressionReadiness;
import com.magmaguy.elitemobs.experimentalcombat.progression.ProgressionCapReason;
import com.magmaguy.elitemobs.experimentalcombat.progression.RunLockResult;
import com.magmaguy.elitemobs.experimentalcombat.progression.SelectionResult;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassAbilityActivationFeedback;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.experimentalcombat.resources.ClassResourceController;
import com.magmaguy.elitemobs.experimentalcombat.weapons.ExperimentalMagicWeaponIntegration;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.parties.Party;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.JdbcClassProgressionStore;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.PlayerIdentityLabelRenderer;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.thirdparty.geyser.GeyserDetector;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Deep module boundary for Experimental Combat classes.
 *
 * <p>Bukkit adapters submit player intent here; this object alone coordinates progression,
 * run locks, resources, passives, threat, contribution and presentation. Active abilities have
 * no cooldowns: resource costs are the only pacing.</p>
 */
public final class ExperimentalCombatModule implements Listener, ClassAbilityInput, AutoCloseable {

    private static final long CAP_WARNING_INTERVAL_NANOS = 15_000_000_000L;

    private static ExperimentalCombatModule instance;

    private final PlayerCombatState combatState;
    private final ClassCatalog catalog = BuiltInClassContent.catalog();
    private final ClassProgressionModule progression;
    private final ClassResourceController resources =
            new ClassResourceController(BuiltInClassContent.resourceDefinitions());
    private final ClassParticipationTracker participation = new ClassParticipationTracker();
    private final AbilityRuntimeEvidenceLedger abilityEvidence = new AbilityRuntimeEvidenceLedger();
    private final FixedAbilityRegistry abilityRegistry = BuiltInClassContent.abilityRegistry();
    private final FixedPassiveRegistry passiveRegistry;
    private final ClassPassiveRuntime passiveRuntime;
    private final Predicate<Player> passiveActive;
    private final ClassAbilityEngine abilityEngine;
    private final ClassAbilityInputRouter inputRouter;
    private final ClassWeaponAffinity weaponAffinity = new ClassWeaponAffinity();
    private final ExperimentalMagicWeaponIntegration magicWeaponIntegration;
    private final Map<UUID, UUID> observedRunIds = new HashMap<>();
    private final Map<UUID, Long> lastCapWarning = new HashMap<>();
    private final Set<UUID> progressionFailureWarnings = new LinkedHashSet<>();
    private BukkitTask updateTask;

    private ExperimentalCombatModule(PlayerCombatState combatState) {
        this(combatState, availability -> new ClassProgressionModule(BuiltInClassContent.catalog(), playerId -> {
            if (!PlayerData.isDataLoaded(playerId)) return Optional.empty();
            Map<SkillType, Integer> levels = new EnumMap<>(SkillType.class);
            for (SkillType skillType : SkillType.values())
                levels.put(skillType, PlayerData.getSkillLevel(playerId, skillType));
            return Optional.of(new FoundationLevelSnapshot(playerId, levels));
        }, new JdbcClassProgressionStore(), availability), ExperimentalCombatRuntime::isActive);
    }

    ExperimentalCombatModule(PlayerCombatState combatState,
                             Function<ClassContentAvailability, ClassProgressionModule> progressionFactory,
                             Predicate<Player> passiveActive) {
        this.combatState = Objects.requireNonNull(combatState, "combatState");
        this.passiveActive = Objects.requireNonNull(passiveActive, "passiveActive");
        this.magicWeaponIntegration = new ExperimentalMagicWeaponIntegration(MetadataHandler.PLUGIN);
        this.progression = Objects.requireNonNull(
                progressionFactory.apply(magicWeaponIntegration::unavailableReason), "progression");
        this.passiveRegistry = BuiltInClassContent.passiveRegistry();
        this.passiveRuntime = new ClassPassiveRuntime(this::passivesFor, passiveActive);
        EliteMobsAbilitySemantics semantics = new EliteMobsAbilitySemantics(
                this::applyThreat,
                this::recordAbilityContribution,
                passiveRuntime::healingDoneMultiplier,
                resources::grant,
                this::activeFormIds,
                this::clearThreatLeases,
                this::abilityMechanicModifiers,
                (player, signal, ticks) -> {
                    if (signal == AbilityRuntimeSignal.WARD_BROKEN)
                        passiveRuntime.signalWardBroken(player, ticks);
                }, abilityEvidence::record);
        this.abilityEngine = new BukkitClassAbilityEngine(
                MetadataHandler.PLUGIN,
                abilityRegistry,
                semantics,
                this::activeMinionProfile,
                new ClassMinionManager.MinionDamageEvaluator() {
                    @Override
                    public double evaluate(
                            Player owner,
                            EliteEntity target,
                            double rawDamage,
                            com.magmaguy.elitemobs.combatsystem.CombatDamageContext.ClassAbilityDamageDomain domain) {
                        return rawDamage * passiveRuntime.outgoingClassAbilityDamageMultiplier(
                                owner, target, domain);
                    }

                    @Override
                    public double healingDoneMultiplier(Player owner) {
                        return passiveRuntime.healingDoneMultiplier(owner);
                    }
                });
        this.inputRouter = new ClassAbilityInputRouter(MetadataHandler.PLUGIN, this);
    }

    public static ExperimentalCombatModule initialize(PlayerCombatState combatState) {
        if (instance != null) throw new IllegalStateException("Experimental Combat module is already initialized");
        ExperimentalCombatModule module = new ExperimentalCombatModule(combatState);
        instance = module;
        ClassAbilityEligibility.install(
                module::controlsAlwaysAvailable,
                module.inputRouter::controlsEnabled);
        module.magicWeaponIntegration.start();
        module.registerGameplayListeners();
        PlayerIdentityLabelRenderer.installClassLabelProvider(module::classLabel);
        ExperimentalCombatRuntime.installHudProvider(module::renderHud);
        module.updateTask = Bukkit.getScheduler().runTaskTimer(
                MetadataHandler.PLUGIN, module::tick, 1L, 20L);
        for (Player player : Bukkit.getOnlinePlayers())
            if (PlayerData.isDataLoaded(player.getUniqueId())) module.load(player);
        return module;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    void registerGameplayListeners() {
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
        Bukkit.getPluginManager().registerEvents(passiveRuntime, MetadataHandler.PLUGIN);
        Bukkit.getPluginManager().registerEvents(inputRouter, MetadataHandler.PLUGIN);
        Bukkit.getPluginManager().registerEvents(weaponAffinity, MetadataHandler.PLUGIN);
    }

    /** Stable observation seam for the external every-class behavior probe. */
    public static List<AbilityRuntimeObservation> abilityEvidence(UUID playerId) {
        return instance == null ? List.of() : instance.abilityEvidence.snapshot(playerId);
    }

    public static List<AbilityRuntimeObservation> drainAbilityEvidence(UUID playerId) {
        return instance == null ? List.of() : instance.abilityEvidence.drain(playerId);
    }

    public static ExperimentalCombatModule get() {
        if (instance == null) throw new IllegalStateException("Experimental Combat is not initialized");
        return instance;
    }

    public static void shutdownIfInitialized() {
        if (instance != null) instance.close();
    }

    public ClassCatalog catalog() {
        return catalog;
    }

    public Optional<ProfileSnapshot> profile(UUID playerId) {
        return progression.snapshot(playerId);
    }

    @Override
    public boolean mechanicsActive(Player player) {
        return inputRouter.controlsEnabled(player);
    }

    @Override
    public boolean hasActiveClass(Player player) {
        return progression.snapshot(player.getUniqueId())
                .flatMap(ProfileSnapshot::optionalActiveLineage)
                .isPresent();
    }

    @Override
    public boolean fLayerSupported(Player player) {
        return !GeyserDetector.bedrockPlayer(player);
    }

    /** Foundation skill pair of the active form; empty without an active class. */
    public Set<SkillType> activeClassSkills(Player player) {
        return progression.snapshot(player.getUniqueId())
                .flatMap(ProfileSnapshot::optionalActiveLineage)
                .map(active -> Set.copyOf(
                        catalog.require(active.activeFormId()).foundationSkills().asList()))
                .orElse(Set.of());
    }

    @Override
    public boolean controlsAlwaysAvailable(Player player) {
        return DungeonCombatRuntime.isEligiblePlayer(player);
    }

    @Override
    public boolean outsideControlsAllowed() {
        return ExperimentalCombatConfig.isAllowClassAbilitiesOutsideEliteMobsWorlds();
    }

    @Override
    public void onControlModeChanged(Player player) {
        reconcilePlayer(player);
    }

    @Override
    public InputProfile activeInputProfile(Player player) {
        if (GeyserDetector.bedrockPlayer(player)) return InputProfile.FOCUS_ITEM;
        return progression.snapshot(player.getUniqueId())
                .map(ProfileSnapshot::activeInputProfile)
                .orElse(InputProfile.DEFAULT);
    }

    @Override
    public String abilityName(Player player, AbilitySlot slot) {
        return progression.snapshot(player.getUniqueId())
                .flatMap(ProfileSnapshot::optionalActiveLineage)
                .map(active -> abilityName(catalog.lineageOf(active.activeFormId()), slot))
                .orElseGet(() -> switch (slot) {
                    case MOBILITY -> "Mobility";
                    case SIGNATURE -> "Signature";
                    case UTILITY -> "Utility";
                });
    }

    public SelectionResult selectForm(Player player, String formId) {
        if (!mayChangeRunSelection(player))
            return new SelectionResult(SelectionResult.Status.LOCKED_FORM,
                    progression.snapshot(player.getUniqueId()).orElse(null));
        SelectionResult result = progression.selectForm(player.getUniqueId(), formId);
        if (result.accepted()) {
            reconcileAfterClassSelection(player);
        }
        return result;
    }

    public ClassProgressionSetResult setClassLevelForAdministration(
            Player player,
            String formId,
            int effectiveLevel) {
        ClassProgressionSetResult result = progression.setLineageForAdministration(
                player.getUniqueId(), formId, effectiveLevel);
        if (result.applied()) {
            reconcileAfterClassSelection(player);
        }
        return result;
    }

    /** Loot-debug fixture: scales every class tree to the requested effective level. */
    public int scaleAllClassesForAdministration(Player player, int effectiveLevel) {
        int formsAtLevel = progression.setAllLineagesForAdministration(
                player.getUniqueId(), Math.max(1, Math.min(100, effectiveLevel)));
        if (formsAtLevel > 0) reconcileAfterClassSelection(player);
        return formsAtLevel;
    }

    public SelectionResult clearSelectedForm(Player player) {
        if (!mayChangeRunSelection(player))
            return new SelectionResult(SelectionResult.Status.LOCKED_FORM,
                    progression.snapshot(player.getUniqueId()).orElse(null));
        SelectionResult result = progression.clearSelectedForm(player.getUniqueId());
        if (result.accepted()) {
            reconcileAfterClassSelection(player);
        }
        return result;
    }

    public SelectionResult selectInput(Player player, InputProfile inputProfile) {
        if (!mayChangeRunSelection(player))
            return new SelectionResult(SelectionResult.Status.LOCKED_FORM,
                    progression.snapshot(player.getUniqueId()).orElse(null));
        InputProfile supportedProfile = GeyserDetector.bedrockPlayer(player)
                ? InputProfile.FOCUS_ITEM
                : inputProfile;
        SelectionResult result = progression.selectInputProfile(player.getUniqueId(), supportedProfile);
        if (result.accepted()) {
            if (supportedProfile == InputProfile.FOCUS_ITEM && mechanicsActive(player))
                giveFocusItem(player);
            else if (supportedProfile != InputProfile.FOCUS_ITEM)
                inputRouter.removeFocusItems(player);
        }
        return result;
    }

    public SelectionResult selectFocusSlot(Player player, int slot) {
        return progression.selectFocusSlot(player.getUniqueId(), slot);
    }

    @Override
    public AbilityResult useAbility(Player player, AbilitySlot slot) {
        Optional<ProfileSnapshot> optionalProfile = progression.snapshot(player.getUniqueId());
        if (!mechanicsActive(player) || optionalProfile.isEmpty()) {
            sendFeedback(player, "&cClass controls are not active here.");
            return AbilityResult.failure("unavailable." + slot.name().toLowerCase(Locale.ROOT),
                    AbilityFailureReason.INVALID_PLAYER);
        }
        ProfileSnapshot profile = optionalProfile.get();
        ActiveLineageSnapshot active = profile.activeLineage();
        if (active == null) {
            sendFeedback(player, "&eSelect an unlocked class with &f/em class&e first.");
            return AbilityResult.failure("unselected." + slot.name().toLowerCase(Locale.ROOT),
                    AbilityFailureReason.INVALID_PLAYER);
        }

        ClassLineage lineage = catalog.lineageOf(active.activeFormId());
        FixedAbilitySpec abilitySpec = abilityRegistry.require(abilityId(lineage, slot));
        double abilityCost = abilityCost(player, abilitySpec);
        if (!resources.canAfford(player, abilityCost)) {
            observeFailedCast(player, abilitySpec);
            ClassResourceController.Snapshot resource = resources.snapshot(player.getUniqueId()).orElse(null);
            String amount = resource == null ? "0" : String.valueOf((int) Math.floor(resource.amount()));
            sendFeedback(player, "&cNot enough " + resourceName(lineage.resourceType()) + " &7(" + amount
                    + "/" + (int) abilityCost + " required).");
            return AbilityResult.failure(abilityId(lineage, slot), AbilityFailureReason.INVALID_PLAYER);
        }

        AbilityResult result = abilityEngine.execute(player, lineage, slot, active.activeEffectiveLevel());
        if (!result.successful()) {
            observeFailedCast(player, abilitySpec);
            sendFeedback(player, failureMessage(abilityName(lineage, slot), result.failureReason()));
            return result;
        }
        if (!resources.trySpend(player, abilityCost))
            throw new IllegalStateException("Class resource changed during a main-thread ability execution");
        AbilityCommitEffects commitEffects = AbilityCommitEffects.resolve(
                abilitySpec, active.activeEffectiveLevel());
        if (commitEffects.resourceGrant() > 0D)
            resources.grant(player, commitEffects.resourceGrant());
        abilityEvidence.record(new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.RESOURCE_SPENT,
                player.getUniqueId(), null, abilitySpec.id(),
                abilityCost, 0, 1,
                Set.of(), abilitySpec.executionTraits().mechanics()));
        if (commitEffects.resourceGrant() > 0D) {
            abilityEvidence.record(new AbilityRuntimeObservation(
                    AbilityRuntimeObservation.Kind.RESOURCE_GAINED,
                    player.getUniqueId(), null, abilitySpec.id(),
                    commitEffects.resourceGrant(), 0, 1,
                    Set.of(), abilitySpec.executionTraits().mechanics()));
        }
        abilityEvidence.record(new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.RESOURCE_DELTA,
                player.getUniqueId(), null, abilitySpec.id(),
                commitEffects.resourceGrant() - abilityCost,
                0, 1, Set.of(), abilitySpec.executionTraits().mechanics()));
        recordAbilityContribution(player, result.abilityId(), result.contribution());
        sendCastFeedback(player, ClassAbilityActivationFeedback.message(
                lineage, slot, abilitySpec, active.activeEffectiveLevel()));
        return result;
    }

    private void observeFailedCast(Player player, FixedAbilitySpec spec) {
        abilityEvidence.record(new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.CAST_FAILED,
                player.getUniqueId(), null, spec.id(), 0D, 0, 1,
                Set.of(), spec.executionTraits().mechanics()));
    }

    /** Passive cost efficiency replaces the removed cooldown system as the only cast pacing. */
    private double abilityCost(Player player, FixedAbilitySpec spec) {
        double multiplier = passiveRuntime.mechanics(player).abilityCostMultiplier();
        double reduction = passivesFor(player.getUniqueId()).abilityCostReductionFraction();
        return Math.max(1D, spec.resourceCost() * multiplier * (1D - reduction));
    }

    /** Same raw reward for every contributor; damage share never enters this method. */
    public AwardResult awardClassXp(Player player, long rawReward) {
        if (!ExperimentalCombatRuntime.isActive(player))
            return new AwardResult(AwardResult.Status.NO_SELECTED_FORM, null, rawReward, 0,
                    Math.max(0, rawReward), 0, 0, 0, 0, 0, 0, 0, List.of());
        AwardResult result = progression.award(player.getUniqueId(), rawReward);
        if (result.changed()) {
            if (result.currentLocalLevel() > result.previousLocalLevel()) {
                ClassFormDefinition form = catalog.require(result.formId());
                player.sendMessage(ChatColorConverter.convert("&b&lClass level up! &f"
                        + form.displayName() + " &7is now level &f" + result.currentEffectiveLevel() + "&7."));
            }
            CombatLevelDisplay.updateDisplay(player);
        }
        if (result.status() == AwardResult.Status.AT_CAP
                || result.status() == AwardResult.Status.PARTIALLY_APPLIED
                || (result.status() == AwardResult.Status.APPLIED
                && result.currentLocalLevel() >= result.localCap()))
            warnAtCap(player, result);
        return result;
    }

    public Set<Player> meaningfulParticipants(EliteEntity elite) {
        return participation.participants(elite);
    }

    public ClassAbilityInputRouter.FocusItemGiveResult giveFocusItem(Player player) {
        int preferredSlot = progression.snapshot(player.getUniqueId())
                .map(ProfileSnapshot::focusSlot)
                .orElse(8);
        return inputRouter.giveFocusItem(player, preferredSlot);
    }

    private void load(Player player) {
        progression.load(player.getUniqueId()).whenComplete((snapshot, failure) ->
                Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                    if (!player.isOnline() || instance != this) return;
                    if (failure != null) {
                        progressionFailureWarnings.add(player.getUniqueId());
                        Logger.warn("Could not load Experimental Combat class data for " + player.getName()
                                + ": " + rootCause(failure).getMessage());
                        player.sendMessage(ChatColorConverter.convert(
                                "&cYour Experimental Combat class data could not be loaded. Please report this to the developer."));
                        return;
                    }
                    progressionFailureWarnings.remove(player.getUniqueId());
                    if (GeyserDetector.bedrockPlayer(player)
                            && snapshot.selectedInputProfile() != InputProfile.FOCUS_ITEM) {
                        progression.selectInputProfile(player.getUniqueId(), InputProfile.FOCUS_ITEM);
                    }
                    CombatLevelDisplay.updateDisplay(player);
                    reconcileRunLock(player);
                    reconcilePlayer(player);
                    if (GeyserDetector.bedrockPlayer(player)
                            && mechanicsActive(player)) {
                        ClassAbilityInputRouter.FocusItemGiveResult focusResult = giveFocusItem(player);
                        if (focusResult.status() == ClassAbilityInputRouter.FocusItemGiveStatus.INVENTORY_FULL) {
                            player.sendMessage(ChatColorConverter.convert(
                                    "&cBedrock Class Focus controls are selected, but your inventory is full. Free a slot and run &f/em class focus&c."));
                        } else if (focusResult.status() == ClassAbilityInputRouter.FocusItemGiveStatus.INVALID_PREFERRED_SLOT) {
                            player.sendMessage(ChatColorConverter.convert(
                                    "&cYour saved Class Focus slot is invalid. Please report this to the developer."));
                        } else {
                            player.sendMessage(ChatColorConverter.convert(
                                    "&eBedrock detected: your universal Class Focus controls are enabled automatically."));
                        }
                    }
                }));
    }

    private void tick() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        resources.tick(onlinePlayers, combatState::isInCombat);
        for (Player player : onlinePlayers) {
            if (!progression.isReady(player.getUniqueId())) {
                ProgressionReadiness readiness = progression.readiness(player.getUniqueId());
                reconcilePlayer(player);
                if (readiness == ProgressionReadiness.FAILED
                        && progressionFailureWarnings.add(player.getUniqueId())) {
                    Throwable failure = progression.failure(player.getUniqueId()).orElse(null);
                    Logger.warn("Experimental Combat class progression became unavailable for "
                            + player.getName() + (failure == null ? "." : ": " + rootCause(failure).getMessage()));
                    player.sendMessage(ChatColorConverter.convert(
                            "&cYour Experimental Combat class data became unavailable. Class mechanics were disabled safely; please report this to the developer."));
                }
                if (readiness == ProgressionReadiness.UNLOADED
                        && PlayerData.isDataLoaded(player.getUniqueId())) load(player);
                continue;
            }
            reconcileRunLock(player);
            reconcilePlayer(player);
        }
    }

    private void reconcileRunLock(Player player) {
        UUID playerId = player.getUniqueId();
        DungeonInstance dungeonInstance = PlayerData.getMatchInstance(player) instanceof DungeonInstance instance
                ? instance
                : null;
        UUID runId = dungeonInstance == null ? null : player.getWorld().getUID();
        UUID lockedRunId = progression.snapshot(playerId)
                .map(ProfileSnapshot::lockedRunId)
                .orElse(null);
        UUID previousRunId = runId == null
                ? observedRunIds.remove(playerId)
                : observedRunIds.put(playerId, runId);

        // A run lock intentionally survives logout. Reconcile against the actual current instance,
        // not only this session's observation map, so logging out while an instance closes cannot
        // strand the player on a stale class forever.
        if (lockedRunId != null && !lockedRunId.equals(runId)) {
            progression.unlockRun(playerId, lockedRunId);
            abilityEngine.deactivate(player);
            resources.close(player);
            lockedRunId = null;
        }
        if (runId != null && lockedRunId == null && !runId.equals(previousRunId)) {
            RunLockResult result = progression.lockRun(playerId, runId);
            if (result.status() == RunLockResult.Status.NO_SELECTED_FORM)
                player.sendMessage(ChatColorConverter.convert(
                        "&eNo class was selected when this instance began, so class abilities are unavailable for this run."));
        }
    }

    private void reconcilePlayer(Player player) {
        Optional<ProfileSnapshot> optional = progression.snapshot(player.getUniqueId());
        if (optional.isEmpty()) {
            abilityEngine.deactivate(player);
            resources.close(player);
            passiveRuntime.reconcile(player, false);
            inputRouter.removeFocusItems(player);
            return;
        }
        ActiveLineageSnapshot active = optional.get().activeLineage();
        boolean controlsActive = active != null && inputRouter.controlsEnabled(player);
        if (!controlsActive) {
            abilityEngine.deactivate(player);
            resources.close(player);
            passiveRuntime.reconcile(player, false);
            inputRouter.removeFocusItems(player);
            return;
        }
        ClassResourceType resourceType = catalog.lineageOf(active.activeFormId()).resourceType();
        UUID runToken = optional.get().lockedRunId();
        if (!resources.isOpen(player.getUniqueId(), resourceType, runToken))
            resources.open(player, resourceType, runToken);
        passiveRuntime.reconcile(player, passiveActive.test(player));
        if (optional.get().activeInputProfile() == InputProfile.FOCUS_ITEM
                && !inputRouter.hasFocusItem(player)) giveFocusItem(player);
    }

    private void reconcileAfterClassSelection(Player player) {
        // A selection is a hard ability-state boundary. Source-scoped fields, links and delayed
        // callbacks from the previous class must not survive merely because the player remains in
        // the same Experimental Combat world.
        abilityEngine.deactivate(player);
        reconcilePlayer(player);
        CombatLevelDisplay.updateDisplay(player);
    }

    private boolean mayChangeRunSelection(Player player) {
        if (PlayerData.getMatchInstance(player) instanceof DungeonInstance) {
            player.sendMessage(ChatColorConverter.convert(
                    "&cYour class and input profile are locked until this dungeon run ends."));
            return false;
        }
        if (combatState.isInCombat(player.getUniqueId())) {
            player.sendMessage(ChatColorConverter.convert("&cYou cannot change classes or controls while in combat."));
            return false;
        }
        return true;
    }

    private Optional<String> classLabel(UUID playerId) {
        return progression.snapshot(playerId).flatMap(snapshot -> {
            ActiveLineageSnapshot active = snapshot.activeLineage();
            if (active == null) return Optional.empty();
            ClassFormDefinition form = catalog.require(active.activeFormId());
            return Optional.of(ClassIdentityLabelFormatter.format(
                    form.displayName(), active.activeEffectiveLevel()));
        });
    }

    private String renderHud(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        String currentHealth = CombatHealthFormatter.format(player.getHealth());
        String maximumHealth = maxHealth == null
                ? currentHealth
                : CombatHealthFormatter.format(maxHealth.getValue());
        Optional<ProfileSnapshot> optional = progression.snapshot(player.getUniqueId());
        if (optional.isEmpty() || optional.get().activeLineage() == null)
            return "&cHP " + currentHealth + "/" + maximumHealth + " &8| &7No active class &8| &e/em class";

        ProfileSnapshot profile = optional.get();
        ActiveLineageSnapshot active = profile.activeLineage();
        ClassLineage lineage = catalog.lineageOf(active.activeFormId());
        ClassResourceController.Snapshot resource = resources.snapshot(player.getUniqueId()).orElse(null);
        int amount = resource == null ? 0 : (int) Math.round(resource.amount());
        int maximum = (int) Math.round(resource == null
                ? BuiltInClassContent.resourceDefinitions().get(lineage.resourceType()).maximum()
                : resource.maximum());
        String controls = activeInputProfile(player) == InputProfile.JAVA_HOTBAR_LAYER
                ? "&7F then 1/2/3, clicks or jump"
                : "&7Focus item";
        return "&cHP " + currentHealth + "/" + maximumHealth
                + " &8| &b[" + active.activeEffectiveLevel() + "] "
                + lineage.activeForm().displayName()
                + " &8| &e" + resourceName(lineage.resourceType()) + " " + amount + "/" + maximum
                + " &8| " + controls;
    }

    private PassiveAggregate passivesFor(UUID playerId) {
        Optional<ProfileSnapshot> optional = progression.snapshot(playerId);
        if (optional.isEmpty() || optional.get().activeLineage() == null) return PassiveAggregate.NEUTRAL;
        ActiveLineageSnapshot active = optional.get().activeLineage();
        return PassiveAggregate.resolve(
                catalog.lineageOf(active.activeFormId()), active, passiveRegistry);
    }

    private AbilityMechanicModifiers abilityMechanicModifiers(Player player) {
        PassiveMechanics mechanics = passiveRuntime.mechanics(player);
        return new AbilityMechanicModifiers(
                mechanics.burstHealingMultiplier(),
                mechanics.periodicHealingMultiplier(),
                mechanics.groupedEnemyHealingMultiplier(),
                mechanics.periodicDurationMultiplier(),
                mechanics.shieldStrengthMultiplier(),
                mechanics.redirectedDamageMultiplier(),
                mechanics.controlDurationMultiplier(),
                mechanics.controlPotencyMultiplier());
    }

    private List<String> activeFormIds(Player player) {
        Optional<ProfileSnapshot> optional = progression.snapshot(player.getUniqueId());
        if (optional.isEmpty() || optional.get().activeLineage() == null) return List.of();
        return List.copyOf(catalog.lineageOf(
                optional.get().activeLineage().activeFormId()).formIds());
    }

    private Optional<ClassMinionManager.OwnerProfile> activeMinionProfile(Player player) {
        if (player == null || !ClassAbilityEligibility.isEligible(player)) return Optional.empty();
        return progression.snapshot(player.getUniqueId())
                .flatMap(ProfileSnapshot::optionalActiveLineage)
                .map(active -> new ClassMinionManager.OwnerProfile(
                        active.activeFormId(), active.activeEffectiveLevel()));
    }

    private void applyThreat(com.magmaguy.elitemobs.experimentalcombat.abilities.AbilitySemantics.ThreatRequest request) {
        List<EliteEntity> affected = new ArrayList<>();
        for (LivingEntity enemy : request.enemies()) {
            EliteEntity elite = com.magmaguy.elitemobs.entitytracker.EntityTracker.getEliteMobEntity(enemy);
            if (elite == null) continue;
            elite.addThreat(request.caster(), request.amountPerEnemy());
            elite.forceTarget(request.caster(), request.forcedTargetTicks());
            affected.add(elite);
        }
        if (!affected.isEmpty()) {
            resources.onTaunt(request.caster(), affected.size());
            participation.recordThreat(request.caster(), affected);
        }
    }

    private void clearThreatLeases(Player player) {
        UUID playerId = player.getUniqueId();
        for (EliteEntity elite : com.magmaguy.elitemobs.entitytracker.EntityTracker
                .getEliteMobEntities().values())
            elite.clearForcedTarget(playerId);
    }

    private void recordAbilityContribution(Player caster, String abilityId, AbilityContribution contribution) {
        if (contribution == null || !contribution.isMeaningful()) return;
        // Damage resource gain comes from the canonical EliteMobDamagedByPlayerEvent carrying the
        // final applied value. Counting the ability result as well would award Fury twice.
        if (contribution.effectiveHealing() > 0D)
            resources.onEffectiveHealing(caster, contribution.effectiveHealing());
        if (contribution.mitigatedDamage() > 0D)
            resources.onDamagePrevented(caster, contribution.mitigatedDamage());
        if (contribution.effectiveHealing() > 0D || contribution.mitigatedDamage() > 0D)
            participation.recordSupport(caster);
    }

    private void warnAtCap(Player player, AwardResult result) {
        long now = System.nanoTime();
        Long previous = lastCapWarning.get(player.getUniqueId());
        if (previous != null && now - previous < CAP_WARNING_INTERVAL_NANOS) return;
        lastCapWarning.put(player.getUniqueId(), now);
        ClassFormDefinition form = catalog.require(result.formId());
        String message;
        if (result.capReason() == ProgressionCapReason.BAND_COMPLETE) {
            String choices = String.join(" or ", catalog.childrenOf(form.id()).stream()
                    .map(ClassFormDefinition::displayName)
                    .toList());
            message = choices.isBlank()
                    ? "&eClass XP paused at &f" + form.displayName() + " " + result.currentEffectiveLevel()
                    + "&e, the current v0 cap. Overflow XP is not banked."
                    : "&eClass XP paused at &f" + form.displayName() + " " + result.currentEffectiveLevel()
                    + "&e. Choose &f" + choices + " &ewith &f/em class&e; overflow XP is not banked.";
        } else {
            String skills = result.limitingSkills().isEmpty()
                    ? "its required skills"
                    : String.join(" and ", result.limitingSkills().stream().map(SkillType::getDisplayName).toList());
            message = "&eClass XP paused at &f" + form.displayName() + " " + result.currentEffectiveLevel()
                    + "&e because &f" + skills + " &eis the limiting requirement. Raise it to progress; overflow XP is not banked.";
        }
        player.sendMessage(ChatColorConverter.convert(message));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
        if (!mechanicsActive(event.getPlayer()) || event.getDamage() <= 0D) return;
        resources.onDamageDealt(event.getPlayer(), event.getDamage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEliteDamagesPlayer(PlayerDamagedByEliteMobEvent event) {
        if (!mechanicsActive(event.getPlayer()) || event.getDamage() <= 0D) return;
        resources.onDamageReceived(event.getPlayer(), event.getDamage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteDeath(EliteMobDeathEvent event) {
        participation.clear(event.getEliteEntity());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDataLoaded(PlayerDataLoadedEvent event) {
        load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        abilityEngine.deactivate(player);
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (!player.isOnline() || instance != this) return;
            reconcilePlayer(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        abilityEngine.deactivate(player);
        resources.discard(player);
        passiveRuntime.discard(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        boolean retainRunState = progression.snapshot(playerId)
                .map(ProfileSnapshot::lockedRunId)
                .isPresent();
        abilityEngine.deactivate(player);
        if (retainRunState) resources.suspend(player);
        else resources.discard(player);
        passiveRuntime.discard(player);
        participation.discard(playerId);
        observedRunIds.remove(playerId);
        lastCapWarning.remove(playerId);
        progressionFailureWarnings.remove(playerId);
        String playerName = player.getName();
        java.util.concurrent.CompletableFuture<Void> flush = progression.flush(playerId);
        progression.unload(playerId);
        flush.whenComplete((ignored, failure) -> {
            if (failure != null)
                Logger.warn("Could not flush Experimental Combat class progression for "
                        + playerName + ": " + rootCause(failure).getMessage());
        });
    }

    @Override
    public void close() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        HandlerList.unregisterAll(this);
        passiveRuntime.shutdown();
        inputRouter.shutdown();
        HandlerList.unregisterAll(inputRouter);
        magicWeaponIntegration.close();
        for (Player player : Bukkit.getOnlinePlayers()) abilityEngine.deactivate(player);
        abilityEngine.close();
        resources.shutdown();
        participation.clearAll();
        observedRunIds.clear();
        lastCapWarning.clear();
        progressionFailureWarnings.clear();
        abilityEvidence.clear();
        ExperimentalCombatRuntime.clearHudProvider();
        PlayerIdentityLabelRenderer.clearClassLabelProvider();
        for (Player player : Bukkit.getOnlinePlayers()) CombatLevelDisplay.updateDisplay(player);
        try {
            progression.closeAsync().join();
        } catch (CompletionException exception) {
            Logger.warn("Could not flush Experimental Combat class progression during shutdown: "
                    + rootCause(exception).getMessage());
        }
        weaponAffinity.shutdown();
        ClassAbilityEligibility.clear();
        if (instance == this) instance = null;
    }

    private static final long CONTROL_TOGGLE_NOTICE_DELAY_TICKS = 100L;

    /** Announces the new outside-content control toggle five seconds after login. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinControlToggleNotice(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            if (!player.isOnline() || !ExperimentalCombatConfig.isEnabled()) return;
            if (!outsideControlsAllowed() || !fLayerSupported(player)) return;
            player.sendMessage(ChatColorConverter.convert(
                    ClassPresentationTheme.gradient(ClassPresentationTheme.ELITE, "Experimental Combat")
                            + " &8» &7New: hold &fsneak&7 and double-tap &fF&7 to toggle class"
                            + " controls anywhere outside EliteMobs content."));
        }, CONTROL_TOGGLE_NOTICE_DELAY_TICKS);
    }

    private static void sendFeedback(Player player, String message) {
        ActionBarCompositor.show(
                player,
                ActionBarCompositor.Source.SKILL_FEEDBACK,
                ChatColorConverter.convert(message));
    }

    private static void sendCastFeedback(Player player, String message) {
        // No explicit duration: the compositor scales the lifetime to slow-reader speed.
        ActionBarCompositor.show(
                player,
                ActionBarCompositor.Source.SKILL_FEEDBACK,
                ChatColorConverter.convert(message));
    }

    private static String failureMessage(String abilityName, AbilityFailureReason reason) {
        return switch (reason) {
            case NO_VALID_TARGET -> "&c" + abilityName + " needs a valid target.";
            case NO_CORPSE -> "&cAim at a recent Elite corpse.";
            case PATH_BLOCKED -> "&c" + abilityName + " is blocked by terrain.";
            case UNSAFE_DESTINATION -> "&c" + abilityName + " could not find safe footing.";
            case ENGINE_CLOSED, WRONG_THREAD, ABILITY_NOT_REGISTERED -> "&c" + abilityName
                    + " is unavailable due to an internal error. Please report this.";
            case INVALID_LEVEL, INVALID_PLAYER, NONE -> "&c" + abilityName + " cannot be used right now.";
        };
    }

    private static String abilityId(ClassLineage lineage, AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> lineage.mobility().id();
            case SIGNATURE -> lineage.signature().id();
            case UTILITY -> lineage.utility().id();
        };
    }

    private static String abilityName(ClassLineage lineage, AbilitySlot slot) {
        return switch (slot) {
            case MOBILITY -> lineage.mobility().displayName();
            case SIGNATURE -> lineage.signature().displayName();
            case UTILITY -> lineage.utility().displayName();
        };
    }

    private static String resourceName(ClassResourceType type) {
        return type.displayName();
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause;
    }
}
