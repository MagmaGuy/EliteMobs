package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SantaConfig extends NPCsConfigFields {
    public SantaConfig() {
        super("santa.yml",
                false,
                "&4Saint Nick",
                "&c<Santa!>",
                Villager.Profession.NITWIT,
                "em_adventurers_guild,212.5,88,236.5,104,0",
                List.of(
                        "Ho ho ho!"),
                new ArrayList<>(List.of(
                        "I have lost some presents, \\ncan you help me?",
                        "Dear traveller, I have\\na request for you!",
                        "Can you help an old man\\nin his time of need?")),
                List.of(
                        "Ho ho ho!"),
                true,
                5,
                NPCInteractions.NPCInteractionType.CUSTOM_QUEST_GIVER);
        super.setQuestFilenames(Collections.singletonList("xmas_quest_0.yml"));
        super.setDisguise("custom:elitemobs_saint_nick_skin");
        super.setCustomDisguiseData("player elitemobs_saint_nick_skin setskin " +
                "{\"id\":\"017f3d21-319d-48ab-b6b0-1084ca56c078\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"" +
                "textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY0MDAzNzk0Nzg2MywKICAicHJvZmlsZUlkIiA6ICJjNjEwOTExMDh" +
                "lOTQ0MWRhODQyZDA5MDVmMDAyOWVhOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZVlvbm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOi" +
                "B0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZ" +
                "nQubmV0L3RleHR1cmUvYThjNWMwZThhZmZkNTgyY2FiNjdhMGI1OGMyMDVkZjNjMGJhYTUwMzIxMDI3ZjZiY2FmYWQ2YjNjNDM4ZDMx" +
                "NiIKICAgIH0KICB9Cn0=\",\"signature\":\"TvI5R8ftaZKTBAl5t6ON2aWbApJ5tQKN8caWuy3f/NJ0MFSk6PRiFzEfFIMHxLICJ" +
                "D5sAQm8NhSGHCWMecLQtPJSoK9u3SSjSxr6wIHCD+AYFNeWAGRZP1swYq8E87nGslChlVB/1++MIpWAudM3HI5qxO3lzs9hDgLXRIEr" +
                "MTolyqltAJXCyJbuho60+z8qRZaVx6NKiIB9bGwYYsDzAraPgn/bXd15nHW0hkal6adXBp45nRcGuD2iUp1X9tC1OBtTcAsjkhZjgki" +
                "sw9b5gR0TFQpdvzezJbJCHKMIb0xAkRCIMaxnt2VSZ8eXGzg6wpTV8PXCRRNN0TXe/Bhodh/T8R7IUts6rz+6aYUCco4dCMDbfqWIFz" +
                "NQ6JfFUPPXM5WhzmU0e/T05NYEKcP8dKAywC1OUPOT2MUNkS0IIXcQdwcdPxT2/YgWGUKKwaDcTcMqBVwbDhAvEl8xG8N8Wsm54YYy0" +
                "McrnoogQZQ7VOMVZJDfeZmbA45fkHp5kSCJ5g9txgR4mRMmVz9mTxzyYUtTTrloey4EqOxJb8Rg/3S6QXGwIdMY+V7yptXAijm0T8qU" +
                "VIWWcNlo7GeeSQ3VUI4L2EOk0xbiQMWMWDFwMcbKBrd6OHuyRbCe6CIZFLGjQDAM1xo/I4reRaagneM57rUTg/4xgGHWRhX1oFmZxGE=" +
                "\"}],\"legacy\":false}");
    }
}
