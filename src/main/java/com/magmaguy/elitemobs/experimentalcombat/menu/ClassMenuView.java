package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassBand;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** UI-neutral projection shared by Java dialogs and inventory menus. */
record ClassMenuView(
        String activeSummary,
        String selectedSummary,
        boolean runLocked,
        String runLockSummary,
        InputProfile selectedInput,
        InputProfile activeInput,
        List<FormView> roots,
        Map<String, FormView> forms) {

    ClassMenuView {
        Objects.requireNonNull(activeSummary, "activeSummary");
        Objects.requireNonNull(selectedSummary, "selectedSummary");
        Objects.requireNonNull(runLockSummary, "runLockSummary");
        Objects.requireNonNull(selectedInput, "selectedInput");
        Objects.requireNonNull(activeInput, "activeInput");
        roots = List.copyOf(roots);
        forms = Map.copyOf(new LinkedHashMap<>(forms));
    }

    FormView requireForm(String formId) {
        FormView form = forms.get(formId);
        if (form == null) throw new IllegalArgumentException("Unknown projected form: " + formId);
        return form;
    }

    record FormView(
            String id,
            String displayName,
            ClassBand band,
            boolean unlocked,
            boolean challengeEligible,
            double challengeFee,
            boolean selected,
            boolean active,
            List<String> lineage,
            String parentId,
            List<FormLink> children,
            int localLevel,
            int effectiveLevel,
            int localCap,
            int effectiveCap,
            String xpSummary,
            List<FoundationRequirement> requirements,
            List<BlockerView> blockers,
            String resourceName,
            String resourceDescription,
            AbilityView mobility,
            AbilityView signature,
            AbilityView utility,
            List<PassiveView> passives) {

        FormView {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(displayName, "displayName");
            Objects.requireNonNull(band, "band");
            lineage = List.copyOf(lineage);
            children = List.copyOf(children);
            Objects.requireNonNull(xpSummary, "xpSummary");
            requirements = List.copyOf(requirements);
            blockers = List.copyOf(blockers);
            Objects.requireNonNull(resourceName, "resourceName");
            Objects.requireNonNull(resourceDescription, "resourceDescription");
            Objects.requireNonNull(mobility, "mobility");
            Objects.requireNonNull(signature, "signature");
            Objects.requireNonNull(utility, "utility");
            passives = List.copyOf(passives);
        }

        String statusLabel() {
            if (!unlocked) return "Locked";
            if (active) return "Active · Lv " + effectiveLevel;
            return "Unlocked · Lv " + effectiveLevel;
        }
    }

    record FormLink(String id, String displayName, boolean unlocked, int effectiveLevel) {
    }

    record FoundationRequirement(String displayName, int currentLevel, int requiredLevel) {
        boolean met() {
            return currentLevel >= requiredLevel;
        }
    }

    record BlockerView(
            String displayName,
            int requiredLevel,
            int currentLevel,
            String classResourceName,
            String reason) {
        BlockerView(
                String displayName,
                int requiredLevel,
                int currentLevel,
                String classResourceName) {
            this(displayName, requiredLevel, currentLevel, classResourceName, null);
        }

        BlockerView {
            Objects.requireNonNull(displayName, "displayName");
            if (requiredLevel < 0 || currentLevel < 0)
                throw new IllegalArgumentException("Blocker levels must be non-negative");
        }

        boolean classRequirement() {
            return classResourceName != null;
        }

        boolean contentRequirement() {
            return reason != null;
        }
    }

    record AbilityView(String displayName, String description) {
    }

    record PassiveView(
            String sourceFormName,
            String description,
            int contributionLevel,
            boolean preview,
            List<PassiveStat> stats) {
        PassiveView {
            Objects.requireNonNull(sourceFormName, "sourceFormName");
            Objects.requireNonNull(description, "description");
            if (contributionLevel < 1)
                throw new IllegalArgumentException("Passive contribution level must be positive");
            stats = List.copyOf(stats);
        }
    }

    record PassiveStat(String label, double fraction) {
        PassiveStat {
            Objects.requireNonNull(label, "label");
            if (!Double.isFinite(fraction))
                throw new IllegalArgumentException("Passive stat must be finite");
        }
    }
}
