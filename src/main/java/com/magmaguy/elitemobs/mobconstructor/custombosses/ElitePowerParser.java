package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.bosspowers.CustomSummonPower;
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
            else if (ElitePower.getElitePower(powerName) != null)
                elitePowers.add(ElitePower.getElitePower(powerName));
            else
                new WarningMessage("Warning: power name " + powerName + " is not registered! Skipping it for custom mob construction...");
        }
        return elitePowers;
    }
}
