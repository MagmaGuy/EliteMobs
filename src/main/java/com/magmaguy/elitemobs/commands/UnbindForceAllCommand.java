package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customenchantments.UnbindEnchantment;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

/**
 * Bulk companion to {@code /em unbind force}: strips the soulbind (and its
 * prestige tag) from every soulbound item in the sender's inventory and
 * refreshes each item's lore. Exists because disabling the soulbind
 * enchantment only stops future enforcement — items that already dropped keep
 * the tag and the "Soulbound to" lore forever, and unbinding a chest full of
 * them one held item at a time is not a real answer for an admin.
 */
public class UnbindForceAllCommand extends AdvancedCommand {
    public UnbindForceAllCommand() {
        super(List.of("unbind"));
        addLiteral("force");
        addLiteral("all");
        setUsage("/em unbind force all");
        setSenderType(SenderType.PLAYER);
        setPermission("elitemobs.unbind.force");
        setDescription("Forcefully unbinds every soulbound item in your inventory.");
    }

    @Override
    public void execute(CommandData commandData) {
        PlayerInventory inventory = commandData.getPlayerSender().getInventory();
        int unbound = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || !itemStack.hasItemMeta()) continue;
            if (!SoulbindEnchantment.itemHasSoulbindEnchantment(itemStack.getItemMeta())) continue;
            // unbindItem works on a clone, so the cleaned stack must be written back.
            inventory.setItem(slot, UnbindEnchantment.unbindItem(itemStack));
            unbound++;
        }
        Logger.sendMessage(commandData.getCommandSender(),
                CommandMessagesConfig.getUnbindForceAllMessage().replace("$amount", String.valueOf(unbound)));
    }
}
