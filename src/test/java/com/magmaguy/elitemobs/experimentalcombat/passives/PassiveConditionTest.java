package com.magmaguy.elitemobs.experimentalcombat.passives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PassiveConditionTest {

    @Test
    void targetConditionsNeverMatchAPlayerOnlyContext() {
        PassiveConditionContext context = PassiveConditionContext.playerOnly(.5D, false, false, false);

        assertFalse(PassiveCondition.TARGET_WOUNDED.matches(context));
        assertFalse(PassiveCondition.TARGET_ORDINARY.matches(context));
        assertFalse(PassiveCondition.TARGET_UNCONTROLLED.matches(context));
        assertFalse(PassiveCondition.TARGET_ISOLATED.matches(context));
        assertFalse(PassiveCondition.CLOSE_RANGE.matches(context));
    }
}
