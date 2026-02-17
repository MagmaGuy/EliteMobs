package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

        // Clear existing contents (but keep the directory itself)
        if (Files.exists(targetPath)) {
            clearDirectoryContents(targetPath);
        } else {
            Files.createDirectories(targetPath);
        }

        // Copy resources from jar
        copyResourceFolder(RSP_RESOURCE_PATH, targetPath);

        // Write checksum file
        Files.writeString(checksumFile, jarChecksum);
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
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().startsWith(RSP_RESOURCE_PATH) && !entry.isDirectory()) {
                            // Include file name and size in checksum
                            digest.update(entry.getName().getBytes());
                            digest.update(Long.toString(entry.getSize()).getBytes());
                        }
                    }
                }
            } else {
                // Running from IDE - use file system
                Path resourcePath = Paths.get(resourceUrl.toURI());
                Files.walk(resourcePath)
                        .filter(Files::isRegularFile)
                        .sorted()
                        .forEach(path -> {
                            try {
                                digest.update(path.toString().getBytes());
                                digest.update(Long.toString(Files.size(path)).getBytes());
                            } catch (IOException e) {
                                // Ignore
                            }
                        });
            }

            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
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
            Logger.warn("Resource folder not found: " + resourcePath);
            return;
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
                Logger.warn("Failed to copy resource folder from IDE: " + e.getMessage());
            }
        }
    }

    /**
     * Recursively copies a directory.
     */
    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetFile = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetFile);
                } else {
                    Files.createDirectories(targetFile.getParent());
                    Files.copy(sourcePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Clears the contents of a directory without deleting the directory itself.
     */
    private static void clearDirectoryContents(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before directories
                    .filter(p -> !p.equals(path)) // Don't delete the root directory
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Ignore deletion errors
                        }
                    });
        }
    }

    public static byte[] sha1CodeByteArray(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(file);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);
        byte[] bytes = new byte[1024];
        // read all file content
        while (digestInputStream.read(bytes) > 0) digest = digestInputStream.getMessageDigest();
        fileInputStream.close();
        return digest.digest();
    }
}
