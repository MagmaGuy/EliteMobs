package com.magmaguy.elitemobs.config.customarenas.premade;

import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;

import java.util.ArrayList;
import java.util.Arrays;

public class WoodLeagueArena extends CustomArenasConfigFields {
    public WoodLeagueArena() {
        super("wood_league", true);
        super.setArenaName("Wood League");
        super.setCorner1("em_adventurers_guild,257,69,333,0,0");
        super.setCorner2("em_adventurers_guild,181,91,257,0,0");
        super.setStartLocation("em_adventurers_guild,219.5,70,295.5,0,0");
        super.setExitLocation("em_adventurers_guild,245.5,92,274.5,0,0");
        super.setWaveCount(50);
        super.setBossList(Arrays.asList(
                "wave=1:spawnPoint=north:boss=wood_league_wave_1_melee.yml",
                "wave=1:spawnPoint=north:boss=wood_league_wave_1_melee.yml",
                "wave=1:spawnPoint=north:boss=wood_league_wave_1_ranged.yml",

                "wave=2:spawnPoint=north:boss=wood_league_wave_2_melee.yml",
                "wave=2:spawnPoint=north:boss=wood_league_wave_2_melee.yml",
                "wave=2:spawnPoint=north:boss=wood_league_wave_2_ranged.yml",
                "wave=2:spawnPoint=south:boss=wood_league_wave_2_melee.yml",
                "wave=2:spawnPoint=south:boss=wood_league_wave_2_melee.yml",
                "wave=2:spawnPoint=south:boss=wood_league_wave_2_ranged.yml",

                "wave=3:spawnPoint=north:boss=wood_league_wave_3_melee.yml",
                "wave=3:spawnPoint=north:boss=wood_league_wave_3_melee.yml",
                "wave=3:spawnPoint=north:boss=wood_league_wave_3_ranged.yml",
                "wave=3:spawnPoint=south:boss=wood_league_wave_3_melee.yml",
                "wave=3:spawnPoint=south:boss=wood_league_wave_3_melee.yml",
                "wave=3:spawnPoint=south:boss=wood_league_wave_3_ranged.yml",

                "wave=4:spawnPoint=north:boss=wood_league_wave_4_melee.yml",
                "wave=4:spawnPoint=north:boss=wood_league_wave_4_melee.yml",
                "wave=4:spawnPoint=north:boss=wood_league_wave_4_ranged.yml",
                "wave=4:spawnPoint=south:boss=wood_league_wave_4_melee.yml",
                "wave=4:spawnPoint=south:boss=wood_league_wave_4_melee.yml",
                "wave=4:spawnPoint=south:boss=wood_league_wave_4_ranged.yml",

                "wave=5:spawnPoint=center:boss=wood_league_wave_5_miniboss.yml",

                "wave=6:spawnPoint=north:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=north:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=north:boss=wood_league_wave_6_ranged.yml",
                "wave=6:spawnPoint=south:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=south:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=south:boss=wood_league_wave_6_ranged.yml",
                "wave=6:spawnPoint=east:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=east:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=east:boss=wood_league_wave_6_ranged.yml",
                "wave=6:spawnPoint=west:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=west:boss=wood_league_wave_6_melee.yml",
                "wave=6:spawnPoint=west:boss=wood_league_wave_6_ranged.yml",

                "wave=7:spawnPoint=north:boss=wood_league_wave_7_melee.yml",
                "wave=7:spawnPoint=north:boss=wood_league_wave_7_melee.yml",
                "wave=7:spawnPoint=north:boss=wood_league_wave_7_ranged.yml",
                "wave=7:spawnPoint=south:boss=wood_league_wave_7_melee.yml",
                "wave=7:spawnPoint=south:boss=wood_league_wave_7_melee.yml",
                "wave=7:spawnPoint=south:boss=wood_league_wave_7_ranged.yml",

                "wave=8:spawnPoint=north:boss=wood_league_wave_8_melee.yml",
                "wave=8:spawnPoint=north:boss=wood_league_wave_8_melee.yml",
                "wave=8:spawnPoint=north:boss=wood_league_wave_8_melee.yml",
                "wave=8:spawnPoint=south:boss=wood_league_wave_8_melee.yml",
                "wave=8:spawnPoint=south:boss=wood_league_wave_8_melee.yml",
                "wave=8:spawnPoint=south:boss=wood_league_wave_8_melee.yml",

                "wave=9:spawnPoint=north:boss=wood_league_wave_9_ranged.yml",
                "wave=9:spawnPoint=north:boss=wood_league_wave_9_ranged.yml",
                "wave=9:spawnPoint=north:boss=wood_league_wave_9_ranged.yml",
                "wave=9:spawnPoint=south:boss=wood_league_wave_9_ranged.yml",
                "wave=9:spawnPoint=south:boss=wood_league_wave_9_ranged.yml",
                "wave=9:spawnPoint=south:boss=wood_league_wave_9_ranged.yml",

                "wave=10:spawnPoint=center:boss=wood_league_wave_10_boss.yml"

        ));
        super.setSpawnPoints(Arrays.asList(
                "name=north:location=em_adventurers_guild,219.5,71,273.5",
                "name=south:location=em_adventurers_guild,219.5,71,316.5",
                "name=west:location=em_adventurers_guild,197.5,71,295.5",
                "name=east:location=em_adventurers_guild,240.5,71,295.5",
                "name=center:location=em_adventurers_guild,219.5,71,295.5"));

        super.setDelayBetweenWaves(5);
        super.setRawArenaRewards(Arrays.asList(
                "wave=10:level=10:filename=summon_merchant_scroll.yml"
        ));
        super.setArenaMessages(new ArrayList<>(Arrays.asList(
                "wave=1:message=&8[Gladius] &fToday's main event! A legendary fighter came all the way to the arena to test his might against the toughest monsters from all the land! But before that, here's a rookie fighting some zombies.",
                "wave=2:message=&8[Gladius] &fWould you look at that, they at least know how to wave their arms! That was just the warmup, let's start putting on some pressure!",
                "wave=3:message=&8[Gladius] &fSo you think you know how to fight? Well this wave will put the &cheat &fon! Ha ha ha ha ha!",
                "wave=4:message=&8[Gladius] &fAlright I guess they can take the heat! Ladies and gentlemen, strap on, we might be in for a show!",
                "wave=5:message=&8[Gladius] &fI found this &cweird creature &foutside, and I thought putting it in the arena would be funny!",
                "wave=6:message=&8[Gladius] &fSeems like you can handle small groups of enemies just fine, &clet's try scaling that up!",
                "wave=7:message=&8[Gladius] &fSo you think you can truly challenge the arena? We shall see...",
                "wave=8:message=&8[Gladius] &fNot bad, but what if every enemy is a &cmelee &fenemy?",
                "wave=9:message=&8[Gladius] &fNice, nice, but what if they're all &cranged &finstead?",
                "wave=10:message=&8[Gladius] &fAlright, enough messing around, &cnow we get serious!"
        )));
    }
}
