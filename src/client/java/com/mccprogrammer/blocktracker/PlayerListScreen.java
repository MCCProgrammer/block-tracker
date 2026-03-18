package com.mccprogrammer.blocktracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.Collection;

public class PlayerListScreen extends Screen {

    private double scrollAmount = 0;
    private int totalContentHeight = 0;

    public PlayerListScreen() {
        super(Text.literal("Menu de Jogadores"));
    }

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null)
            return;

        ModState state = ModState.getInstance();
        Collection<net.minecraft.client.network.AbstractClientPlayerEntity> players = client.world.getPlayers();

        int y = 0;
        int centerX = this.width / 2;

        for (net.minecraft.client.network.AbstractClientPlayerEntity player : players) {
            String name = player.getName().getString();

            if (name == null || !isValidName(name) || name.equals(client.player.getName().getString()))
                continue;

            String relation = state.playerRelations.getOrDefault(name, "neutral");
            String statusText = "neutral".equals(relation) ? "Neutro"
                    : ("friend".equals(relation) ? "Amigo" : "Inimigo");

            // Status Toggle Button
            ButtonWidget btn = ButtonWidget.builder(
                    Text.literal(statusText).styled(style -> style.withColor(finalColor(relation))),
                    button -> {
                        String current = state.playerRelations.getOrDefault(name, "neutral");
                        String next = "neutral".equals(current) ? "friend"
                                : ("friend".equals(current) ? "enemy" : "neutral");

                        if ("neutral".equals(next)) {
                            state.playerRelations.remove(name);
                        } else {
                            state.playerRelations.put(name, next);
                        }
                        state.save();
                        this.clearAndInit();
                    })
                    .dimensions(centerX + 20, y, 80, 20)
                    .build();

            this.addDrawableChild(btn);

            y += 25;
        }

        totalContentHeight = y;

        // Botão Voltar (Fixed at bottom)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Voltar"),
                button -> this.close())
                .dimensions(centerX - 50, this.height - 30, 100, 20)
                .build());
    }

    private int finalColor(String relation) {
        if ("friend".equals(relation))
            return 0x00FF00;
        if ("enemy".equals(relation))
            return 0xFF0000;
        return 0xFFFFFF;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int visibleHeight = this.height - 80;
        if (totalContentHeight > visibleHeight) {
            scrollAmount = Math.max(0,
                    Math.min(scrollAmount - verticalAmount * 15, totalContentHeight - visibleHeight));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        try {
            context.fill(0, 0, this.width, this.height, 0x90000000);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFFFF);

            int listTop = 40;
            int listBottom = this.height - 40;
            int centerX = this.width / 2;

            // Scissor area for the list
            context.enableScissor(0, listTop, this.width, listBottom - listTop);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {
                int yPos = listTop - (int) scrollAmount;
                int btnIndex = 0;

                for (net.minecraft.client.network.AbstractClientPlayerEntity player : client.world.getPlayers()) {
                    String name = player.getName().getString();
                    if (name == null || !isValidName(name) || name.equals(client.player.getName().getString()))
                        continue;

                    // Draw Name
                    context.drawText(this.textRenderer, name, centerX - 100, yPos + 5, 0xFFFFFFFF, true);

                    // Update Button Position
                    if (btnIndex < this.children().size()) {
                        Object child = this.children().get(btnIndex);
                        if (child instanceof net.minecraft.client.gui.widget.ClickableWidget btn
                                && btn.getWidth() == 80) {
                            btn.setY(yPos);
                            // Only show/enable if within scissor bounds (for interaction)
                            btn.visible = (yPos >= listTop - 20 && yPos <= listBottom);
                            btn.active = btn.visible;
                        }
                    }

                    yPos += 25;
                    btnIndex++;
                }
            }

            // Render the buttons (this will respect scissoring for visuals)
            super.render(context, mouseX, mouseY, delta);

            context.disableScissor();

            // Ensure "Voltar" is always visible and above scissor
            for (net.minecraft.client.gui.Element child : this.children()) {
                if (child instanceof net.minecraft.client.gui.widget.ClickableWidget btn && btn.getWidth() == 100) {
                    btn.setY(this.height - 30);
                    btn.visible = true;
                    btn.active = true;
                }
            }

        } catch (Exception e) {
            // Safe exit
        }
    }

    private boolean isValidName(String name) {
        if (name == null)
            return false;
        String stripped = name.replaceAll("§.", "");
        return stripped.matches(".*[a-zA-Z0-9].*");
    }
}
