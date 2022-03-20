package com.magmaguy.elitemobs.instanced;

import lombok.Getter;
import lombok.Setter;

import javax.xml.stream.Location;

public class ArenaEntity {
    @Getter
    @Setter
    private String spawnPointName;
    @Getter
    @Setter
    private Location spawnLocation;
    @Getter
    @Setter
    private int wave;
    @Getter
    @Setter
    private String bossfile;

    public ArenaEntity(String spawnPointName, int wave, String bossfile) {
        this.spawnPointName = spawnPointName;
        this.wave = wave;
        this.bossfile = bossfile;
    }

}
