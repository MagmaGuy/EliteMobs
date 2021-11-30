package com.magmaguy.elitemobs.quests.rewards;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.utils.ObjectSerializer;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RewardEntry implements Serializable {
    @Getter
    private final double chance;
    @Getter
    private final int amount;
    @Getter
    private byte[] deserializedCustomItem;
    @Getter
    private transient ItemStack itemStack;
    @Getter
    private String deserializedItemStack;
    @Getter
    private String command;
    @Getter
    private double currencyAmount = 0;

    public RewardEntry(ItemStack itemStack, double chance, int amount) {
        this.itemStack = itemStack;
        try {
            this.deserializedItemStack = ObjectSerializer.itemStackArrayToBase64(itemStack);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.chance = chance;
        this.amount = amount;
    }

    public RewardEntry(String command, double chance, int amount) {
        this.command = command;
        this.chance = chance;
        this.amount = amount;
    }

    public RewardEntry(double currencyAmount, double chance, int amount, Player player) {
        this.currencyAmount = currencyAmount * GuildRank.currencyBonusMultiplier(player.getUniqueId());
        this.chance = chance;
        this.amount = amount;
    }

    public void serializeReward() {
        if (itemStack == null && deserializedItemStack != null)
            try {
                this.itemStack = ObjectSerializer.itemStackArrayFromBase64(deserializedItemStack);
            } catch (Exception ex) {
                new WarningMessage("Failed to serialize item stack");
                ex.printStackTrace();
            }
    }

    public void doReward(UUID playerUUID, int questLevel) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (ThreadLocalRandom.current().nextDouble() < chance)
            for (int i = 0; i < amount; i++)
                if (itemStack != null)
                    player.getInventory().addItem(itemStack);
                else if (command != null)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("$player", player.getName()));
                else if (currencyAmount != 0)
                    EconomyHandler.addCurrency(playerUUID, currencyAmount);
                else
                    new WarningMessage("Quest failed to dispatch reward! Report this to the dev!", true);
    }
}
