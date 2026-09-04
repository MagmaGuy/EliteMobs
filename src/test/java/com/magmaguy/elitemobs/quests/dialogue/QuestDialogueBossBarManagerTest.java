package com.magmaguy.elitemobs.quests.dialogue;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestDialogueBossBarManagerTest {

    @AfterEach
    void clearPendingQuests() {
        Quest.shutdown();
    }

    @Test
    void npcDialogueStartsWithConfiguredSpeechInsteadOfTheQuestName() {
        Player player = player("TestPlayer");
        CustomQuestsConfigFields questConfig = new CustomQuestsConfigFields("test_quest.yml", true);
        questConfig.setQuestName("QUEST_TITLE_SENTINEL");
        questConfig.setQuestAcceptDialog(List.of("The NPC's first line for $player."));
        CustomQuest quest = new CustomQuest(player, questConfig);

        List<String> dialogue = QuestDialogueBossBarManager.getQuestDialogueLines(quest, player, null);

        assertEquals(List.of("The NPC's first line for TestPlayer."), dialogue);
    }

    private static Player player(String name) {
        UUID uniqueId = UUID.randomUUID();
        Location location = new Location(null, 10, 20, 30);
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getUniqueId" -> uniqueId;
                    case "getName" -> name;
                    case "getLocation" -> location;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    case "toString" -> "TestPlayer[" + name + "]";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        throw new IllegalArgumentException("Unsupported primitive: " + type);
    }
}
