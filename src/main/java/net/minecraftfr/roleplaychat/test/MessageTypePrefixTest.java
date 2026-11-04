package net.minecraftfr.roleplaychat.test;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.RollMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SupportMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class MessageTypePrefixTest implements CustomTestMethodInvoker {

    @GameTest
    public void testShoutPrefix(GameTestHelper context) {
        ShoutMessage msg = new ShoutMessage("!Bonjour", MessageTypeSettings.shoutDefault());
        assertTrue(context, msg.canBeSend(), "ShoutMessage doit détecter le préfixe '!'");
        context.succeed();
    }

    @GameTest
    public void testWhisperGuillemet(GameTestHelper context) {
        WhisperMessage msg = new WhisperMessage("«Bonjour", MessageTypeSettings.whisperDefault());
        assertTrue(context, msg.canBeSend(), "WhisperMessage doit détecter le préfixe '«'");
        context.succeed();
    }

    @GameTest
    public void testWhisperQuote(GameTestHelper context) {
        WhisperMessage msg = new WhisperMessage("\"Bonjour", MessageTypeSettings.whisperDefault());
        assertTrue(context, msg.canBeSend(), "WhisperMessage doit détecter le préfixe '\"'");
        context.succeed();
    }

    @GameTest
    public void testActionPrefix(GameTestHelper context) {
        ActionMessage msg = new ActionMessage("*court vers la porte", MessageTypeSettings.actionDefault());
        assertTrue(context, msg.canBeSend(), "ActionMessage doit détecter le préfixe '*'");
        context.succeed();
    }

    @GameTest
    public void testOocPrefix(GameTestHelper context) {
        OOCMessage msg = new OOCMessage("(je reviens dans 5 min)", MessageTypeSettings.oocDefault());
        assertTrue(context, msg.canBeSend(), "OOCMessage doit détecter le préfixe '('");
        context.succeed();
    }

    @GameTest
    public void testGlobalOocPrefix(GameTestHelper context) {
        GlobalOOCMessage msg = new GlobalOOCMessage("[annonce serveur]", MessageTypeSettings.globalOocDefault());
        assertTrue(context, msg.canBeSend(), "GlobalOOCMessage doit détecter le préfixe '['");
        context.succeed();
    }

    @GameTest
    public void testSupportPrefix(GameTestHelper context) {
        SupportMessage msg = new SupportMessage("?aide svp", MessageTypeSettings.supportDefault());
        assertTrue(context, msg.canBeSend(), "SupportMessage doit détecter le préfixe '?'");
        context.succeed();
    }

    @GameTest
    public void testSpeakNoPrefix(GameTestHelper context) {
        // RoleplayChatConfig.instance est initialisé avec defaults() — pas besoin de load()
        SpeakMessage msg = new SpeakMessage("Bonjour tout le monde", MessageTypeSettings.speakDefault());
        assertTrue(context, msg.canBeSend(), "SpeakMessage doit s'activer pour un message sans préfixe");
        context.succeed();
    }

    @GameTest
    public void testShoutNotDetectedAsSpeak(GameTestHelper context) {
        SpeakMessage msg = new SpeakMessage("!Cri de guerre", MessageTypeSettings.speakDefault());
        assertFalse(context, msg.canBeSend(), "SpeakMessage ne doit pas s'activer pour un message commençant par '!'");
        context.succeed();
    }

    @GameTest
    public void testNoPrefixNotDetectedAsShout(GameTestHelper context) {
        ShoutMessage msg = new ShoutMessage("Bonjour", MessageTypeSettings.shoutDefault());
        assertFalse(context, msg.canBeSend(), "ShoutMessage ne doit pas s'activer sans préfixe '!'");
        context.succeed();
    }

    @GameTest
    public void testSpeakRadiusFiltering(GameTestHelper context) {
        int radius = MessageTypeSettings.speakDefault().radius(); // 30
        int distanceNear = 25;
        int distanceFar  = 35;
        assertTrue(context,  distanceNear <= radius,
            "Distance " + distanceNear + " doit être reçue dans le rayon speak (" + radius + ")");
        assertFalse(context, distanceFar  <= radius,
            "Distance " + distanceFar + " doit être filtrée par le rayon speak (" + radius + ")");
        context.succeed();
    }

    @GameTest
    public void testRadiusDefaultValues(GameTestHelper context) {
        assertEquals(context, 4,  MessageTypeSettings.whisperDefault().radius(),   "Whisper : rayon attendu 4");
        assertEquals(context, 25, MessageTypeSettings.actionDefault().radius(),    "Action  : rayon attendu 25");
        assertEquals(context, 30, MessageTypeSettings.speakDefault().radius(),     "Speak   : rayon attendu 30");
        assertEquals(context, 60, MessageTypeSettings.oocDefault().radius(),       "OOC     : rayon attendu 60");
        assertEquals(context, 80, MessageTypeSettings.shoutDefault().radius(),     "Shout   : rayon attendu 80");
        assertEquals(context, 0,  MessageTypeSettings.globalOocDefault().radius(), "GlobalOOC : rayon attendu 0 (illimité)");
        assertEquals(context, 0,  MessageTypeSettings.supportDefault().radius(),   "Support   : rayon attendu 0 (illimité)");
        assertEquals(context, 25, MessageTypeSettings.rollDefault().radius(),      "Roll      : rayon attendu 25");
        context.succeed();
    }

    @GameTest
    public void testRollCannotBeSentFromChat(GameTestHelper context) {
        RollMessage msg = new RollMessage(20, 0, MessageTypeSettings.rollDefault());
        assertFalse(context, msg.canBeSend(), "RollMessage ne doit pas s'activer depuis le chat (commande uniquement)");
        context.succeed();
    }

    @GameTest
    public void testRollDefaultRadius(GameTestHelper context) {
        assertEquals(context, 25, MessageTypeSettings.rollDefault().radius(), "Roll : rayon par défaut attendu 25");
        context.succeed();
    }

    @GameTest
    public void testRollResultInRange(GameTestHelper context) {
        for (int i = 0; i < 100; i++) {
            RollMessage msg = new RollMessage(20, 0, MessageTypeSettings.rollDefault());
            int roll = msg.getRoll();
            assertTrue(context, roll >= 1 && roll <= 20,
                "Résultat du d20 doit être entre 1 et 20, obtenu : " + roll);
        }
        context.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }

    private static void assertTrue(GameTestHelper context, boolean condition, String message) {
        if (!condition) context.fail(Component.literal(message));
    }

    private static void assertFalse(GameTestHelper context, boolean condition, String message) {
        if (condition) context.fail(Component.literal(message));
    }

    private static void assertEquals(GameTestHelper context, int expected, int actual, String message) {
        if (expected != actual) context.fail(Component.literal(message + " — attendu " + expected + ", obtenu " + actual));
    }
}
