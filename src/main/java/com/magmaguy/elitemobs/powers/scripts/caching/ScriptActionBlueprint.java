package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.powers.scripts.enums.WeatherType;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptVector;
import com.magmaguy.elitemobs.utils.PotionEffectTypeUtil;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.MemorySection;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptActionBlueprint {
    @Getter
    private final String scriptName;
    @Getter
    private final String scriptFilename;
    @Getter
    private ActionType actionType = null;
    @Getter
    private ScriptInteger duration = new ScriptInteger(0);
    @Getter
    private ScriptInteger wait = new ScriptInteger(0);
    @Getter
    private PotionEffectType potionEffectType;
    @Getter
    private ScriptInteger amplifier;
    @Getter
    private List<String> scripts = new ArrayList<>();
    @Getter
    private List<String> landingScripts = new ArrayList<>();
    @Getter
    private ScriptConditionsBlueprint conditionsBlueprint;
    @Getter
    private ScriptInteger times = new ScriptInteger(-1);
    @Getter
    private ScriptInteger repeatEvery = new ScriptInteger(0);
    @Getter
    private ScriptParticlesBlueprint scriptParticlesBlueprint = new ScriptParticlesBlueprint();
    @Getter
    private ScriptFloat multiplier = new ScriptFloat(1f);
    @Getter
    private Material material = Material.TARGET;
    @Getter
    private ScriptFloat amount = new ScriptFloat(1f);
    @Getter
    private Boolean bValue = null;
    @Getter
    private String sValue = "";
    @Getter
    private Vector vValue = null;
    @Getter
    private ScriptTargetsBlueprint scriptTargets;
    @Getter
    private ScriptTargetsBlueprint finalTarget = null;
    @Getter
    private String title = "";
    @Getter
    private String subtitle = "";
    @Getter
    private BarColor barColor = BarColor.WHITE;
    @Getter
    private BarStyle barStyle = BarStyle.SOLID;
    @Getter
    private ScriptInteger fadeIn = new ScriptInteger(1);
    @Getter
    private ScriptInteger fadeOut = new ScriptInteger(1);
    //fireworks
    @Getter
    private boolean flicker = true;
    @Getter
    private boolean withTrail = true;
    @Getter
    private FireworkEffect.Type fireworkEffectType = FireworkEffect.Type.BALL_LARGE;
    @Getter
    private List<FireworkEffect.Type> fireworkEffectTypes = null;
    @Getter
    private List<List<FireworkColor>> fireworkEffects = new ArrayList<>();
    @Getter
    private ScriptInteger power = new ScriptInteger(10);
    @Getter
    private boolean invulnerable = true;
    @Getter
    private ScriptVector offset = new ScriptVector(new ScriptFloat(0), new ScriptFloat(0), new ScriptFloat(0));
    @Getter
    private List<String> tags;
    @Getter
    private ScriptInteger time = new ScriptInteger(0);
    @Getter
    private WeatherType weatherType = WeatherType.CLEAR;
    @Getter
    private boolean onlyRunOneScript = false;
    @Getter
    private ScriptRelativeVectorBlueprint scriptRelativeVectorBlueprint = null;
    @Getter
    private ScriptFloat volume = new ScriptFloat(1f);
    @Getter
    private ScriptFloat pitch = new ScriptFloat(1f);
    @Getter
    private ScriptFloat velocity = new ScriptFloat(1f);
    @Getter
    private ScriptFloat scale = new ScriptFloat(1f);
    @Getter
    private boolean debug = false;


    public ScriptActionBlueprint(Map<?, ?> entry, String scriptName, String scriptFilename) {
        this.scriptName = scriptName;
        this.scriptFilename = scriptFilename;
        processMapList(entry);
        conditionsBlueprint = new ScriptConditionsBlueprint((Map<String, Object>) entry.get("Conditions"), scriptName, scriptFilename);
        if (scriptTargets == null) scriptTargets = new ScriptTargetsBlueprint(entry, scriptName, scriptFilename);
        if (actionType == ActionType.SPAWN_PARTICLE &&
                scriptTargets.isZoneTarget() &&
                scriptTargets.getCoverage().getValue() == 1D &&
                !scriptTargets.isCustomCoverage())
            scriptTargets.setCoverage(.3f);
    }

    private void processMapList(Map<?, ?> entry) {
        for (Map.Entry entrySet : entry.entrySet()) {
            String key = (String) entrySet.getKey();
            processKeyAndValue(key, entrySet.getValue());
        }
    }

    protected void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase(Locale.ROOT)) {
            case "duration" -> duration = parseScriptInteger(key, value, scriptName);
            case "wait" -> wait = parseScriptInteger(key, value, scriptName);
            case "amplifier" -> amplifier = parseScriptInteger(key, value, scriptName);
            case "action" -> actionType = parseEnum(key, value, ActionType.class, scriptName);
            case "potioneffecttype" -> {
                try {
                    potionEffectType = PotionEffectTypeUtil.getByKey(((String) value).toLowerCase(Locale.ROOT));
                } catch (Exception ex) {
                    Logger.warn("Invalid potion effect type " + value + " in file " + scriptFilename + " for script " + scriptName + " !");
                }
            }
            case "scripts" -> scripts = parseStringList(key, value, scriptName);
            case "landingscripts" -> landingScripts = parseStringList(key, value, scriptName);
            case "conditions" ->
                    conditionsBlueprint = new ScriptConditionsBlueprint((Map<?, ?>) value, scriptName, scriptFilename);
            case "times" -> times = parseScriptInteger(key, value, scriptName);
            case "repeatevery" -> repeatEvery = parseScriptInteger(key, value, scriptName);
            case "particles" ->
                    scriptParticlesBlueprint = new ScriptParticlesBlueprint((List<Map<?, ?>>) value, scriptName, scriptFilename);
            case "multiplier" -> multiplier = parseScriptFloat(key, value, scriptName);
            case "material" -> material = parseEnum(key, value, Material.class, scriptName);
            case "amount" -> amount = parseScriptFloat(key, value, scriptName);
            case "bvalue" -> bValue = parseBoolean(key, value, scriptName);
            case "svalue" -> sValue = parseString(key, value, scriptName);
            case "vvalue" -> vValue = parseVector(key, value, scriptName);
            case "title" -> title = parseString(key, value, scriptName);
            case "subtitle" -> subtitle = parseString(key, value, scriptName);
            case "barcolor" -> barColor = parseEnum(key, value, BarColor.class, scriptName);
            case "barstyle" -> barStyle = parseEnum(key, value, BarStyle.class, scriptName);
            case "fadein" -> fadeIn = parseScriptInteger(key, value, scriptName);
            case "fadeout" -> fadeOut = parseScriptInteger(key, value, scriptName);
            case "flicker" -> flicker = parseBoolean(key, value, scriptName);
            case "withtrail" -> withTrail = parseBoolean(key, value, scriptName);
            case "fireworkeffecttype" ->
                    fireworkEffectType = parseEnum(key, value, FireworkEffect.Type.class, scriptName);
            case "fireworkeffecttypes" ->
                    fireworkEffectTypes = parseEnumList(key, value, FireworkEffect.Type.class, scriptName);
            case "fireworkeffects" -> fireworkEffects = parseEnumListList(key, value, FireworkColor.class, scriptName);
            case "power" -> power = parseScriptInteger(key, value, scriptName);
            case "invulnerable" -> invulnerable = parseBoolean(key, value, scriptName);
            case "offset" -> offset = parseScriptVector(key, value, scriptName);
            case "tags" -> tags = parseStringList(key, value, scriptName);
            case "time" -> time = parseScriptInteger(key, value, scriptName);
            case "weather" -> weatherType = parseEnum(key, value, WeatherType.class, scriptName);
            case "target" -> {
                if (value instanceof MemorySection)
                    scriptTargets = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, scriptFilename);
                else
                    scriptTargets = new ScriptTargetsBlueprint((Map) value, scriptName, scriptFilename);
            }
            case "finaltarget" -> {
                if (value instanceof MemorySection)
                    finalTarget = new ScriptTargetsBlueprint(((MemorySection) value).getValues(false), scriptName, scriptFilename);
                else
                    finalTarget = new ScriptTargetsBlueprint((Map) value, scriptName, scriptFilename);
            }
            case "onlyrunonescript" -> onlyRunOneScript = parseBoolean(key, value, scriptName);
            case "relativevector" ->
                    scriptRelativeVectorBlueprint = new ScriptRelativeVectorBlueprint(scriptName, scriptFilename, (Map<String, ?>) value);
            case "pitch" -> pitch = parseScriptFloat(key, value, scriptName);
            case "volume" -> volume = parseScriptFloat(key, value, scriptName);
            case "velocity" -> velocity = parseScriptFloat(key, value, scriptName);
            case "scale" -> scale = parseScriptFloat(key, value, scriptName);
            case "debug" -> debug = parseBoolean(key, value, scriptName);
            default -> Logger.warn("Failed to read key " + key + " for script " + scriptName + " in " + scriptFilename);
        }

    }

    public enum FireworkColor {
        WHITE(Color.WHITE),
        SILVER(Color.SILVER),
        GRAY(Color.GRAY),
        BLACK(Color.BLACK),
        RED(Color.RED),
        MAROON(Color.MAROON),
        YELLOW(Color.YELLOW),
        OLIVE(Color.OLIVE),
        LIME(Color.LIME),
        GREEN(Color.GREEN),
        AQUA(Color.AQUA),
        TEAL(Color.TEAL),
        BLUE(Color.BLUE),
        NAVY(Color.NAVY),
        FUCHSIA(Color.FUCHSIA),
        PURPLE(Color.PURPLE),
        ORANGE(Color.ORANGE);

        @Getter
        private final Color color;

        FireworkColor(Color color) {
            this.color = color;
        }
    }
}
