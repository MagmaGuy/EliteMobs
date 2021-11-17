package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.ServerTime;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BossTrace {
    private final long creationTimeStamp;
    private final double creationTicksStamp;
    //Welcome to my garden of insanity. This is necessary because Minecraft just refuses to cooperate
    private List<String> trace = new ArrayList<>();

    public BossTrace() {
        creationTimeStamp = System.currentTimeMillis();
        creationTicksStamp = ServerTime.getTime();
        String creationString = ChatColor.DARK_GREEN + "Creation timestamp: " + creationTimeStamp;
        trace.add(creationString);
    }


    public void spawnPreprocessor(int version) {
        if (!DebugMessage.isDebugMode()) return;
        String spawnString = ChatColor.YELLOW + getPrefix() + "Spawn preprocessor " + version + "/2";
        trace.add(spawnString);
    }

    public void setSpawn() {
        if (!DebugMessage.isDebugMode()) return;
        String spawnString = ChatColor.GREEN + getPrefix() + "Spawn!";
        trace.add(spawnString);
    }

    public void setRemove(RemovalReason removalReason) {
        if (!DebugMessage.isDebugMode()) return;
        String removeString = ChatColor.RED + getPrefix() + "Remove! Reason: " + removalReason;
        trace.add(removeString);
    }

    private String getPrefix() {
        return "[" + (ServerTime.getTime() - creationTicksStamp) + "t] ";
    }

    private long getTime() {
        return System.currentTimeMillis() - creationTimeStamp;
    }

    public void postLog(Player player) {
        if (!DebugMessage.isDebugMode())
            player.sendMessage(ChatColor.RED + "[EliteMobs] Debug mode must be on for this to work! Do " + ChatColor.GREEN + "/em debugmode");
        player.sendMessage(ChatColor.GREEN + "[EliteMobs] Here is the full activity log for this boss. A copy is also logged into console!");
        new WarningMessage("Sequential code execution list for boss:");
        for (String string : trace) {
            player.sendMessage(string);
            new WarningMessage(string);
        }
    }

}
