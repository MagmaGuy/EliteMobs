package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.ZipFile;
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
            commandSender.sendMessage(CommandMessagesConfig.getPackageNeedsDungeonNameMessage());
            return;
        }
        int version = 0;
        try {
            version = Integer.parseInt(versionNumber);
        } catch (Exception exception) {
            commandSender.sendMessage(CommandMessagesConfig.getPackageNeedsNumberMessage());
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

        commandSender.sendMessage(CommandMessagesConfig.getPackageDoneMessage());
        commandSender.sendMessage(CommandMessagesConfig.getPackageDontForgetMessage());

        try {
            ZipFile.ZipUtility.zip(new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName),
                    MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName + "_packaged.zip");
        } catch (Exception exception) {
            commandSender.sendMessage(CommandMessagesConfig.getPackageZipFailedMessage());
        }

        commandSender.sendMessage(CommandMessagesConfig.getPackageZippedMessage());
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
            commandSender.sendMessage(CommandMessagesConfig.getPackageNoSubdirectoryMessage().replace("$subdirectory", subdirectory));
            return;
        }
        File sourceFolder = new File(path.toString());
        File targetFolder = new File(MetadataHandler.PLUGIN.getDataFolder().getPath() + File.separatorChar + "exports" + File.separatorChar + dungeonFolderName + File.separatorChar + subdirectory + File.separatorChar + dungeonFolderName);
        if (!targetFolder.exists()) {
            try {
                targetFolder.mkdirs();
                targetFolder.mkdir();
            } catch (Exception ex) {
                commandSender.sendMessage(CommandMessagesConfig.getPackageFailedDirectoryMessage().replace("$path", targetFolder.getPath()));
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
            Logger.warn("Failed to recursively pack dungeon!");
        }
    }
}
