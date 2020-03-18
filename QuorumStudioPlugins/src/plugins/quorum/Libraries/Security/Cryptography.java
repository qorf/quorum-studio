/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import quorum.Libraries.System.File_;

/**
 *
 * @author stefika
 */
public class Cryptography {
    public java.lang.Object me_ = null;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static String theKey = "KbPdSgVkYp3s6v9y$B&E)H@McQfThWmZ";
    
    public String Decrypt(String key, File_ quorumInputFile) {
        if(key == null || quorumInputFile == null) {
            return null;
        }
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
             
            File inputFile = new File(quorumInputFile.GetAbsolutePath());
            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
            
            byte[] outputBytes = cipher.doFinal(inputBytes);
            String result = new String(outputBytes); 
            inputStream.close();
            return result;
             
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            throw new RuntimeException("Error decrypting file", ex);
        }
    }
    
    public void Encrypt(String key, String contents, File_ quorumOutputFile) {
        if(key == null || contents == null || quorumOutputFile == null) {
            return;
        }
        
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
             
            byte[] inputBytes = contents.getBytes();
            //InputStream targetStream = new ByteArrayInputStream(inputBytes);
            
            byte[] outputBytes = cipher.doFinal(inputBytes);
            
            File outputFile = new File(quorumOutputFile.GetAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);
             
            outputStream.close();
             
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            throw new RuntimeException("Error encrypting file", ex);
        }
    }
}
