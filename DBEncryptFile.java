package com.example.jay_pc.dcrypt;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Jay-pc on 3/10/2017.
 */
public class DBEncryptFile {

    SecretKey secretKey = null;
    Cipher cipher = null;

    public DBEncryptFile(String masterPassword) {
        try {
            /**
             * Create a Blowfish key
             */
            secretKey = new SecretKeySpec(masterPassword.getBytes(), "Blowfish");
            /**
             * Create an instance of cipher mentioning the name of algorithm
             *     - Blowfish
             */
            cipher = Cipher.getInstance("Blowfish");
        } catch (NoSuchPaddingException ex) {
            System.out.println(ex);
            Log.e("DCrypt", ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.e("DCrypt", ex.getMessage());
        }
    }

    /**
     *
     * @param srcPath
     * @param destPath
     *
     * Encrypts the file in srcPath and creates a file in destPath
     * @throws Exception
     */
    protected void encrypt(String srcPath, String destPath) throws Exception {
        File rawFile = new File(srcPath);
        File encryptedFile = new File(destPath);
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            /**
             * Initialize the cipher for encryption
             */
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            /**
             * Initialize input and output streams
             */
            inStream = new FileInputStream(rawFile);
            outStream = new FileOutputStream(encryptedFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) > 0) {
                outStream.write(cipher.update(buffer, 0, len));
                outStream.flush();
            }
            outStream.write(cipher.doFinal());
            inStream.close();
            outStream.close();
        } catch (Exception ex) {
            Log.e("DCrypt", ex.getMessage());
            throw ex;
        }
    }

    /**
     *
     * @param srcPath
     * @param destPath
     *
     * Decrypts the file in srcPath and creates a file in destPath
     * @throws Exception
     */
    protected void decrypt(String srcPath, String destPath) throws Exception {
        File encryptedFile = new File(srcPath);
        File decryptedFile = new File(destPath);
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            /**
             * Initialize the cipher for decryption
             */
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            /**
             * Initialize input and output streams
             */
            inStream = new FileInputStream(encryptedFile);
            outStream = new FileOutputStream(decryptedFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) > 0) {
                outStream.write(cipher.update(buffer, 0, len));
                outStream.flush();
            }
            outStream.write(cipher.doFinal());
            inStream.close();
            outStream.close();
        } catch (Exception ex) {
            Log.e("DCrypt", ex.getMessage());
            throw ex;
        }
    }

    /**
     *
     * @param filePath
     *
     * Shreds a file
     */
    protected boolean shredFile(String filePath) {
        File file = new File(filePath);
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer buffer;
            try {
                int numBytes = (int) fileChannel.size();
                for (int i = 0; numBytes > 0; numBytes = numBytes - 1024, i++) {
                    byte[] randomBytes = new byte[1024];
                    buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, i * 1024, randomBytes.length);
                    buffer.clear();
                    new Random().nextBytes(randomBytes);
                    buffer.put(randomBytes);
                    fileChannel.write(buffer);
                }
            } finally {
                fileChannel.close();
                randomAccessFile.close();
                System.gc();
                Log.e("DCrypt", "File handles closed!");
            }
        } catch (FileNotFoundException exp) {
            Log.e("DCrypt", exp.getMessage());
        } catch (IOException exp) {
            Log.e("DCrypt", exp.getMessage());
        }

        boolean status = file.delete();
        return status;
    }

}
