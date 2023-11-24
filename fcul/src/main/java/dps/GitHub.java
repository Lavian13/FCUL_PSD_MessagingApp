package dps;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GHContent;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;

public class GitHub {

    private org.kohsuke.github.GitHub github;

    private GHRepository repository;

    public GitHub() throws IOException {
        // Create a GitHub object
        github = new GitHubBuilder().withOAuthToken("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").build();

        // Get the repository
        repository = github.getRepository("fculpsdproject/shares");
    }

    public void GetShare(String share) throws IOException {
        // Check if the file exists
        GHContent content = null;
        try {
            content = repository.getFileContent(share);
        } catch (IOException e) {
            // If the file doesn't exist, an exception will be thrown
            // You can handle this exception as necessary
        }

        if (content != null) {
            System.out.println("File exists on GitHub");
            // If the file exists, download it
            InputStream inputStream = content.read();
            java.io.File fileOnDisk = new java.io.File("shares/share3_github.txt");
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileOnDisk)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Downloaded share from GitHub");
            }
        }
    }

    public void UploadShare(String share) throws IOException {
        // Check if the file exists
        GHContent content = null;
        try {
            content = repository.getFileContent(share);
        } catch (IOException e) {
            // If the file doesn't exist, an exception will be thrown
            // You can handle this exception as necessary
        }
        // If the file doesn't exist, upload it
        if(content == null) {
            Path path = Paths.get(share);
            byte[] fileContent = Files.readAllBytes(path);
            repository.createContent()
                    .path(share)
                    .message("Share_GitHub")
                    .content(fileContent)
                    .commit();
            System.out.println("Uploaded to GitHub");
        } else {
            System.out.println("File exists in GitHub");
        }
    }

}

