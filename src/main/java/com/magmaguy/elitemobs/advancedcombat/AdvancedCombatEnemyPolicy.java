package com.magmaguy.elitemobs.advancedcombat;

/** Pure hostile-target policy shared by class abilities and FMM-owned magic weapons. */
final class AdvancedCombatEnemyPolicy {
    private AdvancedCombatEnemyPolicy() {
    }

    static boolean permits(TargetFacts target) {
        return target.casterReady()
                && target.candidateReady()
                && !target.npc()
                && !target.friendly()
                && !target.classMinion()
                && target.validElite()
                && target.instanceAuthorized();
    }

    /**
     * Magic weapons deliberately reach beyond elites: any living, non-NPC, non-friendly target
     * qualifies (armor stands are excluded upstream), so wands and staves also work on vanilla
     * mobs, players and animals.
     */
    static boolean permitsMagicWeapon(TargetFacts target) {
        return target.casterReady()
                && target.candidateReady()
                && !target.npc()
                && !target.friendly()
                && !target.classMinion()
                && target.instanceAuthorized();
    }

    record TargetFacts(
            boolean casterReady,
            boolean candidateReady,
            boolean npc,
            boolean friendly,
            boolean classMinion,
            boolean validElite,
            boolean instanceAuthorized) {
    }
}
