package com.example.mana_infusion.Utils.Noise;

public class Mathd {
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }
}
