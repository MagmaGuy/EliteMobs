package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.BonusCoins;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ElitePowerParser {

    public static HashSet<ElitePower> parsePowers(CustomBossesConfigFields customBossesConfigFields) {
        if (customBossesConfigFields.getPowers() == null) return new HashSet<>();
        HashSet<ElitePower> elitePowers = new HashSet<>(EliteScript.generateBossScripts(customBossesConfigFields.getEliteScript()));
        CustomSummonPower customSummonPower = null;
        List<Object> powers = new ArrayList<>(customBossesConfigFields.getPowers());
        if (powers == null) return elitePowers;
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
                    PowersConfigFields powersConfigFields = PowersConfig.getPower(parsedPowerName[0]);
                    if (powersConfigFields != null) {
                        if (!powersConfigFields.getEliteScriptBlueprints().isEmpty()) {
                            elitePowers.addAll(EliteScript.generateBossScripts(powersConfigFields.getEliteScriptBlueprints()));
                            continue;
                        }
                        ElitePower elitePower;
                        try {
                            elitePower = powersConfigFields.getElitePowerClass().newInstance();
                            elitePowers.add(elitePower);
                        } catch (Exception ex) {
                            new WarningMessage("Could not process power " + powerName);
                            continue;
                        }
                        if (powersConfigFields.getFilename().equals("bonus_coins.yml"))
                            if (parsedPowerName.length > 1)
                                try {
                                    ((BonusCoins) elitePower).setCoinMultiplier(Double.parseDouble(parsedPowerName[1]));
                                } catch (Exception ex) {
                                    new WarningMessage("Multiplier " + parsedPowerName[1] + " for Bonus Coins power is not a valid multiplier!");
                                }
                    } else
                        new WarningMessage("Warning: power name " + powerName + " is not registered! Skipping it for custom mob construction...");
                }
            } else if (powerObject instanceof Map<?, ?>) {
                //For now the alternative format is only used by custom bosses
                if (customSummonPower == null) {
                    customSummonPower = new CustomSummonPower(powerObject, customBossesConfigFields);
                    elitePowers.add(customSummonPower);
                } else
                    customSummonPower.addEntry(powerObject, customBossesConfigFields.getFilename());
            }
        }
        return elitePowers;
    }
}
