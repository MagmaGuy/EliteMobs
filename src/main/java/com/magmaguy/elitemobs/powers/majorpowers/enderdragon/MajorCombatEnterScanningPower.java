package com.magmaguy.elitemobs.powers.majorpowers.enderdragon;

import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.MajorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;

public abstract class MajorCombatEnterScanningPower extends MajorPower implements Listener {

    public static HashSet<MajorCombatEnterScanningPower> majorCombatEnterScanningPowers = new HashSet<>();
    public BukkitTask bukkitTask = null;
    private boolean isActive = false;
    public MajorCombatEnterScanningPower(PowersConfigFields powersConfigFields) {
        super(powersConfigFields);
        majorCombatEnterScanningPowers.add(this);
    }

    protected void activate(EliteEntity eliteEntity) {
        if (isActive) return;
        isActive = true;
        finishActivation(eliteEntity);
    }

    protected abstract void finishActivation(EliteEntity eliteEntity);

    public void deactivate(EliteEntity eliteEntity) {
        if (bukkitTask != null)
            bukkitTask.cancel();
        isActive = false;
        finishDeactivation(eliteEntity);
    }

    protected abstract void finishDeactivation(EliteEntity eliteEntity);

    protected boolean doExit(EliteEntity eliteEntity) {
        if (eliteEntity == null ||
                !eliteEntity.isValid()) {
            deactivate(eliteEntity);
            return true;
        }

        return false;
    }

    public static class MajorCombatEnterScanningPowerEvents implements Listener {
        @EventHandler
        public void onCombatEnter(EliteMobEnterCombatEvent event) {
            for (MajorCombatEnterScanningPower majorCombatEnterScanningPower : majorCombatEnterScanningPowers) {
                ElitePower elitePowerInstance = event.getEliteMobEntity().getPower(majorCombatEnterScanningPower);
                if (elitePowerInstance == null)
                    continue;
                ((MajorCombatEnterScanningPower) elitePowerInstance).activate(event.getEliteMobEntity());
            }

        }

        @EventHandler
        public void onCombatExit(EliteMobExitCombatEvent event) {
            for (MajorCombatEnterScanningPower majorCombatEnterScanningPower : majorCombatEnterScanningPowers) {
                ElitePower elitePowerInstance = event.getEliteMobEntity().getPower(majorCombatEnterScanningPower);
                if (elitePowerInstance == null)
                    continue;
                ((MajorCombatEnterScanningPower) elitePowerInstance).deactivate(event.getEliteMobEntity());
            }
        }
    }

}
