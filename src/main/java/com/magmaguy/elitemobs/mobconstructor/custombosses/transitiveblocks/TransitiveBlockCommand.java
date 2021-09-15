package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransitiveBlockCommand {

    public static HashMap<Player, TransitiveBlockCommand> activePlayers = new HashMap<>();
    private Player player;
    private CustomBossesConfigFields customBossesConfigFields;
    private List<TransitiveBlock> transitiveBlockList = new ArrayList<>();
    private TransitiveBlockType transitiveBlockType;
    private RegionalBossEntity regionalBossEntity;

    public TransitiveBlockCommand(Player player, CustomBossesConfigFields customBossesConfigFields, TransitiveBlockType transitiveBlockType, boolean edit) {
        this.player = player;
        this.customBossesConfigFields = customBossesConfigFields;
        this.transitiveBlockType = transitiveBlockType;
        List<RegionalBossEntity> regionalBossEntities = RegionalBossEntity.getRegionalBossEntities(customBossesConfigFields);
        if (regionalBossEntities.size() < 1) {
            //check phase bosses
            for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
                if (regionalBossEntity.getPhaseBossEntity() != null)
                    for (PhaseBossEntity.BossPhase bossPhase : regionalBossEntity.getPhaseBossEntity().getBossPhases())
                        if (bossPhase.customBossesConfigFields.equals(customBossesConfigFields)) {
                            this.regionalBossEntity = regionalBossEntity;
                            break;
                        }
            if (this.regionalBossEntity == null) {
                player.sendMessage(ChatColor.RED + "[EliteMobs] Transitive blocks require one Regional Boss from the configuration files to be placed in order to add transitive blocks relative to the spawn coordinates of that boss.");
                player.sendMessage(ChatColor.RED + "[EliteMobs] Cancelling block registration!");
                return;
            }
        }

        if (this.regionalBossEntity == null) {
            if (regionalBossEntities.size() > 1) {
                player.sendMessage(ChatColor.RED + "[EliteMobs] Transitive blocks require having ONLY one Regional Boss from the configuration files to be placed in order to add transitive blocks relative to the spawn coordinates of that boss." +
                        " If there is more than one boss, the plugin can't determine which boss specifically should be getting the transitive blocks");
                player.sendMessage(ChatColor.RED + "[EliteMobs] If want multiple bosses to have transitive blocks, you will need one configuration file per boss that has them!");
                player.sendMessage(ChatColor.RED + "[EliteMobs] Cancelling block registration!");
                return;
            }
            this.regionalBossEntity = regionalBossEntities.get(0);
        }

        if (edit)
            switch (transitiveBlockType){
                case ON_SPAWN:
                    transitiveBlockList = regionalBossEntity.getOnSpawnTransitiveBlocks();
                    break;
                case ON_REMOVE:
                    transitiveBlockList = regionalBossEntity.getOnRemoveTransitiveBlocks();
            }


        activePlayers.put(player, this);
        player.sendMessage(ChatColor.GREEN + "[EliteMobs] Now registering " + transitiveBlockType.toString() + " blocks! " + ChatColor.DARK_GREEN + "Punch blocks to register air at that location, or right click blocks to register the block you right clicked!");
        player.sendMessage(ChatColor.GOLD + "[EliteMobs] Run the same command to stop registering blocks and save!");
        player.sendMessage(ChatColor.GOLD + "[EliteMobs] Or run the command " + "/em cancelblocks " + " to cancel the registration!");
    }

    public static void processCommand(Player player, String filename, String transitiveBlockType, boolean edit) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null) {
            player.sendMessage("Boss file isn't valid! Try again with a valid filename for your custom boss.");
            return;
        }
        if (!customBossesConfigFields.isRegionalBoss()) {
            player.sendMessage(ChatColor.RED + "Boss file isn't for a regional boss! This feature only works for regional bosses.");
            player.sendMessage(ChatColor.GREEN + "Ignore this warning if you are setting blocks for a phase boss whose first phase is a regional boss!");
        }
        TransitiveBlockType tbt = null;
        try {
            tbt = TransitiveBlockType.valueOf(transitiveBlockType);
        } catch (Exception ex) {
            player.sendMessage("Not a valid transitive block type, use ON_SPAWN or ON_REMOVE !");
            return;
        }
        if (activePlayers.containsKey(player))
            activePlayers.get(player).commitLocations();
        else
            new TransitiveBlockCommand(player, customBossesConfigFields, tbt, edit);
    }

    /**
     * Used for the cancel command
     *
     * @param player Player for whom registration should be cancelled
     */
    public static void processCommand(Player player) {
        activePlayers.remove(player);
        player.sendMessage(ChatColor.RED + "[EliteMobs] Block registration successfully cancelled!");
    }

    private Vector getRelativeCoordinate(Location location) {
        return new Vector(location.getX() - regionalBossEntity.getSpawnLocation().getX(),
                location.getY() - regionalBossEntity.getSpawnLocation().getY(),
                location.getZ() - regionalBossEntity.getSpawnLocation().getZ());
    }

    public void registerBlock(Block block) {
        if (doubleEntryCheck(block, false)) return;
        transitiveBlockList.add(new TransitiveBlock(block.getBlockData(), getRelativeCoordinate(block.getLocation())));
        player.sendMessage("Registered " + block.getType() + " block!");
    }

    public void registerAir(Block block) {
        if (doubleEntryCheck(block, true)) return;
        transitiveBlockList.add(new TransitiveBlock(Material.AIR.createBlockData(), getRelativeCoordinate(block.getLocation())));
        player.sendMessage("Registered air block!");
    }

    private boolean doubleEntryCheck(Block block, boolean isAir) {
        TransitiveBlock transitiveBlock = null;
        for (TransitiveBlock element : transitiveBlockList)
            if (element.getRelativeLocation().equals(getRelativeCoordinate(block.getLocation())))
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
        activePlayers.remove(player);
        player.sendMessage(ChatColor.GREEN + "[EliteMobs] Now saving " + transitiveBlockType.toString() + " blocks!");
        List<String> deserializedData = new ArrayList<>();
        for (TransitiveBlock transitiveBlock : transitiveBlockList) {
            String deserializedString = transitiveBlock.getRelativeLocation().getX() + ","
                    + transitiveBlock.getRelativeLocation().getY() + ","
                    + transitiveBlock.getRelativeLocation().getZ() + "/"
                    + transitiveBlock.getBlockData().getAsString();
            deserializedData.add(deserializedString);
        }

        switch (transitiveBlockType) {
            case ON_SPAWN:
                customBossesConfigFields.setOnSpawnBlockStates(deserializedData);
                regionalBossEntity.setOnSpawnTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(deserializedData, customBossesConfigFields.getFilename()));
                player.sendMessage("Locations registered correctly!");
                break;
            case ON_REMOVE:
                customBossesConfigFields.setOnRemoveBlockStates(deserializedData);
                regionalBossEntity.setOnRemoveTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(deserializedData, customBossesConfigFields.getFilename()));
                player.sendMessage("Locations registered correctly!");
                break;
            default:
                player.sendMessage("Failed to commit locations!");
        }
    }

    public enum TransitiveBlockType {
        ON_SPAWN,
        ON_REMOVE
    }

    public static class TemporaryBossBlockCommandEvents implements Listener {
        // @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        // public void onPlayerBreakBlock(BlockBreakEvent event) {
        //     if (!activePlayers.containsKey(event.getPlayer())) return;
        //     activePlayers.get(event.getPlayer()).registerAir(event.getBlock());
        //     event.setCancelled(true);
        // }
//
        // @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        // public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        //     if (!activePlayers.containsKey(event.getPlayer())) return;
        //     activePlayers.get(event.getPlayer()).registerBlock(event.getBlock());
        //     event.setCancelled(true);
        // }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPlayerInteractBlockEvent(PlayerInteractEvent event) {
            if (!activePlayers.containsKey(event.getPlayer())) return;
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getClickedBlock() == null) return;
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (event.getClickedBlock().getType().isAir()) return;
                activePlayers.get(event.getPlayer()).registerBlock(event.getClickedBlock());
                event.setCancelled(true);
            } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                activePlayers.get(event.getPlayer()).registerAir(event.getClickedBlock());
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            if (!activePlayers.containsKey(event.getPlayer())) return;
            activePlayers.remove(event.getPlayer());
        }
    }

}
