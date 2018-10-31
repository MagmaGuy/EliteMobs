package com.magmaguy.elitemobs.runnables;

import com.magmaguy.elitemobs.EntityTracker;

public class EntityListUpdater {

    public static void startUpdating(){

        EntityTracker.checkEntityState();

    }

}
