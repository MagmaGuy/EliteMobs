package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import com.magmaguy.elitemobs.MetadataHandler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModelEngineReservedAddresses {
    /**
     * This method reserves ModelEngine IDs for use with the resource pack distributed by EliteMobs.
     * By default, ModelEngine increments the ID of anything in directories by 1000, and anything inside of that directory
     * inherits and increments this value.
     * That means that if you have 3 folders, the first folder starts counting at 1000, the second at 2000 and the third at 3000.
     * These are reserved slots, so they are not offset by anything other than folders.
     * This is required because ModelEngine assigns IDs based on the files it reads on boot in alphabetical order. This
     * also means that if you have more - or less - than the entirety of the content expected by ModelEngine in your
     * resource pack, it will simply not work.
     * By reserving specific addresses ahead of time the EliteMobs resource pack can distribute valid coordinates and
     * textures for all models and servers can pick which packs they want to use without causing issues.
     * This also means that all EliteMobs reservations must be made in the form of valid directories.
     * For future and backward compatibility reasons, EliteMobs folders for ModelEngine follow the format em_NUMBER_NAME
     * where NUMBER is the number of the resource pack, in release order (guarantees a consistent alphabetical read order)
     * and the NAME is the identifier for that specific package. For Primis, this would be em_1_primis. For the goblins, it's em_2_goblins.
     */
    public static void reserve() {
        //Nothing gets reserved if ModelEngine isn't installed
        if (!ModelEngineChecker.modelEngineIsInstalled()) return;
        Path modelEngineBlueprintsPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getAbsolutePath() + File.separatorChar + "ModelEngine" + File.separatorChar + "blueprints");
        //These are hardcoded and kept updated with releases. No two ways about it!
        makeDirectory(modelEngineBlueprintsPath, "em_1_primis");
        makeDirectory(modelEngineBlueprintsPath, "em_2_goblins");
    }

    private static void makeDirectory(Path originalPath, String directory) {
        Path primisPath = Paths.get(originalPath.toString() + File.separatorChar + directory);
        if (!primisPath.toFile().exists())
            primisPath.toFile().mkdir();
    }

}
