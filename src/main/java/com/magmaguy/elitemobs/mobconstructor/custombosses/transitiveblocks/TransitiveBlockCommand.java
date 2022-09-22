package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
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
    private final Player player;
    private final CustomBossesConfigFields customBossesConfigFields;
    private final TransitiveBlockType transitiveBlockType;
    private List<TransitiveBlock> transitiveBlockList = new ArrayList<>();
    private RegionalBossEntity regionalBossEntity;
    @Getter
    @Setter
    private boolean regionalSelection = false;
    private Location corner1, corner2;

    public TransitiveBlockCommand(Player player, CustomBossesConfigFields customBossesConfigFields, TransitiveBlockType transitiveBlockType, boolean edit) {
        this.player = player;
        this.customBossesConfigFields = customBossesConfigFields;
        this.transitiveBlockType = transitiveBlockType;
        List<RegionalBossEntity> regionalBossEntities = RegionalBossEntity.getRegionalBossEntities(customBossesConfigFields);
        if (regionalBossEntities.isEmpty()) {
            //check phase bosses
            for (RegionalBossEntity iteratedRegionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
                if (iteratedRegionalBossEntity.getPhaseBossEntity() != null)
                    for (PhaseBossEntity.BossPhase bossPhase : iteratedRegionalBossEntity.getPhaseBossEntity().getBossPhases()) {
                        if (bossPhase.customBossesConfigFields == null) {
                            String message = "Could not find valid custom boss config fields for phase boss! This is probably a configuration issue. Check why your phase boss isn't valid on console logs on /em reload and make sure to test the phases in-game!";
                            new WarningMessage(message);
                            player.sendMessage(message);
                        }
                        if (bossPhase.customBossesConfigFields.equals(customBossesConfigFields)) {
                            this.regionalBossEntity = iteratedRegionalBossEntity;
                            break;
                        }
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
            switch (transitiveBlockType) {
                case ON_SPAWN:
                    transitiveBlockList = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnSpawnBlockStates(), customBossesConfigFields.getFilename());
                    break;
                case ON_REMOVE:
                    transitiveBlockList = TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnRemoveBlockStates(), customBossesConfigFields.getFilename());
            }


        activePlayers.put(player, this);
        player.sendMessage(ChatColor.GREEN + "[EliteMobs] Now registering " + transitiveBlockType.toString() + " blocks! " + ChatColor.DARK_GREEN + "Punch blocks to register air at that location, or right click blocks to register the block you right clicked!");
        player.sendMessage(ChatColor.GOLD + "[EliteMobs] Run the same command to stop registering blocks and save!");
        player.sendMessage(ChatColor.GOLD + "[EliteMobs] Or run the command " + "/em cancelblocks " + " to cancel the registration!");
    }

    public static TransitiveBlockCommand processCommand(Player player, String filename, String transitiveBlockType, boolean edit, boolean regionalSelection) {
        TransitiveBlockCommand transitiveBlockCommand = processCommand(player, filename, transitiveBlockType, edit);
        if (transitiveBlockCommand != null)
            transitiveBlockCommand.setRegionalSelection(regionalSelection);
        if (transitiveBlockCommand != null)
            player.sendMessage("[EliteMobs] Now registering large selection for transitive blocks! Left click to set corner 1, right click to set corner 2! Run the same command again to stop registering blocks and run /em cancelblocks to cancel!");
        return transitiveBlockCommand;
    }

    public static TransitiveBlockCommand processCommand(Player player, String filename, String transitiveBlockType, boolean edit) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null) {
            player.sendMessage("Boss file isn't valid! Try again with a valid filename for your custom boss.");
            return null;
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
            return null;
        }
        if (activePlayers.containsKey(player)) {
            activePlayers.get(player).commitLocations();
            return null;
        } else
            return new TransitiveBlockCommand(player, customBossesConfigFields, tbt, edit);
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

    public void setCorner(boolean leftClick, Location location) {
        if (leftClick) {
            corner1 = location;
            player.sendMessage("Set corner 1!");
        } else {
            corner2 = location;
            player.sendMessage("Set corner 2!");
        }

        if (corner1 != null && corner2 != null && corner1.getWorld() == corner2.getWorld()) {
            int blockCount = (int) ((Math.abs(corner1.getX() - corner2.getX()) + 1) * (Math.abs(corner1.getY() - corner2.getY()) + 1) * (Math.abs(corner1.getZ() - corner2.getZ()) + 1));
            if (blockCount > DefaultConfig.getDefaultTransitiveBlockLimiter())
                player.sendMessage("[EliteMobs] Current selection has " + blockCount + " blocks selected. For performance reasons, it is recommended you don't go over " + DefaultConfig.getDefaultTransitiveBlockLimiter() + " blocks!");
        }

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

        if (regionalSelection) {
            int blockCount = (int) ((Math.abs(corner1.getX() - corner2.getX()) + 1) * (Math.abs(corner1.getY() - corner2.getY()) + 1) * (Math.abs(corner1.getZ() - corner2.getZ()) + 1));
            if (blockCount > DefaultConfig.getDefaultTransitiveBlockLimiter()) {
                player.sendMessage("[EliteMobs] You registered more than " + DefaultConfig.getDefaultTransitiveBlockLimiter() + " blocks at once. For performance reasons, it is recommended you keep the selections low. Avoid things like selecting a lot of unnecessary air blocks!");
            }
            int lowestX, highestX, lowestY, highestY, lowestZ, highestZ;
            lowestX = (int) Math.min(corner1.getX(), corner2.getX());
            highestX = (int) Math.max(corner1.getX(), corner2.getX());
            lowestY = (int) Math.min(corner1.getY(), corner2.getY());
            highestY = (int) Math.max(corner1.getY(), corner2.getY());
            lowestZ = (int) Math.min(corner1.getZ(), corner2.getZ());
            highestZ = (int) Math.max(corner1.getZ(), corner2.getZ());

            for (int x = lowestX; x < highestX + 1; x++)
                for (int y = lowestY; y < highestY + 1; y++)
                    for (int z = lowestZ; z < highestZ + 1; z++) {
                        Location blockLocation = new Location(player.getWorld(), x, y, z);
                        transitiveBlockList.add(new TransitiveBlock(blockLocation.getBlock().getBlockData(), getRelativeCoordinate(blockLocation)));
                    }

            player.sendMessage("Successfully registered " + transitiveBlockList.size() + " blocks between your corners!");

        }

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
                if (regionalBossEntity.getCustomBossesConfigFields().getFilename().equals(customBossesConfigFields.getFilename()))
                    regionalBossEntity.setOnSpawnTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(deserializedData, customBossesConfigFields.getFilename()));
                player.sendMessage("Locations registered correctly!");
                break;
            case ON_REMOVE:
                customBossesConfigFields.setOnRemoveBlockStates(deserializedData);
                if (regionalBossEntity.getCustomBossesConfigFields().getFilename().equals(customBossesConfigFields.getFilename()))
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
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPlayerInteractBlockEvent(PlayerInteractEvent event) {
            if (!activePlayers.containsKey(event.getPlayer())) return;
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getClickedBlock() == null) return;
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (event.getClickedBlock().getType().isAir()) return;
                if (activePlayers.get(event.getPlayer()).isRegionalSelection())
                    activePlayers.get(event.getPlayer()).setCorner(false, event.getClickedBlock().getLocation());
                else
                    activePlayers.get(event.getPlayer()).registerBlock(event.getClickedBlock());
                event.setCancelled(true);
            } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (activePlayers.get(event.getPlayer()).isRegionalSelection())
                    activePlayers.get(event.getPlayer()).setCorner(true, event.getClickedBlock().getLocation());
                else
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
