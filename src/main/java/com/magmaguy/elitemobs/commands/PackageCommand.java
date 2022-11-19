package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.ZipFile;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PackageCommand {
    CommandSender commandSender;
    String dungeonFolderName;

    public PackageCommand(CommandSender commandSender, String dungeonFolderName, String versionNumber) {
        this.commandSender = commandSender;
        if (dungeonFolderName == null || dungeonFolderName.isEmpty()) {
            commandSender.sendMessage("[EliteMobs] This commands needs a valid dungeon name!");
            return;
        }
        int version = 0;
        try {
            version = Integer.parseInt(versionNumber);
        } catch (Exception exception) {
            commandSender.sendMessage("This command needs a valid natural number! (1, 2, 3, 4...)");
            return;
        }

        this.dungeonFolderName = dungeonFolderName;
        clearPreviousContents();
        packContents("custombosses");
        packContents("customevents");
        packContents("npcs");
        packContents("customitems");
        packContents("customquests");
        packContents("customarenas");
        packContents("customspawns");
        packContents("customtreasurechests");
        packContents("wormholes");
        packContents("world_blueprints");
        packContents("powers");

        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Done! You can find your package in &9~/plugins/EliteMobs/exports &2. &6If you are making a dungeon, make sure to create your own dungeonpackages file!"));
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &6Don't forget to add your world and schematic files to the package if needed!"));

        try {
            ZipFile.ZipUtility.zip(new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName),
                    MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName + "_packaged.zip");
        } catch (Exception exception) {
            commandSender.sendMessage(ChatColorConverter.convert("4&[EliteMobs] Failed to zip package!"));
        }

        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &6Zipped files for your convenience. Don't forget any additional files like the dungeonpackager or world/schematics files before distributing!"));
    }

    private void clearPreviousContents() {
        File targetFolder = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName);
        if (targetFolder.exists()) delete(targetFolder.toPath());
        if (new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName + "_packaged.zip").exists())
            delete(Path.of(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName + "_packaged.zip"));
    }

    private void delete(Path path) {
        for (File file : path.toFile().listFiles()) {
            if (file.isDirectory()) delete(file.toPath());
            else file.delete();
        }
    }

    private void packContents(String subdirectory) {
        Path path = Path.of(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + subdirectory + File.separatorChar + dungeonFolderName);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            commandSender.sendMessage("[EliteMobs] Could not find any " + subdirectory + " for this dungeon. This might be normal depending on your setup.");
            return;
        }
        File sourceFolder = new File(path.toString());
        File targetFolder = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName + File.separatorChar + subdirectory + File.separatorChar + dungeonFolderName);
        if (!targetFolder.exists()) {
            try {
                targetFolder.mkdirs();
                targetFolder.mkdir();
            } catch (Exception ex) {
                commandSender.sendMessage("[EliteMobs] Failed to create directory " + targetFolder.getPath());
                ex.printStackTrace();
                return;
            }
        }
        for (File file : sourceFolder.listFiles())
            recursivelyGetFiles(file, targetFolder);
    }

    private void recursivelyGetFiles(File scannedFile, File destination) {
        try {
            if (scannedFile.isDirectory()) {
                File newDestination = new File(destination.getAbsolutePath() + File.separatorChar + scannedFile.getName());
                newDestination.mkdir();
                for (File file : scannedFile.listFiles())
                    recursivelyGetFiles(file, newDestination);
            } else
                Files.copy(scannedFile.toPath(), Path.of(destination.getAbsolutePath() + File.separatorChar + scannedFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            new WarningMessage("Failed to recursively pack dungeon!");
        }
    }
}
