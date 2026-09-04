package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.UnbindEnchantment;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class UnbindForceCommand extends AdvancedCommand {
    public UnbindForceCommand() {
        super(List.of("unbind"));
        addLiteral("force");
        setUsage("/em unbind force");
        setSenderType(SenderType.PLAYER);
        setPermission("elitemobs.unbind.force");
        setDescription("Forcefully unbinds a held item.");
    }

    @Override
    public void execute(CommandData commandData) {
        ItemStack itemStack = commandData.getPlayerSender().getInventory().getItemInMainHand();
        // Gate on the soulbind tag itself, not isEliteItem: soulbound currency
        // drops carry the tag without being elite items and could never be
        // force-unbound. unbindItem (unlike removeEnchantment) also strips the
        // prestige tag, and works on a clone that must be written back.
        if (itemStack != null && itemStack.hasItemMeta()
                && SoulbindEnchantment.itemHasSoulbindEnchantment(itemStack.getItemMeta())) {
            commandData.getPlayerSender().getInventory()
                    .setItemInMainHand(UnbindEnchantment.unbindItem(itemStack));
            Logger.sendMessage(commandData.getCommandSender(),
                    CommandMessagesConfig.getUnbindForceSuccessMessage());
        } else {
            Logger.sendMessage(commandData.getCommandSender(),
                    CommandMessagesConfig.getUnbindForceNotSoulboundMessage());
        }
    }
}