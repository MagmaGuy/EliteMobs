package com.magmaguy.elitemobs.config.npcs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/** Shared presentation and entry point for the five root-class instructors. */
public abstract class ClassTrainerConfig extends NPCsConfigFields {
    private static final JsonObject SKINS = readSkins();

    protected ClassTrainerConfig(String root, String name, String className, String location,
                                 String greeting, String farewell) {
        super("class_trainer_" + root, true, "&6" + name, "&e<" + className + " Instructor>",
                Villager.Profession.NITWIT, "em_adventurers_guild," + location,
                List.of(greeting), List.of("Inspect your training\\nand specializations."),
                List.of(farewell), true, 3, NPCInteractions.NPCInteractionType.CLASS_TRAINER);
        setClassRoot(root);
        setDisguise("custom:ag_class_" + root);
        setCustomDisguiseData(disguise(root));
    }

    public static String disguise(String root) {
        return "player " + root + " setskin "
                + SKINS.getAsJsonObject(root).getAsJsonObject("profile");
    }

    private static JsonObject readSkins() {
        try (var stream = Objects.requireNonNull(ClassTrainerConfig.class.getResourceAsStream(
                "/class_trainers/skins.json"), "Missing trainer skins");
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot load bundled class trainer skins", failure);
        }
    }
}
