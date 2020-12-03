package com.magmaguy.elitemobs.config.potioneffects;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.potioneffects.premade.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PotionEffectsConfig {

    private static final HashMap<String, PotionEffectsConfigFields> potionEffects = new HashMap();

    public static void addPotionEffect(String fileName, PotionEffectsConfigFields powersConfigFields) {
        potionEffects.put(fileName, powersConfigFields);
    }

    public static PotionEffectsConfigFields getPotionEffect(String fileName) {
        fileName = fileName.toLowerCase();
        if (!fileName.contains(".yml"))
            fileName += ".yml";
        return potionEffects.get(fileName);
    }

    private static final ArrayList<PotionEffectsConfigFields> potionEffectsConfigFields = new ArrayList<>(Arrays.asList(
            new AbsorptionConfig(),
            new BlindnessConfig(),
            new ConduitPowerConfig(),
            new ConfusionConfig(),
            new DamageResistanceConfig(),
            new DolphinsGraceConfig(),
            new FastDiggingConfig(),
            new FireResistanceConfig(),
            new GlowingConfig(),
            new HarmConfig(),
            new HealConfig(),
            new HealthBoostConfig(),
            new HungerConfig(),
            new IncreaseDamageConfig(),
            new InvisibilityConfig(),
            new JumpConfig(),
            new LevitationConfig(),
            new LuckConfig(),
            new NightVisionConfig(),
            new PoisonConfig(),
            new RegenerationConfig(),
            new SaturationConfig(),
            new SlowConfig(),
            new SlowDiggingConfig(),
            new SlowFallingConfig(),
            new SpeedConfig(),
            new UnluckConfig(),
            new WaterBreathingConfig(),
            new WeaknessConfig(),
            new WitherConfig()
    ));

    public static void initializeConfigs() {
        for (PotionEffectsConfigFields powersConfigFields : potionEffectsConfigFields)
            initializeConfiguration(powersConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(PotionEffectsConfigFields potionEffectsConfigFields) {

        File file = ConfigurationEngine.fileCreator("potionEffects", potionEffectsConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        potionEffectsConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        addPotionEffect(file.getName(), new PotionEffectsConfigFields(fileConfiguration, file));
        return fileConfiguration;

    }

}
