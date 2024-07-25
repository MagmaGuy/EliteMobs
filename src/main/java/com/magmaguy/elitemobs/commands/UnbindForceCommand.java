package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class UnbindForceCommand extends AdvancedCommand {
    public UnbindForceCommand() {
        super(List.of("unbind"));
        addLiteral("force");
        setUsage("/em unbind force");
        setSenderType(SenderType.PLAYER);
        setPermission("elitemobs.*");
        setDescription("Forcefully unbinds a held item.");
    }

    @Override
    public void execute() {
        ItemStack itemStack = getCurrentPlayerSender().getInventory().getItemInMainHand();
        if (ItemTagger.isEliteItem(itemStack))
            SoulbindEnchantment.removeEnchantment(itemStack);
    }
}