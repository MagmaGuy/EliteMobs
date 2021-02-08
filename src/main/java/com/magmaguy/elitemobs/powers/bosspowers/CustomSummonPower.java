package com.magmaguy.elitemobs.powers.bosspowers;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.ElitePower;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomSummonPower extends ElitePower implements Listener {

    public enum SummonType {
        ONCE,
        ON_HIT
    }

    private class CustomBossReinforcement {
        private boolean isSummoned = false;
        private SummonType summonType;
        double summonChance;
        String bossFileName;
    }

    List<CustomBossReinforcement> customBossReinforcements = new ArrayList<>();

    public CustomSummonPower(List<String> powerStrings) {
        super(PowersConfig.getPower("custom_summon.yml"));
        /*
        valid formats:
        summon:once:filename.yml
        summon:onHit:%:filename.yml
         */
        for (String powerString : powerStrings) {
            if (powerString.split(":")[0].equalsIgnoreCase("summon"))
                if (powerString.split(":")[1].equalsIgnoreCase("once"))
                    parseOnce(powerString);
                else if (powerString.split(":")[1].equalsIgnoreCase("onHit"))
                    parseOnHit(powerString);
        }

    }

    private void parseOnce(String powerString) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement();
        customBossReinforcement.summonType = SummonType.ONCE;
        String[] strings = powerString.split(":");
        customBossReinforcement.bossFileName = strings[2];
        customBossReinforcements.add(customBossReinforcement);
    }

    private void parseOnHit(String powerString) {
        CustomBossReinforcement customBossReinforcement = new CustomBossReinforcement();
        customBossReinforcement.summonType = SummonType.ON_HIT;
        String[] strings = powerString.split(":");
        customBossReinforcement.summonChance = Double.parseDouble(strings[2]);
        customBossReinforcement.bossFileName = strings[3];
        customBossReinforcements.add(customBossReinforcement);
    }

    private void summonReinforcement(EliteMobEntity spawningEntity) {
        for (CustomBossReinforcement customBossReinforcement : customBossReinforcements) {
            if (customBossReinforcement.summonType.equals(SummonType.ONCE) && !customBossReinforcement.isSummoned) {
                CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawningEntity.getLivingEntity().getLocation(), spawningEntity.getLevel());
                customBossReinforcement.isSummoned = true;
            }

            if (customBossReinforcement.summonType.equals(SummonType.ON_HIT) && ThreadLocalRandom.current().nextDouble() < customBossReinforcement.summonChance) {
                CustomBossEntity.constructCustomBoss(customBossReinforcement.bossFileName, spawningEntity.getLivingEntity().getLocation(), spawningEntity.getLevel());
            }
        }
    }


    public static class CustomSummonPowerEvent implements Listener {
        @EventHandler
        public void onHit(EliteMobDamagedByPlayerEvent event) {
            CustomSummonPower customSummonPower = (CustomSummonPower) event.getEliteMobEntity().getPower("custom_summon.yml");
            if (customSummonPower == null) return;
            if (!eventIsValid(event, customSummonPower)) return;
            customSummonPower.summonReinforcement(event.getEliteMobEntity());
        }
    }

}
