package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.ZipFile;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class PackageCommand {
    private static final List<String> CONTENT_DIRECTORIES = List.of(
            "custombosses", "customevents", "npcs", "npc_scripts", "transport_routes", "customitems",
            "customquests", "customarenas", "customspawns", "customtreasurechests", "wormholes",
            "world_blueprints", "powers", "behaviors");

    public PackageCommand(CommandSender commandSender, String dungeonFolderName, String versionNumber) {
        if (dungeonFolderName == null || dungeonFolderName.isBlank()) {
            commandSender.sendMessage(CommandMessagesConfig.getPackageNeedsDungeonNameMessage());
            return;
        }
        try {
            Integer.parseInt(versionNumber);
        } catch (NumberFormatException exception) {
            commandSender.sendMessage(CommandMessagesConfig.getPackageNeedsNumberMessage());
            return;
        }

        Path staging = null;
        Path temporaryZip = null;
        Path exports = null;
        try {
            validateFolderName(dungeonFolderName);
            Path data = MetadataHandler.PLUGIN.getDataFolder().toPath().toRealPath();
            Map<String, Path> sources = new LinkedHashMap<>();
            for (String directory : CONTENT_DIRECTORIES) {
                Path source = data.resolve(directory).resolve(dungeonFolderName);
                try {
                    BasicFileAttributes attributes = Files.readAttributes(source, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    if (attributes.isSymbolicLink() || !attributes.isDirectory())
                        throw new IOException("Content source is not an unlinked directory: " + source);
                    sources.put(directory, realOwnedDirectory(data, source));
                } catch (NoSuchFileException missingSource) {
                    commandSender.sendMessage(CommandMessagesConfig.getPackageNoSubdirectoryMessage()
                            .replace("$subdirectory", directory));
                }
            }
            if (sources.isEmpty()) throw new IOException("No content folders exist for " + dungeonFolderName);

            for (ContentPackagesConfigFields fields : java.util.stream.Stream.concat(
                    ContentPackagesConfig.getDungeonPackages().values().stream(),
                    ContentPackagesConfig.getEnchantedChallengeDungeonPackages().values().stream()).toList()) {
                if (!dungeonFolderName.equals(fields.getDungeonConfigFolderName())) continue;
                Path blueprintRoot = sources.get("world_blueprints");
                if (fields.getWorldName() == null || blueprintRoot == null) continue;
                Path blueprint = blueprintRoot.resolve(fields.getWorldName()).normalize();
                if (blueprint.equals(blueprintRoot) || !blueprint.startsWith(blueprintRoot))
                    throw new IOException("World blueprint is outside its content folder: " + blueprint);
                if (Files.exists(blueprint, LinkOption.NOFOLLOW_LINKS)) {
                    realOwnedDirectory(blueprintRoot, blueprint);
                    if (!WorldInstantiator.validateBlueprint(fields.getWorldName(), dungeonFolderName, fields.getEnvironment()))
                        return;
                }
            }

            Path exportDirectory = data.resolve("exports");
            Files.createDirectories(exportDirectory);
            exports = realOwnedDirectory(data, exportDirectory);
            staging = Files.createTempDirectory(exports, ".package-");
            for (Map.Entry<String, Path> source : sources.entrySet()) {
                Path destination = staging.resolve(source.getKey()).resolve(dungeonFolderName);
                copyContents(source.getValue(), destination);
            }
            temporaryZip = Files.createTempFile(exports, ".package-", ".zip");
            ZipFile.ZipUtility.zip(staging.toFile(), temporaryZip.toString());

            // Keep the previous archive until its complete replacement is ready.
            Path expandedExport = exports.resolve(dungeonFolderName);
            if (Files.exists(expandedExport, LinkOption.NOFOLLOW_LINKS)) deleteOwnedTree(exports, expandedExport);
            Files.move(staging, expandedExport);
            Path archive = exports.resolve(dungeonFolderName + "_packaged.zip");
            if (Files.isSymbolicLink(archive)) throw new IOException("Export archive must not be a symbolic link");
            try {
                Files.move(temporaryZip, archive, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporaryZip, archive, StandardCopyOption.REPLACE_EXISTING);
            }
            commandSender.sendMessage(CommandMessagesConfig.getPackageDoneMessage());
            commandSender.sendMessage(CommandMessagesConfig.getPackageDontForgetMessage());
            commandSender.sendMessage(CommandMessagesConfig.getPackageZippedMessage());
        } catch (IOException | InvalidPathException failure) {
            Logger.warn("Failed to package dungeon " + dungeonFolderName + ": " + failure.getMessage());
            commandSender.sendMessage(CommandMessagesConfig.getPackageZipFailedMessage());
        } finally {
            try {
                if (temporaryZip != null) Files.deleteIfExists(temporaryZip);
            } catch (IOException cleanupFailure) {
                Logger.warn("Failed to clean temporary dungeon archive: " + cleanupFailure.getMessage());
            }
            try {
                if (staging != null && Files.exists(staging, LinkOption.NOFOLLOW_LINKS)) deleteOwnedTree(exports, staging);
            } catch (IOException cleanupFailure) {
                Logger.warn("Failed to clean temporary dungeon export: " + cleanupFailure.getMessage());
            }
        }
    }

    private static void validateFolderName(String name) throws IOException {
        Path path = Path.of(name);
        if (name.isBlank() || path.isAbsolute() || path.getNameCount() != 1
                || name.equals(".") || name.equals("..") || name.contains("/") || name.contains("\\")
                || name.contains(":") || name.endsWith(".") || name.endsWith(" "))
            throw new IOException("Expected one content folder name: " + name);
    }

    private static Path realOwnedDirectory(Path owner, Path directory) throws IOException {
        Path real = directory.toRealPath();
        if (real.equals(owner) || !real.startsWith(owner)
                || !real.equals(directory.toRealPath(LinkOption.NOFOLLOW_LINKS))
                || !Files.isDirectory(real))
            throw new IOException("Directory is not an unlinked child of " + owner + ": " + directory);
        return real;
    }

    private static void copyContents(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
                if (!directory.toRealPath().equals(directory)) throw new IOException("Linked content directory: " + directory);
                Files.createDirectories(destination.resolve(source.relativize(directory)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                if (!attributes.isRegularFile() || attributes.isSymbolicLink())
                    throw new IOException("Unsupported linked or special content file: " + file);
                Files.copy(file, destination.resolve(source.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteOwnedTree(Path exports, Path directory) throws IOException {
        Path owned = realOwnedDirectory(exports, directory);
        Files.walkFileTree(owned, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path current, BasicFileAttributes attributes) throws IOException {
                if (!current.toRealPath().equals(current)) throw new IOException("Linked export directory: " + current);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path current, IOException failure) throws IOException {
                if (failure != null) throw failure;
                Files.delete(current);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
