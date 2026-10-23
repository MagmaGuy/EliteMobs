package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

public class PetsConfig extends ConfigurationFile {
    public enum Mode {
        /** Pet species also live wild in the world and can be tamed. */
        CREATURES,
        /** Only pets a player owns exist in the world. */
        COSMETIC
    }

    @Getter
    private static Mode mode;
    @Getter
    private static int maxEquippedPets;
    @Getter
    private static boolean petsInvulnerableToPlayers;
    @Getter
    private static boolean petsDiePermanently;
    @Getter
    private static int wildSpawnIntervalTicks;
    @Getter
    private static double wildSpawnChancePerPlayer;
    @Getter
    private static int wildPetCapPerPlayer;
    @Getter
    private static double wildDespawnDistance;
    @Getter
    private static String tamedMessage;
    @Getter
    private static String tameFailedMessage;
    @Getter
    private static String stayMessage;
    @Getter
    private static String followMessage;
    @Getter
    private static String menuTitle;
    @Getter
    private static String noPetsMessage;
    @Getter
    private static String equippedMessage;
    @Getter
    private static String unequippedMessage;
    @Getter
    private static String maxEquippedMessage;
    @Getter
    private static String breedingCooldownMessage;

    public PetsConfig() {
        super("Pets.yml");
    }

    public static void setModeAndSave(Mode newMode) {
        mode = newMode;
        ConfigurationEngine.writeValue(newMode.name(), instance.file, instance.fileConfiguration, "mode");
    }

    private static PetsConfig instance;

    @Override
    public void initializeValues() {
        instance = this;
        String modeName = ConfigurationEngine.setString(
                List.of("CREATURES: pet species spawn wild in the world and can be tamed; pets can be fought in survival.",
                        "COSMETIC: only pets players own exist in the world; no wild spawns.",
                        "/em petadmin mode <creatures|cosmetic> switches this live."),
                file, fileConfiguration, "mode", Mode.COSMETIC.name(), true);
        try {
            mode = Mode.valueOf(modeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            mode = Mode.COSMETIC;
        }
        maxEquippedPets = ConfigurationEngine.setInt(
                List.of("How many pets a player may have out at once. 0 means no limit."),
                fileConfiguration, "maxEquippedPets", 0);
        petsInvulnerableToPlayers = ConfigurationEngine.setBoolean(
                List.of("Whether owned pets ignore damage from players. Always true in COSMETIC mode."),
                fileConfiguration, "petsInvulnerableToPlayers", true);
        petsDiePermanently = ConfigurationEngine.setBoolean(
                List.of("Whether a killed pet leaves its owner's collection. When false it only despawns until re-equipped."),
                fileConfiguration, "petsDiePermanently", false);
        wildSpawnIntervalTicks = ConfigurationEngine.setInt(
                List.of("CREATURES mode: ticks between wild spawn attempts around each player."),
                fileConfiguration, "wildSpawnIntervalTicks", 200);
        wildSpawnChancePerPlayer = ConfigurationEngine.setDouble(
                List.of("CREATURES mode: chance of a wild spawn attempt per player per interval, 0 to 1."),
                fileConfiguration, "wildSpawnChancePerPlayer", 0.05D);
        wildPetCapPerPlayer = ConfigurationEngine.setInt(
                List.of("CREATURES mode: wild pets allowed within 128 blocks of a player before attempts stop."),
                fileConfiguration, "wildPetCapPerPlayer", 4);
        wildDespawnDistance = ConfigurationEngine.setDouble(
                List.of("CREATURES mode: wild pets this far from every player despawn."),
                fileConfiguration, "wildDespawnDistance", 96D);
        tamedMessage = ConfigurationEngine.setString(file, fileConfiguration, "tamedMessage",
                "&a[EM] &fYou tamed a $pet&f! It now follows you. Open &a/em pets &fto manage your pets.", true);
        tameFailedMessage = ConfigurationEngine.setString(file, fileConfiguration, "tameFailedMessage",
                "&7[EM] The $pet &7eats the food but is not tamed yet.", true);
        stayMessage = ConfigurationEngine.setString(file, fileConfiguration, "stayMessage",
                "&7[EM] Your $pet &7stays here.", true);
        followMessage = ConfigurationEngine.setString(file, fileConfiguration, "followMessage",
                "&7[EM] Your $pet &7follows you again.", true);
        menuTitle = ConfigurationEngine.setString(file, fileConfiguration, "menuTitle", "&2Your pets", true);
        noPetsMessage = ConfigurationEngine.setString(file, fileConfiguration, "noPetsMessage",
                "&7[EM] You have no pets yet. Tame a wild creature by feeding it its favorite food.", true);
        equippedMessage = ConfigurationEngine.setString(file, fileConfiguration, "equippedMessage",
                "&a[EM] &f$pet &fis now with you.", true);
        unequippedMessage = ConfigurationEngine.setString(file, fileConfiguration, "unequippedMessage",
                "&7[EM] $pet &7was sent home.", true);
        maxEquippedMessage = ConfigurationEngine.setString(file, fileConfiguration, "maxEquippedMessage",
                "&c[EM] You can only have $amount pets with you at once.", true);
        breedingCooldownMessage = ConfigurationEngine.setString(file, fileConfiguration, "breedingCooldownMessage",
                "&7[EM] Your $pet &7is not ready to breed yet.", true);
    }
}
