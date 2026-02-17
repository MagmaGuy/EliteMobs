package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
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
import java.util.UUID;

public class TransitiveBlockCommand {

    public static HashMap<UUID, TransitiveBlockCommand> activePlayers = new HashMap<>();

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
                            Logger.warn(message);
                            player.sendMessage(message);
                        }
                        if (bossPhase.customBossesConfigFields.equals(customBossesConfigFields)) {
                            this.regionalBossEntity = iteratedRegionalBossEntity;
                            break;
                        }
                    }
            if (this.regionalBossEntity == null) {
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockRequiresRegionalMessage());
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancellingMessage());
                return;
            }
        }

        if (this.regionalBossEntity == null) {
            if (regionalBossEntities.size() > 1) {
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockMultipleBossesMessage());
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockMultipleBossesHintMessage());
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancellingMessage());
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

        activePlayers.put(player.getUniqueId(), this);
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockNowRegisteringMessage().replace("$type", transitiveBlockType.toString()));
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockRunSameCommandMessage());
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancelCommandMessage());
    }
    private final Player player;
    private final CustomBossesConfigFields customBossesConfigFields;
    private final TransitiveBlockType transitiveBlockType;
    private List<TransitiveBlock> transitiveBlockList = new ArrayList<>();
    private RegionalBossEntity regionalBossEntity;
    @Getter
    @Setter
    private boolean regionalSelection = false;
    private Location corner1, corner2;

    public static void shutdown() {
        activePlayers.clear();
    }

    public static TransitiveBlockCommand processCommand(Player player, String filename, String transitiveBlockType, boolean edit, boolean regionalSelection) {
        TransitiveBlockCommand transitiveBlockCommand = processCommand(player, filename, transitiveBlockType, edit);
        if (transitiveBlockCommand != null)
            transitiveBlockCommand.setRegionalSelection(regionalSelection);
        if (transitiveBlockCommand != null)
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockStartMessage());
        return transitiveBlockCommand;
    }

    public static TransitiveBlockCommand processCommand(Player player, String filename, String transitiveBlockType, boolean edit) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockInvalidBossMessage());
            return null;
        }
        if (!customBossesConfigFields.isRegionalBoss()) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockNotRegionalMessage());
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockIgnoreWarningMessage());
        }
        TransitiveBlockType tbt = null;
        try {
            tbt = TransitiveBlockType.valueOf(transitiveBlockType);
        } catch (Exception ex) {
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockInvalidTypeMessage());
            return null;
        }
        UUID playerUUID = player.getUniqueId();
        if (activePlayers.containsKey(playerUUID)) {
            activePlayers.get(playerUUID).commitLocations();
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
        activePlayers.remove(player.getUniqueId());
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockCancelledMessage());
    }

    public void setCorner(boolean leftClick, Location location) {
        if (leftClick) {
            corner1 = location;
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockCorner1Message());
        } else {
            corner2 = location;
            player.sendMessage(CommandMessagesConfig.getTransitiveBlockCorner2Message());
        }

        if (corner1 != null && corner2 != null && corner1.getWorld() == corner2.getWorld()) {
            int blockCount = (int) ((Math.abs(corner1.getX() - corner2.getX()) + 1) * (Math.abs(corner1.getY() - corner2.getY()) + 1) * (Math.abs(corner1.getZ() - corner2.getZ()) + 1));
            if (blockCount > DefaultConfig.getDefaultTransitiveBlockLimiter())
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockSelectionCountMessage()
                        .replace("$count", String.valueOf(blockCount))
                        .replace("$limit", String.valueOf(DefaultConfig.getDefaultTransitiveBlockLimiter())));
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
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockRegisteredMessage().replace("$type", block.getType().toString()));
    }

    public void registerAir(Block block) {
        if (doubleEntryCheck(block, true)) return;
        transitiveBlockList.add(new TransitiveBlock(Material.AIR.createBlockData(), getRelativeCoordinate(block.getLocation())));
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockRegisteredAirMessage());
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
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockUnregisteredMessage());
        return true;
    }

    public void commitLocations() {
        activePlayers.remove(player.getUniqueId());
        player.sendMessage(CommandMessagesConfig.getTransitiveBlockNowSavingMessage().replace("$type", transitiveBlockType.toString()));
        List<String> deserializedData = new ArrayList<>();

        if (regionalSelection) {
            int blockCount = (int) ((Math.abs(corner1.getX() - corner2.getX()) + 1) * (Math.abs(corner1.getY() - corner2.getY()) + 1) * (Math.abs(corner1.getZ() - corner2.getZ()) + 1));
            if (blockCount > DefaultConfig.getDefaultTransitiveBlockLimiter()) {
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockTooManyMessage().replace("$limit", String.valueOf(DefaultConfig.getDefaultTransitiveBlockLimiter())));
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

            player.sendMessage(CommandMessagesConfig.getTransitiveBlockRegisteredCornerMessage().replace("$count", String.valueOf(transitiveBlockList.size())));

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
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsRegisteredMessage());
                break;
            case ON_REMOVE:
                customBossesConfigFields.setOnRemoveBlockStates(deserializedData);
                if (regionalBossEntity.getCustomBossesConfigFields().getFilename().equals(customBossesConfigFields.getFilename()))
                    regionalBossEntity.setOnRemoveTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(deserializedData, customBossesConfigFields.getFilename()));
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsRegisteredMessage());
                break;
            default:
                player.sendMessage(CommandMessagesConfig.getTransitiveBlockLocationsFailedMessage());
        }
    }

    public enum TransitiveBlockType {
        ON_SPAWN,
        ON_REMOVE
    }

    public static class TemporaryBossBlockCommandEvents implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPlayerInteractBlockEvent(PlayerInteractEvent event) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (!activePlayers.containsKey(playerUUID)) return;
            if (event.getHand() != EquipmentSlot.HAND) return;
            if (event.getClickedBlock() == null) return;
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (event.getClickedBlock().getType().isAir()) return;
                if (activePlayers.get(playerUUID).isRegionalSelection())
                    activePlayers.get(playerUUID).setCorner(false, event.getClickedBlock().getLocation());
                else
                    activePlayers.get(playerUUID).registerBlock(event.getClickedBlock());
                event.setCancelled(true);
            } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (activePlayers.get(playerUUID).isRegionalSelection())
                    activePlayers.get(playerUUID).setCorner(true, event.getClickedBlock().getLocation());
                else
                    activePlayers.get(playerUUID).registerAir(event.getClickedBlock());
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            activePlayers.remove(event.getPlayer().getUniqueId());
        }
    }

}
