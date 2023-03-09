package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class TravellingMerchantConfig extends NPCsConfigFields {
    public TravellingMerchantConfig() {
        super("travelling_merchant",
                true,
                "Jeeves",
                "<Travelling Merchant>",
                Villager.Profession.LIBRARIAN,
                null,
                Arrays.asList(
                        "Got something to sell?",
                        "You called?",
                        "Huh?"),
                Arrays.asList(
                        "I'll buy it on the cheap!",
                        "Got something for me?",
                        "Time is money!"),
                Arrays.asList(
                        "Call me again anytime!",
                        "Was that all?",
                        "Safe travels!",
                        "Don't get yourself killed!",
                        "Good hunting!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.SELL);
        setTimeout(2);
        setDisguise("custom:em_travelling_merchant");
        setCustomDisguiseData("player em_travelling_merchant setskin {\"id\":\"cec11e92-977e-4c19-ba39-dd547b65bb43\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY2NzY4Mzc3NTg5MCwKICAicHJvZmlsZUlkIiA6ICJiMDM1OTg4MWIwMjk0NTNjYTk2MmFhNjA4NjI5ZTdhYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJGYW5vNDMyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MyMjk3MDZjY2M4NzAzM2NhZTE3MjY0MTg5NjQyOGE0ZDBkMTFjYTIyOWJmNjE4MmMwNGI4NDNhNzA5ZmRiODAiCiAgICB9CiAgfQp9\",\"signature\":\"CGc4XyRbLSab5UhxSWVJ/fgTCThqFnc2LaURvp3UpjKXR29sUTqQJtIbrPC3c2IgQr+SyV24BYv7lGGYIIiAY0vjfk6LV6PiofMX0Hm/cg7S1TgvEFMpzRJB7dSEbW4HBT4hCnkjB0wbjRT2jfyy4lFJ2GpNmeyswfC+VAv093ziuwA61nqwl+vSb7yZcgEALdPnwiwb0wXLsQ6/7hq8E5iQsIsKR46CnuaQQoyOmb9en56rMmWXW9gJleGU1ht6LxNqkgCvpjIwa4PfjZbgfE+Fb0Wp7Nilomz+7UaBz3NBpK9ICuFXynCT4L/K4wef6/tMq/5EoUmEuIExgn2WMIh2yi5WTwp8iz7kMz9RNSJR+S9opG3Jq3YHlIaLPboUp6XQ3klX6Wzl9ScCM9K3rK4c6aysuc2qz3rzNcBuHd+ETgW3Ow1kg7qID+tVUgNABt+clpzRM3dE3rfHs2jsH3lsGX5H1iSnsYcjkJXwk5DVEQDacviwDGyN5vrMAPVW/17O3HvShAQJ4yRj5z9cAUADmFVAmk16nyLVb6bGeELVCV1xxue4dIBzWD5YDLkmSZusFAdOMh13SSJ6zr8ylxiZYue0OoJScJVH0rcbid8QDmcYDbATqVsgMS+LWpAiwh65WNKWeubimK683mccs1RlYbcBPxCR75TXt4gEFY8=\"}],\"legacy\":false}");
    }
}
