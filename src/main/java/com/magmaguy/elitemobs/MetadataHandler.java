/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    public final static Plugin PLUGIN = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    //third party compatibility
    public final static String BETTERDROPS_COMPATIBILITY_MD = "betterdrops_ignore";
    public final static String VANISH_NO_PACKET = "vanished";


}
