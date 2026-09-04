package com.magmaguy.elitemobs.experimentalcombat.input;

import org.junit.jupiter.api.Test;

import static com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityGestureState.Outcome.CHORD_OPENED;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityGestureState.Outcome.MOBILITY;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityGestureState.Outcome.PASS_THROUGH;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityGestureState.Outcome.SIGNATURE;
import static com.magmaguy.elitemobs.experimentalcombat.input.ClassAbilityGestureState.Outcome.UTILITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityGestureStateTest {

    @Test
    void firstPlainFPressOpensTheAbilityLayer() {
        ClassAbilityGestureState.Transition first = ClassAbilityGestureState.closed()
                .pressF(100, false);

        assertEquals(CHORD_OPENED, first.outcome());
        assertTrue(first.next().isOpenAt(100));
    }

    @Test
    void heldShiftDoesNotChangeNormalAbilityLayerBehavior() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(200, false)
                .next();

        ClassAbilityGestureState.Transition transition = open.pressF(201, true);

        assertEquals(CHORD_OPENED, transition.outcome());
        assertTrue(transition.next().isOpenAt(201));
    }

    @Test
    void clicksAndJumpInsideTheOpenChordActivateTheirAbilitiesAndCloseIt() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(300, false)
                .next();

        ClassAbilityGestureState.Transition leftClick = open.leftClick(303);
        ClassAbilityGestureState.Transition rightClick = open.rightClick(303);
        ClassAbilityGestureState.Transition jump = open.jump(303);

        assertEquals(MOBILITY, leftClick.outcome());
        assertEquals(SIGNATURE, rightClick.outcome());
        assertEquals(UTILITY, jump.outcome());
        assertTrue(leftClick.consumesInput());
        assertTrue(rightClick.consumesInput());
        assertTrue(jump.consumesInput());
        assertFalse(leftClick.next().isOpenAt(303));
        assertFalse(rightClick.next().isOpenAt(303));
        assertFalse(jump.next().isOpenAt(303));
    }

    @Test
    void firstThreeHotbarKeysAfterFActivateTheirAbilitiesAndCloseTheChord() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(350, false)
                .next();

        ClassAbilityGestureState.Transition mobility = open.selectHotbar(351, 0);
        ClassAbilityGestureState.Transition signature = open.selectHotbar(351, 1);
        ClassAbilityGestureState.Transition utility = open.selectHotbar(351, 2);

        assertEquals(MOBILITY, mobility.outcome());
        assertEquals(SIGNATURE, signature.outcome());
        assertEquals(UTILITY, utility.outcome());
        assertTrue(mobility.consumesInput());
        assertTrue(signature.consumesInput());
        assertTrue(utility.consumesInput());
        assertFalse(mobility.next().isOpenAt(351));
        assertFalse(signature.next().isOpenAt(351));
        assertFalse(utility.next().isOpenAt(351));
    }

    @Test
    void mirroredHotbarKeysAvoidTheVanillaAlreadySelectedSlotDeadKey() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(360, false)
                .next();

        ClassAbilityGestureState.Transition mobility = open.selectHotbar(361, 6);
        ClassAbilityGestureState.Transition signature = open.selectHotbar(361, 7);
        ClassAbilityGestureState.Transition utility = open.selectHotbar(361, 8);

        assertEquals(MOBILITY, mobility.outcome());
        assertEquals(SIGNATURE, signature.outcome());
        assertEquals(UTILITY, utility.outcome());
        assertTrue(mobility.consumesInput());
        assertTrue(signature.consumesInput());
        assertTrue(utility.consumesInput());
    }

    @Test
    void hotbarKeysPassThroughOutsideAnOpenFChord() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(370, false)
                .next();
        long expiredTick = 371 + ClassAbilityGestureState.CHORD_WINDOW_TICKS;

        ClassAbilityGestureState.Transition closedSelection = ClassAbilityGestureState.closed()
                .selectHotbar(370, 0);
        ClassAbilityGestureState.Transition expiredSelection = open.selectHotbar(expiredTick, 1);
        ClassAbilityGestureState.Transition unsupportedSelection = open.selectHotbar(371, 3);

        assertEquals(PASS_THROUGH, closedSelection.outcome());
        assertEquals(PASS_THROUGH, expiredSelection.outcome());
        assertEquals(PASS_THROUGH, unsupportedSelection.outcome());
        assertFalse(closedSelection.consumesInput());
        assertFalse(expiredSelection.consumesInput());
        assertFalse(unsupportedSelection.consumesInput());
    }

    @Test
    void expiredChordDoesNotConsumeAHotbarChangeOrRightClick() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(400, false)
                .next();
        long expiredTick = 401 + ClassAbilityGestureState.CHORD_WINDOW_TICKS;

        ClassAbilityGestureState.Transition rightClick = open.rightClick(expiredTick);

        assertEquals(PASS_THROUGH, rightClick.outcome());
        assertFalse(rightClick.next().isOpenAt(expiredTick));
    }

    @Test
    void plainFAfterExpiryStartsANewChord() {
        ClassAbilityGestureState open = ClassAbilityGestureState.closed()
                .pressF(500, false)
                .next();
        long expiredTick = 501 + ClassAbilityGestureState.CHORD_WINDOW_TICKS;

        ClassAbilityGestureState.Transition transition = open.pressF(expiredTick, false);

        assertEquals(CHORD_OPENED, transition.outcome());
        assertTrue(transition.next().isOpenAt(expiredTick));
    }
}
