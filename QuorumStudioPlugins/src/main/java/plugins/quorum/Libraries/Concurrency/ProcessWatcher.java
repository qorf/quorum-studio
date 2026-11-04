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

/**
 * Reads a program's output and error streams and sends each line to a
 * ProcessCoordinator. Each stream has its own thread, which reads until the
 * stream ends, so no output is left in the pipe when the program finishes.
 *
 * @author stefika
 */
public class ProcessWatcher {
    // How long flush waits for both streams to end, in milliseconds.
    public static final long FLUSH_WAIT = 5000;

    private final InputStream inputStream;
    private final InputStream errorStream;
    private OutputStream outputStream;
    private BufferedWriter bufferedWriter;
    private volatile Thread outputReader = null;
    private volatile Thread errorReader = null;
    private volatile boolean cancelled = false;
    private volatile ProcessCoordinator coordinator;

    public ProcessWatcher(InputStream in, InputStream errors) {
        inputStream = in;
        errorStream = errors;
    }

    public ProcessCoordinator GetProcessCoordinator() {
        return coordinator;
    }

    public void SetProcessCoordinator(ProcessCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public boolean GetCancelled() {
        return cancelled;
    }

    /**
     * Cancelling stops sending lines and closes both streams, so the readers
     * end. Use it only when the output is no longer wanted.
     */
    public void SetCancelled(boolean cancel) {
        cancelled = cancel;
        if (cancel) {
            closeQuietly(inputStream);
            closeQuietly(errorStream);
        }
    }

    public void SendInput(String value) {
        if(bufferedWriter != null) {
            try {
                bufferedWriter.write(value);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException ex) {
            }
        }
    }

    public synchronized void start() {
        if (outputReader == null && errorReader == null) {
            outputReader = startReader(inputStream, "Quorum Process Watcher (output)");
            errorReader = startReader(errorStream, "Quorum Process Watcher (errors)");
        }
    }

    private Thread startReader(InputStream stream, String name) {
        if (stream == null) {
            return null;
        }
        Thread thread = new Thread(new StreamReader(stream), name);
        // A reader stuck on a pipe never keeps Studio from quitting.
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Waits until both streams have ended and every line was sent, for up to
     * FLUSH_WAIT milliseconds. Returns whether both ended.
     */
    public boolean flush() {
        return WaitForEnd(FLUSH_WAIT);
    }

    /**
     * Waits up to the given milliseconds for both readers to reach the end of
     * their streams. Returns whether both did.
     */
    public boolean WaitForEnd(long milliseconds) {
        long deadline = System.currentTimeMillis() + milliseconds;
        boolean outputEnded = join(outputReader, deadline);
        boolean errorsEnded = join(errorReader, deadline);
        return outputEnded && errorsEnded;
    }

    private static boolean join(Thread thread, long deadline) {
        if (thread == null) {
            return true;
        }
        try {
            long left = deadline - System.currentTimeMillis();
            // join(0) would wait forever.
            if (left > 0) {
                thread.join(left);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return !thread.isAlive();
    }

    private void send(String line) {
        ProcessCoordinator target = coordinator;
        if (!cancelled && target != null) {
            target.SendOutput(line);
        }
    }

    private static void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Reads one stream line by line until it ends. readLine also returns a
     * last line that has no newline.
     */
    private class StreamReader implements Runnable {
        private final InputStream stream;

        StreamReader(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            try {
                String line = reader.readLine();
                while (line != null) {
                    send(line);
                    line = reader.readLine();
                }
            } catch (IOException ex) {
                // The stream was closed, as on Stop. There is nothing more to read.
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
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
        if (stream != null) {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(stream));
        }
    }
}
