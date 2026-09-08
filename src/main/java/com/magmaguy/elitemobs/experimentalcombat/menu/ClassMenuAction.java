package com.magmaguy.elitemobs.experimentalcombat.menu;

import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;

import java.util.Objects;

/** Player intent carried by both dialog buttons and inventory slots. */
sealed interface ClassMenuAction {

    record OpenOverview() implements ClassMenuAction {
    }

    record OpenControls() implements ClassMenuAction {
    }

    record OpenForm(String formId, boolean showAllClasses) implements ClassMenuAction {
        OpenForm(String formId) {
            this(formId, true);
        }

        public OpenForm {
            Objects.requireNonNull(formId, "formId");
        }
    }

    record SelectForm(String formId, boolean showAllClasses) implements ClassMenuAction {
        SelectForm(String formId) {
            this(formId, true);
        }

        public SelectForm {
            Objects.requireNonNull(formId, "formId");
        }
    }

    record SelectInput(InputProfile profile) implements ClassMenuAction {
        public SelectInput {
            Objects.requireNonNull(profile, "profile");
        }
    }

    record GiveFocusItem() implements ClassMenuAction {
    }

    record Challenge(String formId, double quotedFee) implements ClassMenuAction {
    }

    record DeactivateClass() implements ClassMenuAction {
    }
}
