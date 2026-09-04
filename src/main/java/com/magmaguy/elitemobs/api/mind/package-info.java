/**
 * Classloader-safe interface for registering Lua Mind modules and programs and assigning them to
 * EliteMobs actors. This package deliberately exposes no MagmaCore type because each consuming
 * plugin relocates its own shaded MagmaCore copy.
 */
package com.magmaguy.elitemobs.api.mind;
