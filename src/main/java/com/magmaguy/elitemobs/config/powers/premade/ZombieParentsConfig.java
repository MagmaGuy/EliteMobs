package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ZombieParentsConfig extends PowersConfigFields {
    public ZombieParentsConfig() {
        super("zombie_parents",
                true,
                "Parents",
                Material.SKELETON_SKULL.toString());

        super.getAdditionalConfigOptions().put("deathMessage", Arrays.asList(
                "You monster!",
                "My baby!",
                "What have you done!?",
                "Revenge!",
                "Nooooo!",
                "You will pay for that!",
                "Eh, he was adopted",
                "He's dead! Again!",
                "He's deader than before!",
                "You broke him!"));

        super.getAdditionalConfigOptions().put("bossEntityDialog", Arrays.asList(
                "You're embarrassing me!",
                "He's bullying me!",
                "He's the one picking on me!",
                "I can deal with this alone!",
                "Leave me alone, I got this!",
                "Stop following me around!",
                "God this is so embarrassing!",
                "He took my lunch money!",
                "He's bullying me!"));

        super.getAdditionalConfigOptions().put("zombieDad", Arrays.asList(
                "Get away from my son!",
                "Stand up for yourself son!",
                "I'll deal with him!",
                "Stop picking on my son!",
                "Why are you doing this?",
                "I'll talk to your parents!",
                "You go kiddo!",
                "Show him who's boss kiddo!",
                "Nice punch kiddo!"));

        super.getAdditionalConfigOptions().put("zombieMom", Arrays.asList(
                "Hands off my child!",
                "Are you hurt sweetie?",
                "Did he hurt you sweetie?",
                "Let me see that booboo sweetie",
                "I'll talk to his parents!",
                "You forgot your jacket sweetie!",
                "Posture, sweetheart",
                "Break it up!",
                "Stop this!",
                "Did you take out the garbage?"));
    }
}