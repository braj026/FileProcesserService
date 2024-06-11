package com.imsi_main.service;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CSVReader {

    private static final Logger logger = Logger.getLogger(CSVReader.class.getName());

    public static List<String[]> readCSV(File file) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                data.add(values);
            }
        } catch (IOException e) {
            logger.severe("Failed to read CSV file: " + e.getMessage());
        }
        return data;
    }
}
