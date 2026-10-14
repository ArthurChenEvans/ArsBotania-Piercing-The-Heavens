package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.item.DivineArmorItem;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** Divine has its own look: every texture the armor and icons point at must ship with the mod. */
class DivineTexturesTest {

    private static InputStream resource(ResourceLocation id, String prefix) {
        return DivineTexturesTest.class.getResourceAsStream("/assets/" + id.getNamespace() + "/" + prefix + id.getPath());
    }

    private static boolean exists(ResourceLocation texture) {
        return resource(texture, "") != null;
    }

    @Test
    void eachClassHasAnAnimatedDivineArmorTexture() {
        for (ArcanistClass cls : ArcanistClass.values()) {
            ResourceLocation texture = DivineArmorItem.textureFor(cls);
            assertEquals(ArsBotania.MODID, texture.getNamespace());
            assertTrue(exists(texture), "missing " + texture);
            assertTrue(exists(texture.withSuffix(".mcmeta")), "not animated: " + texture);
        }
    }

    @Test
    void itemIconsUseDivineTexturesThatExist() throws Exception {
        for (DivinePiece piece : DivinePiece.all()) {
            String path = "/assets/arsbotania/models/item/" + piece.registryName() + ".json";
            try (InputStream in = getClass().getResourceAsStream(path)) {
                assertNotNull(in, "missing model " + path);
                JsonObject textures = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonObject("textures");
                assertNotNull(textures, piece.registryName() + " should have its own textures, not Ars's");
                for (String layer : textures.keySet()) {
                    ResourceLocation tex = ResourceLocation.parse(textures.get(layer).getAsString());
                    assertEquals(ArsBotania.MODID, tex.getNamespace(), piece.registryName() + " " + layer);
                    assertTrue(exists(tex.withPrefix("textures/").withSuffix(".png")), "missing " + tex);
                }
            }
        }
    }
}
