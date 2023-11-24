package dps;

// *** DropBox ***

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;

//Github
import com.microsoft.graph.authentication.*;
import com.microsoft.graph.logger.ILogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.requests.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import com.backblaze.b2.client.B2Sdk;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.B2Sdk;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.microsoft.graph.models.DriveItem;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.requests.GraphServiceClient;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemRequestBuilder;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemRequestBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.requests.GraphServiceClient;

import java.io.InputStream;
import java.nio.file.Files;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * Hello world!
 *
 */
public class App {

    private static final String APPLICATION_NAME = "FCUL_Project";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static void main(String[] args) throws IOException {
        String accessToken = "sl.BqdUx0kxhYSkfr4fr0jFTW27uvlGd0f-zTZjghtAhI8OgRdkAOuDaUUumpCyTNb6zLrEYCUFnl2as3jp5DIxcRx1w0z8k0LUEWWT_hJFMMXdn_bMjrt1BXMtsFBD3BUZGf-ByCHLFVgus97DUd9vHYU";
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);

        try {
            // Get metadata for the file
            Metadata metadata = client.files().getMetadata("/Share/test.txt");

            // If the file exists, metadata will be returned
            if (metadata instanceof FileMetadata) {
                System.out.println("File exists on Dropbox.");
            } else {
                System.out.println("File does not exist on Dropbox.");
            }
        } catch (GetMetadataErrorException e) {
            // If the file doesn't exist, Dropbox API returns an error
            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                System.out.println("File does not exist on Dropbox.");
                try (InputStream in = new FileInputStream("test.txt")) {
                    client.files().uploadBuilder("/Share/test.txt").uploadAndFinish(in);
                } catch (Exception ex) {
                    System.err.println("ERROR: Upload to DropBox failed!");
                    throw new RuntimeException(ex);
                }
                System.out.println("Upload share to Dropbox!");
            } else {
                // Handle other errors
                e.printStackTrace();
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }

        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            // Load client secrets
            // Make sure to replace 'path/to/client_secrets.json' with the actual path to your downloaded JSON file
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(new FileInputStream("client_secret_701643948405-t8euh54g98bi58b1q55avafd1mjh9d53.apps.googleusercontent.com.json")));

            // Set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singleton(DriveScopes.DRIVE_FILE))
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                    .setAccessType("offline")
                    .build();

            // Authorize
            Credential credential = new AuthorizationCodeInstalledApp(
                    flow, new LocalServerReceiver()).authorize("user");

            // Initialize the Drive service
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Now you can use 'service' to interact with the Google Drive API

            //Upload

            // Specify the file you want to upload
            java.io.File fileContent = new java.io.File("test.txt");

            // Set metadata for the file
            File fileMetadata = new File();
            fileMetadata.setName("test.txt");
            fileMetadata.setMimeType("text/plain");

            // Create a FileContent instance with the file's MIME type and content
            FileContent mediaContent = new FileContent("text/plain", fileContent);

            // Use the Drive service to upload the file
            File uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();

            System.out.println("File uploaded: " + uploadedFile.getName() + " (ID: " + uploadedFile.getId() + ")");
            // For example, list files in the root folder
            FileList result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        //Microsoft

        // Create a ClientSecretCredential object
        ClientSecretCredential credential2 = new ClientSecretCredentialBuilder()
                .clientId("4aa964e6-1fb5-4694-8571-b1596721a40a")
                .clientSecret("1b856a1b-f9cc-410b-a4f9-ad7dc51600c5")
                .tenantId("f8cdef31-a31e-4b4a-93e4-5f571e91255a")
                .build();

        DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
                .clientId("4aa964e6-1fb5-4694-8571-b1596721a40a")
                .challengeConsumer(challenge -> System.out.println(challenge.getMessage()))
                .build();
        List<String> SCOPE = new ArrayList<>();
        SCOPE.add("Files.ReadWrite");
        SCOPE.add("Files.Read");

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(SCOPE, credential);

        GraphServiceClient graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();

        //GITHUB

        GitHub github = new GitHubBuilder().withOAuthToken("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").build();

        Path path = Paths.get("test.txt");
        byte[] fileContent = Files.readAllBytes(path);
        GHRepository repository = github.getRepository("fculpsdproject/shares");

        GHContent content = repository.getFileContent("test.txt");
        String sha = content.getSha();

        repository.createContent()
                .path("test.txt")
                .message("Your commit message")
                .content(fileContent)
                .sha(sha)
                .commit();
    }
}

