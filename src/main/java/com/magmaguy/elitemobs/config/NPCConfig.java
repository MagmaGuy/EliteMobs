package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

import java.util.Arrays;

public class NPCConfig {

    public static final String CONFIG_NAME = "NPCs.yml";

    public static final String ENABLED = "Enabled";
    public static final String NAME = "Name";
    public static final String ROLE = "Role";
    public static final String TYPE = "Type";
    public static final String LOCATION = "Location";
    public static final String GREETINGS = "Greetings";
    public static final String DIALOG = "Dialog";
    public static final String FAREWELL = "Farewell";
    public static final String CAN_MOVE = "Can move";
    public static final String CAN_TALK = "Can talk";
    public static final String ACTIVATION_RADIUS = "Activation radius";
    public static final String DISAPPEARS_AT_NIGHT = "Disappears at night";
    public static final String INTERACTION_TYPE = "Interaction type";


    public static final String GUILD_GREETER = "Guild_Greeter.";
    public static final String GUILD_GREETER_ENABLED = GUILD_GREETER + ENABLED;
    public static final String GUILD_GREETER_NAME = GUILD_GREETER + NAME;
    public static final String GUILD_GREETER_ROLE = GUILD_GREETER + ROLE;
    public static final String GUILD_GREETER_TYPE = GUILD_GREETER + TYPE;
    public static final String GUILD_GREETER_LOCATION = GUILD_GREETER + LOCATION;
    public static final String GUILD_GREETER_GREET = GUILD_GREETER + GREETINGS;
    public static final String GUILD_GREETER_DIALOG = GUILD_GREETER + DIALOG;
    public static final String GUILD_GREETER_FAREWELL = GUILD_GREETER + FAREWELL;
    public static final String GUILD_GREETER_CAN_MOVE = GUILD_GREETER + CAN_MOVE;
    public static final String GUILD_GREETER_CAN_TALK = GUILD_GREETER + CAN_TALK;
    public static final String GUILD_GREETER_ACTIVATION_RADIUS = GUILD_GREETER + ACTIVATION_RADIUS;
    public static final String GUILD_GREETER_DISAPPEARS_AT_NIGHT = GUILD_GREETER + DISAPPEARS_AT_NIGHT;
    public static final String GUILD_GREETER_INTERACTION_MENU = GUILD_GREETER + INTERACTION_TYPE;

    public static final String BLACKSMITH = "Blacksmith.";
    public static final String BLACKSMITH_ENABLED = BLACKSMITH + ENABLED;
    public static final String BLACKSMITH_NAME = BLACKSMITH + NAME;
    public static final String BLACKSMITH_ROLE = BLACKSMITH + ROLE;
    public static final String BLACKSMITH_TYPE = BLACKSMITH + TYPE;
    public static final String BLACKSMITH_LOCATION = BLACKSMITH + LOCATION;
    public static final String BLACKSMITH_GREET = BLACKSMITH + GREETINGS;
    public static final String BLACKSMITH_DIALOG = BLACKSMITH + DIALOG;
    public static final String BLACKSMITH_FAREWELL = BLACKSMITH + FAREWELL;
    public static final String BLACKSMITH_CAN_MOVE = BLACKSMITH + CAN_MOVE;
    public static final String BLACKSMITH_CAN_TALK = BLACKSMITH + CAN_TALK;
    public static final String BLACKSMITH_ACTIVATION_RADIUS = BLACKSMITH + ACTIVATION_RADIUS;
    public static final String BLACKSMITH_DISAPPEARS_AT_NIGHT = BLACKSMITH + DISAPPEARS_AT_NIGHT;
    public static final String BLACKSMITH_INTERACTION_MENU = BLACKSMITH + INTERACTION_TYPE;

    public static final String SPECIAL_BLACKSMITH = "Special_Blacksmith.";
    public static final String SPECIAL_BLACKSMITH_ENABLED = SPECIAL_BLACKSMITH + ENABLED;
    public static final String SPECIAL_BLACKSMITH_NAME = SPECIAL_BLACKSMITH + NAME;
    public static final String SPECIAL_BLACKSMITH_ROLE = SPECIAL_BLACKSMITH + ROLE;
    public static final String SPECIAL_BLACKSMITH_TYPE = SPECIAL_BLACKSMITH + TYPE;
    public static final String SPECIAL_BLACKSMITH_LOCATION = SPECIAL_BLACKSMITH + LOCATION;
    public static final String SPECIAL_BLACKSMITH_GREET = SPECIAL_BLACKSMITH + GREETINGS;
    public static final String SPECIAL_BLACKSMITH_DIALOG = SPECIAL_BLACKSMITH + DIALOG;
    public static final String SPECIAL_BLACKSMITH_FAREWELL = SPECIAL_BLACKSMITH + FAREWELL;
    public static final String SPECIAL_BLACKSMITH_CAN_MOVE = SPECIAL_BLACKSMITH + CAN_MOVE;
    public static final String SPECIAL_BLACKSMITH_CAN_TALK = SPECIAL_BLACKSMITH + CAN_TALK;
    public static final String SPECIAL_BLACKSMITH_ACTIVATION_RADIUS = SPECIAL_BLACKSMITH + ACTIVATION_RADIUS;
    public static final String SPECIAL_BLACKSMITH_DISAPPEARS_AT_NIGHT = SPECIAL_BLACKSMITH + DISAPPEARS_AT_NIGHT;
    public static final String SPECIAL_BLACKSMITH_INTERACTION_MENU = SPECIAL_BLACKSMITH + INTERACTION_TYPE;

    public static final String BARKEEP = "Barkeep.";
    public static final String BARKEEP_ENABLED = BARKEEP + ENABLED;
    public static final String BARKEEP_NAME = BARKEEP + NAME;
    public static final String BARKEEP_ROLE = BARKEEP + ROLE;
    public static final String BARKEEP_TYPE = BARKEEP + TYPE;
    public static final String BARKEEP_LOCATION = BARKEEP + LOCATION;
    public static final String BARKEEP_GREET = BARKEEP + GREETINGS;
    public static final String BARKEEP_DIALOG = BARKEEP + DIALOG;
    public static final String BARKEEP_FAREWELL = BARKEEP + FAREWELL;
    public static final String BARKEEP_CAN_MOVE = BARKEEP + CAN_MOVE;
    public static final String BARKEEP_CAN_TALK = BARKEEP + CAN_TALK;
    public static final String BARKEEP_ACTIVATION_RADIUS = BARKEEP + ACTIVATION_RADIUS;
    public static final String BARKEEP_DISAPPEARS_AT_NIGHT = BARKEEP + DISAPPEARS_AT_NIGHT;
    public static final String BARKEEP_INTERACTION_MENU = BARKEEP + INTERACTION_TYPE;

    public static final String COMBAT_INSTRUCTOR = "Combat_Instructor.";
    public static final String COMBAT_INSTRUCTOR_ENABLED = COMBAT_INSTRUCTOR + ENABLED;
    public static final String COMBAT_INSTRUCTOR_NAME = COMBAT_INSTRUCTOR + NAME;
    public static final String COMBAT_INSTRUCTOR_ROLE = COMBAT_INSTRUCTOR + ROLE;
    public static final String COMBAT_INSTRUCTOR_TYPE = COMBAT_INSTRUCTOR + TYPE;
    public static final String COMBAT_INSTRUCTOR_LOCATION = COMBAT_INSTRUCTOR + LOCATION;
    public static final String COMBAT_INSTRUCTOR_GREET = COMBAT_INSTRUCTOR + GREETINGS;
    public static final String COMBAT_INSTRUCTOR_DIALOG = COMBAT_INSTRUCTOR + DIALOG;
    public static final String COMBAT_INSTRUCTOR_FAREWELL = COMBAT_INSTRUCTOR + FAREWELL;
    public static final String COMBAT_INSTRUCTOR_CAN_MOVE = COMBAT_INSTRUCTOR + CAN_MOVE;
    public static final String COMBAT_INSTRUCTOR_CAN_TALK = COMBAT_INSTRUCTOR + CAN_TALK;
    public static final String COMBAT_INSTRUCTOR_ACTIVATION_RADIUS = COMBAT_INSTRUCTOR + ACTIVATION_RADIUS;
    public static final String COMBAT_INSTRUCTOR_DISAPPEARS_AT_NIGHT = COMBAT_INSTRUCTOR + DISAPPEARS_AT_NIGHT;
    public static final String COMBAT_INSTRUCTOR_INTERACTION_MENU = COMBAT_INSTRUCTOR + INTERACTION_TYPE;

    public CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    public Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        //Guild greeter
        configuration.addDefault(GUILD_GREETER_ENABLED, true);
        configuration.addDefault(GUILD_GREETER_NAME, "Gillian");
        configuration.addDefault(GUILD_GREETER_ROLE, "<Guild Attendant>");
        configuration.addDefault(GUILD_GREETER_TYPE, "LIBRARIAN");
        configuration.addDefault(GUILD_GREETER_LOCATION, "EliteMobs_adventurers_guild,283.5,91,229.5,179,0");
        configuration.addDefault(GUILD_GREETER_GREET, Arrays.asList(
                "Welcome to the\\nAdventurer's Guild!",
                "Welcome!"
        ));
        configuration.addDefault(GUILD_GREETER_DIALOG, Arrays.asList(
                "Check the questboard to\\nsee active quests!",
                "You can talk to me to\\nchange your guild rank!",
                "You can sell items to the\\nblacksmith for coins!",
                "You can talk to the arena\\nmaster to take the arena on!",
                "You can buy equipment from\\nthe blacksmith!",
                "You can talk to the combat\\nmaster to check your combat level!",
                "Unlocking new guid tiers\\nincreases your maximum health!"
        ));
        configuration.addDefault(GUILD_GREETER_FAREWELL, Arrays.asList(
                "See you soon!",
                "Thanks for stopping by!",
                "Happy hunting!"
        ));
        configuration.addDefault(GUILD_GREETER_CAN_MOVE, false);
        configuration.addDefault(GUILD_GREETER_CAN_TALK, true);
        configuration.addDefault(GUILD_GREETER_ACTIVATION_RADIUS, 3);
        configuration.addDefault(GUILD_GREETER_DISAPPEARS_AT_NIGHT, false);
        configuration.addDefault(GUILD_GREETER_INTERACTION_MENU, "GUILD_GREETER");

        //Blacksmith
        configuration.addDefault(BLACKSMITH_ENABLED, true);
        configuration.addDefault(BLACKSMITH_NAME, "Greg");
        configuration.addDefault(BLACKSMITH_ROLE, "<Blacksmith>");
        configuration.addDefault(BLACKSMITH_TYPE, "BLACKSMITH");
        configuration.addDefault(BLACKSMITH_LOCATION, "EliteMobs_adventurers_guild,285.5,93,261.5,179,0");
        configuration.addDefault(BLACKSMITH_GREET, Arrays.asList(
                "Welcome to our shop!",
                "Sell your goods here!",
                "Fresh goods, just for you!",
                "Got something to sell?",
                "Want to buy something good?",
                "Fresh goods every time!"
        ));
        configuration.addDefault(BLACKSMITH_DIALOG, Arrays.asList(
                "Higher level mobs drop\\nhigher value items!",
                "Items with lots of \\nenchantments are worth more!",
                "Higher level mobs drop\\nbetter items!",
                "Higher level mobs have\\na higher chance of dropping loot!",
                "Elite mobs are attracted to\\ngood armor, the better the\\narmor the higher their level!",
                "Some items have special\\npotion effects!",
                "Some items have unique\\neffects!",
                "The hunter enchantment\\nattracts elite mobs to your\\nlocation!",
                "Special weapons and armor\\ndropped by elite mobs can\\nbe sold here!",
                "Higher guild ranks will\\nincrease the quality of the\\nloot from elite mobs!"
        ));
        configuration.addDefault(BLACKSMITH_FAREWELL, Arrays.asList(
                "Thank you for your business!",
                "Come back soon!",
                "Come back any time!",
                "Recommend this shop to your\\nfriends!"
        ));
        configuration.addDefault(BLACKSMITH_CAN_MOVE, false);
        configuration.addDefault(BLACKSMITH_CAN_TALK, true);
        configuration.addDefault(BLACKSMITH_ACTIVATION_RADIUS, 3);
        configuration.addDefault(BLACKSMITH_DISAPPEARS_AT_NIGHT, true);
        configuration.addDefault(BLACKSMITH_INTERACTION_MENU, "PROCEDURALLY_GENERATED_SHOP");

        //Special blacksmith
        configuration.addDefault(SPECIAL_BLACKSMITH_ENABLED, true);
        configuration.addDefault(SPECIAL_BLACKSMITH_NAME, "Grog");
        configuration.addDefault(SPECIAL_BLACKSMITH_ROLE, "<Master Blacksmith>");
        configuration.addDefault(SPECIAL_BLACKSMITH_TYPE, "BLACKSMITH");
        configuration.addDefault(SPECIAL_BLACKSMITH_LOCATION, "EliteMobs_adventurers_guild,282.5,93,258.5,-90,0");
        configuration.addDefault(SPECIAL_BLACKSMITH_GREET, Arrays.asList(
                "Need something?",
                "Got anything good?",
                "We have nothing but the best",
                "Got anything worth selling?",
                "We only buy elite mob gear."
        ));
        configuration.addDefault(SPECIAL_BLACKSMITH_DIALOG, Arrays.asList(
                "We buy low and sell high.",
                "No refunds.",
                "Absolutely no refunds.",
                "There are no refunds.",
                "No taksies backsies.",
                "Shop purchases are final."
        ));
        configuration.addDefault(SPECIAL_BLACKSMITH_FAREWELL, Arrays.asList(
                "Come back when you have\\nmore to spend",
                "Don't get yourself killed,\\nwe want you to bring us\\nmore gear.",
                "Next time buy something \\mmore expensive.",
                "Don't forget, no refunds."
        ));
        configuration.addDefault(SPECIAL_BLACKSMITH_CAN_MOVE, false);
        configuration.addDefault(SPECIAL_BLACKSMITH_CAN_TALK, true);
        configuration.addDefault(SPECIAL_BLACKSMITH_ACTIVATION_RADIUS, 3);
        configuration.addDefault(SPECIAL_BLACKSMITH_DISAPPEARS_AT_NIGHT, true);
        configuration.addDefault(SPECIAL_BLACKSMITH_INTERACTION_MENU, "CUSTOM_SHOP");

        //Barkeep
        configuration.addDefault(BARKEEP_ENABLED, true);
        configuration.addDefault(BARKEEP_NAME, "Bartley");
        configuration.addDefault(BARKEEP_ROLE, "<Barkeep>");
        configuration.addDefault(BARKEEP_TYPE, "BUTCHER");
        configuration.addDefault(BARKEEP_LOCATION, "EliteMobs_adventurers_guild,285.5,91,209.5,0,0");
        configuration.addDefault(BARKEEP_GREET, Arrays.asList(
                "Need a drink?",
                "Want a drink",
                "Thirsty?",
                "Howdy, partner."
        ));
        configuration.addDefault(BARKEEP_DIALOG, Arrays.asList(
                "Have one of our house specialties.",
                "Special drinks won't find them\\nanywhere else.",
                "One taste and will keep you\\ncoming back from more."
        ));
        configuration.addDefault(BARKEEP_FAREWELL, Arrays.asList(
                "Come back anytime",
                "Bottoms up!",
                "Kampai!",
                "Salut!",
                "Brosd!",
                "Salud!",
                "À la vôtre !",
                "Prost!",
                "Santi!",
                "Salute!",
                "Saúde!",
                "Cheers!",
                "乾杯!"
        ));
        configuration.addDefault(BARKEEP_CAN_MOVE, false);
        configuration.addDefault(BARKEEP_CAN_TALK, true);
        configuration.addDefault(BARKEEP_ACTIVATION_RADIUS, 3);
        configuration.addDefault(BARKEEP_DISAPPEARS_AT_NIGHT, true);
        configuration.addDefault(BARKEEP_INTERACTION_MENU, "BAR");

        //Combat Instructor
        configuration.addDefault(COMBAT_INSTRUCTOR_ENABLED, true);
        configuration.addDefault(COMBAT_INSTRUCTOR_NAME, "Charles");
        configuration.addDefault(COMBAT_INSTRUCTOR_ROLE, "<Combat Instructor>");
        configuration.addDefault(COMBAT_INSTRUCTOR_TYPE, "NITWIT");
        configuration.addDefault(COMBAT_INSTRUCTOR_LOCATION, "EliteMobs_adventurers_guild,264.5,87,217.5,40,0");
        configuration.addDefault(COMBAT_INSTRUCTOR_GREET, Arrays.asList(
                "Want to learn about combat?",
                "Need a combat lesson?",
                "Want to know more about combat?",
                "Ready to fight Elite Mobs?"
        ));
        configuration.addDefault(COMBAT_INSTRUCTOR_DIALOG, Arrays.asList(
                "The items around Elite Mobs\\nshow what powers they have.",
                "The higher the level of\\nthe Elite Mob, the\\nmore powers they can have",
                "The higher the level of\\nthe Elite Mob, the\\nbetter the loot they can drop",
                "Higher level Elite Mobs\\nhave higher chances\\nof dropping special items",
                "Elite Mobs with floating\\narrows fire arrows at\\nnearby players",
                "Elite Mobs with floating\\nender eyes blind you\\nwhen they hit you",
                "Elite Mobs with floating\\nspell particles confuse you\\nwhen they hit you",
                "Elite Mobs with floating\\nlava buckets set you on fire\\nwhen they hit you",
                "Elite Mobs with floating\\nfireballs fire fireballs\nat nearby players",
                "Elite Mobs with floating\\npacked ice freeze you\\nwhen they hit you",
                "Elite Mobs with floating\\nelytras make you float\\nwhen they hit you",
                "Elite Mobs with floating\\nemeralds poison you\\nwhen they hit you",
                "Elite Mobs with floating\\npistons push you\\nwhen they hit you",
                "Elite Mobs with floating\\ntotems weaken you\\nwhen they hit you",
                "Elite Mobs with floating\\nwebs web you\\nwhen they hit you",
                "Elite Mobs with floating\\nskulls wither you\\nwhen they hit you",
                "Elite Mobs with floating\\nchests drop bonus loot\\nwhen you kill them",
                "Elite Mobs with floating\\ngold boots are faster",
                "Elite Mobs with floating\\njukeboxes taunt you\\nduring combat",
                "Elite Mobs with floating\\nglass panes are invisible",
                "Elite Mobs with floating\\nspectral arrows are immune\\nto arrows",
                "Elite Mobs with floating\\nfeathers are immune\\nto fall damage",
                "Elite Mobs with floating\\nflames are immune\\nto fire",
                "Elite Mobs with floating\\nanvils are heavier\\nthan usual",
                "Elite Mobs with floating\\nbone blocks leave their\\ncorpses behind when killed",
                "Elite Mobs with floating\\nslime blocks jump higher\\nthan usual",
                "Elite Mobs with floating\\nslime balls create a vaccum\\nupon death",
                "Elite Mobs with floating\\nleads pull you\\nwhen they hit you",
                "Elite Mobs with floating\\nbones create bone pillars\\nduring combat",
                "Elite Mobs with floating\\ntipped arrows fire tracking\\narrows during combat",
                "Elite Mobs with floating\\nmushrooms bloat up to become giants\\nwhen hit",
                "Elite Mobs with floating\\nskulls summon reinforcements\\nwhen hit",
                "Elite Mobs with floating\\nbooks summon reinforcements\\nuntil you kill the summoner",
                "Elite Mobs with floating\\neggs summon reinforcements\\nwhen hit",
                "Elite Mobs with floating\\nfireworks summon Team Rocket\\nwhen hit"

        ));
        configuration.addDefault(COMBAT_INSTRUCTOR_FAREWELL, Arrays.asList(

        ));
        configuration.addDefault(COMBAT_INSTRUCTOR_CAN_MOVE, false);
        configuration.addDefault(COMBAT_INSTRUCTOR_CAN_TALK, true);
        configuration.addDefault(COMBAT_INSTRUCTOR_ACTIVATION_RADIUS, 3);
        configuration.addDefault(COMBAT_INSTRUCTOR_DISAPPEARS_AT_NIGHT, true);
        configuration.addDefault(COMBAT_INSTRUCTOR_INTERACTION_MENU, "CHAT");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}



