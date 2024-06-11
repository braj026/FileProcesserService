package com.imsi_main.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;

@Component
public class FileValidator {

    private static final Logger logger = Logger.getLogger(FileValidator.class.getName());

    @Value("${file.separator:,}")
    private String fileSeparator;

    @Value("${fileCorruptPath}")
    private String fileCorruptPath;

    @Value("${header.addFile}")
    private String addFileHeader;

    @Value("${addFilePath.msisdn}")
    private String addFileMsisdn;

    @Value("${addFilePath.imsi}")
    private String addFileImsi;

    @Value("${addFilePath.created_date}")
    private String addFileCreatedDate;

    public boolean validateHeaders(File file, String expectedHeader) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            return expectedHeader.equals(header);
        } catch (IOException e) {
            logger.severe("Failed to read file: " + file.getName() + " (" + e.getMessage() + ")");
            return false;
        }
    }

    public boolean validateContents(File file, String msisdnColumn, String imsiColumn, String dateColumn) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            String[] columns = header.split(fileSeparator);

            int msisdnIndex = getIndex(columns, msisdnColumn);
            int imsiIndex = getIndex(columns, imsiColumn);
            int dateIndex = getIndex(columns, dateColumn);

            if (msisdnIndex == -1 || imsiIndex == -1 || dateIndex == -1) {
                return false;
            }

            Set<String> msisdnSet = new HashSet<>();
            Set<String> imsiSet = new HashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(fileSeparator);

                if (values[msisdnIndex].isEmpty() || values[imsiIndex].isEmpty()) {
                    return false;
                }

                if (!msisdnSet.add(values[msisdnIndex]) || !imsiSet.add(values[imsiIndex])) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            logger.severe("Failed to read file: " + file.getName() + " (" + e.getMessage() + ")");
            return false;
        }
    }

    private int getIndex(String[] columns, String column) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(column)) {
                return i;
            }
        }
        return -1;
    }

    public boolean validateAddFile(File addFile) {
        return validateHeaders(addFile, addFileHeader) && validateContents(addFile, addFileMsisdn, addFileImsi, addFileCreatedDate);
    }

    public void moveFileToCorruptFolder(File file) {
        Path sourcePath = file.toPath();
        Path targetPath = Paths.get(fileCorruptPath, file.getName());
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved to corrupt folder: " + targetPath);
        } catch (IOException e) {
            logger.severe("Failed to move file to corrupt folder: " + e.getMessage());
        }
    }

    public void moveFileToCorruptFolder(String filePath) {
        Path sourcePath = Paths.get(filePath);
        Path targetPath = Paths.get(fileCorruptPath, sourcePath.getFileName().toString());
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved to corrupt folder: " + targetPath);
        } catch (IOException e) {
            logger.severe("Failed to move file to corrupt folder: " + e.getMessage());
        }
    }
}


