package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BonusCoins extends BossPower {
    private double coinMultiplier = 2D;

    public BonusCoins() {
        super(PowersConfig.getPower("bonus_coins.yml"));
    }

    public void setCoinMultiplier(double coinMultiplier) {
        this.coinMultiplier = coinMultiplier;
    }

    public void doCoinDrop(EliteEntity eliteEntity, Player player) {
        new ItemLootShower(eliteEntity.getLevel() * coinMultiplier, eliteEntity.getUnsyncedLivingEntity().getLocation(), player);
    }

    public static class BonusCoinsEvents implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            BonusCoins bonusCoins = (BonusCoins) event.getEliteEntity().getPower("bonus_coins.yml");
            if (bonusCoins == null) return;
            for (Player player : event.getEliteEntity().getDamagers().keySet())
                if (!player.hasMetadata("NPC") && PlayerData.isInMemory(player.getUniqueId()))
                    if (event.getEliteEntity().getDamagers().get(player) / event.getEliteEntity().getMaxHealth() > 0.1)
                        bonusCoins.doCoinDrop(event.getEliteEntity(), player);
        }
    }
}
