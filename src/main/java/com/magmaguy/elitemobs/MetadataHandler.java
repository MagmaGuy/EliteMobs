package com.magmaguy.elitemobs;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by MagmaGuy on 26/04/2017.
 */
public class MetadataHandler implements Listener {

    /*
    EliteMobs has ceased using metadata as its primary get/set way of cheating associating data to mobs
    The remaining metadata are here purely for third party compatibility.
    EntityTracker and EliteMobEntity expose pretty much everything you could want API-wise
     */

    //plugin name
    public final static String ELITE_MOBS = "EliteMobs";
    //third party compatibility
    public final static String BETTERDROPS_COMPATIBILITY_MD = "betterdrops_ignore";
    //plugin getter
    public static JavaPlugin PLUGIN;

    public static int signatureID = 31173;

}
