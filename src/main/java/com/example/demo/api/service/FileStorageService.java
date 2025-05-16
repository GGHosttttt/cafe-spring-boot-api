package com.example.demo.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private String serverPort;

    public FileStorageService() {
        try {
            Files.createDirectories(fileStorageLocation);
            logger.info("Upload directory created at: {}", fileStorageLocation);
        } catch (IOException e) {
            logger.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, String existingImageUrl) {
        if (file == null || file.isEmpty()) {
            logger.debug("No new file provided, retaining existing image URL: {}", existingImageUrl);
            return existingImageUrl;
        }
        try {
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + "_image" + fileExtension;
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            String fileUrl = String.format("http://%s:%s/uploads/%s", serverAddress, serverPort, fileName);
            logger.info("File stored at {} and accessible via URL: {} (original name: {})", 
                        targetLocation, fileUrl, file.getOriginalFilename());

            // Delete old image if it exists
            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                deleteOldImage(existingImageUrl);
            }

            return fileUrl;
        } catch (IOException e) {
            logger.error("Could not store file", e);
            throw new RuntimeException("Could not store file", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".png"; // Default to .png if no extension
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    private void deleteOldImage(String imageUrl) {
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = fileStorageLocation.resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Deleted old image file: {}", filePath);
            } else {
                logger.warn("Old image file not found: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Could not delete old image file: {}", imageUrl, e);
        }
    }
}