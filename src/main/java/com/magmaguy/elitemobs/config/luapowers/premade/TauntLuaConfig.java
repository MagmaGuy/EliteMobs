package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;

public class TauntLuaConfig extends LuaPowersConfigFields {

    private static final List<String> ON_DAMAGED = List.of(
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
            "Hit me with your best shot!");

    private static final List<String> ON_DAMAGED_BY_BOW = List.of(
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
            "Hit me with your best shot!");

    private static final List<String> ON_DAMAGE = List.of(
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
            "I'll make you endangered!");

    private static final List<String> ON_DEATH = List.of(
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
            "Mediocre...");

    private static final List<String> ON_TARGET = List.of(
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
            "I go all shiny and chrome!");

    public TauntLuaConfig() {
        super("taunt", Material.JUKEBOX.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
    }

    @Override
    public String getSource() {
        return """
                local target_taunt_list = %s
                local generic_damaged_list = %s
                local damaged_by_bow_list = %s
                local hit_list = %s
                local death_list = %s

                local function schedule_name_reset(context, entity, ticks)
                  context.scheduler:run_after(ticks, function()
                    if entity ~= nil and entity.is_alive ~= nil and entity:is_alive() then
                      entity:reset_custom_name()
                    end
                  end)
                end

                local function say_random_line(context, entity, list)
                  if entity == nil or #list == 0 then
                    return
                  end
                  entity:set_custom_name(list[math.random(#list)])
                  schedule_name_reset(context, entity, 4 * 20)
                end

                return {
                  api_version = 1,
                  on_boss_target_changed = function(context)
                    say_random_line(context, context.boss, target_taunt_list)
                  end,
                  on_boss_damaged = function(context)
                    if not context.boss:is_alive()
                      or (context.boss:get_health() - context.event.damage_amount) <= 0 then
                      return
                    end

                    if context.event.damage_cause == "PROJECTILE" then
                      say_random_line(context, context.boss, damaged_by_bow_list)
                    else
                      say_random_line(context, context.boss, generic_damaged_list)
                    end
                  end,
                  on_player_damaged_by_boss = function(context)
                    say_random_line(context, context.boss, hit_list)
                  end,
                  on_death = function(context)
                    local entity = context.event.entity
                    if entity == nil then
                      entity = context.boss
                    end
                    say_random_line(context, entity, death_list)
                  end
                }
                """.formatted(
                luaList(ON_TARGET),
                luaList(ON_DAMAGED),
                luaList(ON_DAMAGED_BY_BOW),
                luaList(ON_DAMAGE),
                luaList(ON_DEATH));
    }
}
