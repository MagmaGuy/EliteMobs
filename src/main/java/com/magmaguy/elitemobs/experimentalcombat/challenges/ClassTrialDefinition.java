package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import org.bukkit.Bukkit;

/** Selected disk-authored boss; admission and progression remain owned by the challenge. */
public record ClassTrialDefinition(ClassFormDefinition form, CustomBossesConfigFields boss,
                                   String opening, String halfway, String victory, String defeat) {
    public static ClassTrialDefinition forForm(String formId) {
        var encounter = TrialEncounterAssets.require(formId);
        var form = BuiltInClassContent.catalog().require(formId);
        var boss = encounter.boss();
        if (boss.getLevel() != form.requiredFoundationSkillLevel())
            throw new IllegalArgumentException("Trial boss level must match the class admission level "
                    + form.requiredFoundationSkillLevel() + " in " + boss.getFilename());
        // Resolve required presentation before charging the attempt fee.
        if (boss.getDisguise() != null && !boss.getDisguise().isBlank()
                && !Bukkit.getPluginManager().isPluginEnabled("LibsDisguises"))
            throw new IllegalArgumentException("Configured instructor disguise requires LibsDisguises: " + boss.getFilename());
        if (boss.getCustomModel() != null && !boss.getCustomModel().isBlank()
                && (!CustomModel.customModelsEnabled() || !CustomModel.modelExists(boss.getCustomModel())))
            throw new IllegalArgumentException("Missing instructor custom model: " + boss.getCustomModel());
        return new ClassTrialDefinition(form, boss,
                encounter.opening(), encounter.halfway(), encounter.victory(), encounter.defeat());
    }
}
