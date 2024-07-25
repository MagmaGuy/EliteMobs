package com.magmaguy.elitemobs.instanced.arena;

import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ArenaWaves {

    private final HashMap<Integer, List<ArenaEntity>> arenaEntities = new HashMap();

    //wave=X:spawnPoint=Y:boss=Z.yml:mythicmob=false
    public ArenaWaves(List<String> rawBosses) {
        for (String iteratedBoss : rawBosses) {
            String[] subString = iteratedBoss.split(":");
            String waveString = "";
            String spawnpointString = "";
            String boss = "";
            String mythicMobString = "";
            String levelString = "";
            for (String iteratedString : subString) {
                String[] valuesString = iteratedString.split("=");
                switch (valuesString[0].toLowerCase(Locale.ROOT)) {
                    case "wave":
                        waveString = valuesString[1];
                        break;
                    case "spawnpoint":
                        spawnpointString = valuesString[1];
                        break;
                    case "boss":
                        boss = valuesString[1];
                        break;
                    case "mythicmob":
                        mythicMobString = valuesString[1];
                        break;
                    case "level":
                        levelString = valuesString[1];
                        break;
                }
            }
            int wave;
            try {
                wave = Integer.valueOf(waveString);
            } catch (Exception exception) {
                Logger.warn("Invalid value for wave in arena wave: " + waveString);
                continue;
            }
            boolean mythicMob;
            if (mythicMobString.isEmpty()) mythicMob = false;
            else
                try {
                    mythicMob = Boolean.valueOf(mythicMobString);
                } catch (Exception exception) {
                    Logger.warn("Invalid value for mythic mob in arena wave: " + waveString);
                    continue;
                }
            int level = -1;
            if (!levelString.isEmpty()) try {
                level = Integer.valueOf(levelString);
            } catch (Exception exception) {
                Logger.warn("Invalid value for level in: " + levelString);
                continue;
            }
            ArenaEntity arenaEntity = new ArenaEntity(spawnpointString, wave, boss);
            if (mythicMob) arenaEntity.setMythicMob(true);
            if (level != -1) arenaEntity.setLevel(level);
            if (arenaEntities.get(wave) == null)
                arenaEntities.put(wave, new ArrayList<>(List.of(arenaEntity)));
            else
                arenaEntities.get(wave).add(arenaEntity);
        }
    }

    public List<ArenaEntity> getWaveEntities(int wave) {
        return arenaEntities.get(wave);
    }
}
