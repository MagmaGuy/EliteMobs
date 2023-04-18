package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class TheCaveTeleporter extends NPCsConfigFields {
    public TheCaveTeleporter(){
        super("the_cave_teleporter",
                true,
                "Miner Regulus",
                "<[10] The Cave Teleporter>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,303.5,78,204.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp the_cave_sanctum.yml");
        setDisguise("custom:elitemobs_the_cave_teleporter");
    setCustomDisguiseData("player elitemobs_the_cave_teleporter setskin {\"id\":\"acb830c3-4415-40b8-aec4-16f32c2f0424\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3NTg2MzQ2NDg5OCwKICAicHJvZmlsZUlkIiA6ICIzZmM3ZmRmOTM5NjM0YzQxOTExOTliYTNmN2NjM2ZlZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJZZWxlaGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I1MDg2NmE1ZjEzZTQxNmRhZGYxZDY2ZTNjOGJmOTY2NGI2MGE2N2RmNTBlZDM2ZWRiMWRiZjIxYjI1Mjc2NiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9\",\"signature\":\"SFgkH/mIf2EL0V4DVNkdt6DcJh85XcLmwiM0cixad7x+ZTQA3nynhQJkUzM/0x4NsYEbvSbRINRPcI3CxuPg6XNwkYTRBKBoGHO33e/py5GJbah6t/Cqn4HG8CHs3kmlSbaOBirXsbclRlKfsEtbpxgNpjYRROyfZUPnDiFjDCKF9j0mO5EFPufEGmErYoJCvgZh4Cu9WFvImrVXVdI9/CR0yWU2isBS+sot9iCK97d7C9GSZL4ThTpxzDVajhBPVwccA4WsIdtyxyzN6DHkIeOmoKvan9U2HtttEUcGxwfuMmZEk8zqQHVSgkKpvDC7ZtzEKULfDx5+5l3getpbLFoVBur6XOPT+GgSMplfbx250fT20yCC62hr/z2h2aV2v9gHvFx18cPphqHaCcuSUpOPgdXoRA34xsarv6xZfZYkQ3h3vh9q6+Vii/hCHzcfJ9AZ2CVC8de2J0RGuQ3Q/F6FOzp6ZchuxqDpToQHjEWO8Ql3U+XvMYYdEUqHKQHFgucnk/z0hGadjQxvLQqqqILi2BGrvvXScupBpFlx+D4bRIXpX5NAw57hBAAO9bZqy6BST0/LS7LaAScl/ijgqcsczYi6TIe3ouWpDkAdF82Rt2geseZ1vBOAFTJY/Em6iG2H4YXDmH1qW/NyFeD8jzdC8Y+oM0V0wcwYxcEwY7Y=\"}],\"legacy\":false}");
    }
}
