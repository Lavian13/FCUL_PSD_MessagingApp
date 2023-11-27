package dps;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Arrays;


public class DownloadShares {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        DropBox db = new DropBox();
        GoogleDrive gd = new GoogleDrive();
        GitHub gh = new GitHub();
        db.GetShare("share_dropbox.txt");
        gd.GetShare("share_googledrive.txt");
        gh.GetShare("share_github.txt");

        // Specify the folder path
        String folderPath = "shares";
        Share[] shares;
        // Create a File object for the folder
        File folder = new File(folderPath);
        int shareHolder = 1;
        // Check if the specified path is a directory

        if (folder.isDirectory()) {
            // Get all files in the directory
            File[] files = folder.listFiles();
            shares = new Share[files.length];
            int index = 0;
            // Iterate through each file
            for (File file : files) {
                // Check if the file is a text file
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    // Read and print the content of the text file
                    System.out.println("Reading share: " + file.getName());
                    shares[index] = new Share(BigInteger.valueOf(index + 1),readTextFileAsBigInteger(file));
                    index++;
                    System.out.println("----------------------");
                }
            }

            for(Share s : shares){
                System.out.printf("\t(%s, %s)\n", s.getShareholder(), s.getShare());
            }

            BigInteger recoveredSecret = combine(shares);
            byte[] recoveredKeyBytes = recoveredSecret.toByteArray();
            // Ensure the key is the correct length
            if (recoveredKeyBytes.length < 32) {
                byte[] paddedKeyBytes = new byte[32];
                System.arraycopy(recoveredKeyBytes, 0, paddedKeyBytes, 32 - recoveredKeyBytes.length, recoveredKeyBytes.length);
                recoveredKeyBytes = paddedKeyBytes;
            } else if (recoveredKeyBytes.length > 32) {
                recoveredKeyBytes = Arrays.copyOfRange(recoveredKeyBytes, recoveredKeyBytes.length - 32, recoveredKeyBytes.length);
            }

            System.out.printf("Recovered secret: %s\n", recoveredSecret);
            // Convert the byte array back to a SecretKey
            SecretKey recoveredAesKey = new SecretKeySpec(recoveredKeyBytes, "AES");
            System.out.println("Recovered key: " + bytesToHex(recoveredAesKey.getEncoded()));
            System.out.println("Shares can be found in the 'shares' folder");

        } else {
            System.out.println("Specified path is not a directory.");
        }

    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    private static BigInteger readTextFileAsBigInteger(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine().trim();
            if (line != null) {
                System.out.println(line);
                // Parse the content as a BigInteger
                return new BigInteger(line);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        // Return zero if there was an issue reading or parsing the content
        return BigInteger.ZERO;
    }

    /**
     * This method combines shares, using Lagrange polynomials, to recover the secret.
     * 	Lagrange's polynomials: https://en.wikipedia.org/wiki/Lagrange_polynomial.
     * @param shares Shares of the secret.
     * @return Recovered secret.
     */
    private static BigInteger combine(DownloadShares.Share[] shares) {

        BigInteger result = BigInteger.valueOf(0);

        for(int j = 0; j < shares.length; j++){
            BigInteger upper = BigInteger.valueOf(1);
            BigInteger lower = BigInteger.valueOf(1);
            BigInteger divisionResult = BigInteger.valueOf(0);
            for( int m = 0; m < shares.length; m++) {
                if ( j != m) {
                    upper = upper.multiply(shares[m].getShareholder());
                    lower = lower.multiply(shares[m].getShareholder().subtract(shares[j].getShareholder()));
                }
            }
            divisionResult = upper.divide(lower);
            BigInteger aux = shares[j].getShare().multiply(divisionResult);
            result = result.add(aux);
        }

        return result;

    }
    private static class Share {
        private final BigInteger shareholder;
        private final BigInteger share;

        private Share(BigInteger shareholder, BigInteger share) {
            this.shareholder = shareholder;
            this.share = share;
        }

        public BigInteger getShare() {
            return share;
        }

        public BigInteger getShareholder() {
            return shareholder;
        }
    }
}
