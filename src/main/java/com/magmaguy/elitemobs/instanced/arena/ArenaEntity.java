package com.magmaguy.elitemobs.instanced.arena;

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
    @Getter
    @Setter
    private boolean mythicMob = false;
    @Getter
    @Setter
    private int level = -1;

    public ArenaEntity(String spawnPointName, int wave, String bossfile) {
        this.spawnPointName = spawnPointName;
        this.wave = wave;
        this.bossfile = bossfile;
    }

}
