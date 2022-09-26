package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.ZombieNecronomicon;
import org.bukkit.Material;

public class ZombieNecronomiconConfig extends PowersConfigFields {
    public static String summoningChant;

    public ZombieNecronomiconConfig() {
        super("zombie_necronomicon",
                true,
                Material.ENCHANTED_BOOK.toString(),
                ZombieNecronomicon.class,
                PowerType.MAJOR_ZOMBIE);
    }

    @Override
    public void processAdditionalFields() {
        summoningChant = ConfigurationEngine.setString(file, fileConfiguration, "summoningChant", "Nor is it to be thought..." +
                "that man is either the oldest " +
                "or the last of earth's masters, or that the common bulk of life and substance walks alone. The Old Ones" +
                " were, the Old Ones are, and the Old Ones shall be. Not in the spaces we know, but between them, they " +
                "walk serene and primal, undimensioned and to us unseen. Yog-Sothoth knows the gate. Yog-Sothoth is the " +
                "gate. Yog-Sothoth is the key and guardian of the gate. Past, present, future, all are one in Yog-Sothoth. " +
                "He knows where the Old Ones broke through of old, and where They shall break through again. He knows " +
                "where They had trod earth's fields, and where They still tread them, and why no one can behold Them as " +
                "They tread. By Their smell can men sometimes know Them near, but of Their semblance can no man know, " +
                "saving only in the features of those They have begotten on mankind; and of those are there many sorts, " +
                "differing in likeness from man's truest eidolon to that shape without sight or substance which is Them. " +
                "They walk unseen and foul in lonely places where the Words have been spoken and the Rites howled through " +
                "at their Seasons. The wind gibbers with Their voices, and the earth mutters with Their consciousness. " +
                "They bend the forest and crush the city, yet may not forest or city behold the hand that smites. Kadath " +
                "in the cold waste hath known Them, and what man knows Kadath? The ice desert of the South and the sunken " +
                "isles of Ocean hold stones whereon Their seal is engraver, but who hath seen the deep frozen city or the " +
                "sealed tower long garlanded with seaweed and barnacles? Great Cthulhu is Their cousin, yet can he spy " +
                "Them only dimly. Iä! Shub-Niggurath! As a foulness shall ye know Them. Their hand is at your throats, " +
                "yet ye see Them not; and Their habitation is even one with your guarded threshold. Yog-Sothoth is the " +
                "key to the gate, whereby the spheres meet. Man rules now where They ruled once; They shall soon rule " +
                "where man rules now. After summer is winter, after winter summer. They wait patient and potent, for " +
                "here shall They reign again.", true);
    }

}
