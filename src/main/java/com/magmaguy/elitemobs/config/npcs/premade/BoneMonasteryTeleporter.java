package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class BoneMonasteryTeleporter extends NPCsConfigFields {
    public BoneMonasteryTeleporter() {
        super("bone_monastery_teleporter",
                true,
                "<g:#C8B8A0:#D8C8B0>Sister Frieda</g>",
                "<g:#B8A890:#C8B8A0><Bone Monastery></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,295.5,92,301.5,90,0",
                List.of(""),
                List.of(),
                List.of(),
                true,
                1,
                NPCInteractions.NPCInteractionType.COMMAND);
        setCommand("em dungeontp bone_monastery_dungeon.yml");
        setCustomModel("em_ag_sisterfrieda");
        setSyncMovement(false);
        setDisguise("custom:elitemobs_bone_monastery_teleporter");
        setCustomDisguiseData("player elitemobs_bone_monastery_teleporter setskin {\"id\":\"a30519df-b8b1-42a8-a537-64d5ff8b5651\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY0NzgwMjAxMzExNCwKICAicHJvZmlsZUlkIiA6ICJhYjlkYmMzZjk4NGE0ZWI4YTVmY2RlYWMzNzEzZWFkMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDeWJvcm51dDIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcwZjM0OThhYjRlMDhlMjIyYzI4OTBlYmMyMTU2ZjhhYjY1NmIwM2U3OGVhM2ZhMmViNmE0Y2ZjM2NiZjE1NSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9\",\"signature\":\"KP+QjsBszTPBIGI/RU50q2aF9DhFFzLxqsYwxBlL8t+idG1AKYd4nt8LrDs4C8fKVmT+fHmCCJSxpakmZgQ1aHgPLN67A8Q614K3SR67vaseNMhVnmEmZ0clBFRHxjnVR134/ozeXFSGd2d8ttbZPzO8GxXWMa6ezbrWEPEsxl6n4l/Mj6v2zZQWkHBxhmgqxWh+z/708JBop0H5TDha+y2gikaRdRhurXXq67Ud/oghcMK6mTO5ET0m8/8TNz0uBF42rcMbT2Ew6t/h2SuW0ULvd3cb/gB4zUMjAXmhrtb0kHoC+gAPPv715TWG7LBnuOkAbl9SLCvMeSkSKUsKWTswb07DojrEl+kt8LZToebFlR37esNqSubPvFx7LJJZ+wBspTv+c9PNdfVSUMkzh02jKdgWEDFQv6WG26OZw+f9PPOztYRzmYEajezl0Re8G66aIa3G3eynWa+JMX3Ml9Jpym+P1wBkIo1nWlazJkumkPG6sVLqMOkvrCQlDedWoQacCDRVsUVnzBHrHkEoin5y+USkcxWwU4yhaPi7mX3SSG0mRiI3LpBurcCiJcQzlGszml2w5fJBLAeEUyO9UtUMxP/mS477FmbGeLvmP7+AcnNe2COe+/G8mdfQ+pXlgjnWDzhsdVktY2kJ5QJZZ9cD5Ru7BOqU2Uqn6Y1PkMA=\"}],\"legacy\":false}");
    }
}
