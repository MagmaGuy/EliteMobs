package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransitiveBlockCommand {

    public static HashMap<Player, TransitiveBlockCommand> activePlayers = new HashMap<>();

    public static void processCommand(Player player, String filename, String transitiveBlockType) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null) {
            player.sendMessage("Boss file isn't valid! Try again with a valid filename for your custom boss.");
            return;
        }
        if (!customBossesConfigFields.isRegionalBoss()) {
            player.sendMessage("Boss file isn't for a regional boss! This feature only works for regional bosses.");
            return;
        }
        TransitiveBlockType tbt = null;
        try {
            tbt = TransitiveBlockType.valueOf(transitiveBlockType);
        } catch (Exception ex) {
            player.sendMessage("Not a valid transitive block type, use ON_SPAWN or ON_REMOVE !");
            return;
        }
        new TransitiveBlockCommand(player, customBossesConfigFields, tbt);
    }

    public enum TransitiveBlockType {
        ON_SPAWN,
        ON_DEATH
    }

    private Player player;
    private CustomBossesConfigFields customBossesConfigFields;
    private List<TransitiveBlock> transitiveBlockList = new ArrayList<>();
    private TransitiveBlockType transitiveBlockType;

    public TransitiveBlockCommand(Player player, CustomBossesConfigFields customBossesConfigFields, TransitiveBlockType transitiveBlockType) {
        if (!activePlayers.containsKey(player))
            activePlayers.put(player, this);
        else {
            activePlayers.remove(player);
            commitLocations();
            return;
        }
        this.player = player;
        this.customBossesConfigFields = customBossesConfigFields;
        this.transitiveBlockType = transitiveBlockType;
    }

    public void registerBlock(Block block) {
        if (doubleEntryCheck(block, false)) return;
        transitiveBlockList.add(new TransitiveBlock(block.getBlockData(), block.getLocation()));
        player.sendMessage("Registered " + block.getType() + " block!");
    }

    public void registerAir(Block block) {
        if (doubleEntryCheck(block, true)) return;
        transitiveBlockList.add(new TransitiveBlock(Material.AIR.createBlockData(), block.getLocation()));
        player.sendMessage("Registered air block!");
    }

    private boolean doubleEntryCheck(Block block, boolean isAir) {
        TransitiveBlock transitiveBlock = null;
        for (TransitiveBlock element : transitiveBlockList)
            if (element.getLocation().equals(block.getLocation()))
                if (element.isAir() && !isAir || !element.isAir() && isAir) {
                    transitiveBlock = element;
                    break;
                }
        if (transitiveBlock == null) return false;
        transitiveBlockList.remove(transitiveBlock);
        player.sendMessage("Unregistered block!");
        return true;
    }

    public void commitLocations() {
        List<String> deserializedData = new ArrayList<>();
        for (TransitiveBlock transitiveBlock : transitiveBlockList)
            deserializedData.add(ConfigurationLocation.deserialize(transitiveBlock.getLocation()) + ":" + transitiveBlock.getBlockData().getAsString());
        switch (transitiveBlockType) {
            case ON_SPAWN:
                customBossesConfigFields.setOnSpawnBlockStates(deserializedData);
                player.sendMessage("Locations registered correctly!");
                break;
            case ON_DEATH:
                customBossesConfigFields.setOnRemoveBlockStates(deserializedData);
                player.sendMessage("Locations registered correctly!");
                break;
            default:
                player.sendMessage("Failed to commit locations!");
        }
    }

    public static class TemporaryBossBlockCommandEvents implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPlayerBreakBlock(BlockBreakEvent event) {
            if (!activePlayers.containsKey(event.getPlayer())) return;
            activePlayers.get(event.getPlayer()).registerAir(event.getBlock());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPlayerPlaceBlock(BlockPlaceEvent event) {
            if (!activePlayers.containsKey(event.getPlayer())) return;
            activePlayers.get(event.getPlayer()).registerBlock(event.getBlock());
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (!activePlayers.containsKey(event.getPlayer())) return;
            activePlayers.remove(event.getPlayer());
        }
    }

}
