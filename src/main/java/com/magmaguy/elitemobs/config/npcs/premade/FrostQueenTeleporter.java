package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class FrostQueenTeleporter extends NPCsConfigFields {
    public FrostQueenTeleporter() {
        super("frost_queen_teleporter",
                true,
                "Ishard the Frostbitten",
                "<Frost Palace Teleport>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,294.5,91,215.5,0,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp frost_palace_sanctum.yml");
        setDisguise("custom:elitemobs_frost_queen_teleporter");
        setCustomDisguiseData("player elitemobs_frost_queen_teleporter setskin {\"id\":\"3aeab183-452a-44b7-9734-306a8930e199\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY2MzgwOTYxMjUxMCwKICAicHJvZmlsZUlkIiA6ICJmNThkZWJkNTlmNTA0MjIyOGY2MDIyMjExZDRjMTQwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ1bnZlbnRpdmV0YWxlbnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI3YjY2MjcwZmY5MTIyMWU4NjhjZTg2MDUwMTkxYTk1NzdkMjE1Nzc4YzY2MmI2NjliYjFmYjAzM2UyYTkyYiIKICAgIH0KICB9Cn0=\",\"signature\":\"EnaEhcygFbcO/ABDSPvou1lT2DIhENO6CiB5ZODGmU/h2Tgf54CCskFwwRYKIe4UR1Qmam+1MyxdS65UzkszhsdswyKyRHJtpQ2xO9/jarAt/Q/gVlaEOSExt4CvPicl4QZPyD1auBOVFS5ZGwfFfCp/fqGF71hRESRnJTrGdTx1ITeuyI27MQGyzPT3GyHvJkJQCXYtrJHdaeiS71m1jVxeDz/uDksI973J/HPFt5ywgf0vwOqed3rWnYV6Oc3wfMuv3RAqlMufu89MVfj47nCZ9E/wzp7CirWz5/JYTXoAz/7+ihkYxdCH0NOvPWo6ytcwI04S46SG9h6E4HW8RKUdb9qIoNekrQccMEoeiSq30LmN5fLIQE7O+4KE+tZiHgca/xxXoeNDoEp3JoBm8NInyHz54uOptNa5eOIrF+aNR10JbpzmeUEd6nxhXBi03+Ti7FJgvKyyh4+MZxkr9ceLj+K/EGnjyAUyU7gceligbfpqU6RcsniCxfl9YzLa+nZBy89790AR6jmccc+rl8zlV0xBEf/Vb4ik8aCTnBevd8QV0YAIUeZ3TRAhqv3FrbrHdo6wetj4HHTkIoVH7ej4KMAvmgoh4TyiCHHQIjhtz4iEqPQUNejje+eLw00Rp7YeeePeHLmk/v+uL2HbyfECwQKkML9nbB9Ts3aka/A=\"}],\"legacy\":false}");
    }
}
