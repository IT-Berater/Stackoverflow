
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class ChaCha20Poly1305JceNoStream {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        System.out.println("File En-/Decryption with ChaCha20-Poly1305 JCE No stream");
        System.out.println("see: https://stackoverflow.com/questions/61520639/chacha20-poly1305-fails-with-shortbufferexception-output-buffer-too-small");
        System.out.println("\n* * * WARNING - the complete is loaded into memory");
        // loads the complete plaintext file into memory and performs encryption/decryption
        System.out.println("java version: " + Runtime.version());
        String filenamePlain = "test1024.txt";
        String filenameEnc = "test1024enc.txt";
        String filenameDec = "test1024dec.txt";
        Files.deleteIfExists(new File(filenamePlain).toPath());
        generateRandomFile(filenamePlain, 1024);
        // setup chacha20-poly1305-cipher
        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] key = new byte[32]; // 32 for 256 bit key or 16 for 128 bit
        byte[] nonce = new byte[12]; // nonce = 96 bit
        sr.nextBytes(key);
        sr.nextBytes(nonce);
        // Get Cipher Instance
        Cipher cipherE = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
        // Create parameterSpec
        AlgorithmParameterSpec algorithmParameterSpec = new IvParameterSpec(nonce);
        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");
        System.out.println("keySpec: " + keySpec.getAlgorithm() + " " + keySpec.getFormat());
        System.out.println("cipher algorithm: " + cipherE.getAlgorithm());
        // initialize the cipher for encryption
        cipherE.init(Cipher.ENCRYPT_MODE, keySpec, algorithmParameterSpec);
        // encryption
        System.out.println("start encryption");
        byte[] plaintext = Files.readAllBytes(Paths.get(filenamePlain));
        byte[] encryptedtext = cipherE.doFinal(plaintext);
        Files.write(Paths.get(filenameEnc), encryptedtext);

        // decryption
        System.out.println("start decryption");
        Cipher cipherD = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
        // initialize the cipher for decryption
        cipherD.init(Cipher.DECRYPT_MODE, keySpec, algorithmParameterSpec);
        byte[] encryptedtextRead = Files.readAllBytes(Paths.get(filenameEnc));
        byte[] decryptedtext = cipherD.doFinal(encryptedtextRead);
        Files.write(Paths.get(filenameDec), decryptedtext);

        // file compare
        System.out.println("compare plain <-> dec: " + Arrays.equals(sha256(filenamePlain), sha256(filenameDec)));
    }

    public static void generateRandomFile(String filename, int size) throws IOException, NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] data = new byte[size];
        sr.nextBytes(data);
        Files.write(Paths.get(filename), data, StandardOpenOption.CREATE);
    }

    public static byte[] sha256(String filenameString) throws IOException, NoSuchAlgorithmException {
        byte[] buffer= new byte[8192];
        int count;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filenameString));
        while ((count = bis.read(buffer)) > 0) {
            md.update(buffer, 0, count);
        }
        bis.close();
        return md.digest();
    }
}