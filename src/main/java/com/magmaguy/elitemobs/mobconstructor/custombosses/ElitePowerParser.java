package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.BonusCoins;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.HashSet;
import java.util.List;

public class ElitePowerParser {

    public static HashSet<ElitePower> parsePowers(CustomBossesConfigFields customBossesConfigFields) {
        HashSet<ElitePower> elitePowers = new HashSet<>();
        CustomSummonPower customSummonPower = null;
        List<String> powers = customBossesConfigFields.getPowers();
        if (powers == null) return elitePowers;
        for (String powerName : powers) {
            if (powerName.split(":")[0].equalsIgnoreCase("summon")
                    || powerName.split(":")[0].equalsIgnoreCase("summonable"))
                if (customSummonPower == null) {
                    customSummonPower = new CustomSummonPower(powerName);
                    elitePowers.add(customSummonPower);
                } else
                    customSummonPower.addEntry(powerName);
            else {
                String[] parsedPowerName = powerName.split(":");
                PowersConfigFields powersConfigFields = PowersConfig.getPower(parsedPowerName[0]);
                if (powersConfigFields != null) {
                    if (!powersConfigFields.getEliteScripts().isEmpty()) {
                        elitePowers.addAll(powersConfigFields.getEliteScripts());
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
        }
        if (customBossesConfigFields.getEliteScript() != null)
            elitePowers.addAll(customBossesConfigFields.getEliteScript());
        return elitePowers;
    }
}
