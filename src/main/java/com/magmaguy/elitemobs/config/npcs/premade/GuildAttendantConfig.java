package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import org.bukkit.entity.Villager;

import java.util.Arrays;

public class GuildAttendantConfig extends NPCsConfigFields {
    public GuildAttendantConfig() {
        super("guild_attendant",
                true,
                "Gillian",
                "<Guild Attendant>",
                Villager.Profession.LIBRARIAN,
                "em_adventurers_guild,283.5,91,229.5,179,0",
                Arrays.asList(
                        "Welcome to the\\nAdventurer's Guild!",
                        "Welcome!"),
                Arrays.asList(
                        "Check the questboard to\\nsee active quests!",
                        "You can talk to me to\\nchange your guild rank!",
                        "You can sell items to the\\nblacksmith for coins!",
                        "You can talk to the arena\\nmaster to take the arena on!",
                        "You can buy equipment from\\nthe blacksmith!",
                        "You can talk to the combat\\nmaster to check your combat level!",
                        "Unlocking new guid tiers\\nincreases your maximum health!"),
                Arrays.asList(
                        "See you soon!",
                        "Thanks for stopping by!",
                        "Happy hunting!"),
                true,
                3,
                NPCInteractions.NPCInteractionType.GUILD_GREETER);
        setDisguise("custom:ag_guild_attendant");
        setCustomDisguiseData("player ag_guild_attendant setskin {\"id\":\"be36a657-78e8-45e5-bd91-ea5404863495\",\"name\":\"Unknown\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTYyMjk3ODkyNzA0MiwKICAicHJvZmlsZUlkIiA6ICIxOTI1MjFiNGVmZGI0MjVjODkzMWYwMmE4NDk2ZTExYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXJpYWxpemFibGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTFmYmRiNzE5NTI0YWMxMDY1YTMyOWUwZjRhODAxZDM1M2UxOWU5MTVlNjE3ZDhkNDY0YTRjOWFkYzZiYmY2IgogICAgfQogIH0KfQ==\",\"signature\":\"OL866yngEcY+GnuMH1yDCYo+s3BXvKRGq1LhBSD+bwV7BxDSFqbEGjHTKVzU8xsn7EH5/dHovWm/lW48b7vEiV4UTzurHeF5rMPJxQuIE+B2qRCWiag3R6yyuP+iUwIzDPJcpFDr7CY/53eVJqLn4BEvN3iAPjyBSfeFvNzNgCA55I7L8wjfBVZfgJJB+y8fDe1+F3fc+kPm9NMncSbxeHrEhl7WWFTaVgRPrZWdWu4z9sYpktE0/qDKrKbhD8FUbk0z9VEOBenakPqpAuGmyRJGrvNMWeYrskxbcwtkPCYVlcKTrX7zYNPvHS3QsxcAVNtpPnawUa60Sp2TlGtsl/v5QGEj1J+Xisgr1b/sSdC+rNH6CUn0fNJmFDGwGUnaqS8v3iCSeTX7aZa0sc9tgl7VPGrXlD9juvXVKj5U0GOX2TQIU50dTQSuUmJNbHzp1p+L135tTuVmmnBoiyvg0cZN50ndVnR4qOlSP2OEOIx5kPV09Eu7HNiEArVCavzFQG38HzZsqtDJw6Hg/0NjiOd8CTrgw2ZS+NsR08U0ktEQxRAXs91pKMJ+ZpIrqzNpq/81Xmw3C7d0PgiAQPoQbg9fJMzfzwNuf2EcXhQssY881kEbOs6MiM9v3xT2d7gR50xe2WZCRxsbj10+UGGpdA414NmJWTQzX0MI0KucRnU=\"}],\"legacy\":false}");
    }
}
