package com.magmaguy.elitemobs.experimentalcombat.menu;


import java.util.Objects;

/** Player intent carried by both dialog buttons and inventory slots. */
sealed interface ClassMenuAction {

    record OpenOverview() implements ClassMenuAction {
    }

    record OpenControls() implements ClassMenuAction {
    }

    record OpenForm(String formId, boolean showAllClasses) implements ClassMenuAction {
        public OpenForm {
            Objects.requireNonNull(formId, "formId");
        }
    }

    record SelectForm(String formId, boolean showAllClasses) implements ClassMenuAction {
        public SelectForm {
            Objects.requireNonNull(formId, "formId");
        }
    }

    record Challenge(String formId, double quotedFee) implements ClassMenuAction {
    }

    record DeactivateClass() implements ClassMenuAction {
    }
}
