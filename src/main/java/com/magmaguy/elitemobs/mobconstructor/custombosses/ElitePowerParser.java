package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.powers.bosspowers.BonusCoins;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.HashSet;
import java.util.List;

public class ElitePowerParser {

    public static HashSet<ElitePower> parsePowers(List<String> powers) {
        HashSet<ElitePower> elitePowers = new HashSet<>();
        CustomSummonPower customSummonPower = null;
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
                if (ElitePower.getElitePower(parsedPowerName[0]) != null) {
                    ElitePower elitePower = ElitePower.getElitePower(parsedPowerName[0]);
                    elitePowers.add(elitePower);
                    if (elitePower instanceof BonusCoins)
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
        return elitePowers;
    }
}
