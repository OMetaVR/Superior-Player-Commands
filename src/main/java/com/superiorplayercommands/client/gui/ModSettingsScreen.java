package com.superiorplayercommands.client.gui;

import com.superiorplayercommands.alias.AliasManager;
import com.superiorplayercommands.bind.BindManager;
import com.superiorplayercommands.bind.KeyHelper;
import com.superiorplayercommands.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModSettingsScreen extends Screen {
    private final Screen parent;
    private int currentTab = 0;
    
    private static final String[] TAB_NAMES = {"Commands", "Aliases", "Binds"};
    private static final int ENTRY_HEIGHT = 22;
    private static final int CATEGORY_HEIGHT = 20;
    
    // Scroll state
    private int scrollOffset = 0;
    private int maxScroll = 0;
    
    // Commands tab
    private final List<CommandEntry> commandEntries = new ArrayList<>();
    private record CommandEntry(String name, String category, boolean isCategory) {}
    
    // Aliases tab
    private List<Map.Entry<String, String>> aliasList = new ArrayList<>();
    private TextFieldWidget aliasNameField;
    private TextFieldWidget aliasCommandField;
    private String selectedAlias = null;
    
    // Binds tab
    private List<Map.Entry<String, String>> bindList = new ArrayList<>();
    private TextFieldWidget bindCommandField;
    private boolean waitingForKey = false;
    private String pendingKey = null;
    private String selectedBind = null;
    
    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Superior Player Commands Settings"));
        this.parent = parent;
        buildCommandEntries();
    }
    
    private void buildCommandEntries() {
        commandEntries.clear();
        for (Map.Entry<String, String[]> category : ModConfig.COMMAND_CATEGORIES.entrySet()) {
            commandEntries.add(new CommandEntry(category.getKey(), category.getKey(), true));
            for (String cmd : category.getValue()) {
                commandEntries.add(new CommandEntry(cmd, category.getKey(), false));
            }
        }
    }
    
    private void refreshAliasList() {
        aliasList = new ArrayList<>(AliasManager.getAllAliases().entrySet());
    }
    
    private void refreshBindList() {
        bindList = new ArrayList<>(BindManager.getAllBindings().entrySet());
    }
    
    @Override
    protected void init() {
        // Tab buttons
        int tabWidth = 80;
        int startX = (this.width - (TAB_NAMES.length * tabWidth + (TAB_NAMES.length - 1) * 4)) / 2;
        
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIndex = i;
            ButtonWidget tabBtn = ButtonWidget.builder(
                Text.literal(TAB_NAMES[i]),
                button -> switchTab(tabIndex)
            ).dimensions(startX + i * (tabWidth + 4), 25, tabWidth, 20).build();
            tabBtn.active = (i != currentTab);
            this.addDrawableChild(tabBtn);
        }
        
        // Done button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.done"),
            button -> close()
        ).dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build());
        
        // Initialize tab-specific widgets
        initCurrentTab();
    }
    
    private void initCurrentTab() {
        scrollOffset = 0;
        
        switch (currentTab) {
            case 0 -> initCommandsTab();
            case 1 -> initAliasesTab();
            case 2 -> initBindsTab();
        }
        
        calculateMaxScroll();
    }
    
    private void initCommandsTab() {
        int centerX = this.width / 2;
        int buttonY = getContentTop() + 5;
        
        // Master toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Master: " + (ModConfig.isMasterEnabled() ? "§aON" : "§cOFF")),
            button -> {
                ModConfig.setMasterEnabled(!ModConfig.isMasterEnabled());
                button.setMessage(Text.literal("Master: " + (ModConfig.isMasterEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(centerX - 155, buttonY, 100, 20).build());
        
        // Enable all
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Enable All"),
            button -> ModConfig.enableAll()
        ).dimensions(centerX - 50, buttonY, 100, 20).build());
        
        // Disable all
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Disable All"),
            button -> ModConfig.disableAll()
        ).dimensions(centerX + 55, buttonY, 100, 20).build());
    }
    
    private void initAliasesTab() {
        refreshAliasList();
        int centerX = this.width / 2;
        int topY = getContentTop() + 5;
        
        // Alias name input
        aliasNameField = new TextFieldWidget(this.textRenderer, centerX - 155, topY, 80, 18, Text.literal("Alias"));
        aliasNameField.setPlaceholder(Text.literal("alias"));
        aliasNameField.setMaxLength(32);
        this.addDrawableChild(aliasNameField);
        
        // Command input
        aliasCommandField = new TextFieldWidget(this.textRenderer, centerX - 70, topY, 150, 18, Text.literal("Command"));
        aliasCommandField.setPlaceholder(Text.literal("command to run"));
        aliasCommandField.setMaxLength(256);
        this.addDrawableChild(aliasCommandField);
        
        // Add/Save button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Add"),
            button -> {
                String name = aliasNameField.getText().trim();
                String command = aliasCommandField.getText().trim();
                if (!name.isEmpty() && !command.isEmpty()) {
                    AliasManager.setAlias(name, command);
                    aliasNameField.setText("");
                    aliasCommandField.setText("");
                    selectedAlias = null;
                    refreshAliasList();
                    calculateMaxScroll();
                }
            }
        ).dimensions(centerX + 85, topY - 1, 40, 20).build());
        
        // Reset button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Reset"),
            button -> {
                AliasManager.resetToDefaults();
                refreshAliasList();
                calculateMaxScroll();
            }
        ).dimensions(centerX + 130, topY - 1, 45, 20).build());
    }
    
    private void initBindsTab() {
        refreshBindList();
        int centerX = this.width / 2;
        int topY = getContentTop() + 5;
        
        // Key bind button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(pendingKey != null ? pendingKey.toUpperCase() : "Click for Key"),
            button -> {
                waitingForKey = true;
                button.setMessage(Text.literal("§e> Press Key <"));
            }
        ).dimensions(centerX - 155, topY - 1, 80, 20).build());
        
        // Command input
        bindCommandField = new TextFieldWidget(this.textRenderer, centerX - 70, topY, 150, 18, Text.literal("Command"));
        bindCommandField.setPlaceholder(Text.literal("command to execute"));
        bindCommandField.setMaxLength(256);
        this.addDrawableChild(bindCommandField);
        
        // Bind button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Bind"),
            button -> {
                String command = bindCommandField.getText().trim();
                if (pendingKey != null && !command.isEmpty()) {
                    BindManager.setBind(pendingKey, command);
                    pendingKey = null;
                    bindCommandField.setText("");
                    selectedBind = null;
                    refreshBindList();
                    calculateMaxScroll();
                    clearAndReinit();
                }
            }
        ).dimensions(centerX + 85, topY - 1, 45, 20).build());
        
        // Clear all button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Clear"),
            button -> {
                for (Map.Entry<String, String> entry : new ArrayList<>(bindList)) {
                    BindManager.removeBind(entry.getKey());
                }
                refreshBindList();
                calculateMaxScroll();
            }
        ).dimensions(centerX + 135, topY - 1, 40, 20).build());
    }
    
    private void calculateMaxScroll() {
        int totalHeight = 0;
        int visibleHeight = getContentBottom() - getListTop() - 5;
        
        switch (currentTab) {
            case 0 -> {
                for (CommandEntry entry : commandEntries) {
                    totalHeight += entry.isCategory ? CATEGORY_HEIGHT : ENTRY_HEIGHT;
                }
            }
            case 1 -> totalHeight = aliasList.size() * ENTRY_HEIGHT;
            case 2 -> totalHeight = bindList.size() * ENTRY_HEIGHT;
        }
        
        maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }
    
    private void switchTab(int tab) {
        if (tab == currentTab) return;
        this.currentTab = tab;
        this.clearAndReinit();
    }
    
    private void clearAndReinit() {
        this.clearChildren();
        this.init();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        
        // Content background
        context.fill(20, 50, this.width - 20, this.height - 35, 0x80000000);
        
        // Render tab content
        switch (currentTab) {
            case 0 -> renderCommandsTab(context, mouseX, mouseY);
            case 1 -> renderAliasesTab(context, mouseX, mouseY);
            case 2 -> renderBindsTab(context, mouseX, mouseY);
        }
        
        super.render(context, mouseX, mouseY, delta);
        
        // Render waiting for key overlay on top
        if (waitingForKey) {
            context.fill(0, 0, this.width, this.height, 0xC0000000);
            context.drawCenteredTextWithShadow(this.textRenderer, "§ePress any key to bind...", 
                this.width / 2, this.height / 2 - 10, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, "§7(Press ESC to cancel)", 
                this.width / 2, this.height / 2 + 5, 0xAAAAAA);
        }
    }
    
    private void renderCommandsTab(DrawContext context, int mouseX, int mouseY) {
        int listTop = getListTop();
        int listBottom = getContentBottom() - 5;
        int listLeft = getContentLeft() + 5;
        int listRight = getContentRight() - 5;
        
        context.enableScissor(listLeft, listTop, listRight, listBottom);
        
        int y = listTop - scrollOffset;
        
        for (CommandEntry entry : commandEntries) {
            if (entry.isCategory) {
                if (y + CATEGORY_HEIGHT > listTop && y < listBottom) {
                    boolean hovered = mouseX >= listLeft && mouseX <= listRight && 
                                      mouseY >= y && mouseY < y + CATEGORY_HEIGHT - 2;
                    int bgColor = hovered ? 0x80505050 : 0x60404040;
                    context.fill(listLeft, y, listRight, y + CATEGORY_HEIGHT - 2, bgColor);
                    context.drawTextWithShadow(this.textRenderer, "§e§l" + entry.name + (hovered ? " §7(click to toggle)" : ""), 
                        listLeft + 5, y + 5, 0xFFFFFF);
                }
                y += CATEGORY_HEIGHT;
            } else {
                if (y + ENTRY_HEIGHT > listTop && y < listBottom) {
                    boolean enabled = ModConfig.isCommandEnabled(entry.name);
                    boolean hovered = mouseX >= listLeft + 10 && mouseX <= listRight - 10 && 
                                      mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2;
                    int bgColor = enabled ? (hovered ? 0x50009000 : 0x40008000) : (hovered ? 0x50900000 : 0x40800000);
                    
                    context.fill(listLeft + 10, y, listRight - 10, y + ENTRY_HEIGHT - 2, bgColor);
                    context.drawTextWithShadow(this.textRenderer, "/" + entry.name, listLeft + 15, y + 7, 0xFFFFFF);
                    
                    String status = enabled ? "§a[ON]" : "§c[OFF]";
                    int statusWidth = this.textRenderer.getWidth(status);
                    context.drawTextWithShadow(this.textRenderer, status, listRight - 20 - statusWidth, y + 7, 0xFFFFFF);
                }
                y += ENTRY_HEIGHT;
            }
        }
        
        context.disableScissor();
        renderScrollbar(context, listTop, listBottom, listRight);
    }
    
    private void renderAliasesTab(DrawContext context, int mouseX, int mouseY) {
        int listTop = getListTop();
        int listBottom = getContentBottom() - 5;
        int listLeft = getContentLeft() + 5;
        int listRight = getContentRight() - 5;
        
        context.drawTextWithShadow(this.textRenderer, "§7Aliases: type shortcut → runs full command", 
            listLeft, listTop - 14, 0xAAAAAA);
        
        context.enableScissor(listLeft, listTop, listRight, listBottom);
        
        int y = listTop - scrollOffset;
        
        for (Map.Entry<String, String> entry : aliasList) {
            if (y + ENTRY_HEIGHT > listTop && y < listBottom) {
                boolean hovered = mouseX >= listLeft && mouseX <= listRight - 22 && 
                                  mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2;
                boolean selected = entry.getKey().equals(selectedAlias);
                
                int bgColor = selected ? 0x60606080 : (hovered ? 0x40404060 : 0x30303030);
                context.fill(listLeft, y, listRight - 22, y + ENTRY_HEIGHT - 2, bgColor);
                
                // Alias name (clickable area)
                int aliasWidth = this.textRenderer.getWidth("/" + entry.getKey());
                boolean aliasHovered = mouseX >= listLeft + 5 && mouseX <= listLeft + 5 + aliasWidth &&
                                       mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2;
                context.drawTextWithShadow(this.textRenderer, (aliasHovered ? "§b§n" : "§b") + "/" + entry.getKey(), 
                    listLeft + 5, y + 6, 0xFFFFFF);
                
                context.drawTextWithShadow(this.textRenderer, "§7→", listLeft + 80, y + 6, 0xFFFFFF);
                
                // Command (clickable area)
                String cmd = entry.getValue();
                String displayCmd = cmd.length() > 35 ? cmd.substring(0, 32) + "..." : cmd;
                boolean cmdHovered = mouseX >= listLeft + 95 && mouseX <= listRight - 25 &&
                                     mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2;
                context.drawTextWithShadow(this.textRenderer, (cmdHovered ? "§f§n" : "§f") + "/" + displayCmd, 
                    listLeft + 95, y + 6, 0xFFFFFF);
                
                // Delete button
                boolean deleteHovered = mouseX >= listRight - 20 && mouseX <= listRight - 4 && 
                                        mouseY >= y + 2 && mouseY < y + ENTRY_HEIGHT - 4;
                int deleteBg = deleteHovered ? 0xFFAA0000 : 0xFF660000;
                context.fill(listRight - 20, y + 2, listRight - 4, y + ENTRY_HEIGHT - 4, deleteBg);
                context.drawCenteredTextWithShadow(this.textRenderer, "§cX", listRight - 12, y + 5, 0xFFFFFF);
            }
            y += ENTRY_HEIGHT;
        }
        
        context.disableScissor();
        
        if (aliasList.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§7No aliases defined. Add one above!", 
                this.width / 2, listTop + 30, 0x888888);
        }
        
        renderScrollbar(context, listTop, listBottom, listRight);
    }
    
    private void renderBindsTab(DrawContext context, int mouseX, int mouseY) {
        int listTop = getListTop();
        int listBottom = getContentBottom() - 5;
        int listLeft = getContentLeft() + 5;
        int listRight = getContentRight() - 5;
        
        context.drawTextWithShadow(this.textRenderer, "§7Key binds: press key in-game → runs command", 
            listLeft, listTop - 14, 0xAAAAAA);
        
        context.enableScissor(listLeft, listTop, listRight, listBottom);
        
        int y = listTop - scrollOffset;
        
        for (Map.Entry<String, String> entry : bindList) {
            if (y + ENTRY_HEIGHT > listTop && y < listBottom) {
                boolean hovered = mouseX >= listLeft && mouseX <= listRight - 22 && 
                                  mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2;
                boolean selected = entry.getKey().equals(selectedBind);
                
                int bgColor = selected ? 0x60606080 : (hovered ? 0x40404060 : 0x30303030);
                context.fill(listLeft, y, listRight - 22, y + ENTRY_HEIGHT - 2, bgColor);
                
                // Key name
                context.drawTextWithShadow(this.textRenderer, "§d[" + entry.getKey().toUpperCase() + "]", 
                    listLeft + 5, y + 6, 0xFFFFFF);
                
                context.drawTextWithShadow(this.textRenderer, "§7→", listLeft + 70, y + 6, 0xFFFFFF);
                
                // Command
                String cmd = entry.getValue();
                String displayCmd = cmd.length() > 40 ? cmd.substring(0, 37) + "..." : cmd;
                context.drawTextWithShadow(this.textRenderer, "§f/" + displayCmd, listLeft + 85, y + 6, 0xFFFFFF);
                
                // Delete button
                boolean deleteHovered = mouseX >= listRight - 20 && mouseX <= listRight - 4 && 
                                        mouseY >= y + 2 && mouseY < y + ENTRY_HEIGHT - 4;
                int deleteBg = deleteHovered ? 0xFFAA0000 : 0xFF660000;
                context.fill(listRight - 20, y + 2, listRight - 4, y + ENTRY_HEIGHT - 4, deleteBg);
                context.drawCenteredTextWithShadow(this.textRenderer, "§cX", listRight - 12, y + 5, 0xFFFFFF);
            }
            y += ENTRY_HEIGHT;
        }
        
        context.disableScissor();
        
        if (bindList.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§7No key bindings. Add one above!", 
                this.width / 2, listTop + 30, 0x888888);
        }
        
        renderScrollbar(context, listTop, listBottom, listRight);
    }
    
    private void renderScrollbar(DrawContext context, int listTop, int listBottom, int listRight) {
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, (listBottom - listTop) * (listBottom - listTop) / (maxScroll + listBottom - listTop));
            int scrollBarY = listTop + (int)((listBottom - listTop - scrollBarHeight) * ((float)scrollOffset / maxScroll));
            context.fill(listRight - 3, scrollBarY, listRight, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (waitingForKey) {
            String keyName = "mouse" + button;
            pendingKey = keyName;
            waitingForKey = false;
            clearAndReinit();
            return true;
        }
        
        int listTop = getListTop();
        int listBottom = getContentBottom() - 5;
        int listLeft = getContentLeft() + 5;
        int listRight = getContentRight() - 5;
        
        if (mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            switch (currentTab) {
                case 0 -> {
                    return handleCommandsClick(mouseX, mouseY, listLeft, listRight, listTop);
                }
                case 1 -> {
                    return handleAliasesClick(mouseX, mouseY, listLeft, listRight, listTop);
                }
                case 2 -> {
                    return handleBindsClick(mouseX, mouseY, listLeft, listRight, listTop);
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean handleCommandsClick(double mouseX, double mouseY, int listLeft, int listRight, int listTop) {
        int y = listTop - scrollOffset;
        
        for (CommandEntry entry : commandEntries) {
            int entryHeight = entry.isCategory ? CATEGORY_HEIGHT : ENTRY_HEIGHT;
            
            if (mouseY >= y && mouseY < y + entryHeight - 2) {
                if (entry.isCategory) {
                    // Toggle all commands in this category
                    toggleCategory(entry.category);
                    return true;
                } else if (mouseX >= listLeft + 10 && mouseX <= listRight - 10) {
                    boolean current = ModConfig.isCommandEnabled(entry.name);
                    ModConfig.setCommandEnabled(entry.name, !current);
                    return true;
                }
            }
            y += entryHeight;
        }
        return false;
    }
    
    private void toggleCategory(String category) {
        String[] commands = ModConfig.COMMAND_CATEGORIES.get(category);
        if (commands == null) return;
        
        // Check if any are enabled
        boolean anyEnabled = false;
        for (String cmd : commands) {
            if (ModConfig.getAllToggles().getOrDefault(cmd, true)) {
                anyEnabled = true;
                break;
            }
        }
        
        // If any enabled, disable all. Otherwise enable all.
        for (String cmd : commands) {
            ModConfig.setCommandEnabled(cmd, !anyEnabled);
        }
    }
    
    private boolean handleAliasesClick(double mouseX, double mouseY, int listLeft, int listRight, int listTop) {
        int y = listTop - scrollOffset;
        
        for (Map.Entry<String, String> entry : aliasList) {
            if (mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2) {
                // Delete button
                if (mouseX >= listRight - 20 && mouseX <= listRight - 4) {
                    AliasManager.removeAlias(entry.getKey());
                    refreshAliasList();
                    calculateMaxScroll();
                    return true;
                }
                
                // Click on alias name - edit it
                int aliasWidth = this.textRenderer.getWidth("/" + entry.getKey());
                if (mouseX >= listLeft + 5 && mouseX <= listLeft + 5 + aliasWidth) {
                    selectedAlias = entry.getKey();
                    aliasNameField.setText(entry.getKey());
                    aliasCommandField.setText(entry.getValue());
                    aliasNameField.setFocused(true);
                    return true;
                }
                
                // Click on command - edit it
                if (mouseX >= listLeft + 95 && mouseX <= listRight - 25) {
                    selectedAlias = entry.getKey();
                    aliasNameField.setText(entry.getKey());
                    aliasCommandField.setText(entry.getValue());
                    aliasCommandField.setFocused(true);
                    return true;
                }
            }
            y += ENTRY_HEIGHT;
        }
        return false;
    }
    
    private boolean handleBindsClick(double mouseX, double mouseY, int listLeft, int listRight, int listTop) {
        int y = listTop - scrollOffset;
        
        for (Map.Entry<String, String> entry : bindList) {
            if (mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2) {
                // Delete button
                if (mouseX >= listRight - 20 && mouseX <= listRight - 4) {
                    BindManager.removeBind(entry.getKey());
                    refreshBindList();
                    calculateMaxScroll();
                    return true;
                }
                
                // Click anywhere else - load into editor
                selectedBind = entry.getKey();
                pendingKey = entry.getKey();
                bindCommandField.setText(entry.getValue());
                clearAndReinit();
                return true;
            }
            y += ENTRY_HEIGHT;
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!waitingForKey) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(amount * 20)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKey = false;
                return true;
            }
            
            String keyName = KeyHelper.getKeyName(keyCode);
            if (keyName != null && !keyName.equals("unknown")) {
                pendingKey = keyName;
                waitingForKey = false;
                clearAndReinit();
            }
            return true;
        }
        
        // Tab between fields
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            if (currentTab == 1 && aliasNameField != null && aliasCommandField != null) {
                if (aliasNameField.isFocused()) {
                    aliasNameField.setFocused(false);
                    aliasCommandField.setFocused(true);
                    return true;
                } else if (aliasCommandField.isFocused()) {
                    aliasCommandField.setFocused(false);
                    aliasNameField.setFocused(true);
                    return true;
                }
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
    
    private int getContentTop() { return 55; }
    private int getContentBottom() { return this.height - 40; }
    private int getContentLeft() { return 25; }
    private int getContentRight() { return this.width - 25; }
    private int getListTop() { return getContentTop() + 35; }
}
