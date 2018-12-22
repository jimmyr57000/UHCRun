package fr.jrich.uhcrun.util;

import java.lang.reflect.Field;
import java.util.Arrays;

import fr.jrich.uhcrun.util.ReflectionHandler.PackageType;

public class BiomeChanger {
    private static Field biomesField;
    private static Object[] biomesCopy;

    static {
        try {
            BiomeChanger.biomesField = ReflectionHandler.getField("BiomeBase", PackageType.MINECRAFT_SERVER, true, "biomes");
            BiomeChanger.biomesCopy = Arrays.copyOf(BiomeChanger.getBiomes(), 256);
        } catch (NoSuchFieldException | SecurityException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void changeBiome(int id, int toId) {
        Object[] biomes = BiomeChanger.getBiomes();
        biomes[id] = BiomeChanger.biomesCopy[toId];
    }

    public static Object[] getBiomes() {
        try {
            return (Object[]) BiomeChanger.biomesField.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return new Object[256];
    }
}
