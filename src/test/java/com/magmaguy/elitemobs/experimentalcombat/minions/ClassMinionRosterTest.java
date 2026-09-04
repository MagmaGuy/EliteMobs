package com.magmaguy.elitemobs.experimentalcombat.minions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassMinionRosterTest {

    @Test
    void resummoningAtCapReplacesOldestWithoutDisturbingNewerMinions() {
        ClassMinionRoster<String> roster = new ClassMinionRoster<>(3);
        assertTrue(roster.admit("first").isEmpty());
        roster.admit("second");
        roster.admit("third");

        assertEquals(List.of("first"), roster.admit("fourth"));
        assertEquals(List.of("second", "third", "fourth"), roster.entries());
    }

    @Test
    void batchAdmissionEvictsOnlyAsManyOldestEntriesAsRequired() {
        ClassMinionRoster<Integer> roster = new ClassMinionRoster<>(3);
        roster.admitAll(List.of(1, 2));

        assertEquals(List.of(1), roster.admitAll(List.of(3, 4)));
        assertEquals(List.of(2, 3, 4), roster.entries());
    }

    @Test
    void explicitRemovalAndDrainAreDeterministic() {
        ClassMinionRoster<Integer> roster = new ClassMinionRoster<>(3);
        roster.admitAll(List.of(1, 2, 3));
        assertTrue(roster.remove(2));
        assertEquals(List.of(1, 3), roster.drain());
        assertTrue(roster.entries().isEmpty());
    }
}
