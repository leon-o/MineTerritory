package top.leonx.territory.util;

import java.awt.*;

public class ColorUtil {
    public static Color HSL2RGB(float H, float S, float L) {


        float R, G, B, var_1, var_2;
        if (S == 0) {
            R = L;
            G = L;
            B = L;
        } else {
            if (L < 128) {
                var_2 = (L * (256 + S)) / 256;
            } else {
                var_2 = (L + S) - (S * L) / 256;
            }

            if (var_2 > 255) {
                var_2 = Math.round(var_2);
            }

            if (var_2 > 254) {
                var_2 = 255;
            }

            var_1 = 2 * L - var_2;
            R = RGBFromHue(var_1, var_2, H + 120);
            G = RGBFromHue(var_1, var_2, H);
            B = RGBFromHue(var_1, var_2, H - 120);
        }
        R = R < 0 ? 0 : R;
        R = R > 255 ? 255 : R;
        G = G < 0 ? 0 : G;
        G = G > 255 ? 255 : G;
        B = B < 0 ? 0 : B;
        B = B > 255 ? 255 : B;
        return new Color(Math.round(R), Math.round(G), Math
                .round(B));
    }

    public static float RGBFromHue(float a, float b, float h) {
        if (h < 0) {
            h += 360;
        }
        if (h >= 360) {
            h -= 360;
        }
        if (h < 60) {
            return a + ((b - a) * h) / 60;
        }
        if (h < 180) {
            return b;
        }

        if (h < 240) {
            return a + ((b - a) * (240 - h)) / 60;
        }

        return a;
    }
}
