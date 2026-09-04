package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.magmacore.ai.MindProgram;
import org.bukkit.plugin.Plugin;

/** Internal executable-program seam shared by cataloged Lua and per-actor Java minds. */
interface EliteMindProgramEntry {
    Plugin owner();

    EliteMindProgram descriptor();

    MindProgram instantiate();
}
