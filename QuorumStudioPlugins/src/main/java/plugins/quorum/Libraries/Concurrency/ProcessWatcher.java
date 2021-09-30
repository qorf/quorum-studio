/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefika
 */
public class ProcessWatcher implements Runnable {
    private BufferedReader bufferedReader = null;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader bufferedErrorReader = null;
    private InputStream errorStream;
    
    BufferedWriter bufferedWriter;
    private Thread blinker = null;
    public boolean running = false;
    //private quorum.Libraries.Concurrency.ProcessRunner myProcess = null;
    public boolean cancelled = false;
    ProcessCoordinator coordinator;
    public boolean wasDestroyed = false;

    public ProcessCoordinator GetProcessCoordinator() {
        return coordinator;
    }
    
    public void SetProcessCoordinator(ProcessCoordinator coordinator) {
        this.coordinator = coordinator;
    }
    
    public boolean GetCancelled() {
        return cancelled;
    }
    
    public void SetCancelled(boolean cancel) {
        cancelled = cancel;
    }
    
    public ProcessWatcher(InputStream in, InputStream errors) {
        inputStream = in;
        bufferedReader = new BufferedReader(new InputStreamReader(in));
        
        errorStream = errors;
        bufferedErrorReader = new BufferedReader(new InputStreamReader(errors));
    }

    public void SendInput(String value) {
        if(bufferedWriter != null) {
            try {
                bufferedWriter.write(value);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException ex) {

                //Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void start() {
        if (!running) {
            blinker = new Thread(this);
            blinker.setName("Quorum Process Watcher");
            blinker.start();
        }
    }

    /**
     * This method is used to flush only if a process was destroyed before
     * fully flushing the output stream.
     */
    public void flush() {
        try {
            while (bufferedReader.ready()) {
                final String line = bufferedReader.readLine();
                if(coordinator != null) {
                    coordinator.SendOutput(line);
                }
            }

        } catch (IOException ex) {
        }
    }

    @Override
    public void run() {
        running = true;
        // Watch the input stream, send its output to the console.
        while (!cancelled && running) {
            try {
                if (bufferedReader.ready()) {
                    final String line = bufferedReader.readLine();
                    if(coordinator != null) {
                        coordinator.SendOutput(line);
                    }
                }
                
                if (bufferedErrorReader.ready()) {
                    final String line = bufferedErrorReader.readLine();
                    if(coordinator != null) {
                        coordinator.SendOutput(line);
                    }
                }
                Thread.sleep(50);
            } catch (IOException ex) {
            } catch (InterruptedException ex) {
            } 
        }
    }

    /**
     * @return the stream
     */
    public OutputStream getStream() {
        return outputStream;
    }

    /**
     * @param stream the stream to set
     */
    public void setStream(OutputStream stream) {
        this.outputStream = stream;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(stream));
    }
}