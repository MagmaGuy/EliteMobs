package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.Configuration;

import java.util.Arrays;

public class NPCConfig {

    public static final String CONFIG_NAME = "NPCConfig.yml";

    public static final String NAME = "Name";
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
    public static final String GUILD_GREETER_NAME = GUILD_GREETER + NAME;
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

    CustomConfigLoader customConfigLoader = new CustomConfigLoader();
    private Configuration configuration = customConfigLoader.getCustomConfig(CONFIG_NAME);

    public void initializeConfig() {

        configuration.addDefault(GUILD_GREETER_NAME, "Guild greeter");
        configuration.addDefault(GUILD_GREETER_TYPE, "LIBRARIAN");
        configuration.addDefault(GUILD_GREETER_LOCATION, "EliteMobs_adventurers_guild,283,91,229,179,0");
        configuration.addDefault(GUILD_GREETER_GREET, Arrays.asList(
                "Welcome to the Adventurer's Guild!",
                "Welcome!"
        ));
        configuration.addDefault(GUILD_GREETER_DIALOG, Arrays.asList(
                "Check the questboard to see active quests!",
                "You can talk to me to change your guild rank!",
                "You can sell items to the blacksmith for coins!",
                "You can talk to the arena master to take the arena on!",
                "You can buy equipment from the blacksmith!",
                "You can talk to the combat master to check your combat level!"
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
        configuration.addDefault(GUILD_GREETER_INTERACTION_MENU, "");

        customConfigLoader.getCustomConfig(CONFIG_NAME).options().copyDefaults(true);
        UnusedNodeHandler.clearNodes(configuration);
        customConfigLoader.saveDefaultCustomConfig(CONFIG_NAME);
        customConfigLoader.saveCustomConfig(CONFIG_NAME);

    }

}



