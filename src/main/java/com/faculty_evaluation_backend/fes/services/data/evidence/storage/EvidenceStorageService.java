package com.faculty_evaluation_backend.fes.services.data.evidence.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvidenceStorageService {

    private static final String GOOGLE_DRIVE_PROVIDER = "google-drive";
    private static final String LOCAL_PROVIDER = "local";
    private static final String DRIVE_FOLDER_MIME_TYPE =
            "application/vnd.google-apps.folder";
    private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.file";
    private static final String SERVICE_ACCOUNT_GRANT_TYPE =
            "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String provider;
    private final Path localStorageRoot;
    private final String credentialsPath;
    private final String folderId;
    private final String tokenUri;
    private final String driveFilesUri;
    private final String driveUploadUri;

    private String cachedAccessToken;
    private Instant cachedAccessTokenExpiresAt = Instant.EPOCH;

    public EvidenceStorageService(
            ObjectMapper objectMapper,
            @Value("${app.evidence.storage-provider:google-drive}") String provider,
            @Value("${app.evidence.storage-dir:local-data/evidences}") String storageDir,
            @Value("${app.evidence.google-drive.credentials-path:}") String credentialsPath,
            @Value("${app.evidence.google-drive.folder-id:}") String folderId,
            @Value("${app.evidence.google-drive.token-uri:https://oauth2.googleapis.com/token}") String tokenUri,
            @Value("${app.evidence.google-drive.files-uri:https://www.googleapis.com/drive/v3/files}") String driveFilesUri,
            @Value("${app.evidence.google-drive.upload-uri:https://www.googleapis.com/upload/drive/v3/files}") String driveUploadUri
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.provider = provider == null ? GOOGLE_DRIVE_PROVIDER : provider.trim();
        this.localStorageRoot = Path.of(storageDir).toAbsolutePath().normalize();
        this.credentialsPath = credentialsPath == null ? "" : credentialsPath.trim();
        this.folderId = folderId == null ? "" : folderId.trim();
        this.tokenUri = tokenUri;
        this.driveFilesUri = driveFilesUri;
        this.driveUploadUri = driveUploadUri;

        if (LOCAL_PROVIDER.equalsIgnoreCase(this.provider)) {
            createLocalStorageRoot();
        }
    }

    public StoredEvidenceFile store(
            MultipartFile file,
            String originalFilename,
            String storedFilename,
            String facultyId
    ) {
        if (GOOGLE_DRIVE_PROVIDER.equalsIgnoreCase(provider)) {
            return storeInGoogleDrive(file, storedFilename, facultyId);
        }

        if (LOCAL_PROVIDER.equalsIgnoreCase(provider)) {
            return storeLocally(file, originalFilename, storedFilename, facultyId);
        }

        throw new BadRequestException("Unsupported evidence storage provider: " + provider);
    }

    public EvidenceDownload download(
            String filePath,
            String originalFilename,
            String contentType
    ) {
        if (GOOGLE_DRIVE_PROVIDER.equalsIgnoreCase(provider)) {
            return downloadFromGoogleDrive(filePath, originalFilename, contentType);
        }

        if (LOCAL_PROVIDER.equalsIgnoreCase(provider)) {
            return downloadLocally(filePath, originalFilename, contentType);
        }

        throw new BadRequestException("Unsupported evidence storage provider: " + provider);
    }

    public void delete(String filePath) {
        if (GOOGLE_DRIVE_PROVIDER.equalsIgnoreCase(provider)) {
            deleteFromGoogleDrive(filePath);
            return;
        }

        if (LOCAL_PROVIDER.equalsIgnoreCase(provider)) {
            deleteLocally(filePath);
            return;
        }

        throw new BadRequestException("Unsupported evidence storage provider: " + provider);
    }

    private StoredEvidenceFile storeInGoogleDrive(
            MultipartFile file,
            String storedFilename,
            String facultyId
    ) {
        String contentType = contentType(file.getContentType());
        String boundary = "fes-evidence-" + System.nanoTime();

        try {
            String targetFolderId = evidenceFolderId(facultyId);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("name", storedFilename);

            if (!targetFolderId.isBlank()) {
                metadata.put("parents", List.of(targetFolderId));
            }

            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            body.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            body.write(objectMapper.writeValueAsBytes(metadata));
            body.write(("\r\n--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            file.getInputStream().transferTo(body);
            body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            driveUploadUri
                                    + "?uploadType=multipart"
                                    + "&fields=id,name,mimeType,size"
                                    + "&supportsAllDrives=true"
                    ))
                    .header("Authorization", "Bearer " + accessToken())
                    .header("Content-Type", "multipart/related; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw googleDriveException(
                        "upload",
                        response.statusCode(),
                        response.body()
                );
            }

            Map<String, Object> responseBody =
                    objectMapper.readValue(
                            response.body(),
                            new TypeReference<>() {
                            }
                    );

            String driveFileId = String.valueOf(responseBody.get("id"));

            return new StoredEvidenceFile(
                    storedFilename,
                    driveFileId,
                    contentType,
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read evidence file for Google Drive upload.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Google Drive upload was interrupted.");
        }
    }

    private EvidenceDownload downloadFromGoogleDrive(
            String fileId,
            String originalFilename,
            String contentType
    ) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            driveFilesUri
                                    + "/" + urlEncode(fileId)
                                    + "?alt=media&supportsAllDrives=true"
                    ))
                    .header("Authorization", "Bearer " + accessToken())
                    .GET()
                    .build();

            HttpResponse<byte[]> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 404) {
                throw new ResourceNotFoundException("Evidence file not found in Google Drive.");
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw googleDriveException(
                        "download",
                        response.statusCode(),
                        new String(response.body(), StandardCharsets.UTF_8)
                );
            }

            Resource resource = new ByteArrayResource(response.body()) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };

            return new EvidenceDownload(
                    resource,
                    originalFilename,
                    contentType
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to download evidence file from Google Drive.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Google Drive download was interrupted.");
        }
    }

    private void deleteFromGoogleDrive(String fileId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            driveFilesUri
                                    + "/" + urlEncode(fileId)
                                    + "?supportsAllDrives=true"
                    ))
                    .header("Authorization", "Bearer " + accessToken())
                    .DELETE()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                return;
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw googleDriveException(
                        "delete",
                        response.statusCode(),
                        response.body()
                );
            }
        } catch (IOException ex) {
            throw new BadRequestException("Failed to delete evidence file from Google Drive.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Google Drive delete was interrupted.");
        }
    }

    private StoredEvidenceFile storeLocally(
            MultipartFile file,
            String originalFilename,
            String storedFilename,
            String facultyId
    ) {
        Path targetFolder =
                localStorageRoot
                        .resolve(LocalDate.now().toString())
                        .resolve(safeFolderName(facultyId))
                        .normalize();
        ensureTargetInsideLocalStorage(targetFolder);

        Path target = targetFolder.resolve(storedFilename).normalize();
        ensureTargetInsideLocalStorage(target);

        try {
            Files.createDirectories(targetFolder);
            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store evidence file.");
        }

        return new StoredEvidenceFile(
                storedFilename,
                localStorageRoot.relativize(target).toString(),
                contentType(file.getContentType()),
                file.getSize()
        );
    }

    private String evidenceFolderId(String facultyId)
            throws IOException, InterruptedException {
        String dateFolderId =
                findOrCreateDriveFolder(LocalDate.now().toString(), folderId);

        return findOrCreateDriveFolder(safeFolderName(facultyId), dateFolderId);
    }

    private String findOrCreateDriveFolder(
            String name,
            String parentFolderId
    ) throws IOException, InterruptedException {
        String existingFolderId = findDriveFolder(name, parentFolderId);

        if (!existingFolderId.isBlank()) {
            return existingFolderId;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", name);
        metadata.put("mimeType", DRIVE_FOLDER_MIME_TYPE);

        if (parentFolderId != null && !parentFolderId.isBlank()) {
            metadata.put("parents", List.of(parentFolderId));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        driveFilesUri
                                + "?fields=id,name"
                                + "&supportsAllDrives=true"
                ))
                .header("Authorization", "Bearer " + accessToken())
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(metadata)
                ))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw googleDriveException(
                    "folder creation",
                    response.statusCode(),
                    response.body()
            );
        }

        Map<String, Object> responseBody =
                objectMapper.readValue(
                        response.body(),
                        new TypeReference<>() {
                        }
                );

        return stringValue(responseBody.get("id"));
    }

    private String findDriveFolder(
            String name,
            String parentFolderId
    ) throws IOException, InterruptedException {
        String parentQuery =
                parentFolderId == null || parentFolderId.isBlank()
                        ? "'root' in parents"
                        : "'" + escapeDriveQueryValue(parentFolderId) + "' in parents";
        String query =
                parentQuery
                        + " and mimeType = '" + DRIVE_FOLDER_MIME_TYPE + "'"
                        + " and name = '" + escapeDriveQueryValue(name) + "'"
                        + " and trashed = false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        driveFilesUri
                                + "?q=" + urlEncode(query)
                                + "&fields=files(id,name)"
                                + "&pageSize=1"
                                + "&supportsAllDrives=true"
                                + "&includeItemsFromAllDrives=true"
                ))
                .header("Authorization", "Bearer " + accessToken())
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw googleDriveException(
                    "folder lookup",
                    response.statusCode(),
                    response.body()
            );
        }

        Map<String, Object> responseBody =
                objectMapper.readValue(
                        response.body(),
                        new TypeReference<>() {
                        }
                );
        Object files = responseBody.get("files");

        if (!(files instanceof List<?> folderList) || folderList.isEmpty()) {
            return "";
        }

        Object firstFolder = folderList.getFirst();

        if (!(firstFolder instanceof Map<?, ?> folder)) {
            return "";
        }

        return stringValue(folder.get("id"));
    }

    private EvidenceDownload downloadLocally(
            String filePath,
            String originalFilename,
            String contentType
    ) {
        Path path = localStorageRoot.resolve(filePath).normalize();
        ensureTargetInsideLocalStorage(path);

        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Evidence file not found.");
        }

        return new EvidenceDownload(
                new FileSystemResource(path),
                originalFilename,
                contentType
        );
    }

    private void deleteLocally(String filePath) {
        Path path = localStorageRoot.resolve(filePath).normalize();
        ensureTargetInsideLocalStorage(path);

        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to delete evidence file.");
        }
    }

    private synchronized String accessToken() {
        if (cachedAccessToken != null
                && Instant.now().isBefore(cachedAccessTokenExpiresAt)) {
            return cachedAccessToken;
        }

        ServiceAccountCredentials credentials = serviceAccountCredentials();
        String assertion = serviceAccountAssertion(credentials);
        String body =
                "grant_type=" + urlEncode(SERVICE_ACCOUNT_GRANT_TYPE)
                        + "&assertion=" + urlEncode(assertion);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(credentials.tokenUri()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw googleDriveException(
                        "authentication",
                        response.statusCode(),
                        response.body()
                );
            }

            Map<String, Object> responseBody =
                    objectMapper.readValue(
                            response.body(),
                            new TypeReference<>() {
                            }
                    );

            cachedAccessToken = String.valueOf(responseBody.get("access_token"));
            long expiresIn =
                    ((Number) responseBody.getOrDefault("expires_in", 3600))
                            .longValue();
            cachedAccessTokenExpiresAt =
                    Instant.now().plusSeconds(Math.max(60, expiresIn - 60));

            return cachedAccessToken;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to authenticate with Google Drive.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Google Drive authentication was interrupted.");
        }
    }

    private ServiceAccountCredentials serviceAccountCredentials() {
        if (credentialsPath.isBlank()) {
            throw new BadRequestException("Google Drive credentials path is not configured.");
        }

        try {
            Map<String, Object> credentials =
                    objectMapper.readValue(
                            Files.readString(Path.of(credentialsPath)),
                            new TypeReference<>() {
                            }
                    );

            String clientEmail = stringValue(credentials.get("client_email"));
            String privateKey = stringValue(credentials.get("private_key"));
            String configuredTokenUri =
                    stringValue(credentials.getOrDefault("token_uri", tokenUri));

            if (clientEmail.isBlank() || privateKey.isBlank()) {
                throw new BadRequestException("Invalid Google Drive service account credentials.");
            }

            return new ServiceAccountCredentials(
                    clientEmail,
                    privateKey,
                    configuredTokenUri
            );
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read Google Drive credentials.");
        }
    }

    private String serviceAccountAssertion(
            ServiceAccountCredentials credentials
    ) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + 3600;

        try {
            Map<String, Object> header =
                    Map.of("alg", "RS256", "typ", "JWT");
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("iss", credentials.clientEmail());
            claims.put("scope", DRIVE_SCOPE);
            claims.put("aud", credentials.tokenUri());
            claims.put("iat", issuedAt);
            claims.put("exp", expiresAt);

            String unsignedToken =
                    base64Url(objectMapper.writeValueAsBytes(header))
                            + "."
                            + base64Url(objectMapper.writeValueAsBytes(claims));

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey(credentials.privateKey()));
            signature.update(unsignedToken.getBytes(StandardCharsets.UTF_8));

            return unsignedToken + "." + base64Url(signature.sign());
        } catch (Exception ex) {
            throw new BadRequestException("Failed to create Google Drive authentication token.");
        }
    }

    private PrivateKey privateKey(String privateKeyPem) throws Exception {
        String key = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String contentType(String contentType) {
        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private BadRequestException googleDriveException(
            String action,
            int statusCode,
            String responseBody
    ) {
        return new BadRequestException(
                "Google Drive "
                        + action
                        + " failed ("
                        + statusCode
                        + "): "
                        + googleDriveErrorMessage(responseBody)
        );
    }

    private String googleDriveErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "No response details were returned.";
        }

        try {
            Map<String, Object> body =
                    objectMapper.readValue(
                            responseBody,
                            new TypeReference<>() {
                            }
                    );
            Object error = body.get("error");

            if (error instanceof Map<?, ?> errorMap) {
                String message = stringValue(errorMap.get("message"));

                if (!message.isBlank()) {
                    return limitMessage(message);
                }
            }
        } catch (IOException ignored) {
            return limitMessage(responseBody);
        }

        return limitMessage(responseBody);
    }

    private String limitMessage(String message) {
        String compact = message.replaceAll("\\s+", " ").trim();

        if (compact.length() <= 300) {
            return compact;
        }

        return compact.substring(0, 300) + "...";
    }

    private String safeFolderName(String value) {
        String folderName = stringValue(value)
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        return folderName.isBlank() ? "unknown-faculty" : folderName;
    }

    private String escapeDriveQueryValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private void createLocalStorageRoot() {
        try {
            Files.createDirectories(localStorageRoot);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to create evidence storage directory.",
                    ex
            );
        }
    }

    private void ensureTargetInsideLocalStorage(Path target) {
        if (!target.startsWith(localStorageRoot)) {
            throw new BadRequestException("Invalid evidence storage path.");
        }
    }

    private record ServiceAccountCredentials(
            String clientEmail,
            String privateKey,
            String tokenUri
    ) {
    }

    public record StoredEvidenceFile(
            String storedFilename,
            String filePath,
            String contentType,
            Long fileSize
    ) {
    }

    public record EvidenceDownload(
            Resource resource,
            String filename,
            String contentType
    ) {
    }
}
