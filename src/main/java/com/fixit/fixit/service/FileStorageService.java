package com.fixit.fixit.service;

import com.fixit.fixit.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    };

    private static final String[] ALLOWED_DOCUMENT_TYPES = {
            "application/pdf", "image/jpeg", "image/jpg", "image/png"
    };

    // =============================================
    // STORE FILE
    // =============================================
    public String storeFile(MultipartFile file, String subDirectory) {
        validateFile(file);

        try {
            // Create directory if not exists
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Store file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            return subDirectory + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to store file: " + e.getMessage());
        }
    }

    // =============================================
    // STORE PROFILE IMAGE
    // =============================================
    public String storeProfileImage(MultipartFile file, Long userId) {
        validateImageFile(file);
        return storeFile(file, "profiles/" + userId);
    }

    // =============================================
    // STORE CERTIFICATE
    // =============================================
    public String storeCertificate(MultipartFile file, Long helperId) {
        validateDocumentFile(file);
        return storeFile(file, "certificates/" + helperId);
    }

    // =============================================
    // DELETE FILE
    // =============================================
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new InvalidOperationException("Failed to delete file: " + e.getMessage());
        }
    }

    // =============================================
    // VALIDATE FILE
    // =============================================
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidOperationException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new InvalidOperationException("Invalid file name");
        }
    }

    // =============================================
    // VALIDATE IMAGE FILE
    // =============================================
    private void validateImageFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new InvalidOperationException("Invalid image file type. Allowed types: JPEG, PNG, GIF");
        }
    }

    // =============================================
    // VALIDATE DOCUMENT FILE
    // =============================================
    private void validateDocumentFile(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_DOCUMENT_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new InvalidOperationException("Invalid document file type. Allowed types: PDF, JPEG, PNG");
        }
    }

    // =============================================
    // GET FILE EXTENSION
    // =============================================
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    // =============================================
    // GET FILE PATH
    // =============================================
    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDir, relativePath);
    }
}
