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

package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobPowersConfig;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.utils.EntityFinder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackFreeze extends MinorPower {

    private ArrayList<LivingEntity> cooldownList = new ArrayList<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void attackFreeze(EntityDamageByEntityEvent event) {

        if (!EventValidator.eventIsValid(this, event)) return;
        Player player = EntityFinder.findPlayer(event);
        LivingEntity eliteMob = EntityFinder.getRealDamager(event);
        if (PowerCooldown.cooldownChecker(eliteMob, cooldownList)) return;

        /*
        Slow player down
         */
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 10));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(
                        ChatColorConverter.convert(
                                ConfigValues.mobPowerConfig.getString(
                                        MobPowersConfig.FROZEN_MESSAGE))));

        PowerCooldown.startCooldownTimer(eliteMob, cooldownList, 20 * 15);

        /*
        Add block effect
         */
        Block block = player.getLocation().getBlock();
        Material originalMaterial = block.getType();

        if (!originalMaterial.equals(Material.AIR))
            return;

        block.setType(Material.PACKED_ICE);

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(originalMaterial);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

    }

}
