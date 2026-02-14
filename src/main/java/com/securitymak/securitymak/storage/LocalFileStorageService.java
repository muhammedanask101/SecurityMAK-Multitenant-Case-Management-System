package com.securitymak.securitymak.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${securecase.storage.path}")
    private String storagePath;

    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation);
    }

    @Override
    public String store(MultipartFile file) {
        try {
            String original = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String storedName = uuid + "_" + original;

            Path destination = rootLocation.resolve(storedName);

            Files.copy(file.getInputStream(), destination,
                    StandardCopyOption.REPLACE_EXISTING);

            return storedName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
public Resource loadAsResource(String filename) {
    try {
        Path file = rootLocation.resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("File not found or unreadable");
        }

    } catch (MalformedURLException e) {
        throw new RuntimeException("File retrieval failed", e);
    }
}

    @Override
    public void delete(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
