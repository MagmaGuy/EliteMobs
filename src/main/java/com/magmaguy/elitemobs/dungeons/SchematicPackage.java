package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.thirdparty.worldedit.WorldEditUtils;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SchematicPackage extends EMPackage {
    private static boolean warnedAboutWorldEdit = false;

    public SchematicPackage(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        super(dungeonPackagerConfigFields);
    }

    @Override
    public void baseInitialization() {
        super.baseInitialization();
        if (dungeonPackagerConfigFields.getAnchorPoint() != null) {
            this.isInstalled = this.isDownloaded = true;
            dungeonPackagerConfigFields.initializeSchematic();
            return;
        }

        if (this.dungeonPackagerConfigFields.getSchematicName() == null || this.dungeonPackagerConfigFields.getSchematicName().isEmpty()) {
            this.isDownloaded = false;
            new WarningMessage("The schematic package " + this.dungeonPackagerConfigFields.getFilename() + " does not have a valid schematic file name!");
        }

        this.isDownloaded = Files.exists(Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath()
                + File.separator + "schematics" + File.separator + dungeonPackagerConfigFields.getSchematicName()));


        //If worldedit isn't installed, the checks can't be continued
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            this.isDownloaded = false;
            if (!warnedAboutWorldEdit) {
                warnedAboutWorldEdit = true;
                new WarningMessage("Schematic dungeons are downloaded but neither WorldEdit nor FastAsyncWorldEdit are installed! As such, you will not be able to install the dungeons.");
            }
        }

    }

    public Vector toRelativePosition(Location location) {
        return dungeonPackagerConfigFields.getAnchorPoint().clone().subtract(location).toVector().rotateAroundY(Math.toRadians(getDungeonPackagerConfigFields().getCalculatedRotation()));
    }

    public Location toRealPosition(Vector vector) {
        return dungeonPackagerConfigFields.getAnchorPoint().clone().add(vector.clone().rotateAroundY(Math.toRadians(getDungeonPackagerConfigFields().getCalculatedRotation())));
    }

    @Override
    public boolean install(Player player, boolean paste) {
        if (!super.install(player, paste)) return false;
        //Determine rotation based on the direction that is set as facing for the schematic creation
        int currentRotation = Math.round(player.getLocation().getYaw() / 90);
        SchematicRotation pastedRotation;
        switch (currentRotation) {
            case 0:
                pastedRotation = SchematicRotation.SOUTH;
                break;
            case 1:
                pastedRotation = SchematicRotation.EAST;
                break;
            case -1:
                pastedRotation = SchematicRotation.WEST;
                break;
            case 2:
            case -2:
                pastedRotation = SchematicRotation.NORTH;
                break;
            default:
                pastedRotation = SchematicRotation.SOUTH;
        }
        dungeonPackagerConfigFields.setCalculatedRotation(pastedRotation.value - dungeonPackagerConfigFields.getDefaultSchematicRotation().value);
        Location anchorLocation = player.getLocation().getBlock().getLocation().add(new Vector(0.5, 1, 0.5));
        anchorLocation.setDirection(player.getLocation().getDirection());
        //Places the schematic using the WorldEdit API
        if (paste &&
                !WorldEditUtils.place(
                        dungeonPackagerConfigFields.getSchematicName(),
                        getDungeonPackagerConfigFields().getCalculatedRotation(),
                        anchorLocation.clone().subtract(new Vector(0, 1, 0)),
                        this))
            return false;
        dungeonPackagerConfigFields.installSchematic(anchorLocation, getDungeonPackagerConfigFields().getCalculatedRotation(), this);
        WorldGuardCompatibility.defineMinidungeon(
                toRealPosition(dungeonPackagerConfigFields.getCorner1()),
                toRealPosition(dungeonPackagerConfigFields.getCorner2()),
                dungeonPackagerConfigFields.getAnchorPoint(),
                dungeonPackagerConfigFields.getSchematicName(),
                this);
        for (Wormhole wormhole : Wormhole.getWormholes())
            wormhole.onDungeonInstall(dungeonPackagerConfigFields.getFilename());
        player.sendMessage("[EliteMobs] Successfully installed " + dungeonPackagerConfigFields.getName() + "! To uninstall, do /em setup again and click on this content again.");
        return true;
    }

    @Override
    public boolean uninstall(Player player) {
        if (!super.uninstall(player)) return false;
        WorldEditUtils.undo(this);
        WorldGuardCompatibility.removeMinidungeon(dungeonPackagerConfigFields.getSchematicName(), dungeonPackagerConfigFields.getAnchorPoint());
        dungeonPackagerConfigFields.uninstallSchematic();
        for (Wormhole wormhole : Wormhole.getWormholes())
            wormhole.onDungeonUninstall(dungeonPackagerConfigFields.getFilename());
        return true;
    }

    public enum SchematicRotation {
        NORTH(180),
        SOUTH(0),
        EAST(-90),
        WEST(90);

        public final int value;

        SchematicRotation(int value) {
            this.value = value;
        }
    }
}
