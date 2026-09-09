package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;

/** Selected disk-authored boss; admission and progression remain owned by the challenge. */
public record ClassTrialDefinition(ClassFormDefinition form, CustomBossesConfigFields boss,
                                   String opening, String halfway, String victory, String defeat) {
    public static ClassTrialDefinition forForm(String formId) {
        var encounter = TrialEncounterAssets.require(formId);
        // Resolve required presentation before charging the attempt fee.
        if (encounter.magicWeapon() != null) TrialEquipment.magic(encounter.magicWeapon());
        return new ClassTrialDefinition(BuiltInClassContent.catalog().require(formId), encounter.boss(),
                encounter.opening(), encounter.halfway(), encounter.victory(), encounter.defeat());
    }
}
