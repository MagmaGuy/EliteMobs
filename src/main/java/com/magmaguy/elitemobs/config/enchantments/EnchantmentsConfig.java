package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.premade.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EnchantmentsConfig {

    private static HashMap<String, EnchantmentsConfigFields> enchantments = new HashMap();

    public static void addPowers(String fileName, EnchantmentsConfigFields enchantmentsConfigFields) {
        enchantments.put(fileName, enchantmentsConfigFields);
    }

    public static HashMap<String, EnchantmentsConfigFields> getEnchantments() {
        return enchantments;
    }

    public static EnchantmentsConfigFields getEnchantment(String fileName) {
        return enchantments.get(fileName);
    }

    public static EnchantmentsConfigFields getEnchantment(Enchantment enchantment) {
        for (EnchantmentsConfigFields enchantmentsConfigFields : enchantments.values())
            if (enchantmentsConfigFields.getEnchantment() != null)
                if (enchantmentsConfigFields.getEnchantment().equals(enchantment))
                    return enchantmentsConfigFields;
        return null;
    }

    private static ArrayList<EnchantmentsConfigFields> enchantmentsConfigFields = new ArrayList(Arrays.asList(
            new ArrowDamageConfig(),
            new ArrowFireConfig(),
            new ArrowInfiniteConfig(),
            new ArrowKnockbackConfig(),
            new BindingCurseConfig(),
            new ChannelingConfig(),
            new DamageAllConfig(),
            new DamageArthropodsConfig(),
            new DamageUndeadConfig(),
            new DepthStriderConfig(),
            new DigSpeedConfig(),
            new DurabilityConfig(),
            new FireAspectConfig(),
            new FrostWalkerConfig(),
            new ImpalingConfig(),
            new KnockbackConfig(),
            new LootBonusBlocksConfig(),
            new LootBonusMobsConfig(),
            new LoyaltyConfig(),
            new LuckConfig(),
            new MendingConfig(),
            new MultishotConfig(),
            new OxygenConfig(),
            new PiercingConfig(),
            new ProtectionFireConfig(),
            new ProtectionExplosionsConfig(),
            new ProtectionFallConfig(),
            new ProtectionFireConfig(),
            new ProtectionProjectileConfig(),
            new QuickChargeConfig(),
            new RiptideConfig(),
            new SilkTouchConfig(),
            new SweepingEdgeConfig(),
            new ThornsConfig(),
            new VanishingCurseConfig(),
            new WaterWorkerConfig(),

            new FlamethrowerConfig(),
            new HunterConfig()
    ));

    public static void initializeConfigs() {
        for (EnchantmentsConfigFields enchantmentsConfigFields : enchantmentsConfigFields)
            initializeConfiguration(enchantmentsConfigFields);

    }

    /**
     * Initializes a single instance of a premade configuration using the default values.
     */
    private static FileConfiguration initializeConfiguration(EnchantmentsConfigFields enchantmentsConfigFields) {

        File file = ConfigurationEngine.fileCreator("enchantments", enchantmentsConfigFields.getFileName());
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
        enchantmentsConfigFields.generateConfigDefaults(fileConfiguration);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
        addPowers(file.getName(), new EnchantmentsConfigFields(fileConfiguration, file));
        return fileConfiguration;

    }
}
