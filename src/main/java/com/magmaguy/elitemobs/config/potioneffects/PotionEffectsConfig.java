package com.magmaguy.elitemobs.config.potioneffects;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.potioneffects.premade.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PotionEffectsConfig {

    private static final HashMap<String, PotionEffectsConfigFields> potionEffects = new HashMap();
    private static final List<PotionEffectsConfigFields> potionEffectsConfigFields = new ArrayList<>(new ArrayList<>(List.of(
            new AbsorptionConfig(),
            new BlindnessConfig(),
            new ConduitPowerConfig(),
            new NauseaConfig(),
            new ResistanceConfig(),
            new DolphinsGraceConfig(),
            new HasteConfig(),
            new FireResistanceConfig(),
            new GlowingConfig(),
            new InstantDamageConfig(),
            new InstantHealthConfig(),
            new HealthBoostConfig(),
            new HungerConfig(),
            new StrengthConfig(),
            new InvisibilityConfig(),
            new JumpBoostConfig(),
            new LevitationConfig(),
            new LuckConfig(),
            new NightVisionConfig(),
            new PoisonConfig(),
            new RegenerationConfig(),
            new SaturationConfig(),
            new SlownessConfig(),
            new MiningFatigueConfig(),
            new SlowFallingConfig(),
            new SpeedConfig(),
            new UnluckConfig(),
            new WaterBreathingConfig(),
            new WeaknessConfig(),
            new WitherConfig(),
            new DarknessConfig(),
            new InfestedConfig(),
            new OozingConfig(),
            new RaidOmenConfig(),
            new TrialOmenConfig(),
            new WeavingConfig(),
            new WindChargedConfig()
    )));

    public static void addPotionEffect(String fileName, PotionEffectsConfigFields powersConfigFields) {
        potionEffects.put(fileName, powersConfigFields);
    }

    public static PotionEffectsConfigFields getPotionEffect(String fileName) {
        fileName = fileName.toLowerCase(Locale.ROOT);
        if (!fileName.contains(".yml"))
            fileName += ".yml";
        return potionEffects.get(fileName);
    }

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
