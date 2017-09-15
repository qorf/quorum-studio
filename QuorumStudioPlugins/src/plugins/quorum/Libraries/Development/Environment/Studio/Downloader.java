/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Environment.Studio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author stefika
 */
public class Downloader {

    static {
        //this needs more work, but is a start.
        //more information on how to do this is here:
        //https://stackoverflow.com/questions/19005318/implementing-x509trustmanager-passing-on-part-of-the-verification-to-existing
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Activate the new trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }

    public java.lang.Object me_ = null;
    String path = "";
    quorum.Libraries.System.File_ file;
    URL url = null;

    public boolean Exists() {
        return url != null;
    }
    private int downloaded = 0; // number of bytes downloaded

    public void Download() {
        OutputStream outstream = null;
        InputStream stream = null;
        try {
            HttpsURLConnection connection
                    = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Range",
                    "bytes=" + downloaded + "-");
            connection.connect();
            if (connection.getResponseCode() / 100 != 2) {
            }
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
            }
          
            File targetFile = new File(this.file.GetAbsolutePath());
            outstream = new FileOutputStream(targetFile);
            
            stream = connection.getInputStream();
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                try {
                    outstream.write(buffer, 0, bytesRead);
                } catch (IOException ex) {
                    Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outstream.close();
            } catch (IOException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public void SetPathNative(String value) {
        path = value;
        url = null;
        try {
            url = new URL(path);
        } catch (Exception e) {
            url = null;
            return;
        }
        if (url.getFile().length() < 2) {
            url = null;
        }
    }

    public void SetFileNative(quorum.Libraries.System.File_ value) {
        file = value;
    }
}
