package com.magmaguy.elitemobs.config.customarenas;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CustomArenasConfigFields extends CustomConfigFields {

    @Getter
    @Setter
    private String arenaName;
    @Getter
    @Setter
    private String corner1;
    @Getter
    @Setter
    private String corner2;
    @Getter
    @Setter
    private String startLocation;
    @Getter
    @Setter
    private String exitLocation;
    @Getter
    @Setter
    private int waveCount;
    @Getter
    @Setter
    private int delayBetweenWaves;
    @Getter
    @Setter
    private List<String> spawnPoints = new ArrayList<>();
    @Getter
    @Setter
    private List<String> bossList = new ArrayList<>();
    @Getter
    @Setter
    private List<String> rawArenaRewards = new ArrayList<>();
    @Getter
    @Setter
    private CustomLootTable arenaRewards;
    @Getter
    @Setter
    private int minimumPlayerCount = 1;
    @Getter
    @Setter
    private int maximumPlayerCount;
    @Getter
    @Setter
    private List<String> arenaMessages = new ArrayList<>();
    @Getter
    @Setter
    private boolean cylindricalArena = false;
    @Getter
    @Setter
    private String permission = null;
    @Getter
    private Location teleportLocation = null;

    public CustomArenasConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.arenaName = translatable(filename, "arenaName", processString("arenaName", arenaName, "Default name", false));
        this.corner1 = processString("corner1", corner1, null, false);
        this.corner2 = processString("corner2", corner2, null, false);
        this.startLocation = processString("startLocation", startLocation, null, false);
        this.exitLocation = processString("exitLocation", exitLocation, null, false);
        this.waveCount = processInt("waveCount", waveCount, 0, false);
        this.delayBetweenWaves = processInt("delayBetweenWaves", delayBetweenWaves, 0, false);
        this.spawnPoints = processStringList("spawnPoints", spawnPoints, new ArrayList<>(), false);
        this.bossList = processStringList("bossList", bossList, new ArrayList<>(), false);
        this.rawArenaRewards = processStringList("rawArenaReward", rawArenaRewards, new ArrayList<>(), false);
        arenaRewards = new CustomLootTable(this);
        this.minimumPlayerCount = processInt("minimumPlayerCount", minimumPlayerCount, 1, false);
        this.maximumPlayerCount = processInt("maximumPlayerCount", maximumPlayerCount, 100, false);
        this.arenaMessages = translatable(filename, "arenaMessages", processStringList("arenaMessages", arenaMessages, new ArrayList<>(), false));
        this.cylindricalArena = processBoolean("cylindricalArena", cylindricalArena, false, false);
        this.permission = processString("permission", permission, null, false);
        this.teleportLocation = processLocation("teleportLocation", teleportLocation, null, false);
    }

}
