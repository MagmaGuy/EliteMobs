package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindModule;
import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.magmacore.scripting.LuaMindDefinition;
import com.magmaguy.magmacore.scripting.LuaMindModuleDescriptor;
import com.magmaguy.magmacore.scripting.LuaMindModuleRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Validated program catalog. Executable Lua state never lives in this map. */
final class EliteMindCatalog {
    private final Map<NamespacedKey, Entry> entries = new LinkedHashMap<>();
    private final LuaMindModuleRegistry moduleRegistry = new LuaMindModuleRegistry();
    private final Map<Plugin, LuaMindModuleRegistry.OwnerScope> moduleOwners =
            new IdentityHashMap<>();

    EliteMindModule registerModule(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(source, "source");
        if (revision < 1L) throw new IllegalArgumentException("revision must be positive");
        requireOwnerNamespace(owner, key);
        LuaMindModuleDescriptor descriptor = ownerScope(owner).registerModule(
                key.toString(),
                revision,
                key + ".lua",
                source);
        return toPublicModule(descriptor);
    }

    EliteMindProgram register(Plugin owner, NamespacedKey key, long revision, String source) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(source, "source");
        if (revision < 1L) throw new IllegalArgumentException("revision must be positive");
        requireOwnerNamespace(owner, key);

        Entry current = entries.get(key);
        if (current != null) {
            if (current.owner() != owner) {
                throw new IllegalStateException("Mind program key is already owned by "
                        + current.owner().getName() + ": " + key);
            }
            if (current.revision() == revision && current.source().equals(source)) {
                return current.descriptor();
            }
            if (revision <= current.revision()) {
                throw new IllegalArgumentException("Mind program replacements require a newer revision: " + key);
            }
        }

        LuaMindDefinition definition = ownerScope(owner).validateProgram(key + ".lua", source);
        if (!definition.programIdentifier().equals(key.toString())) {
            throw new IllegalArgumentException("Lua mind id must match its catalog key " + key);
        }
        if (definition.programRevision() != revision) {
            throw new IllegalArgumentException("Lua mind revision must match catalog revision " + revision);
        }

        Entry registered = new Entry(owner, key, revision, source, definition);
        entries.put(key, registered);
        return registered.descriptor();
    }

    Entry require(NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        Entry entry = entries.get(key);
        if (entry == null) throw new IllegalArgumentException("Unknown EliteMobs mind program: " + key);
        return entry;
    }

    void unregisterOwner(Plugin owner) {
        Objects.requireNonNull(owner, "owner");
        Iterator<Entry> iterator = entries.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().owner() == owner) iterator.remove();
        }
        LuaMindModuleRegistry.OwnerScope moduleOwner = moduleOwners.remove(owner);
        if (moduleOwner != null) moduleOwner.close();
    }

    void clear() {
        entries.clear();
        for (LuaMindModuleRegistry.OwnerScope owner : new ArrayList<>(moduleOwners.values())) {
            owner.close();
        }
        moduleOwners.clear();
        moduleRegistry.close();
    }

    private LuaMindModuleRegistry.OwnerScope ownerScope(Plugin owner) {
        LuaMindModuleRegistry.OwnerScope current = moduleOwners.get(owner);
        if (current != null) return current;
        String namespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
        LuaMindModuleRegistry.OwnerScope opened = moduleRegistry.openOwner(namespace);
        moduleOwners.put(owner, opened);
        return opened;
    }

    private static void requireOwnerNamespace(Plugin owner, NamespacedKey key) {
        String ownerNamespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
        if (!ownerNamespace.equals(key.getNamespace())) {
            throw new IllegalArgumentException("Mind program namespace " + key.getNamespace()
                    + " does not belong to plugin " + owner.getName());
        }
    }

    private static EliteMindModule toPublicModule(LuaMindModuleDescriptor descriptor) {
        NamespacedKey key = NamespacedKey.fromString(descriptor.identifier());
        if (key == null) {
            throw new IllegalStateException(
                    "MagmaCore returned an invalid Mind module key: " + descriptor.identifier());
        }
        List<NamespacedKey> dependencies = descriptor.dependencies().stream()
                .map(identifier -> {
                    NamespacedKey dependency = NamespacedKey.fromString(identifier);
                    if (dependency == null) {
                        throw new IllegalStateException(
                                "MagmaCore returned an invalid Mind module dependency: " + identifier);
                    }
                    return dependency;
                })
                .toList();
        return new EliteMindModule(key, descriptor.revision(), dependencies);
    }

    record Entry(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source,
            LuaMindDefinition definition) implements EliteMindProgramEntry {

        Entry {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(definition, "definition");
        }

        @Override
        public EliteMindProgram descriptor() {
            return new EliteMindProgram(
                    key,
                    revision,
                    definition.resolvedModules().stream()
                            .map(EliteMindCatalog::toPublicModule)
                            .toList(),
                    definition.programFingerprint());
        }

        @Override
        public com.magmaguy.magmacore.ai.MindProgram instantiate() {
            return definition.instantiate();
        }
    }
}
