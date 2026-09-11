package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

/**
 * The deliberately narrow feature flag for the [Alpha] Advanced Combat System baseline.
 *
 * <p>Balance values and class definitions are intentionally versioned with the plugin while the
 * feature is in alpha. This prevents locally modified values from contaminating tester
 * feedback.</p>
 */
public final class AdvancedCombatSystemConfig extends ConfigurationFile {

    @Getter
    private static boolean enabled;

    @Getter
    private static boolean allowClassAbilitiesOutsideEliteMobsWorlds;

    @Getter
    private static boolean enableCombatHud;

    public AdvancedCombatSystemConfig() {
        super("AdvancedCombatSystem.yml");
    }

    @Override
    public void initializeValues() {
        enabled = ConfigurationEngine.setBoolean(
                List.of(
                        "Enables the fixed [Alpha] Advanced Combat System baseline in EliteMobs dungeons and worlds.",
                        "This feature is under active development. Please test the unchanged defaults and send feedback to the developer.",
                        "Localization and customization settings are deliberately deferred during alpha.",
                        "They will be added after the system has been tested, feedback has been gathered, and the system is out of alpha.",
                        "Please keep the fixed defaults while we establish a solid baseline; no balance settings are exposed yet."),
                fileConfiguration,
                "enabled",
                true);
        allowClassAbilitiesOutsideEliteMobsWorlds = ConfigurationEngine.setBoolean(
                List.of(
                        "Allows players to enable class ability controls outside EliteMobs dungeons and protected worlds.",
                        "Players opt in per session by double-tapping F while sneaking. EliteMobs combat content is always enabled."),
                fileConfiguration,
                "allowClassAbilitiesOutsideEliteMobsWorlds",
                true);
        enableCombatHud = ConfigurationEngine.setBoolean(
                List.of(
                        "Displays the graphical combat HUD when ResourcePackManager is enabled.",
                        "Set to false to use the text action bar for health, class resources and ability feedback.",
                        "The text display is also used automatically when ResourcePackManager is absent or disabled."),
                fileConfiguration,
                "enableCombatHud",
                true);
    }
}
