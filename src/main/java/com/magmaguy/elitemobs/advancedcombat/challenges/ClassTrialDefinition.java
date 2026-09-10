package com.magmaguy.elitemobs.advancedcombat.challenges;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;

/** Resolves the ordinary boss selected for a class and validates admission prerequisites. */
public record ClassTrialDefinition(ClassFormDefinition form, CustomBossesConfigFields boss) {
    public static ClassTrialDefinition forForm(String formId) {
        var form = BuiltInClassContent.catalog().require(formId);
        var matches = CustomBossesConfig.getCustomBosses().values().stream()
                .filter(boss -> formId.equals(boss.getFileConfiguration().getString("classTrial.class")))
                .limit(2).toList();
        if (matches.size() != 1)
            throw new IllegalArgumentException("Expected one enabled instructor for " + formId + ", found " + matches.size());
        var boss = matches.getFirst();
        if (boss.getLevel() != form.requiredFoundationSkillLevel())
            throw new IllegalArgumentException("Trial boss level must match the class admission level "
                    + form.requiredFoundationSkillLevel() + " in " + boss.getFilename());
        if (boss.getPowers() == null || boss.getPowers().isEmpty())
            throw new IllegalArgumentException("Instructor requires ordinary powers: " + boss.getFilename());
        if (boss.getCustomModel() != null && !boss.getCustomModel().isBlank()
                && (!CustomModel.customModelsEnabled() || !CustomModel.modelExists(boss.getCustomModel())))
            throw new IllegalArgumentException("Missing instructor custom model: " + boss.getCustomModel());
        return new ClassTrialDefinition(form, boss);
    }
}
