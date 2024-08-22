package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.mobproperties.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MobPropertiesConfig {

    private static final HashMap<EntityType, MobPropertiesConfigFields> mobProperties = new HashMap<>();
    private static final ArrayList<MobPropertiesConfigFields> mobPropertiesConfigFieldsList = new ArrayList<MobPropertiesConfigFields>(new ArrayList<>(List.of(
            new EliteBlazeConfig(),
            new EliteCaveSpiderConfig(),
            new EliteCreeperConfig(),
            new EliteDrownedConfig(),
            new EliteElderGuardianConfig(),
            new EliteGuardianConfig(),
            new EliteEndermanConfig(),
            new EliteEndermiteConfig(),
            new EliteEvokerConfig(),
            new EliteHuskConfig(),
            new EliteIllusionerConfig(),
            new EliteIronGolemConfig(),
            new ElitePhantomConfig(),
            new ElitePillagerConfig(),
            new ElitePolarBearConfig(),
            new EliteRavagerConfig(),
            new EliteSilverfishConfig(),
            new EliteSkeletonConfig(),
            new EliteSpiderConfig(),
            new EliteStrayConfig(),
            new EliteVexConfig(),
            new EliteVindicatorConfig(),
            new EliteWitchConfig(),
            new EliteWitherSkeletonConfig(),
            new EliteZombieConfig(),
            new EliteGhastConfig(),
            new EliteWolfConfig(),
            new EliteEnderDragon(),
            new EliteShulkerConfig(),
            new EliteKillerBunnyConfig(),
            new EliteLlamaConfig(),
            new EliteSlimeConfig(),
            new EliteMagmaCubeConfig(),
            new EliteBoggedConfig(),
            new EliteWardenConfig(),
            new EliteGoatConfig(),
            new EliteZombiefiedPiglin(),
            new EliteZoglinConfig(),
            new ElitePiglinConfig(),
            new EliteHoglinConfig(),
            new ElitePiglinBruteConfig(),
            new EliteBeeConfig(),
            new EliteBreezeConfig(),
            new EliteWitherConfig()
    )));

    public static HashMap<EntityType, MobPropertiesConfigFields> getMobProperties() {
        return mobProperties;
    }

    public static void addMobProperties(EntityType entityType, MobPropertiesConfigFields mobPropertiesConfigFields) {
        mobProperties.put(entityType, mobPropertiesConfigFields);
    }

    public static void initializeConfigs() {

        //Version checking goes here, only leaving this as an easy reference for post 1.21 versions
//        if (!VersionChecker.serverVersionOlderThan(16, 2))
//            mobPropertiesConfigFieldsList.add(new ElitePiglinBruteConfig());

        for (MobPropertiesConfigFields mobPropertiesConfigFields : mobPropertiesConfigFieldsList)
            initializeConfiguration(mobPropertiesConfigFields);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(MobPropertiesConfigFields mobPropertiesConfigFields) {

        File file = ConfigurationEngine.fileCreator("mobproperties", mobPropertiesConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        mobPropertiesConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

        addMobProperties(mobPropertiesConfigFields.getEntityType(), new MobPropertiesConfigFields(fileConfiguration, file));

        return fileConfiguration;

    }

}
