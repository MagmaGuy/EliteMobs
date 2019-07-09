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

package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossSpecialAttackDamage {

    public static boolean dealSpecialDamage(LivingEntity damager, LivingEntity damagee, double damage) {

        if (damagee.isInvulnerable() || damagee.getHealth() <= 0) return false;

        if (damagee instanceof Player) {

            if (!(((Player) damagee).getGameMode().equals(GameMode.SURVIVAL) ||
                    ((Player) damagee).getGameMode().equals(GameMode.ADVENTURE))) return false;

        }

        damagee.damage(damage);
        damagee.setNoDamageTicks(0);

        if (damagee instanceof Player && damagee.getHealth() <= 0)
            PlayerDeathMessageByEliteMob.initializeDeathMessage((Player) damagee, damager);

        return true;

    }

}
