package dps;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.Metadata;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DropBox {

    private String accessToken;
    private DbxRequestConfig config;
    private DbxClientV2 client;
    public DropBox() {
        accessToken = "sl.BqmM9JWeRrWJ_hpVnE3RWvheeoTyhv4I0vfHlhVh3XnAQ8MOUYWu29IZiO43tkJLMYkFXJR-CyPxKaLc7qKPf_2wCVh-62KTADFt1NOjhNuYJRA0ZhYcj4r5kLuoWWuumHM_djOSTCWKjn_rkzb3-HA";
        config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        client = new DbxClientV2(config, accessToken);
    }

    public void GetShare(String share) {
        try {
            // Get metadata for the file
            Metadata metadata = client.files().getMetadata("/Share/" + share);

            // If the file exists, metadata will be returned
            if (metadata instanceof FileMetadata) {
                System.out.println("Share exists on Dropbox.");

                // Download the file
                try (OutputStream outputStream = new FileOutputStream("shares/share1_dropbox.txt")) {
                    client.files().downloadBuilder("/Share/share_dropbox.txt").download(outputStream);
                } catch (Exception ex) {
                    System.err.println("ERROR: Download from Dropbox failed!");
                    throw new RuntimeException(ex);
                }
                System.out.println("Downloaded share from Dropbox!");
            } else {
                System.out.println("Share does not exist on Dropbox.");
            }
        } catch (GetMetadataErrorException e) {
            // If the file doesn't exist, Dropbox API returns an error
            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                System.out.println("Share does not exist on Dropbox.");
            } else {
                // Handle other errors
                e.printStackTrace();
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void UploadShare(String share) {
        try {
            // Get metadata for the file
            Metadata metadata = client.files().getMetadata("/Share/share_dropbox.txt");

            // If the file exists, metadata will be returned
            if (metadata instanceof FileMetadata) {
                System.out.println("Share exists on Dropbox.");

            } else {
                System.out.println("Share does not exist on Dropbox.");
            }
        } catch (GetMetadataErrorException e) {
            // If the file doesn't exist, Dropbox API returns an error
            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                System.out.println("Share does not exist on Dropbox.");

                // Upload the file
                try (InputStream in = new FileInputStream(share)) {
                    client.files().uploadBuilder("/Share/" + share).uploadAndFinish(in);
                } catch (Exception ex) {
                    System.err.println("ERROR: Upload to Dropbox failed!");
                    throw new RuntimeException(ex);
                }
                System.out.println("Uploaded Share to Dropbox!");
            } else {
                // Handle other errors
                e.printStackTrace();
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }
}
