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

package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class PowerCooldown {

    /*
    This class just removes metadata after a variable delay, allows
     */
    public static void startCooldownTimer(EliteMobEntity eliteMob, HashSet<EliteMobEntity> cooldownList, int duration) {

        cooldownList.add(eliteMob);

        new BukkitRunnable() {

            @Override
            public void run() {

                cooldownList.remove(eliteMob);

            }

        }.runTaskLater(MetadataHandler.PLUGIN, duration);

    }

    /*
    This method checks if the cooldown is active
     */
    public static boolean isInCooldown(EliteMobEntity eliteMob, HashSet<EliteMobEntity> cooldownList) {
        return cooldownList.contains(eliteMob);
    }

}
