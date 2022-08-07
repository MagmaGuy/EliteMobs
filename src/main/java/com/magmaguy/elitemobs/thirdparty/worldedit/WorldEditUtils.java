package com.magmaguy.elitemobs.thirdparty.worldedit;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

public class WorldEditUtils {
    private static final HashMap<SchematicPackage, EditSession> schematicPackageEdits = new HashMap<>();

    private WorldEditUtils() {
    }

    public static boolean place(String schematicName, int rotation, Location anchorLocation, SchematicPackage schematicPackage) {
        Clipboard clipboard = load(schematicName);
        if (clipboard == null) return false;
        paste(clipboard, anchorLocation, schematicPackage, rotation);
        return true;
    }

    public static Clipboard load(String schematicName) {
        File schematicFile = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "schematics" + File.separatorChar + schematicName);
        if (!schematicFile.exists()) return null;
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            clipboard = reader.read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clipboard;
    }

    public static void paste(Clipboard clipboard, Location location, SchematicPackage schematicPackage, int rotation) {
        AffineTransform affineTransform = new AffineTransform();
        World world = BukkitAdapter.adapt(location.getWorld());
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            clipboardHolder.setTransform(clipboardHolder.getTransform().combine(affineTransform.rotateY(rotation)));

            Operation operation = clipboardHolder
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                    // configure here
                    .build();
            Operations.complete(operation);
            schematicPackageEdits.put(schematicPackage, editSession);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    public static void undo(SchematicPackage schematicPackage) {
        EditSession editSession = schematicPackageEdits.get(schematicPackage);
        if (editSession == null) return;
        editSession.undo(editSession);
    }
}
