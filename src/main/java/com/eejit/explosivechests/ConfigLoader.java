package com.eejit.explosivechests;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigLoader {
    public static ModConfig loadConfig(Path configPath) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(configPath.toFile())) {
            System.out.println("config loaded");
            return gson.fromJson(reader, ModConfig.class);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;  // or create and return default config here
        }
    }
}
