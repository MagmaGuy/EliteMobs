package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.mobproperties.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MobPropertiesConfig {

    private static HashMap<EntityType, MobPropertiesConfigFields> mobProperties = new HashMap<>();

    public static HashMap<EntityType, MobPropertiesConfigFields> getMobProperties() {
        return mobProperties;
    }

    public static void addMobProperties(EntityType entityType, MobPropertiesConfigFields mobPropertiesConfigFields) {
        mobProperties.put(entityType, mobPropertiesConfigFields);
    }


    private static ArrayList<MobPropertiesConfigFields> mobPropertiesConfigFieldsList = new ArrayList<>(Arrays.asList(
            new EliteBlazeConfig(),
            new EliteCaveSpiderConfig(),
            new EliteCreeperConfig(),
            new EliteDrownedConfig(),
            new EliteEndermanConfig(),
            new EliteEndermiteConfig(),
            new EliteHuskConfig(),
            new EliteIronGolemConfig(),
            new ElitePhantomConfig(),
            new ElitePigZombieConfig(),
            new ElitePolarBearConfig(),
            new EliteSilverfishConfig(),
            new EliteSkeletonConfig(),
            new EliteSpiderConfig(),
            new EliteStrayConfig(),
            new EliteVexConfig(),
            new EliteVindicatorConfig(),
            new EliteWitchConfig(),
            new EliteWitherSkeletonConfig(),
            new EliteZombieConfig(),
            new SuperChickenConfig(),
            new SuperCowConfig(),
            new SuperMushroomCowConfig(),
            new SuperPigConfig(),
            new SuperSheepConfig()
    ));

    public static void initializeConfigs() {
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
