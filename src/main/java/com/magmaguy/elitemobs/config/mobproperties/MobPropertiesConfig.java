package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.mobproperties.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MobPropertiesConfig {

    private static Map<EntityType, MobPropertiesConfigFields> mobProperties = Map.of();
    // Existing explicit defaults are preserved. Eligibility comes from the runtime Mob contract below.
    private static final List<MobPropertiesConfigFields> mobPropertiesConfigFieldsList = List.of(
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
    );

    public static Map<EntityType, MobPropertiesConfigFields> getMobProperties() {
        return mobProperties;
    }

    public static String defaultNameTemplate(EntityType type) {
        return MobTypeDefaults.nameTemplate(type);
    }

    /** The body contract excludes players, armor stands, projectiles, displays and other non-Mob objects. */
    public static boolean isEligible(EntityType type) {
        return type != null && type.isSpawnable() && type.getEntityClass() != null
                && Mob.class.isAssignableFrom(type.getEntityClass());
    }

    public static void initializeConfigs() {
        Map<EntityType, MobPropertiesConfigFields> defaults = new EnumMap<>(EntityType.class);
        for (MobPropertiesConfigFields fields : mobPropertiesConfigFieldsList) {
            defaults.put(fields.getEntityType(), fields);
        }
        Map<EntityType, MobPropertiesConfigFields> loaded = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values()) {
            if (!isEligible(type)) continue;
            MobPropertiesConfigFields fields = defaults.get(type);
            if (fields == null) {
                fields = new MobPropertiesConfigFields(
                        "elite_" + type.name().toLowerCase(Locale.ROOT), type, false,
                        MobTypeDefaults.nameTemplate(type), List.of("$player &cwas slain by $entity&c!"),
                        2.0D, MobPropertiesConfigFields.NATIVE_BASE_HEALTH);
            }
            loaded.put(type, initializeConfiguration(fields));
        }
        // Publish a complete generation; a failed reload never exposes a half-populated catalog.
        mobProperties = Collections.unmodifiableMap(loaded);
    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static MobPropertiesConfigFields initializeConfiguration(MobPropertiesConfigFields mobPropertiesConfigFields) {

        File file = ConfigurationEngine.fileCreator("mobproperties", mobPropertiesConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        mobPropertiesConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);

        return new MobPropertiesConfigFields(fileConfiguration, file, mobPropertiesConfigFields);

    }

}
