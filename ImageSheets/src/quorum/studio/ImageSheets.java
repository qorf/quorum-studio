/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quorum.studio;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import java.io.File;

/**
 *
 * @author stefika
 */
public class ImageSheets {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.flattenPaths = true;
        
        TexturePacker packer = new TexturePacker(settings);
        String path = args[0];
        String sheetName = args[1];
        
        for(int i = 2; i < args.length; i++) {
            File file = new java.io.File(args[i]);
            packer.addImage(file);
            
        }
        
        File output = new java.io.File(path);
        
        //first delete them if they exist
        java.io.File png = new java.io.File(path + "/" + sheetName + ".png");
        java.io.File atlas = new java.io.File(path + "/" + sheetName + ".atlas");
        if(png.exists()) {
            png.delete();
        }
        if(atlas.exists()) {
            atlas.delete();
        }
        packer.pack(output, sheetName);
    }
}
