package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.ScriptTargets;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptActionBlueprint {
    @Getter
    private final String scriptName;
    @Getter
    private ActionType actionType = null;
    @Getter
    private int duration = 0;
    @Getter
    private int wait = 0;
    @Getter
    private PotionEffectType potionEffectType;
    @Getter
    private int amplifier;
    @Getter
    private List<String> scripts = new ArrayList<>();
    @Getter
    private ScriptConditionsBlueprint conditionsBlueprint;
    @Getter
    private int times = -1;
    @Getter
    private int repeatEvery = 0;
    @Getter
    private ScriptParticlesBlueprint scriptParticlesBlueprint = new ScriptParticlesBlueprint();
    @Getter
    private double multiplier = 1;
    @Getter
    private Material material = Material.TARGET;
    @Getter
    private double amount = 1;
    @Getter
    private Boolean bValue = null;
    @Getter
    private String sValue = "";
    @Getter
    private Vector vValue = null;
    @Getter
    private String scriptFilename;
    @Getter
    private ScriptTargetsBlueprint scriptTargets;
    @Getter
    private String title = "";
    @Getter
    private String subtitle = "";
    @Getter
    private BarColor barColor = BarColor.WHITE;
    @Getter
    private BarStyle barStyle = BarStyle.SOLID;
    @Getter
    private int fadeIn = 1;
    @Getter
    private int fadeOut = 1;
    //fireworks
    @Getter
    private boolean flicker = true;
    @Getter
    private boolean withTrail = true;
    @Getter
    private FireworkEffect.Type fireworkEffectType = FireworkEffect.Type.BALL_LARGE;
    @Getter
    private List<List<FireworkColor>> fireworkEffects = new ArrayList<>();
    @Getter
    private int power = 10;
    @Getter
    private boolean invulnerable = true;
    private String location = null;
    @Getter
    private Vector offset = new Vector(0, 0, 0);


    public ScriptActionBlueprint(Map<?, ?> entry, String scriptName, String scriptFilename) {
        this.scriptName = scriptName;
        this.scriptFilename = scriptFilename;
        processMapList(entry);
        conditionsBlueprint = new ScriptConditionsBlueprint((Map<String, Object>) entry.get("Conditions"), scriptName, scriptFilename);
        scriptTargets = new ScriptTargetsBlueprint(entry, scriptName);
    }

    private void processMapList(Map<?, ?> entry) {
        for (Map.Entry entrySet : entry.entrySet()) {
            String key = (String) entrySet.getKey();
            processKeyAndValue(key, entrySet.getValue());
        }
    }

    protected void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase()) {
            case "duration" -> duration = parseInteger(key, value, scriptName);
            case "wait" -> wait = parseInteger(key, value, scriptName);
            case "amplifier" -> amplifier = parseInteger(key, value, scriptName);
            case "action" -> actionType = parseEnum(key, value, ActionType.class, scriptName);
            case "potioneffecttype" -> {
                try {
                    potionEffectType = PotionEffectType.getByKey(NamespacedKey.minecraft(((String) value).toLowerCase()));
                } catch (Exception ex) {
                    new WarningMessage("Invalid potion effect type " + value + " in file " + scriptFilename + " for script " + scriptName + " !");
                }
            }
            case "scripts" -> scripts = parseStringList(key, value, scriptName);
            case "conditions" ->
                    conditionsBlueprint = new ScriptConditionsBlueprint((Map<?, ?>) value, scriptName, scriptFilename);
            case "times" -> times = parseInteger(key, value, scriptName);
            case "repeatevery" -> repeatEvery = parseInteger(key, value, scriptName);
            case "particles" ->
                    scriptParticlesBlueprint = new ScriptParticlesBlueprint((List<Map<?, ?>>) value, scriptName, scriptFilename);
            case "multiplier" -> multiplier = parseDouble(key, value, scriptName);
            case "material" -> material = parseEnum(key, value, Material.class, scriptName);
            case "amount" -> amount = parseInteger(key, value, scriptName);
            case "bvalue" -> bValue = parseBoolean(key, value, scriptName);
            case "svalue" -> sValue = parseString(key, value, scriptName);
            case "vvalue" -> vValue = parseVector(key, value, scriptName);
            case "title" -> title = parseString(key, value, scriptName);
            case "subtitle" -> subtitle = parseString(key, value, scriptName);
            case "barcolor" -> barColor = parseEnum(key, value, BarColor.class, scriptName);
            case "barstyle" -> barStyle = parseEnum(key, value, BarStyle.class, scriptName);
            case "fadein" -> fadeIn = parseInteger(key, value, scriptName);
            case "fadeout" -> fadeOut = parseInteger(key, value, scriptName);
            case "flicker" -> flicker = parseBoolean(key, value, scriptName);
            case "withtrail" -> withTrail = parseBoolean(key, value, scriptName);
            case "fireworkeffecttype" ->
                    fireworkEffectType = parseEnum(key, value, FireworkEffect.Type.class, scriptName);
            case "fireworkeffects" -> fireworkEffects = parseEnumListList(key, value, FireworkColor.class, scriptName);
            case "power" -> power = parseInteger(key, value, scriptName);
            case "invulnerable" -> invulnerable = parseBoolean(key, value, scriptName);
            case "location" -> location = parseString(key, value, scriptName);
            case "offset" -> offset = parseVector(key, value, scriptName);
            //Managed by ScriptTargets
            case "target", "locations", "range", "track" -> {
            }
            default ->
                    new WarningMessage("Failed to read key " + key + " for script " + scriptName + " in " + scriptFilename);
        }

    }

    public Location getLocation(EliteEntity eliteEntity) {
        return ScriptTargets.processLocationFromString(eliteEntity, location, scriptName, scriptFilename, offset);
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
