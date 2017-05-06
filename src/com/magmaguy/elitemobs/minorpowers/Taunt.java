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

package com.magmaguy.elitemobs.minorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class Taunt extends MinorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.TAUNT_MD;

    int processID;

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void onTarget (EntityTargetEvent event) {

        if (event.getEntity().hasMetadata(powerMetadata)) {

            if (event.getTarget() instanceof Player) {

                Entity targetter = event.getEntity();

                List<String> targetTauntList = new ArrayList<>();

                targetTauntList.add("OI! Get over here!");
                targetTauntList.add("What's that I see? A coward?");
                targetTauntList.add("Sir, prepare your fisticuffs!");
                targetTauntList.add("Let's settle this like cavemen!");
                targetTauntList.add("En garde!");
                targetTauntList.add("Look out! A creeper, behind you!");
                targetTauntList.add("Aren't you a right ugly mug!");
                targetTauntList.add("You look delicious.");
                targetTauntList.add("Prepare for trouble!");
                targetTauntList.add("Let the best click spammer win!");
                targetTauntList.add("SPARTAAAAAA");
                targetTauntList.add("I am actually level 9001");
                targetTauntList.add("Not an Elite Mob");
                targetTauntList.add("Poorly disguised Elite Mob");
                targetTauntList.add("Ah, fresh prey!");
                targetTauntList.add("The hunter becomes the hunted!");
                targetTauntList.add("Killing me crashes the server");
                targetTauntList.add("I'm actually a disguised player!");
                targetTauntList.add("I'm not going to drop any loot!");
                targetTauntList.add("Why can't we be friends?");
                targetTauntList.add("This is my last day on the job!");
                targetTauntList.add("Stop right there criminal scum!");
                targetTauntList.add("Prepare to die!");
                targetTauntList.add("Praise the Sun!");
                targetTauntList.add("Git gud");
                targetTauntList.add("Alea jacta est!");
                targetTauntList.add("Facta, non verba!");
                targetTauntList.add("Skulls for the skull throne!");
                targetTauntList.add("Blood for the blood throne!");
                targetTauntList.add("A sacrifice for Herobrine!");

                nametagProcessor(targetter, targetTauntList);

            }

        }

    }

    @EventHandler
    public void onDamaged (EntityDamageEvent event) {

        if (((LivingEntity) event.getEntity()).getHealth() == 0) {

            return;

        }

        if (event.getEntity().hasMetadata(powerMetadata)) {

            Entity entity = event.getEntity();
            List<String> genericDamagedList = new ArrayList<>();

            genericDamagedList.add("Ow!");
            genericDamagedList.add("Oi!");
            genericDamagedList.add("Stop that!");
            genericDamagedList.add("Why?!");
            genericDamagedList.add("You fight like a Dairy Farmer!");
            genericDamagedList.add("You are a pain in the backside!");
            genericDamagedList.add("En garde! Touche!");
            genericDamagedList.add("Hacks!");
            genericDamagedList.add("My health is bugged, unkillable!");
            genericDamagedList.add("Your hits only make me stronger!");
            genericDamagedList.add("Sticks and stones...");
            genericDamagedList.add("'tis but a flesh wound!");
            genericDamagedList.add("Emperor protects!");
            genericDamagedList.add("Herobrine, aid me!");
            genericDamagedList.add("Behind you! A creeper!");
            genericDamagedList.add("I have made a terrible mistake.");
            genericDamagedList.add("What doesn't kill me...");
            genericDamagedList.add("Zombies dig scars");
            genericDamagedList.add("I shall show you my true power!");
            genericDamagedList.add("Prepare for my ultimate attack!");
            genericDamagedList.add("My reinforcements are arriving!");
            genericDamagedList.add("You're going to pay for that!");
            genericDamagedList.add("That's going to leave a mark!");
            genericDamagedList.add("An eye for an eye!");
            genericDamagedList.add("Health insurance will cover that");
            genericDamagedList.add("I felt that one in my bones");
            genericDamagedList.add("'tis but a scratch");
            genericDamagedList.add("What was that? A soft breeze?");
            genericDamagedList.add("You'll never defeat me like that");
            genericDamagedList.add("Pathetic.");
            genericDamagedList.add("Weak.");
            genericDamagedList.add("That didn't even dent my armor!");
            genericDamagedList.add("This is going to be an easy one");
            genericDamagedList.add("My grandchildren will feel that");
            genericDamagedList.add("Are you even trying?");
            genericDamagedList.add("An admin just fully healed me");
            genericDamagedList.add("Haxxor!");
            genericDamagedList.add("Me? Damaged? Hacks!");
            genericDamagedList.add("I can't be defeated!");
            genericDamagedList.add("Good thing I'm using hacks!");
            genericDamagedList.add("Watch out! Herobrine behind you!");
            genericDamagedList.add("My life for Aiur!");
            genericDamagedList.add("Your home is getting griefed!");
            genericDamagedList.add("Why can't we be friends?");
            genericDamagedList.add("Why must we fight?");
            genericDamagedList.add("Valhalla!");
            genericDamagedList.add("Notch save me!");
            genericDamagedList.add("No retreat!");

            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {

                List<String> damagedByBowList = new ArrayList<>();

                damagedByBowList.add("Fight me like a Player!");
                damagedByBowList.add("Afraid to come up-close?");
                damagedByBowList.add("I can smell your fear from here!");
                damagedByBowList.add("Did you forget your sword?");
                damagedByBowList.add("Coward! Bow are no fair!");
                damagedByBowList.add("Bows? That's so 2011...");
                damagedByBowList.add("Bows are for the weak of mind!");
                damagedByBowList.add("Bows are for the weak of spirit!");
                damagedByBowList.add("I thought we agreed no bows!");
                damagedByBowList.add("Bows? You're going to regret that");

                damagedByBowList.addAll(genericDamagedList);

                nametagProcessor(entity, damagedByBowList);

            } else {

                nametagProcessor(entity, genericDamagedList);

            }

        }

    }

    @EventHandler
    public void onHit (EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player) {

            if (event.getDamager().hasMetadata(powerMetadata) || event.getDamager() instanceof Projectile &&
                    ProjectileMetadataDetector.projectileMetadataDetector((Projectile) event.getDamager(), powerMetadata)) {

                List<String> hitList = new ArrayList<>();

                hitList.add("A solid hit!");
                hitList.add("He shoots, and he scores!");
                hitList.add("You'll feel that in the morning!");
                hitList.add("Victory approaches!");
                hitList.add("Victory shall be mine!");
                hitList.add("That was only half of my power!");
                hitList.add("You came to the wrong hood!");
                hitList.add("You messed with the wrong mob!");
                hitList.add("Get ready to get mobbed on!");
                hitList.add("This was your last mistake!");
                hitList.add("Feel the burn!");
                hitList.add("John Cena");
                hitList.add("How are you still standing?");
                hitList.add("I will end you!");
                hitList.add("Hope you came prepared for pain!");
                hitList.add("The hunter becomes the hunted!");
                hitList.add("Blood for the blood throne!");
                hitList.add("Skulls for the skull throne!");
                hitList.add("A good player is a dead player!");
                hitList.add("Git gud!");
                hitList.add("Praise the Sun!");
                hitList.add("The end draws near!");
                hitList.add("You are not prepared.");
                hitList.add("Zug zug!");
                hitList.add("Hardly worth my time!");
                hitList.add("I thought this was a challenge?");
                hitList.add("Pro tip: don't get hit");
                hitList.add("Ima firin mah lazer bwooooooooo");
                hitList.add("For the Emperor!");
                hitList.add("Herobrine shall be pleased!");
                hitList.add("This is the end for you!");
                hitList.add("Are you even trying?");
                hitList.add("Weakling!");
                hitList.add("Pathetic.");
                hitList.add("Maybe this will wake you up.");

                if (event.getDamager().hasMetadata(powerMetadata)) {

                    nametagProcessor(event.getDamager(), hitList);

                } else if (event.getDamager() instanceof Projectile &&
                        ProjectileMetadataDetector.projectileMetadataDetector((Projectile) event.getDamager(), powerMetadata)) {

                    Entity entity = (Entity) ((Projectile) event.getDamager()).getShooter();

                    nametagProcessor(entity, hitList);

                }

            }

        }

    }

    private void nametagProcessor (Entity entity, List<String> list){

        if (entity.hasMetadata(MetadataHandler.CUSTOM_NAME)) {

            return;

        }

        entity.setMetadata(MetadataHandler.CUSTOM_NAME, new FixedMetadataValue(plugin, true));

        String originalName = entity.getCustomName();

        Random random = new Random();
        int randomizedKey = random.nextInt(list.size());
        String tempName = list.get(randomizedKey);

        entity.setCustomName(tempName);

        processID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {

                entity.setCustomName(originalName);
                entity.removeMetadata(MetadataHandler.CUSTOM_NAME, plugin);

            }

        }, 4 * 20);

    }

}
