package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.premade.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PowersConfig {

    private static final HashMap<String, PowersConfigFields> powers = new HashMap();

    public static void addPowers(String fileName, PowersConfigFields powersConfigFields) {
        powers.put(fileName, powersConfigFields);
    }

    public static HashMap<String, PowersConfigFields> getPowers() {
        return powers;
    }

    public static PowersConfigFields getPower(String fileName) {
        return powers.get(fileName);
    }

    private static final ArrayList<PowersConfigFields> powersConfigFieldsList = new ArrayList(Arrays.asList(
            new FlamePyreConfig(),
            new FlamethrowerConfig(),
            new GoldExplosionConfig(),
            new GoldShotgunConfig(),
            new HyperLootConfig(),
            new SpiritWalkConfig(),
            new SummonRaugConfig(),
            new SummonTheReturnedConfig(),
            new InvisibilityConfig(),
            new InvulnerabilityArrowConfig(),
            new InvulnerabilityFallDamageConfig(),
            new InvulnerabilityFireConfig(),
            new InvulnerabilityKnockbackConfig(),
            new SkeletonPillarConfig(),
            new SkeletonTrackingArrowConfig(),
            new ZombieBloatConfig(),
            new ZombieFriendsConfig(),
            new ZombieNecronomiconConfig(),
            new ZombieParentsConfig(),
            new BonusLootConfig(),
            new CorpseConfig(),
            new ImplosionConfig(),
            new MoonwalkConfig(),
            new MovementSpeedConfig(),
            new TauntConfig(),
            new AttackArrowConfig(),
            new AttackBlindingConfig(),
            new AttackConfusingConfig(),
            new AttackFireConfig(),
            new AttackFireballConfig(),
            new AttackFreezeConfig(),
            new AttackGravityConfig(),
            new AttackLightningConfig(),
            new AttackPoisonConfig(),
            new AttackPushConfig(),
            new AttackVacuumConfig(),
            new AttackWeaknessConfig(),
            new AttackWebConfig(),
            new AttackWitherConfig(),
            new SummonEmbersConfig(),
            new MeteorShowerConfig(),
            new BulletHellConfig(),
            new CustomSummonPowerConfig(),
            new ArrowFireworksConfig(),
            new ArrowRainConfig(),
            new GroundPoundConfig(),
            new DeathSliceConfig(),
            new TrackingFireballConfig()

    ));

    public static void initializeConfigs() {
        for (PowersConfigFields powersConfigFields : powersConfigFieldsList)
            initializeConfiguration(powersConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(PowersConfigFields powersConfigFields) {

        File file = ConfigurationEngine.fileCreator("powers", powersConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        powersConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        addPowers(file.getName(), new PowersConfigFields(fileConfiguration, file));
        return fileConfiguration;

    }

}
