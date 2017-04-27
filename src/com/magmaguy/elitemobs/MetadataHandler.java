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

import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MagmaGuy on 26/04/2017.
 */
public class MetadataHandler {

    //if you're wondering why I thought this was needed, it's because I got tired of finding and replacing
    //individual metadata strings and stuff breaking due to it

    public final String eliteMobMD = "EliteMob";
    public final String passiveEliteMobMD = "PassiveEliteMob";
    public final String naturalMob = "NaturalMob";
    public final String minorPowerAmountMD = "MinorPowerAmount";
    public final String visualEffect = "VisualEffect";
    public final String forbidden = "Forbidden";
    //Powers
    public final String attackGravityMD = "AttackGravity";
    public final String attackPoisonMD = "AttackPoison";
    public final String attackPushMD = "AttackPush";
    public final String attackWitherMD = "AttackWither";
    public final String invulnerabilityArrowMD = "InvulnerabilityArrow";
    public final String invulnerabilityFallDamageMD = "InvulnerabilityFallDamage";
    public final String invulnerabilityFireMD = "InvulnerabilityFire";
    public final String movementSpeedMD = "MovementSpeed";
    private EliteMobs plugin;

    public MetadataHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    public List<String> metadataList() {

        List<String> metadataList = new ArrayList<>();

        metadataList.add(eliteMobMD);
        metadataList.add(passiveEliteMobMD);
        metadataList.add(naturalMob);
        metadataList.add(minorPowerAmountMD);
        metadataList.add(visualEffect);
        metadataList.add(forbidden);
        metadataList.add(attackGravityMD);
        metadataList.add(attackPoisonMD);
        metadataList.add(attackPushMD);
        metadataList.add(attackWitherMD);
        metadataList.add(invulnerabilityArrowMD);
        metadataList.add(invulnerabilityFallDamageMD);
        metadataList.add(invulnerabilityFireMD);
        metadataList.add(movementSpeedMD);

        return metadataList;

    }

    public void flushMetadata(Entity entity) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        for (String string : metadataList()) {

            if (entity.hasMetadata(string)) {

                if (!(entity instanceof IronGolem) && entity.hasMetadata(metadataHandler.visualEffect)) {

                    entity.remove();

                }

                entity.removeMetadata(string, plugin);

            }

            if (!(entity instanceof IronGolem) && ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity,
                    plugin.getConfig().getList("Valid aggressive EliteMobs"))) {

                entity.remove();

            }

        }

    }

}
