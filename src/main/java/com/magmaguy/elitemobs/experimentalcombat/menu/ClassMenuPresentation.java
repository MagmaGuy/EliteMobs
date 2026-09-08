package com.magmaguy.elitemobs.experimentalcombat.menu;

import java.util.List;
import java.util.Objects;

/** Pure player-facing page consumed by dialog and inventory renderers. */
record ClassMenuPresentation(
        String title,
        List<String> bodyLines,
        int columns,
        int buttonWidth,
        List<ActionView> actions) {

    ClassMenuPresentation {
        Objects.requireNonNull(title, "title");
        bodyLines = List.copyOf(bodyLines);
        if (columns < 1) throw new IllegalArgumentException("columns must be positive");
        if (buttonWidth < 1) throw new IllegalArgumentException("buttonWidth must be positive");
        actions = List.copyOf(actions);
    }

    String bodyText() {
        return String.join("\n", bodyLines);
    }

    enum ActionKind {
        FORM,
        PARENT,
        SELECT,
        CONTROLS,
        OVERVIEW,
        DEACTIVATE
    }

    enum Tone {
        LOCKED,
        ACTIVE,
        UNLOCKED,
        NAVIGATION,
        CONTROL
    }

    record ActionView(
            ActionKind kind,
            Tone tone,
            String label,
            String tooltip,
            ClassMenuAction action) {

        ActionView {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(tone, "tone");
            Objects.requireNonNull(label, "label");
            Objects.requireNonNull(tooltip, "tooltip");
            Objects.requireNonNull(action, "action");
        }
    }
}
