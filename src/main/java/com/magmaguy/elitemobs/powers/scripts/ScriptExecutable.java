package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface ScriptExecutable {

    void check(EliteEntity eliteEntity, LivingEntity directTarget, ScriptActionData previousScriptActionData);

    void check(Location landingLocation, ScriptActionData previousScriptActionData);
}
