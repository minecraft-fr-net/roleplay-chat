package net.minecraftfr.roleplaychat.test;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SupportMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class MessageTypePrefixTest implements CustomTestMethodInvoker {

    @GameTest
    public void testShoutPrefix(TestContext context) {
        ShoutMessage msg = new ShoutMessage("!Bonjour", MessageTypeSettings.shoutDefault());
        assertTrue(context, msg.canBeSend(), "ShoutMessage doit détecter le préfixe '!'");
        context.complete();
    }

    @GameTest
    public void testWhisperGuillemet(TestContext context) {
        WhisperMessage msg = new WhisperMessage("«Bonjour", MessageTypeSettings.whisperDefault());
        assertTrue(context, msg.canBeSend(), "WhisperMessage doit détecter le préfixe '«'");
        context.complete();
    }

    @GameTest
    public void testWhisperQuote(TestContext context) {
        WhisperMessage msg = new WhisperMessage("\"Bonjour", MessageTypeSettings.whisperDefault());
        assertTrue(context, msg.canBeSend(), "WhisperMessage doit détecter le préfixe '\"'");
        context.complete();
    }

    @GameTest
    public void testActionPrefix(TestContext context) {
        ActionMessage msg = new ActionMessage("*court vers la porte", MessageTypeSettings.actionDefault());
        assertTrue(context, msg.canBeSend(), "ActionMessage doit détecter le préfixe '*'");
        context.complete();
    }

    @GameTest
    public void testOocPrefix(TestContext context) {
        OOCMessage msg = new OOCMessage("(je reviens dans 5 min)", MessageTypeSettings.oocDefault());
        assertTrue(context, msg.canBeSend(), "OOCMessage doit détecter le préfixe '('");
        context.complete();
    }

    @GameTest
    public void testGlobalOocPrefix(TestContext context) {
        GlobalOOCMessage msg = new GlobalOOCMessage("[annonce serveur]", MessageTypeSettings.globalOocDefault());
        assertTrue(context, msg.canBeSend(), "GlobalOOCMessage doit détecter le préfixe '['");
        context.complete();
    }

    @GameTest
    public void testSupportPrefix(TestContext context) {
        SupportMessage msg = new SupportMessage("?aide svp", MessageTypeSettings.supportDefault());
        assertTrue(context, msg.canBeSend(), "SupportMessage doit détecter le préfixe '?'");
        context.complete();
    }

    @GameTest
    public void testSpeakNoPrefix(TestContext context) {
        // RoleplayChatConfig.instance est initialisé avec defaults() — pas besoin de load()
        SpeakMessage msg = new SpeakMessage("Bonjour tout le monde", MessageTypeSettings.speakDefault());
        assertTrue(context, msg.canBeSend(), "SpeakMessage doit s'activer pour un message sans préfixe");
        context.complete();
    }

    @GameTest
    public void testShoutNotDetectedAsSpeak(TestContext context) {
        SpeakMessage msg = new SpeakMessage("!Cri de guerre", MessageTypeSettings.speakDefault());
        assertFalse(context, msg.canBeSend(), "SpeakMessage ne doit pas s'activer pour un message commençant par '!'");
        context.complete();
    }

    @GameTest
    public void testNoPrefixNotDetectedAsShout(TestContext context) {
        ShoutMessage msg = new ShoutMessage("Bonjour", MessageTypeSettings.shoutDefault());
        assertFalse(context, msg.canBeSend(), "ShoutMessage ne doit pas s'activer sans préfixe '!'");
        context.complete();
    }

    @GameTest
    public void testRadiusDefaultValues(TestContext context) {
        assertEquals(context, 4,  MessageTypeSettings.whisperDefault().radius(),   "Whisper : rayon attendu 4");
        assertEquals(context, 25, MessageTypeSettings.actionDefault().radius(),    "Action  : rayon attendu 25");
        assertEquals(context, 30, MessageTypeSettings.speakDefault().radius(),     "Speak   : rayon attendu 30");
        assertEquals(context, 60, MessageTypeSettings.oocDefault().radius(),       "OOC     : rayon attendu 60");
        assertEquals(context, 80, MessageTypeSettings.shoutDefault().radius(),     "Shout   : rayon attendu 80");
        assertEquals(context, 0,  MessageTypeSettings.globalOocDefault().radius(), "GlobalOOC : rayon attendu 0 (illimité)");
        assertEquals(context, 0,  MessageTypeSettings.supportDefault().radius(),   "Support   : rayon attendu 0 (illimité)");
        context.complete();
    }

    @Override
    public void invokeTestMethod(TestContext context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }

    private static void assertTrue(TestContext context, boolean condition, String message) {
        if (!condition) context.throwGameTestException(Text.literal(message));
    }

    private static void assertFalse(TestContext context, boolean condition, String message) {
        if (condition) context.throwGameTestException(Text.literal(message));
    }

    private static void assertEquals(TestContext context, int expected, int actual, String message) {
        if (expected != actual) context.throwGameTestException(Text.literal(message + " — attendu " + expected + ", obtenu " + actual));
    }
}
