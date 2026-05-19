package com.agileforge.infrastructure.storage;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.port.out.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    private final Path storageDir;

    public LocalFileStorageAdapter(@Value("${agileforge.storage.path:./uploads}") String storagePath) {
        this.storageDir = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageDir);
            log.info("File storage initialized at: {}", storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + storageDir, e);
        }
    }

    @Override
    public String store(String fileName, String contentType, InputStream inputStream) {
        try {
            String uniqueName = UUID.randomUUID() + "_" + sanitizeFileName(fileName);
            Path targetPath = storageDir.resolve(uniqueName);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {}", uniqueName);
            return uniqueName;
        } catch (IOException e) {
            throw new BusinessException("Failed to store file: " + fileName);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path filePath = storageDir.resolve(storagePath);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", storagePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", storagePath, e);
        }
    }

    @Override
    public String getUrl(String storagePath) {
        return "/api/attachments/" + storagePath + "/download";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }

    public Path getFilePath(String storagePath) {
        return storageDir.resolve(storagePath);
    }
}
