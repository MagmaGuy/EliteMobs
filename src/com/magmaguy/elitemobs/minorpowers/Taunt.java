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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class Taunt extends MinorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.TAUNT_MD;

    private static Random random = new Random();

    private final static List<String> TARGET_TAUNT_LIST = new ArrayList<>(Arrays.asList(
            "OI! Get over here!",
            "What's that I see? A coward?",
            "Sir, prepare your fisticuffs!",
            "Let's settle this like cavemen!",
            "En garde!",
            "Look out! A creeper, behind you!",
            "Aren't you a right ugly mug!",
            "You look delicious.",
            "Prepare for trouble!",
            "Let the best click spammer win!",
            "SPARTAAAAAA",
            "I am actually level 9001",
            "Not an Elite Mob",
            "Poorly disguised Elite Mob",
            "Ah, fresh prey!",
            "The hunter becomes the hunted!",
            "Killing me crashes the server",
            "I'm actually a disguised player!",
            "I'm not going to drop any loot!",
            "Why can't we be friends?",
            "Sssssss",
            "This is my last day on the job!",
            "Stop right there criminal scum!",
            "Prepare to die!",
            "Praise the Sun!",
            "Git gud",
            "Alea jacta est!",
            "Facta, non verba!",
            "Skulls for the skull throne!",
            "Blood for the blood throne!",
            "A sacrifice for Herobrine!",
            "I shall pierce the heavens!",
            "Just who do you think I am!?",
            "A weakling! He's mine.",
            "Dibs on the weakling!",
            "Accio player!",
            "A duel to the death!",
            "I challenge you to a duel!",
            "Pika pika!",
            "Did you just call me an infernal",
            "What did you just call me?",
            "I know what you did you monster!",
            "This is how your story ends!",
            "Prepare for trouble!",
            "I've been preparing for this!",
            "I am prepared. Are you?",
            "A worthy foe!",
            "A worthy opponent!",
            "A newbie! It's my lucky day!",
            "Your stuff or your life!",
            "This is a robbery!",
            "It's you! You monster!",
            "So hungry, give me your flesh!",
            "There he is!",
            "Must've been my imagination",
            "Hi I'm Mr Meeseeks!",
            "I'm Mr Meeseeks, look at me!",
            "Your diamonds or your life!",
            "For king and country!",
            "Hit me with your best shot!",
            "Witness me!",
            "Witness me blood bag!",
            "I go all shiny and chrome!"
    ));

    private final static List<String> GENERIC_DAMAGED_LIST = new ArrayList<>(Arrays.asList(
            "Ow!",
            "Oi!",
            "Stop that!",
            "Why?!",
            "You fight like a Dairy Farmer!",
            "You are a pain in the backside!",
            "En garde! Touche!",
            "Hacks!",
            "My health is bugged, unkillable!",
            "Your hits only make me stronger!",
            "Sticks and stones...",
            "'tis but a flesh wound!",
            "Emperor protects!",
            "Herobrine, aid me!",
            "Behind you! A creeper!",
            "I have made a terrible mistake.",
            "What doesn't kill me...",
            "Zombies dig scars",
            "I shall show you my true power!",
            "Prepare for my ultimate attack!",
            "My reinforcements are arriving!",
            "You're going to pay for that!",
            "That's going to leave a mark!",
            "An eye for an eye!",
            "Health insurance will cover that",
            "I felt that one in my bones",
            "'tis but a scratch",
            "What was that? A soft breeze?",
            "You'll never defeat me like that",
            "Pathetic.",
            "Weak.",
            "That didn't even dent my armor!",
            "This is going to be an easy one",
            "My grandchildren will feel that",
            "Are you even trying?",
            "An admin just fully healed me",
            "Haxxor!",
            "Me? Damaged? Hacks!",
            "I can't be defeated!",
            "Good thing I'm using hacks!",
            "Watch out! Herobrine behind you!",
            "My life for Aiur!",
            "Your home is getting griefed!",
            "Why can't we be friends?",
            "Valhalla!",
            "Notch save me!",
            "No retreat!",
            "Hit me with your best shot!"

    ));

    private final static List<String> DAMAGED_BY_BOW_LIST = new ArrayList<>(Arrays.asList(
            "Fight me like a Player!",
            "Afraid to come up-close?",
            "I can smell your fear from here!",
            "Did you forget your sword?",
            "Coward! Bow are no fair!",
            "Bows? That's so 2011...",
            "Bows are for the weak of mind!",
            "Bows are for the weak of spirit!",
            "Bows are for the weak of heart!",
            "I thought we agreed no bows!",
            "Bows? You'll regret that",

            //regular hits
            "Ow!",
            "Oi!",
            "Stop that!",
            "Why?!",
            "You fight like a Dairy Farmer!",
            "You are a pain in the backside!",
            "En garde! Touche!",
            "Hacks!",
            "My health is bugged, unkillable!",
            "Your hits only make me stronger!",
            "Sticks and stones...",
            "'tis but a flesh wound!",
            "Emperor protects!",
            "Herobrine, aid me!",
            "Behind you! A creeper!",
            "I have made a terrible mistake.",
            "What doesn't kill me...",
            "Zombies dig scars",
            "I shall show you my true power!",
            "Prepare for my ultimate attack!",
            "My reinforcements are arriving!",
            "You're going to pay for that!",
            "That's going to leave a mark!",
            "An eye for an eye!",
            "Health insurance will cover that",
            "I felt that one in my bones",
            "'tis but a scratch",
            "What was that? A soft breeze?",
            "You'll never defeat me like that",
            "Pathetic.",
            "Weak.",
            "That didn't even dent my armor!",
            "This is going to be an easy one",
            "My grandchildren will feel that",
            "Are you even trying?",
            "An admin just fully healed me",
            "Haxxor!",
            "Me? Damaged? Hacks!",
            "I can't be defeated!",
            "Good thing I'm using hacks!",
            "Watch out! Herobrine behind you!",
            "My life for Aiur!",
            "Your home is getting griefed!",
            "Why can't we be friends?",
            "Valhalla!",
            "Notch save me!",
            "No retreat!",
            "Hit me with your best shot!"
    ));

    private final static List<String> HIT_LIST = new ArrayList<>(Arrays.asList(
            "A solid hit!",
            "He shoots, and he scores!",
            "You'll feel that in the morning!",
            "Victory approaches!",
            "Victory shall be mine!",
            "That was only half of my power!",
            "You came to the wrong hood!",
            "You messed with the wrong mob!",
            "Get ready to get mobbed on!",
            "This was your last mistake!",
            "Feel the burn!",
            "John Cena",
            "How are you still standing?",
            "Just give up already!",
            "Give up!",
            "I have the high ground!",
            "I will end you!",
            "Hope you came prepared for pain!",
            "The hunter becomes the hunted!",
            "Blood for the blood throne!",
            "Skulls for the skull throne!",
            "A good player is a dead player!",
            "Git gud!",
            "Praise the Sun!",
            "The end draws near!",
            "You are not prepared.",
            "Zug zug!",
            "Hardly worth my time!",
            "I thought this was a challenge?",
            "Pro tip: don't get hit",
            "Ima firin mah lazer bwooooooooo",
            "For the Emperor!",
            "Herobrine shall be pleased!",
            "This is the end for you!",
            "Are you even trying?",
            "Weakling!",
            "Pathetic.",
            "Maybe this will wake you up.",
            "This is just the beggining!",
            "We're just getting started!",
            "That was just a warm-up!",
            "You are no match for me!",
            "RKO out of nowhere!",
            "I can do this all night long!",
            "A fine punching bag!",
            "I am the night!",
            "I wonder what will break first",
            "Revenge for Steve! You monster!",
            "Join the mob side!",
            "Too easy, too easy!",
            "Too easy!",
            "You can't defeat me!",
            "Pikachuuuuuu",
            "Avada kedavra!",
            "Crucio!",
            "Jierda!",
            "A critical hit!",
            "This is what skills looks like!",
            "9001 damage!",
            "Still standing?",
            "Face your defeat!",
            "A taste of pain to come!",
            "I'll make you endangered!"
    ));

    private final static List<String> DEATH_LIST = new ArrayList<>(Arrays.asList(
            "Alas, poor Yorick!",
            "The rest is silence",
            "I shall return",
            "Unforgivable",
            "I live, I die, I live again!",
            "WITNESS ME",
            "I ride to valhalla!",
            "VALHALLA!",
            "VALHALLA! DELIVERANCE",
            "You win, this time",
            "A blight upon your land!",
            "A pox upon thee!",
            "You'll join me soon enough!",
            "This is not over",
            "Death is but a setback",
            "Not like this...",
            "I just wanted to go into space",
            "We could've been friends",
            "Your sins crawling on your back",
            "See you in a bit",
            "See you later",
            "I'll be waiting for you",
            "I will be avenged!",
            "I won't go gently into the night",
            "Should've gotten life insurance",
            "It was my last day on the job",
            "It was my last day here",
            "Et tu, Brute?",
            "RIP",
            "x.x",
            "(x.x)",
            "xox",
            "Fainted",
            "GG, WP",
            "GG",
            "WP",
            "GG no RE",
            "HAX",
            "Hacker",
            "Not fair I was lagging",
            "You win, this time",
            "You monster...",
            "Mediocre..."
    ));

    private int processID;

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

                nametagProcessor(targetter, TARGET_TAUNT_LIST);

            }

        }

    }

    @EventHandler
    public void onDamaged (EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity) || ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() <= 0 || !event.getEntity().isValid()) {

            return;

        }

        if (event.getEntity().hasMetadata(powerMetadata)) {

            Entity entity = event.getEntity();

            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {

                nametagProcessor(entity, DAMAGED_BY_BOW_LIST);

            } else {

                nametagProcessor(entity, GENERIC_DAMAGED_LIST);

            }

        }

    }

    @EventHandler
    public void onHit (EntityDamageByEntityEvent event) {


            if (event.getDamager().hasMetadata(powerMetadata) || event.getDamager() instanceof Projectile &&
                    ProjectileMetadataDetector.projectileMetadataDetector((Projectile) event.getDamager(), powerMetadata)) {

                if (event.getDamager().hasMetadata(powerMetadata)) {

                    nametagProcessor(event.getDamager(), HIT_LIST);

                } else if (event.getDamager() instanceof Projectile &&
                        ProjectileMetadataDetector.projectileMetadataDetector((Projectile) event.getDamager(), powerMetadata)) {

                    Entity entity = (Entity) ((Projectile) event.getDamager()).getShooter();

                    nametagProcessor(entity, HIT_LIST);

                }

            }


    }

    @EventHandler
    public void onDeath (EntityDeathEvent event){

        if (event.getEntity().hasMetadata(powerMetadata)){

            Entity entity = event.getEntity();

            nametagProcessor(entity, DEATH_LIST);

        }

    }

    private void nametagProcessor (Entity entity, List<String> list){

        if (entity.hasMetadata(MetadataHandler.TAUNT_NAME)) {

            return;

        }

        entity.setMetadata(MetadataHandler.TAUNT_NAME, new FixedMetadataValue(plugin, true));

        String originalName = entity.getCustomName();

        int randomizedKey = random.nextInt(list.size());
        String tempName = list.get(randomizedKey);

        entity.setCustomName(tempName);

        processID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {

                entity.setCustomName(originalName);
                entity.removeMetadata(MetadataHandler.TAUNT_NAME, plugin);

            }

        }, 4 * 20);

    }

}
