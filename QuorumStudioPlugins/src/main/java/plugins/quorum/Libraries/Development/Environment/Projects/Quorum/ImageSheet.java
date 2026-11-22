/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Environment.Projects.Quorum;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import quorum.Libraries.Containers.Array_;
import quorum.Libraries.Containers.Iterator_;
import quorum.Libraries.Language.Object_;
import quorum.Libraries.Language.Types.Text;
import quorum.Libraries.System.File_;


/**
 *
 * @author stefika
 */
public class ImageSheet {
    public java.lang.Object me_ = null;
    
    public String NativeSave(File_ path) {
        quorum.Libraries.Development.Environment.Projects.Quorum.ImageSheet sheet = (quorum.Libraries.Development.Environment.Projects.Quorum.ImageSheet) me_;
        Array_ array = sheet.Get_Libraries_Development_Environment_Projects_Quorum_ImageSheet__values_();
        Iterator_ it = array.GetIterator();
        
        int boost = 5;
        String[] args = new String[boost + array.GetSize()];
        String projectPath =  path.GetAbsolutePath();
        
        int i = boost;
        while(it.HasNext()) {
            Object_ o = it.Next();
            Text t = (Text) o;
            String next = t.GetValue();
            java.io.File file = new java.io.File(projectPath + java.io.File.separator + next);
            String imagePath = file.getAbsolutePath();
            args[i] = imagePath;
            i++;
        }
        
        String buildPath = sheet.GetBuildPath();
        java.io.File output = new java.io.File(projectPath + java.io.File.separator + buildPath);
        String outputPath = output.getAbsolutePath();
        String sheetName = sheet.GetName();
        args[3] = outputPath;
        args[4] = sheetName;
        
        args[0] = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        args[1] = "-jar";
        args[2] = sheet.GetImageSheetCreator().GetAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(args);
        File log = new File("log");
        try {
            pb.redirectErrorStream(true);
            pb.redirectOutput(Redirect.appendTo(log));
            Process p = pb.start();
            int exit = p.waitFor();
            if (exit != 0) {
                return "The image sheet " + sheetName + " couldn't be built (exit code " + exit + "). Details are in " + log.getAbsolutePath() + ".";
            }
            return "";
        } catch (IOException ex) {
            return "The image sheet " + sheetName + " couldn't be built: " + ex.getMessage();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "Building the image sheet " + sheetName + " was interrupted.";
        }
    }
}
