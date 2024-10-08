package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootStats extends AdvancedCommand {
    public LootStats() {
        super(List.of("loot"));
        addLiteral("stats");
        setUsage("/em loot stats");
        setPermission("elitemobs.loot.stats");
        setSenderType(SenderType.PLAYER);
        setDescription("Provides EliteMobs stats for the currently held item.");
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        ItemStack item = player.getInventory().getItemInMainHand();
        double attackSpeed = EliteItemManager.getAttackSpeed(item);
        double damage = EliteItemManager.getBaseDamage(item);
        double dps = EliteItemManager.getDPS(item);
        double itemLevel = EliteItemManager.getWeaponLevel(item);
        double bonusEDPS = EliteItemManager.getTotalDPS(item);
        player.sendMessage("[EliteMobs] Item Stats:");
        player.sendMessage("Item attack speed: " + attackSpeed);
        player.sendMessage("Item damage: " + damage);
        player.sendMessage("Item EDPS: " + dps);
        player.sendMessage("Item level: " + itemLevel);
        player.sendMessage("Item bonus EDPS: " + bonusEDPS);
    }
}