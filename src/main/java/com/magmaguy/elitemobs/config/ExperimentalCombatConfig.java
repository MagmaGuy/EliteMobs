package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

/**
 * The deliberately narrow feature flag for the Experimental Combat baseline.
 *
 * <p>Balance values and class definitions are intentionally versioned with the plugin while the
 * feature is experimental. This prevents locally modified values from contaminating tester
 * feedback.</p>
 */
public final class ExperimentalCombatConfig extends ConfigurationFile {

    @Getter
    private static boolean enabled;

    @Getter
    private static boolean allowClassAbilitiesOutsideEliteMobsWorlds;

    public ExperimentalCombatConfig() {
        super("Experimental Combat.yml");
    }

    @Override
    public void initializeValues() {
        enabled = ConfigurationEngine.setBoolean(
                List.of(
                        "Enables the fixed Experimental Combat baseline in EliteMobs dungeons and worlds.",
                        "This feature is under active development. Please test the unchanged defaults and send feedback to the developer.",
                        "No balance settings are exposed until a stable baseline has been established."),
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
    }
}
