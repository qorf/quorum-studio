/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

import java.io.File;
import java.io.IOException;
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
    private File_ directory = null;
    
    public void Run(String name, Array_ flags) {
        ProcessBuilder builder;
        
        ArrayList<String> list = new ArrayList<>();
        list.add(name);
        for(int i = 0; i < flags.GetSize(); i++) {
            Object_ o = flags.Get(i);
            Text_ t = (Text_) o;
            String text = t.GetValue();
            list.add(text);
            System.out.println(text);
        }
        
        builder = new ProcessBuilder(list);
        builder.directory(new File(directory.GetAbsolutePath()));
        try {
            Process start = builder.start();
            boolean alive = start.isAlive();
            start.waitFor();
            start.destroy();
        } catch (IOException ex) {
            Logger.getLogger(ProcessRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void SetDirectory(File_ folder) {
        directory = folder;
    }
}
