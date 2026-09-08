package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.npcs.ClassTrainerConfig;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilityDefinition;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.mobconstructor.BossType;
import org.bukkit.entity.EntityType;

import java.util.List;

/** Each form supplies its own named signature and utility, inheriting only root mobility. */
public record ClassTrialDefinition(ClassFormDefinition form, String root, List<AbilityDefinition> abilities,
                                   String opening, String halfway, String victory, String defeat) {
    public ClassTrialDefinition { abilities = List.copyOf(abilities); }

    public static ClassTrialDefinition forForm(String formId) {
        var lineage = BuiltInClassContent.catalog().lineageOf(formId);
        var form = lineage.activeForm();
        String root = lineage.root().id();
        String[] voice = switch (root) {
            case "paladin" -> new String[]{"Stand your ground. Show me what your oath is worth.",
                    "Your guard holds. Now make every opening count.",
                    "Well fought. Carry this strength for those who need it.",
                    "An oath outlasts a defeat. Steady yourself and try again."};
            case "berserker" -> new String[]{"Enough talk. Show me the fire in your blood!",
                    "There it is! Now keep your head when the blood runs hot!",
                    "HA! That is a fighter worth training.",
                    "You have the hunger. Come back with the control."};
            case "ranger" -> new String[]{"Watch my footing. The first lesson is knowing where to stand.",
                    "You are reading the field. Keep your next escape in sight.",
                    "A clean hunt. You have earned your place on the trail.",
                    "Every missed shot teaches. Breathe, then try again."};
            case "cleric" -> new String[]{"Take heart. This is a lesson, and I am here to see you grow.",
                    "You are doing well. Trust your timing; save your strength.",
                    "Beautifully done. May this gift bring others hope.",
                    "Rest a moment. A stumble does not diminish your calling."};
            default -> new String[]{"Power answers to discipline. Let us see yours.",
                    "A promising answer. Can you keep control under pressure?",
                    "Well reasoned, and well cast. Your next lesson awaits.",
                    "Study the pattern you missed. Knowledge survives failure."};
        };
        if (lineage.contains("necromancer"))
            voice = new String[]{"Even death obeys a practiced hand. Convince me yours is steady.",
                    "Still breathing? Good. I have not finished teaching.",
                    "You may yet be useful. Do not make me regret this.",
                    "The dead have more patience than you. Learn from them."};
        return new ClassTrialDefinition(form, root,
                List.of(lineage.mobility(), form.signature(), form.utility()),
                voice[0] + " First lesson: " + form.signature().displayName() + ".",
                voice[1] + " Watch " + form.utility().displayName() + ".", voice[2], voice[3]);
    }

    public CustomBossesConfigFields boss() {
        var boss = new CustomBossesConfigFields("class_trial_" + form.id() + ".yml", EntityType.HUSK,
                true, "$bossLevel &6" + form.displayName() + " Instructor",
                Integer.toString(form.requiredFoundationSkillLevel()));
        // Dungeon boss baseline: roughly 30 matched sword hits, before class abilities.
        boss.setBossType(BossType.BOSS);
        boss.setHealthMultiplier(10D);
        boss.setDamageMultiplier(1D);
        boss.setNormalizedCombat(true);
        boss.setMovementSpeedAttribute(root.equals("ranger") ? .29 : .24);
        boss.setFollowDistance(70);
        var lineage = BuiltInClassContent.catalog().lineageOf(form.id());
        String skin = lineage.contains("necromancer") ? "necromancer"
                : form.id().equals("pyromancer") ? "pyromancer" : root;
        boss.setDisguise(ClassTrainerConfig.disguise(skin));
        boss.setDropsEliteMobsLoot(false);
        boss.setDropsVanillaLoot(false);
        boss.setDropsRandomLoot(false);
        boss.setDropsSkillXP(false);
        boss.setPowers(List.of());
        boss.setUniqueLootList(List.of());
        return boss;
    }
}
