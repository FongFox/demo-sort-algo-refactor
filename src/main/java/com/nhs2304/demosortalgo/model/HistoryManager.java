package com.nhs2304.demosortalgo.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to load and save {@link HistoryEntry} objects from JSON.
 */
public final class HistoryManager {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HistoryManager() {
    }

    public static List<HistoryEntry> loadHistory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(
                    file, new TypeReference<List<HistoryEntry>>() {
                    });
        } catch (IOException e) {
            // Nếu đọc file thất bại, trả về danh sách rỗng
            return new ArrayList<>();
        }
    }

    public static void saveHistory(String path, List<HistoryEntry> history) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(path), history);
        } catch (IOException e) {
            // Bỏ qua lỗi ghi file
        }
    }
}
