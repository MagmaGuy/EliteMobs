package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.powers.lua.LuaPowerDefinition;
import lombok.Getter;

import java.io.File;

public class LuaPowerConfigFields extends PowersConfigFields {

    @Getter
    private final LuaPowerDefinition luaPowerDefinition;

    public LuaPowerConfigFields(String fileName, File file, LuaPowerDefinition luaPowerDefinition) {
        this(fileName, file, luaPowerDefinition, null, PowerType.MISCELLANEOUS);
    }

    public LuaPowerConfigFields(String fileName,
                                File file,
                                LuaPowerDefinition luaPowerDefinition,
                                String effect,
                                PowerType powerType) {
        super(fileName, true);
        this.filename = fileName;
        this.luaPowerDefinition = luaPowerDefinition;
        setEffect(effect);
        setPowerType(powerType);
        setFile(file);
    }
}
