package com.magmaguy.elitemobs.testing;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestReportActiveAbilityBalanceTest {

    @Test
    void savedCombatReportContainsActiveAbilityGraphAndCsv() {
        String report = new TestReport(UUID.randomUUID()).exportToText();

        assertTrue(report.contains("ACTIVE ABILITY BALANCE GRAPH"));
        assertTrue(report.contains("ACTIVE ABILITY DATA (CSV)"));
        assertTrue(report.contains(
                "root_class,form_id,ability_id,slot,band,start_level,end_level,resource,cost"));
    }
}
