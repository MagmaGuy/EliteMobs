package com.magmaguy.elitemobs.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ResourcePackAtlasTest {

    private static final Path RESOURCE_ROOT = Path.of("src", "main", "resources", "em_rsp_defaults", "assets");
    private static final Path BLOCKS_ATLAS = RESOURCE_ROOT.resolve(Path.of("minecraft", "atlases", "blocks.json"));
    private static final Path ENTITY_TEXTURES = RESOURCE_ROOT.resolve(Path.of("elitemobs", "textures", "entity"));

    @Test
    void entityAtlasUsesOnlyPackOwnedEliteMobsTextures() throws IOException {
        JsonArray sources;
        try (Reader reader = Files.newBufferedReader(BLOCKS_ATLAS, UTF_8)) {
            sources = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("sources");
        }

        Set<String> actualEntityTextures = new TreeSet<>();
        for (var sourceElement : sources) {
            JsonObject source = sourceElement.getAsJsonObject();
            String type = source.get("type").getAsString();
            assertFalse(type.equals("directory") && source.get("source").getAsString().equals("entity"),
                    "A broad entity directory source also collects vanilla entity textures");
            if (!type.equals("single") || !source.has("resource")) continue;

            String resource = source.get("resource").getAsString();
            if (!resource.startsWith("elitemobs:entity/")) continue;

            assertFalse(source.has("sprite"), "EliteMobs entity textures must retain their resource ID");
            actualEntityTextures.add(resource);
        }

        Set<String> expectedEntityTextures = new TreeSet<>();
        try (Stream<Path> paths = Files.walk(ENTITY_TEXTURES)) {
            paths.filter(Files::isRegularFile)
                    .map(ENTITY_TEXTURES::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    .filter(path -> path.endsWith(".png"))
                    .map(path -> "elitemobs:entity/" + path.substring(0, path.length() - ".png".length()))
                    .forEach(expectedEntityTextures::add);
        }

        assertEquals(expectedEntityTextures, actualEntityTextures,
                "Every pack-owned EliteMobs entity texture needs one exact atlas source");
    }
}
