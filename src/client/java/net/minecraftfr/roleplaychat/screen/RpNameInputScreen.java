package net.minecraftfr.roleplaychat.screen;

import org.jetbrains.annotations.Nullable;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.RoleplayChatClient;
import net.minecraftfr.roleplaychat.nameplate.RpNameInputPayload;

public class RpNameInputScreen extends Screen {

  private static final int MAX_NAME_LENGTH = 32;
  private static final int FIELD_WIDTH = 200;
  private static final int FIELD_HEIGHT = 20;
  private static final int BUTTON_WIDTH = 100;
  private static final int BUTTON_HEIGHT = 20;

  /** Texte d'erreur à afficher, null si aucune erreur. */
  private final @Nullable Text errorText;
  private TextFieldWidget nameField;
  private ButtonWidget confirmButton;

  public RpNameInputScreen(String errorKey, String errorArg) {
    super(Text.translatable("screen.roleplay-chat.rp_name_input.title"));
    if (errorKey.isEmpty()) {
      this.errorText = null;
    } else if (errorArg.isEmpty()) {
      this.errorText = Text.translatable(errorKey);
    } else {
      this.errorText = Text.translatable(errorKey, errorArg);
    }
  }

  /** TextWidget ne gère plus l'alignement depuis 1.21.11 : on le dimensionne au texte et on le centre. */
  private TextWidget centeredText(Text text, int color, int centerX, int y) {
    Text colored = text.copy().withColor(color);
    int width = this.textRenderer.getWidth(colored);
    return new TextWidget(centerX - width / 2, y, width, 9, colored, this.textRenderer);
  }

  @Override
  protected void init() {
    int centerX = this.width / 2;
    int centerY = this.height / 2;

    // Titre centré
    this.addDrawableChild(centeredText(this.title, 0xFFFFFF, centerX, centerY - 40));

    // Label "Pseudo RP" / "RP Name"
    this.addDrawableChild(new TextWidget(
        centerX - FIELD_WIDTH / 2, centerY - 13,
        FIELD_WIDTH, 9,
        Text.translatable("screen.roleplay-chat.rp_name_input.label").copy().withColor(0xAAAAAA),
        this.textRenderer
    ));

    // Message d'erreur éventuel
    if (errorText != null) {
      this.addDrawableChild(centeredText(
          errorText, 0xFF5555, centerX, centerY + FIELD_HEIGHT + 8 + BUTTON_HEIGHT + 6));
    }

    // Champ de saisie
    nameField = new TextFieldWidget(
        this.textRenderer,
        centerX - FIELD_WIDTH / 2,
        centerY,
        FIELD_WIDTH,
        FIELD_HEIGHT,
        Text.empty()
    );
    nameField.setMaxLength(MAX_NAME_LENGTH);
    nameField.setChangedListener(text -> confirmButton.active = !text.trim().isEmpty());
    this.addDrawableChild(nameField);
    this.setInitialFocus(nameField);

    // Bouton Confirmer
    confirmButton = ButtonWidget.builder(
        Text.translatable("screen.roleplay-chat.rp_name_input.confirm"),
        button -> submit()
    )
        .dimensions(centerX - BUTTON_WIDTH / 2, centerY + FIELD_HEIGHT + 8, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();
    confirmButton.active = false;
    this.addDrawableChild(confirmButton);
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    int centerX = this.width / 2;
    int centerY = this.height / 2;

    // Panneau sombre derrière le formulaire
    int padH = 20;
    int padV = 16;
    int panelTop    = centerY - 40 - padV;
    int panelBottom = errorText == null
        ? centerY + FIELD_HEIGHT + 8 + BUTTON_HEIGHT + padV
        : centerY + FIELD_HEIGHT + 8 + BUTTON_HEIGHT + 6 + 9 + padV + 8;
    int panelLeft   = centerX - FIELD_WIDTH / 2 - padH;
    int panelRight  = centerX + FIELD_WIDTH / 2 + padH;
    context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xC0000000);

    super.render(context, mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
    int keyCode = input.key();
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      if (this.client != null) this.client.setScreen(new GameMenuScreen(true));
      return true;
    }
    if (keyCode == GLFW.GLFW_KEY_ENTER && confirmButton.active) {
      submit();
      return true;
    }
    return super.keyPressed(input);
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  private void submit() {
    String name = nameField.getText().trim();
    if (name.isEmpty()) return;
    RoleplayChatClient.clearNeedsRpName();
    ClientPlayNetworking.send(new RpNameInputPayload(name));
    this.close();
  }
}
