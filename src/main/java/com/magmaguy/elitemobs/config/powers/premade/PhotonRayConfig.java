package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.PhotonRay;

public class PhotonRayConfig extends PowersConfigFields {
    public PhotonRayConfig() {
        super("photon_ray",
                true,
                null,
                60,
                20,
                PhotonRay.class,
                PowerType.UNIQUE);
    }
}
