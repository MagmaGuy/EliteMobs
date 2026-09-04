package com.magmaguy.elitemobs.experimentalcombat.progression;

import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassProgressionModuleTest {

    @Test
    void rootAndSpecializationRequireBothSkillsAndACompletedParentBand() {
        UUID playerId = UUID.randomUUID();
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 9);
        InMemoryStore store = InMemoryStore.empty(playerId);
        ClassProgressionModule progression = module(foundation, store);

        ProfileSnapshot initial = progression.load(playerId).join();
        assertFalse(initial.forms().get("paladin").unlocked());
        assertEquals(SelectionResult.Status.LOCKED_FORM,
                progression.selectForm(playerId, "paladin").status());

        foundation.setLevel(SkillType.ARMOR, 10);
        assertFalse(progression.snapshot(playerId).orElseThrow().forms().get("paladin").unlocked());
        foundation.setLevel(SkillType.SWORDS, 10);
        assertTrue(progression.snapshot(playerId).orElseThrow().forms().get("paladin").unlocked());
        assertEquals(SelectionResult.Status.APPLIED,
                progression.selectForm(playerId, "paladin").status());

        AwardResult rootCap = progression.award(
                playerId, SkillXPCalculator.totalXPForLevel(30));
        assertEquals(AwardResult.Status.PARTIALLY_APPLIED, rootCap.status());
        assertEquals(10, rootCap.currentEffectiveLevel());

        foundation.setAllLevels(30);
        AwardResult completedRoot = progression.award(
                playerId, SkillXPCalculator.totalXPForLevel(30));
        assertEquals(30, completedRoot.currentEffectiveLevel());
        assertEquals(SelectionResult.Status.LOCKED_FORM,
                progression.selectForm(playerId, "guardian").status());

        foundation.setLevel(SkillType.ARMOR, 31);
        foundation.setLevel(SkillType.SPEARS, 31);
        SelectionResult guardian = progression.selectForm(playerId, "guardian");
        assertEquals(SelectionResult.Status.APPLIED, guardian.status());
        assertEquals(31, guardian.snapshot().activeLineage().activeEffectiveLevel());
        assertEquals(1, guardian.snapshot().forms().get("guardian").localLevel());

        progression.closeAsync().join();
    }

    @Test
    void classXpStopsAtTheLowestRequiredSkillAndNeverBanksOverflow() {
        UUID playerId = UUID.randomUUID();
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 100);
        foundation.setLevel(SkillType.ARMOR, 15);
        foundation.setLevel(SkillType.SWORDS, 20);
        InMemoryStore store = InMemoryStore.empty(playerId);
        ClassProgressionModule progression = module(foundation, store);
        progression.load(playerId).join();
        assertEquals(SelectionResult.Status.APPLIED,
                progression.selectForm(playerId, "paladin").status());

        long levelFifteenXp = SkillXPCalculator.totalXPForLevel(15);
        AwardResult capped = progression.award(playerId, Long.MAX_VALUE);
        assertEquals(AwardResult.Status.PARTIALLY_APPLIED, capped.status());
        assertEquals(levelFifteenXp, capped.appliedXp());
        assertEquals(15, capped.currentEffectiveLevel());
        assertEquals(List.of(SkillType.ARMOR), capped.limitingSkills());

        AwardResult rejectedOverflow = progression.award(playerId, 50_000L);
        assertEquals(AwardResult.Status.AT_CAP, rejectedOverflow.status());
        assertEquals(0L, rejectedOverflow.appliedXp());
        assertEquals(50_000L, rejectedOverflow.discardedXp());
        progression.flush().join();
        assertEquals(levelFifteenXp, store.progress("paladin").xp());

        foundation.setLevel(SkillType.ARMOR, 16);
        AwardResult nextLevel = progression.award(
                playerId, SkillXPCalculator.xpToNextLevel(15));
        assertEquals(AwardResult.Status.APPLIED, nextLevel.status());
        assertEquals(16, nextLevel.currentEffectiveLevel());

        progression.closeAsync().join();
    }

    @Test
    void siblingBranchesKeepIndependentLevelsWhileInheritingTheSameParent() {
        UUID playerId = UUID.randomUUID();
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 100);
        InMemoryStore store = new InMemoryStore(
                new StoredClassProfile(
                        playerId,
                        "guardian",
                        InputProfile.DEFAULT.storedId(),
                        StoredClassProfile.DEFAULT_FOCUS_SLOT,
                        BuiltInClassContent.PERSISTENCE_VERSION),
                new StoredClassProgress(
                        playerId,
                        "paladin",
                        SkillXPCalculator.totalXPForLevel(30),
                        BuiltInClassContent.PERSISTENCE_VERSION));
        ClassProgressionModule progression = module(foundation, store);

        ProfileSnapshot guardian = progression.load(playerId).join();
        assertEquals(31, guardian.activeLineage().activeEffectiveLevel());
        progression.award(playerId, SkillXPCalculator.xpToNextLevel(31));
        assertEquals(32, progression.snapshot(playerId).orElseThrow()
                .forms().get("guardian").effectiveLevel());

        SelectionResult warlord = progression.selectForm(playerId, "warlord");
        assertEquals(SelectionResult.Status.APPLIED, warlord.status());
        assertEquals(31, warlord.snapshot().activeLineage().activeEffectiveLevel());
        assertEquals(30, warlord.snapshot().forms().get("paladin").effectiveLevel());
        assertEquals(32, warlord.snapshot().forms().get("guardian").effectiveLevel());
        assertEquals(1, warlord.snapshot().forms().get("warlord").localLevel());

        progression.closeAsync().join();
    }

    @Test
    void loweringFoundationCapAndAwardingDoesNotEraseEarnedClassXp() {
        UUID playerId = UUID.randomUUID();
        long levelTwentyXp = SkillXPCalculator.totalXPForLevel(20);
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 10);
        InMemoryStore store = new InMemoryStore(
                new StoredClassProfile(
                        playerId,
                        "paladin",
                        InputProfile.DEFAULT.storedId(),
                        StoredClassProfile.DEFAULT_FOCUS_SLOT,
                        BuiltInClassContent.PERSISTENCE_VERSION),
                new StoredClassProgress(
                        playerId,
                        "paladin",
                        levelTwentyXp,
                        BuiltInClassContent.PERSISTENCE_VERSION));

        ClassProgressionModule capped = module(foundation, store);
        ProfileSnapshot cappedSnapshot = capped.load(playerId).join();
        assertEquals(10, cappedSnapshot.forms().get("paladin").effectiveLevel());
        assertEquals(AwardResult.Status.AT_CAP, capped.award(playerId, 100).status());
        capped.closeAsync().join();

        assertEquals(levelTwentyXp, store.progress("paladin").xp());

        foundation.setAllLevels(20);
        ClassProgressionModule restored = module(foundation, store);
        ProfileSnapshot restoredSnapshot = restored.load(playerId).join();
        assertEquals(20, restoredSnapshot.forms().get("paladin").effectiveLevel());
        restored.closeAsync().join();
    }

    @Test
    void administrativeFixtureBuildsOnlyAValidRequestedLineageAndSelectsIt() {
        UUID playerId = UUID.randomUUID();
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 100);
        InMemoryStore store = InMemoryStore.empty(playerId);
        ClassProgressionModule progression = module(foundation, store);
        progression.load(playerId).join();

        ClassProgressionSetResult set = progression.setLineageForAdministration(
                playerId, "bulwark", 91);
        assertEquals(ClassProgressionSetResult.Status.APPLIED, set.status());
        assertEquals("bulwark", set.snapshot().selectedFormId());
        assertEquals(30, set.snapshot().forms().get("paladin").effectiveLevel());
        assertEquals(60, set.snapshot().forms().get("guardian").effectiveLevel());
        assertEquals(90, set.snapshot().forms().get("aegis").effectiveLevel());
        assertEquals(91, set.snapshot().forms().get("bulwark").effectiveLevel());
        assertEquals(1, set.snapshot().forms().get("bulwark").localLevel());
        assertEquals(31, set.snapshot().forms().get("warlord").effectiveLevel());

        progression.flush().join();
        assertEquals(SkillXPCalculator.totalXPForLevel(30), store.progress("paladin").xp());
        assertEquals(
                SkillXPCalculator.totalXPForLevel(60) - SkillXPCalculator.totalXPForLevel(31),
                store.progress("guardian").xp());
        assertEquals(
                SkillXPCalculator.totalXPForLevel(90) - SkillXPCalculator.totalXPForLevel(61),
                store.progress("aegis").xp());
        assertEquals(0L, store.progress("bulwark").xp());
        progression.closeAsync().join();
    }

    @Test
    void administrativeFixturePreservesFoundationAndRunLockInvariants() {
        UUID playerId = UUID.randomUUID();
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 60);
        InMemoryStore store = InMemoryStore.empty(playerId);
        ClassProgressionModule progression = module(foundation, store);
        progression.load(playerId).join();

        ClassProgressionSetResult capped = progression.setLineageForAdministration(
                playerId, "aegis", 61);
        assertEquals(ClassProgressionSetResult.Status.FOUNDATION_SKILL_CAP, capped.status());
        assertEquals("aegis", capped.blockingFormId());
        assertEquals(60, capped.effectiveCap());
        assertNull(store.progress("paladin"));

        ClassProgressionSetResult root = progression.setLineageForAdministration(
                playerId, "paladin", 30);
        assertEquals(ClassProgressionSetResult.Status.APPLIED, root.status());
        UUID runId = UUID.randomUUID();
        assertEquals(RunLockResult.Status.LOCKED, progression.lockRun(playerId, runId).status());
        assertEquals(ClassProgressionSetResult.Status.RUN_LOCKED,
                progression.setLineageForAdministration(playerId, "guardian", 31).status());

        progression.unlockRun(playerId, runId);
        assertEquals(ClassProgressionSetResult.Status.LEVEL_OUTSIDE_FORM_BAND,
                progression.setLineageForAdministration(playerId, "guardian", 30).status());
        progression.closeAsync().join();
    }

    @Test
    void unavailableRootContentLocksItsEntireTreeWithoutAffectingOtherClasses() {
        UUID playerId = UUID.randomUUID();
        MutableFoundationLevels foundation = new MutableFoundationLevels(playerId, 100);
        InMemoryStore store = InMemoryStore.empty(playerId);
        String reason = "FreeMinecraftModels with its bundled staff and wand is required.";
        ClassProgressionModule progression = new ClassProgressionModule(
                BuiltInClassContent.catalog(),
                foundation,
                store,
                Runnable::run,
                rootFormId -> "spellcaster".equals(rootFormId)
                        ? Optional.of(reason)
                        : Optional.empty());

        ProfileSnapshot snapshot = progression.load(playerId).join();
        assertFalse(snapshot.forms().get("spellcaster").unlocked());
        assertFalse(snapshot.forms().get("mage").unlocked());
        assertTrue(snapshot.forms().get("paladin").unlocked());
        UnlockBlocker blocker = snapshot.forms().get("mage").unlockBlockers().stream()
                .filter(candidate -> candidate.kind() == UnlockBlocker.Kind.CONTENT_REQUIREMENT)
                .findFirst()
                .orElseThrow();
        assertEquals("spellcaster", blocker.formId());
        assertEquals(reason, blocker.reason());
        assertEquals(SelectionResult.Status.LOCKED_FORM,
                progression.selectForm(playerId, "spellcaster").status());

        progression.closeAsync().join();
    }

    private static ClassProgressionModule module(
            FoundationLevels foundation,
            ClassProgressionStore store) {
        return new ClassProgressionModule(
                BuiltInClassContent.catalog(), foundation, store, Runnable::run);
    }

    private static final class MutableFoundationLevels implements FoundationLevels {
        private final UUID playerId;
        private final Map<SkillType, Integer> levels = new EnumMap<>(SkillType.class);

        private MutableFoundationLevels(UUID playerId, int initialLevel) {
            this.playerId = playerId;
            setAllLevels(initialLevel);
        }

        private void setAllLevels(int level) {
            for (SkillType skillType : SkillType.values()) levels.put(skillType, level);
        }

        private void setLevel(SkillType skillType, int level) {
            levels.put(skillType, level);
        }

        @Override
        public Optional<FoundationLevelSnapshot> snapshot(UUID requestedPlayerId) {
            return playerId.equals(requestedPlayerId)
                    ? Optional.of(new FoundationLevelSnapshot(playerId, levels))
                    : Optional.empty();
        }
    }

    private static final class InMemoryStore implements ClassProgressionStore {
        private StoredClassProfile profile;
        private final Map<String, StoredClassProgress> progress = new LinkedHashMap<>();

        private InMemoryStore(StoredClassProfile profile, StoredClassProgress... initialProgress) {
            this.profile = profile;
            for (StoredClassProgress row : initialProgress) progress.put(row.formId(), row);
        }


        private static InMemoryStore empty(UUID playerId) {
            return new InMemoryStore(new StoredClassProfile(
                    playerId,
                    null,
                    InputProfile.DEFAULT.storedId(),
                    StoredClassProfile.DEFAULT_FOCUS_SLOT,
                    BuiltInClassContent.PERSISTENCE_VERSION));
        }

        private StoredClassProgress progress(String formId) {
            return progress.get(formId);
        }

        @Override
        public StoredClassProfile loadOrCreateProfile(UUID playerId, int catalogVersion) {
            return profile;
        }

        @Override
        public List<StoredClassProgress> loadAllProgress(UUID playerId) {
            return List.copyOf(progress.values());
        }

        @Override
        public StoredClassProgress loadProgressOrZero(UUID playerId, String formId, int catalogVersion) {
            return progress.getOrDefault(formId, StoredClassProgress.zero(playerId, formId, catalogVersion));
        }

        @Override
        public void saveProfile(StoredClassProfile profile) {
            this.profile = profile;
        }

        @Override
        public void saveProgress(StoredClassProgress progress) {
            this.progress.put(progress.formId(), progress);
        }

        @Override
        public void savePlayerAggregate(
                StoredClassProfile profile,
                Collection<StoredClassProgress> progress) throws SQLException {
            this.profile = profile;
            for (StoredClassProgress row : progress) this.progress.put(row.formId(), row);
        }
    }
}
