package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.FileUtils;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ConfigurationExporter {
    private static final String RSP_RESOURCE_PATH = "em_rsp_defaults";
    private static final String CHECKSUM_FILE = ".rsp_checksum";

    private ConfigurationExporter() {
    }

    public static void initializeConfigs() {
        Path pluginFolder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath());
        Path exportsPath = pluginFolder.resolve("resource_pack");

        // Create exports directory if it doesn't exist
        if (!Files.isDirectory(exportsPath)) {
            try {
                Files.createDirectories(exportsPath);
            } catch (Exception exception) {
                Logger.warn("Failed to create exports directory! Tell the dev!");
                exception.printStackTrace();
                return;
            }
        }

        // Export the resource pack from jar resources
        try {
            exportResourcePack(exportsPath);
        } catch (Exception e) {
            Logger.warn("Failed to export resource pack! Tell the dev!");
            e.printStackTrace();
        }
    }

    /**
     * Exports the em_rsp_defaults folder from jar resources to the exports folder.
     * Uses checksum comparison to avoid unnecessary copying.
     */
    private static void exportResourcePack(Path exportsPath) throws IOException {
        Path targetPath = exportsPath.resolve(RSP_RESOURCE_PATH);
        Path checksumFile = exportsPath.resolve(CHECKSUM_FILE);

        // Calculate checksum of resources in jar
        String jarChecksum = calculateJarResourceChecksum();
        if (jarChecksum == null) {
            Logger.warn("Could not calculate resource pack checksum from jar!");
            return;
        }

        // Check if we need to copy (checksum mismatch or folder doesn't exist)
        boolean needsCopy = true;
        if (Files.exists(checksumFile) && Files.isDirectory(targetPath)) {
            try {
                String existingChecksum = Files.readString(checksumFile).trim();
                if (existingChecksum.equals(jarChecksum)) {
                    needsCopy = false;
                }
            } catch (IOException e) {
                // If we can't read the checksum, we'll recopy
            }
        }

        if (!needsCopy) {
            return; // Resource pack is up to date
        }

        // Stage into a sibling tmp directory first, then swap it in, so a crash mid-copy never
        // leaves a half-written resource pack at the final path.
        Path stagingPath = exportsPath.resolve(RSP_RESOURCE_PATH + ".tmp");
        Path backupPath = exportsPath.resolve(RSP_RESOURCE_PATH + ".backup");

        // Recover the only complete copy if the previous process stopped between moving the live
        // directory aside and installing its staged replacement. If the live directory exists,
        // any leftover backup is merely from a completed swap and can be discarded.
        if (!Files.exists(targetPath) && Files.isDirectory(backupPath))
            Files.move(backupPath, targetPath);
        else
            FileUtils.deleteDirectory(backupPath);

        FileUtils.deleteDirectory(stagingPath);
        Files.createDirectories(stagingPath);

        // Copy resources from jar into the staging directory
        copyResourceFolder(RSP_RESOURCE_PATH, stagingPath);

        // Swap the staged copy into place while retaining the old complete tree until the new one
        // is live. A direct delete-then-move leaves no resource pack if the process stops between
        // those operations.
        boolean movedExistingPack = false;
        if (Files.exists(targetPath)) {
            Files.move(targetPath, backupPath);
            movedExistingPack = true;
        }
        try {
            Files.move(stagingPath, targetPath);
        } catch (IOException exception) {
            if (movedExistingPack && !Files.exists(targetPath) && Files.exists(backupPath))
                Files.move(backupPath, targetPath);
            throw exception;
        }
        FileUtils.deleteDirectory(backupPath);

        // Write checksum file via a tmp file so a partial write never masquerades as a valid checksum
        Path checksumStaging = exportsPath.resolve(CHECKSUM_FILE + ".tmp");
        Files.writeString(checksumStaging, jarChecksum);
        try {
            Files.move(checksumStaging, checksumFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(checksumStaging, checksumFile, StandardCopyOption.REPLACE_EXISTING);
        }
        Logger.info("Resource pack exported to resource_pack folder.");
    }

    /**
     * Calculates a checksum based on the jar resource folder contents.
     */
    private static String calculateJarResourceChecksum() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            URL resourceUrl = MetadataHandler.PLUGIN.getClass().getClassLoader().getResource(RSP_RESOURCE_PATH);

            if (resourceUrl == null) {
                return null;
            }

            if (resourceUrl.getProtocol().equals("jar")) {
                JarURLConnection jarConnection = (JarURLConnection) resourceUrl.openConnection();
                try (JarFile jarFile = jarConnection.getJarFile()) {
                    List<JarEntry> entries = Collections.list(jarFile.entries()).stream()
                            .filter(entry -> entry.getName().startsWith(RSP_RESOURCE_PATH) && !entry.isDirectory())
                            .sorted(Comparator.comparing(JarEntry::getName))
                            .toList();
                    for (JarEntry entry : entries) {
                        digest.update((entry.getName() + "\0").getBytes(StandardCharsets.UTF_8));
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            updateDigest(digest, inputStream);
                        }
                    }
                }
            } else {
                // Running from IDE - use file system
                Path resourcePath = Paths.get(resourceUrl.toURI());
                return calculateDirectoryChecksum(resourcePath);
            }

            return toHex(digest.digest());
        } catch (Exception e) {
            Logger.warn("Error calculating jar resource checksum: " + e.getMessage());
            return null;
        }
    }

    /**
     * Copies a resource folder from the jar to the target path.
     */
    private static void copyResourceFolder(String resourcePath, Path targetPath) throws IOException {
        URL resourceUrl = MetadataHandler.PLUGIN.getClass().getClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            throw new IOException("Resource folder not found: " + resourcePath);
        }

        if (resourceUrl.getProtocol().equals("jar")) {
            // Running from jar
            JarURLConnection jarConnection = (JarURLConnection) resourceUrl.openConnection();
            try (JarFile jarFile = jarConnection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.startsWith(resourcePath + "/")) {
                        // Get relative path from resource root
                        String relativePath = entryName.substring(resourcePath.length() + 1);
                        if (relativePath.isEmpty()) continue;

                        Path targetFile = targetPath.resolve(relativePath);

                        if (entry.isDirectory()) {
                            Files.createDirectories(targetFile);
                        } else {
                            Files.createDirectories(targetFile.getParent());
                            try (InputStream is = jarFile.getInputStream(entry)) {
                                Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }
            }
        } else {
            // Running from IDE - use file system copy
            try {
                Path sourcePath = Paths.get(resourceUrl.toURI());
                copyDirectory(sourcePath, targetPath);
            } catch (Exception e) {
                throw new IOException("Failed to copy resource folder from IDE", e);
            }
        }
    }

    /**
     * Recursively copies a directory.
     */
    private static void copyDirectory(Path source, Path target) throws IOException {
        List<Path> sourcePaths;
        try (Stream<Path> paths = Files.walk(source)) {
            sourcePaths = paths.toList();
        }
        for (Path sourcePath : sourcePaths) {
            Path targetFile = target.resolve(source.relativize(sourcePath));
            if (Files.isDirectory(sourcePath)) {
                Files.createDirectories(targetFile);
            } else {
                Files.createDirectories(targetFile.getParent());
                Files.copy(sourcePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static byte[] sha1CodeByteArray(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fileInputStream = Files.newInputStream(file.toPath());
             DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest)) {
            byte[] bytes = new byte[8192];
            while (digestInputStream.read(bytes) != -1) {
                // Reading through DigestInputStream updates the digest.
            }
        }
        return digest.digest();
    }

    static String calculateDirectoryChecksum(Path resourcePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        List<Path> files;
        try (Stream<Path> paths = Files.walk(resourcePath)) {
            files = paths
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> normalizedRelativePath(resourcePath, path)))
                    .toList();
        }
        for (Path path : files) {
            digest.update((normalizedRelativePath(resourcePath, path) + "\0").getBytes(StandardCharsets.UTF_8));
            try (InputStream inputStream = Files.newInputStream(path)) {
                updateDigest(digest, inputStream);
            }
        }
        return toHex(digest.digest());
    }

    private static String normalizedRelativePath(Path root, Path path) {
        return root.relativize(path).toString().replace(File.separatorChar, '/');
    }

    private static void updateDigest(MessageDigest digest, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1)
            digest.update(buffer, 0, bytesRead);
    }

    private static String toHex(byte[] hashBytes) {
        StringBuilder result = new StringBuilder(hashBytes.length * 2);
        for (byte hashByte : hashBytes)
            result.append(String.format("%02x", hashByte));
        return result.toString();
    }
}
