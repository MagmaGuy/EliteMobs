package com.magmaguy.elitemobs.powers.majorpowers.enderdragon;

import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.MajorPower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;

public abstract class MajorCombatEnterScanningPower extends MajorPower implements Listener {

    //todo: check if instancing elite powers will cause this value to keep growing or if it will stop it from registering new powers
    public static HashSet<MajorCombatEnterScanningPower> majorCombatEnterScanningPowers = new HashSet<>();

    public MajorCombatEnterScanningPower(PowersConfigFields powersConfigFields) {
        super(powersConfigFields);
        majorCombatEnterScanningPowers.add(this);
    }

    private boolean isActive = false;
    public BukkitTask bukkitTask = null;

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

    protected void activate(EliteMobEntity eliteMobEntity) {
        if (isActive) return;
        isActive = true;
        finishActivation(eliteMobEntity);
    }

    protected abstract void finishActivation(EliteMobEntity eliteMobEntity);

    public void deactivate(EliteMobEntity eliteMobEntity) {
        if (bukkitTask != null)
            bukkitTask.cancel();
        isActive = false;
        finishDeactivation(eliteMobEntity);
    }

    protected abstract void finishDeactivation(EliteMobEntity eliteMobEntity);

    protected boolean doExit(EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity == null ||
                !eliteMobEntity.getLivingEntity().isValid()) {
            deactivate(eliteMobEntity);
            return true;
        }

        return false;
    }

}
