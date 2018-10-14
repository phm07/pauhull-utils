package de.pauhull.utils.misc;

import java.util.Random;

/**
 * Util to generate random colors.
 *
 * @author pauhull
 * @version 1.0
 */
public class ColorUtil {

    private static Random random = new Random();

    /**
     * Picks random color from HSB-Spectrum
     *
     * @return The color
     */
    public static org.bukkit.Color getRandomHSBColor() {
        java.awt.Color color = java.awt.Color.getHSBColor(random.nextFloat(), 1f, 1f);
        return org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

}
