/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Environment.Studio;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
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
    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;

    // These are the status names.
    public static final String STATUSES[] = {"Downloading",
        "Paused", "Complete", "Cancelled", "Error"};
    // These are the status codes.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    URL url = null;

    public boolean Exists() {
        return url != null;
    }

    private int size; // size of download in bytes
    private int downloaded = 0; // number of bytes downloaded
    private int status; // current status of download

    public void Download() {
        RandomAccessFile file = null;
        InputStream stream = null;
        System.out.println(this.file.GetAbsolutePath());
        try {
            // Open connection to URL.
            HttpsURLConnection connection
                    = (HttpsURLConnection) url.openConnection();
            System.out.println("Connection opened to " + url.toURI().toString());
            // Specify what portion of file to download.
            //connection.set.setRequestProperty("Range",
            //        "bytes=" + downloaded + "-");

            // Connect to server.
            //connection.connect();
            
            System.out.println("" + connection.getResponseMessage());
            //System.out.println("Connected with code: " + connection.getResponseCode());
            // Make sure response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2) {
                
                //error();
            }

            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                //error();
            }
            System.out.println(contentLength);
            /* Set the size for this download if it
         hasn't been already set. */
            if (size == -1) {
                size = contentLength;
                //stateChanged();
            }

            // Open file and seek to the end of it.
            file = new RandomAccessFile(this.file.GetAbsolutePath(), "rw");
            file.seek(downloaded);
            status = DOWNLOADING;
            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
                /* Size buffer according to how much of the
           file is left to download. */
                //System.out.println("...");
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1) {
                    break;
                }

                // Write buffer to file.
                file.write(buffer, 0, read);
                downloaded += read;
                //stateChanged();
            }

            /* Change status to complete if this point was
         reached because downloading has finished. */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                //stateChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //error();
        } finally {
            System.out.println("sdlfjsdlfkjsd");
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                }
            }

            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                }
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
