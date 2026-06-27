package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.magmacore.scripting.ScriptDefinition;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LuaPremadeCorpusTest {

    private static final String PREMADE_PACKAGE = "com.magmaguy.elitemobs.config.luapowers.premade";
    private static final Path PREMADE_SOURCE_ROOT = Path.of(
            "src", "main", "java",
            "com", "magmaguy", "elitemobs", "config", "luapowers", "premade");

    @TempDir
    Path tempDir;

    @TestFactory
    List<DynamicTest> validatesEveryPremadeLuaPower() throws Exception {
        try (Stream<Path> paths = Files.walk(PREMADE_SOURCE_ROOT)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(this::toClassName)
                    .map(this::loadClass)
                    .filter(clazz -> LuaPowersConfigFields.class.isAssignableFrom(clazz))
                    .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                    .map(clazz -> dynamicTest(clazz.getSimpleName(), () -> validatePremade(clazz)))
                    .toList();
        }
    }

    private String toClassName(Path path) {
        String relative = PREMADE_SOURCE_ROOT.relativize(path).toString()
                .replace('\\', '.')
                .replace('/', '.');
        return PREMADE_PACKAGE + "." + relative.substring(0, relative.length() - ".java".length());
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Could not load premade Lua power class " + className, exception);
        }
    }

    private void validatePremade(Class<?> clazz) throws Exception {
        LuaPowersConfigFields fields = (LuaPowersConfigFields) clazz.getConstructor().newInstance();
        String source = fields.getSource();
        assertFalse(source.isBlank(), fields.getFilename() + " has blank Lua source");
        ScriptDefinition.validate(fields.getFilename(), tempDir.resolve(fields.getFilename()).toFile(), source,
                new EliteMobsScriptProvider(tempDir));
    }
}
