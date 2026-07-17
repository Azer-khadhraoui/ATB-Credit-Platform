package tn.atb.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.atb.backend.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.public-path:/uploads}")
    private String publicPath;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize upload directory: " + uploadDir, ex);
        }
    }

    /**
     * Stores an avatar image under {baseName}.{ext} and returns its public URL path.
     */
    public String storeImage(MultipartFile file, String baseName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est vide.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("L'image ne doit pas dépasser 2 Mo.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Format non supporté. Utilisez PNG, JPEG ou WEBP.");
        }

        String extension = switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };

        String filename = StringUtils.cleanPath(baseName + "." + extension);
        Path destination = rootLocation.resolve(filename).normalize();

        if (!destination.getParent().equals(rootLocation)) {
            throw new BadRequestException("Chemin de fichier invalide.");
        }

        try {
            // Remove any previous avatar with a different extension for this user.
            deleteExistingVariants(baseName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Échec de l'enregistrement du fichier.", ex);
        }

        return publicPath + "/" + filename;
    }

    private void deleteExistingVariants(String baseName) throws IOException {
        for (String ext : new String[] {"png", "jpg", "webp"}) {
            Files.deleteIfExists(rootLocation.resolve(baseName + "." + ext));
        }
    }
}
