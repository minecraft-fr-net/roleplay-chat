package net.minecraftfr.roleplaychat.screen;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
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

  @Override
  protected void init() {
    int centerX = this.width / 2;
    int centerY = this.height / 2;

    // Titre centré
    TextWidget titleWidget = new TextWidget(
        centerX - FIELD_WIDTH / 2, centerY - 40,
        FIELD_WIDTH, 9,
        this.title, this.textRenderer
    );
    titleWidget.alignCenter();
    titleWidget.setTextColor(0xFFFFFF);
    this.addDrawableChild(titleWidget);

    // Label "Pseudo RP" / "RP Name"
    TextWidget labelWidget = new TextWidget(
        centerX - FIELD_WIDTH / 2, centerY - 13,
        FIELD_WIDTH, 9,
        Text.translatable("screen.roleplay-chat.rp_name_input.label"),
        this.textRenderer
    );
    labelWidget.alignLeft();
    labelWidget.setTextColor(0xAAAAAA);
    this.addDrawableChild(labelWidget);

    // Message d'erreur éventuel
    if (errorText != null) {
      TextWidget errorWidget = new TextWidget(
          centerX - FIELD_WIDTH / 2, centerY + FIELD_HEIGHT + 8 + BUTTON_HEIGHT + 6,
          FIELD_WIDTH, 9,
          errorText, this.textRenderer
      );
      errorWidget.alignCenter();
      errorWidget.setTextColor(0xFF5555);
      this.addDrawableChild(errorWidget);
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
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == 257 && confirmButton.active) {
      submit();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return true;
  }

  private void submit() {
    String name = nameField.getText().trim();
    if (name.isEmpty()) return;
    ClientPlayNetworking.send(new RpNameInputPayload(name));
    this.close();
  }
}
