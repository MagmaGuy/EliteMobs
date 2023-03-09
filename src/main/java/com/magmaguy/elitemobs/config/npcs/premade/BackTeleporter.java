package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.List;

public class BackTeleporter extends NPCsConfigFields {
    public BackTeleporter() {
        super("back_teleporter",
                true,
                "Hermes",
                "<Transporter>",
                Villager.Profession.FLETCHER,
                "em_adventurers_guild,213.5,88,232,0,0",
                List.of("Welcome to the\\nAdventurer's Guild Hub!"),
                List.of("Need to go back?"),
                List.of("Safe travels, friend."),
                true,
                3,
                NPCInteractions.NPCInteractionType.TELEPORT_BACK);
        setNoPreviousLocationMessage("&8[EliteMobs] &cCouldn't send you back to your previous location - no previous location found!");
        setDisguise("custom:ag_back_teleporter");
        setCustomDisguiseData("player ag_back_teleporter setskin {\"id\":\"a946c147-4fc4-4ade-b06f-cae6462bf3e1\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY3MzU1ODQyMTE5OSwKICAicHJvZmlsZUlkIiA6ICI2MDJmMjA0M2YzYjU0OGU1ODQyYjE4ZjljMDg2Y2U0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb3J5c18iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjdmNjg1NjhmYzlhYzA2YWIwMjk4ODY2OTkyNmEwYjUwNmRlNjc3YTAyZTJhYWIzYWM4ZGM5ZWMyMTVjMDQ0YiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9\",\"signature\":\"gwhAsHKA2jq8AY7MKZtsEkCqrxH6wR1SqDH85QOPdJW9AP5ZVLUg0G+hd+VDF/9lL1tIn/o3WuY6T9mE6oNrUtSvGCtEwusKHgbg+K+OGacWGWYQFPTGQoekYpZ0sfPXfK69gh7L+C1t6GvCdswow7l+O9eebPQcIMFuwixThVcMeeegGFibkGCl3lYHvhSXrcl9xEHTgOym/tsVxt9+gZCQvh8L6U5MBViClYDktBVgz68Ny1FBw1EA6LNUBK63t4kBQEpRCTsEJrfN9k5CS2AApVrjqCcor8oai2K5crZao0XM6qVmjxNb4Y1WZ9svFCIfgrJW2mXPc2lL5ctODfza/TGMRnCy+6yGKPPp5SHTbSLmyZ7e+q6UiwcSqlOHQibmrAK81Z3hiEEyA96NBZ3hNny2TqEk6ok2uFmIZcqgaBtnFEL9YeO2zob/pCf3iOV1uvQw0nDTeuqJC9VXlkIVkJ2+t8FBqV7ZjNH0L1djxW0NMcjEfFQkvMp9SP3t0aPpbkmSWfWFpiItS6pNxxviLFF8ogueFbYZMqA/1BXiBKY+EqnOi8TAI25GotgMjDCtdOeUbWPVc/hDb4jgFncGaTBnNAgFcRgL3RIqtpO8kUaNSHBuXjAZl+WfRXUvbFznkcecHzoiMZoaCSu7nubtehVB+v1wLGnawALT1MQ=\"}],\"legacy\":false}");
    }
}
