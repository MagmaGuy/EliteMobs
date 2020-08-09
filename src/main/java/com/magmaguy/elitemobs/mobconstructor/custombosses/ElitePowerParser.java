package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.bosspowers.CustomSummonPower;
import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.HashSet;
import java.util.List;

public class ElitePowerParser {

    public static HashSet<ElitePower> parsePowers(List<String> powers) {
        HashSet<ElitePower> elitePowers = new HashSet<>();
        boolean hasCustomSummons = false;
        for (String powerName : powers) {
            if (!hasCustomSummons && powerName.split(":")[0].equalsIgnoreCase("summon")) {
                elitePowers.add(new CustomSummonPower(powers));
                hasCustomSummons = true;
            } else if (hasCustomSummons && powerName.split(":")[0].equalsIgnoreCase("summon")) {
                continue;
            } else if (ElitePower.getElitePower(powerName) != null)
                elitePowers.add(ElitePower.getElitePower(powerName));
            else
                new WarningMessage("Warning: power name " + powerName + " is not registered! Skipping it for custom mob construction...");
        }
        return elitePowers;
    }
}
