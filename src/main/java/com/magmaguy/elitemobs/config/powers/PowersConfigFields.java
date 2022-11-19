package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowersConfigFields extends CustomConfigFields {

    @Getter
    @Setter
    private String effect;
    @Getter
    @Setter
    private int powerCooldown = 0;
    @Getter
    @Setter
    private int globalCooldown = 0;
    @Getter
    @Setter
    private ConfigurationSection scripts = null;
    @Getter
    @Setter
    private Map<String, Object> rawScripts = new HashMap<>();
    @Getter
    @Setter
    private List<EliteScriptBlueprint> eliteScriptBlueprints = new ArrayList<>();
    @Getter
    private Class<? extends ElitePower> elitePowerClass = null;

    @Getter
    private PowerType powerType;

    /**
     * Constructor for hardcoded powers
     *
     * @param fileName        Filename of the power
     * @param isEnabled       If the power is enabled
     * @param material        Material of the trail
     * @param elitePowerClass Class of the hardcoded power
     */
    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String material,
                              Class<? extends ElitePower> elitePowerClass,
                              PowerType powerType) {
        super(fileName, isEnabled);
        this.effect = material;
        this.elitePowerClass = elitePowerClass;
        this.powerType = powerType;
    }

    /**
     * Constructor for script-based powers
     *
     * @param fileName   Filename of the power
     * @param isEnabled  If the power is enabled
     * @param material   Material of the power
     * @param rawScripts Script of the power
     */
    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String material,
                              Map<String, Object> rawScripts,
                              PowerType powerType) {
        super(fileName, isEnabled);
        this.effect = material;
        this.rawScripts = rawScripts;
        this.powerType = powerType;
    }

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String material,
                              int powerCooldown,
                              int globalCooldown,
                              Class<? extends ElitePower> elitePowerClass,
                              PowerType powerType) {
        super(fileName, isEnabled);
        this.effect = material;
        this.powerCooldown = powerCooldown;
        this.globalCooldown = globalCooldown;
        this.elitePowerClass = elitePowerClass;
        this.powerType = powerType;
    }


    public PowersConfigFields(String fileName,
                              boolean isEnabled) {
        super(fileName, isEnabled);
    }

    protected static Map<String, Object> addScriptEntry(String scriptName,
                                                        List<String> events,
                                                        List<String> conditions,
                                                        List<Map<String, Object>> actions,
                                                        Map<String, Object> cooldowns) {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("eliteScript" + "." + scriptName + "." + "Events", events);
        defaults.put("eliteScript" + "." + scriptName + "." + "Conditions", conditions);
        defaults.put("eliteScript" + "." + scriptName + "." + "Actions", actions);
        defaults.put("eliteScript" + "." + scriptName + "." + "Cooldowns", cooldowns);
        return defaults;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.effect = processString("effect", effect, null, false);
        this.powerCooldown = processInt("powerCooldown", powerCooldown, 0, false);
        this.globalCooldown = processInt("globalCooldown", globalCooldown, 0, false);
        this.rawScripts = processMap("eliteScript", rawScripts);
        this.scripts = processConfigurationSection("eliteScript", rawScripts);
        this.powerType = processEnum("powerType", powerType, null, PowerType.class, false);

        processAdditionalFields();
    }

    public void initializeScripts() {
        try {
            if (scripts != null) eliteScriptBlueprints = EliteScriptBlueprint.parseBossScripts(scripts, this);
        } catch (Exception exception) {
            new WarningMessage("You have a script with invalid data! Script in " + filename + " is not valid.");
            exception.printStackTrace();
        }
    }

    public void processAdditionalFields() {
    }

    public enum PowerType {
        OFFENSIVE,
        DEFENSIVE,
        MISCELLANEOUS,
        MAJOR_ZOMBIE,
        MAJOR_SKELETON,
        MAJOR_BLAZE,
        MAJOR_ENDERMAN,
        MAJOR_GHAST,
        UNIQUE
    }

}
