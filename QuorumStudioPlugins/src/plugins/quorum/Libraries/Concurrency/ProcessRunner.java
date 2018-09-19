/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import quorum.Libraries.Containers.Array_;
import quorum.Libraries.Language.Object_;
import quorum.Libraries.Language.Types.Text_;
import quorum.Libraries.System.File_;

/**
 *
 * @author stefika
 */
public class ProcessRunner {
    public java.lang.Object me_ = null;
    private quorum.Libraries.Concurrency.ProcessRunner myProcess = null;
    private File_ directory = null;
    public boolean cancelled = false;
    QuorumProcessWatcher watch = null;
    
    public void Run(String name, Array_ flags) {
        myProcess = (quorum.Libraries.Concurrency.ProcessRunner) me_;
        ProcessBuilder builder;
        ArrayList<String> list = new ArrayList<>();
        list.add(name);
        for(int i = 0; i < flags.GetSize(); i++) {
            Object_ o = flags.Get(i);
            Text_ t = (Text_) o;
            String text = t.GetValue();
            list.add(text);
        }
        builder = new ProcessBuilder(list);
        builder.directory(new File(directory.GetAbsolutePath()));
        try {
            myProcess.FireProcessStartedEvent();
            Process process = builder.start();
            watch = new QuorumProcessWatcher(process.getInputStream());
            OutputStream outputStream = process.getOutputStream();
            watch.setStream(outputStream);
            watch.start();
            boolean alive = process.isAlive();
            process.waitFor();
            watch.wasDestroyed = true;
            cancelled = true;
            watch.flush();
            process.destroy();
            myProcess.FireProcessStoppedEvent();
            watch = null;
        } catch (IOException ex) {
            Logger.getLogger(ProcessRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void SetDirectory(File_ folder) {
        directory = folder;
    }
    
    public File_ GetDirectory() {
        return directory;
    }
    
    public void Cancel() {
        cancelled = true;
    }
    
    public void SendInput(String value) {
        if(watch != null) {
            watch.SendInput(value);
        }
    }
    
    protected class QuorumProcessWatcher implements Runnable {
        private BufferedReader bufferedReader = null;
        private OutputStream outputStream;
        private InputStream inputStream;
        BufferedWriter bufferedWriter;
        private Thread blinker = null;
        public boolean running = false;
        
        public boolean wasDestroyed = false;

        public QuorumProcessWatcher(InputStream in) {
            inputStream = in;
            bufferedReader = new BufferedReader(new InputStreamReader(in));
        }

        public void SendInput(String value) {
            if(bufferedWriter != null) {
                try {
                    bufferedWriter.write(value);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        public void start() {
            if (!running) {
                blinker = new Thread(this);
                blinker.setName("Quorum Process Watcher");
                blinker.start();
                
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        Reader in = io.getIn();
//                        BufferedReader br = new BufferedReader(in);
//                        
//                        while (!cancelled) {
//                            try {
//                                if(br.ready()) {
//                                    String line = br.readLine();
//                                    if(bufferedWriter != null) {
//                                    try {
//                                            bufferedWriter.write(line);
//                                            bufferedWriter.newLine();
//                                            bufferedWriter.flush();
//                                        } catch (IOException ex) {
//                                            ex.printStackTrace();
//                                        }
//                                    }
//                                }
//                                Thread.sleep(20);
//                            } catch (IOException ex) {
//                                ex.printStackTrace();
//                            } catch (InterruptedException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                        try {
//                            br.close();
//                            in.close();
//                            bufferedWriter.close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
                    }
                });
                thread.setName("IDE Input");
                thread.start();
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
                    if(myProcess != null) {
                        myProcess.FireProcessOutputEvent(line);
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
                        if(myProcess != null) {
                            myProcess.FireProcessOutputEvent(line);
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
}
