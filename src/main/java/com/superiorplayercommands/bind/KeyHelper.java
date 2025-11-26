package com.superiorplayercommands.bind;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class KeyHelper {
    private static final Map<String, Integer> NAME_TO_KEY = new HashMap<>();
    private static final Map<Integer, String> KEY_TO_NAME = new HashMap<>();
    
    static {
        for (char c = 'a'; c <= 'z'; c++) {
            int keyCode = GLFW.GLFW_KEY_A + (c - 'a');
            register(String.valueOf(c), keyCode);
        }
        
        for (int i = 0; i <= 9; i++) {
            register(String.valueOf(i), GLFW.GLFW_KEY_0 + i);
        }
        
        for (int i = 1; i <= 12; i++) {
            register("f" + i, GLFW.GLFW_KEY_F1 + (i - 1));
        }
        
        register("space", GLFW.GLFW_KEY_SPACE);
        register("tab", GLFW.GLFW_KEY_TAB);
        register("enter", GLFW.GLFW_KEY_ENTER);
        register("escape", GLFW.GLFW_KEY_ESCAPE);
        register("backspace", GLFW.GLFW_KEY_BACKSPACE);
        register("delete", GLFW.GLFW_KEY_DELETE);
        register("insert", GLFW.GLFW_KEY_INSERT);
        register("home", GLFW.GLFW_KEY_HOME);
        register("end", GLFW.GLFW_KEY_END);
        register("pageup", GLFW.GLFW_KEY_PAGE_UP);
        register("pagedown", GLFW.GLFW_KEY_PAGE_DOWN);
        
        register("up", GLFW.GLFW_KEY_UP);
        register("down", GLFW.GLFW_KEY_DOWN);
        register("left", GLFW.GLFW_KEY_LEFT);
        register("right", GLFW.GLFW_KEY_RIGHT);
        
        register("lshift", GLFW.GLFW_KEY_LEFT_SHIFT);
        register("rshift", GLFW.GLFW_KEY_RIGHT_SHIFT);
        register("lctrl", GLFW.GLFW_KEY_LEFT_CONTROL);
        register("rctrl", GLFW.GLFW_KEY_RIGHT_CONTROL);
        register("lalt", GLFW.GLFW_KEY_LEFT_ALT);
        register("ralt", GLFW.GLFW_KEY_RIGHT_ALT);
        
        register("minus", GLFW.GLFW_KEY_MINUS);
        register("equals", GLFW.GLFW_KEY_EQUAL);
        register("lbracket", GLFW.GLFW_KEY_LEFT_BRACKET);
        register("rbracket", GLFW.GLFW_KEY_RIGHT_BRACKET);
        register("backslash", GLFW.GLFW_KEY_BACKSLASH);
        register("semicolon", GLFW.GLFW_KEY_SEMICOLON);
        register("apostrophe", GLFW.GLFW_KEY_APOSTROPHE);
        register("grave", GLFW.GLFW_KEY_GRAVE_ACCENT);
        register("comma", GLFW.GLFW_KEY_COMMA);
        register("period", GLFW.GLFW_KEY_PERIOD);
        register("slash", GLFW.GLFW_KEY_SLASH);
        
        for (int i = 0; i <= 9; i++) {
            register("numpad" + i, GLFW.GLFW_KEY_KP_0 + i);
        }
        register("numpadenter", GLFW.GLFW_KEY_KP_ENTER);
        register("numpadadd", GLFW.GLFW_KEY_KP_ADD);
        register("numpadsubtract", GLFW.GLFW_KEY_KP_SUBTRACT);
        register("numpadmultiply", GLFW.GLFW_KEY_KP_MULTIPLY);
        register("numpaddivide", GLFW.GLFW_KEY_KP_DIVIDE);
        register("numpaddecimal", GLFW.GLFW_KEY_KP_DECIMAL);
    }
    
    private static void register(String name, int keyCode) {
        NAME_TO_KEY.put(name.toLowerCase(), keyCode);
        KEY_TO_NAME.put(keyCode, name.toLowerCase());
    }
    
    public static int getKeyCode(String name) {
        return NAME_TO_KEY.getOrDefault(name.toLowerCase(), -1);
    }
    
    public static String getKeyName(int keyCode) {
        return KEY_TO_NAME.getOrDefault(keyCode, "unknown");
    }
    
    public static boolean isValidKey(String name) {
        return NAME_TO_KEY.containsKey(name.toLowerCase());
    }
}




