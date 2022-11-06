package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.BonusCoins;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ElitePowerParser {

    public static HashSet<ElitePower> parsePowers(CustomBossesConfigFields customBossesConfigFields, CustomBossEntity customBossEntity) {
        HashSet<ElitePower> elitePowers = new HashSet<>(EliteScript.generateBossScripts(customBossesConfigFields.getEliteScript()));
        if (customBossesConfigFields.getPowers() == null) return elitePowers;
        CustomSummonPower customSummonPower = null;
        List<Object> powers = new ArrayList<>(customBossesConfigFields.getPowers());
        if (powers == null) return elitePowers;
        //Parses "traditional" powers
        for (Object powerObject : powers) {
            if (powerObject instanceof String powerName) {
                if (powerName.split(":")[0].equalsIgnoreCase("summon")
                        || powerName.split(":")[0].equalsIgnoreCase("summonable"))
                    if (customSummonPower == null) {
                        customSummonPower = new CustomSummonPower(powerName, customBossesConfigFields);
                        elitePowers.add(customSummonPower);
                    } else
                        customSummonPower.addEntry(powerName, customBossesConfigFields.getFilename());
                else {
                    String[] parsedPowerName = powerName.split(":");
                    ElitePower elitePower = addPower(parsedPowerName[0], elitePowers);
                    if (elitePower == null) continue;
                    if (elitePower.getPowersConfigFields().getFilename().equals("bonus_coins.yml"))
                        if (parsedPowerName.length > 1)
                            try {
                                ((BonusCoins) elitePower).setCoinMultiplier(Double.parseDouble(parsedPowerName[1]));
                            } catch (Exception ex) {
                                new WarningMessage("Multiplier " + parsedPowerName[1] + " for Bonus Coins power is not a valid multiplier!");
                            }
                }
                //Parses more advanced powers, like reinforcements and powers with difficulty settings
            } else if (powerObject instanceof Map<?, ?> map) {

                boolean isReinforcementsConfig = false;
                for (Object key : map.keySet()) {
                    if (((String) key).equalsIgnoreCase("summontype")) {
                        isReinforcementsConfig = true;
                        break;
                    }
                }

                if (customBossEntity instanceof InstancedBossEntity instancedBossEntity) {
                    List<String> difficulties = new ArrayList<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (((String) entry.getKey()).equalsIgnoreCase("difficultyID")) {
                            difficulties = MapListInterpreter.parseStringList((String) entry.getKey(), entry.getValue(), customBossesConfigFields.getFilename());
                            break;
                        }
                    }
                    //If the boss is in an instanced dungeon with difficulties and the difficulty doesn't match, skip assigning that power
                    if (!difficulties.isEmpty() && !difficulties.contains(instancedBossEntity.getDungeonInstance().getDifficultyID()))
                        continue;
                }

                if (isReinforcementsConfig)
                    //Parse reinforcements bosses
                    if (customSummonPower == null) {
                        customSummonPower = new CustomSummonPower(powerObject, customBossesConfigFields);
                        elitePowers.add(customSummonPower);
                    } else
                        customSummonPower.addEntry(powerObject, customBossesConfigFields.getFilename());
                else {
                    //Parse powers with difficulties in them
                    String string = MapListInterpreter.parseString("filename", map.get("filename"), customBossesConfigFields.getFilename());
                    if (string != null && !string.isEmpty()) {
                        PowersConfigFields powersConfigFields = PowersConfig.getPower(string);
                        if (powersConfigFields == null) {
                            new WarningMessage("Invalid power name " + string + " in file " + customBossesConfigFields.getFilename());
                        } else {
                            addPower(string, elitePowers);
                        }
                    } else
                        new WarningMessage("No valid power name in boss config " + customBossesConfigFields.getFilename());
                }
            }
        }
        return elitePowers;
    }

    private static ElitePower addPower(String powerName, HashSet<ElitePower> elitePowers) {
        PowersConfigFields powersConfigFields = PowersConfig.getPower(powerName);
        if (powersConfigFields != null) {
            if (!powersConfigFields.getEliteScriptBlueprints().isEmpty()) {
                elitePowers.addAll(EliteScript.generateBossScripts(powersConfigFields.getEliteScriptBlueprints()));
                return null;
            }
            ElitePower elitePower;
            try {
                elitePower = powersConfigFields.getElitePowerClass().newInstance();
                elitePowers.add(elitePower);
                return elitePower;
            } catch (Exception ex) {
                new WarningMessage("Could not process power " + powerName);
                return null;
            }
        } else
            new WarningMessage("Warning: power name " + powerName + " is not registered! Skipping it for custom mob construction...");
        return null;
    }
}
