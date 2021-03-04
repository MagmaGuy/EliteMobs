package com.magmaguy.elitemobs;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

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

    //plugin getter
    public static Plugin PLUGIN = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    //third party compatibility
    public final static String BETTERDROPS_COMPATIBILITY_MD = "betterdrops_ignore";
    public final static String VANISH_NO_PACKET = "vanished";

    //Plugin values
    //This value stores the level as an int. Any mob with this metadata is an Elite Mob
    public final static String ELITE_MOB_METADATA = "Elitemob";
    //This value only stores if the passive mob is a Super Mob. The stored value is always true
    public final static String SUPER_MOB_METADATA = "Supermob";
    //Just tags an entity as an EliteMobs NPC
    public final static String NPC_METADATA = "NPC";
    //Tags an entity as an armor stand
    public final static String ARMOR_STAND = "ArmorStand";
    //Tags an entity as a visual effect
    public final static String VISUAL_EFFECT = "VisualEffect";


}
