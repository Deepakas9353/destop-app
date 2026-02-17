package com.printapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsService {
    private static final String SETTINGS_FILE = "settings.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public void saveLastPrinter(String printerName) {
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("lastPrinter", printerName);
            mapper.writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastPrinter() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try {
                return mapper.readValue(file, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                }).get("lastPrinter");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
