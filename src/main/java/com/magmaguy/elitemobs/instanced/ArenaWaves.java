package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.utils.WarningMessage;

import java.util.*;

public class ArenaWaves {

    private HashMap<Integer, List<ArenaEntity>> arenaEntities = new HashMap();

    //wave=X:spawnPoint=Y:boss=Z.yml
    public ArenaWaves(List<String> rawBosses) {
        for (String iteratedBoss : rawBosses) {
            String[] subString = iteratedBoss.split(":");
            String waveString = "";
            String spawnpointString = "";
            String boss = "";
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
                }
            }
            int wave;
            try {
                wave = Integer.valueOf(waveString);
            } catch (Exception exception) {
                new WarningMessage("Invalid value for wave in arena wave: " + waveString);
                continue;
            }
            ArenaEntity arenaEntity = new ArenaEntity(spawnpointString, wave, boss);
            if (arenaEntities.get(wave) == null)
                arenaEntities.put(wave, new ArrayList<>(Arrays.asList(arenaEntity)));
            else
                arenaEntities.get(wave).add(arenaEntity);
        }
    }

    public List<ArenaEntity> getWaveEntities(int wave) {
        return arenaEntities.get(wave);
    }
}
