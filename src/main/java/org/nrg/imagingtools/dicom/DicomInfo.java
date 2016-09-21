/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.dicom;

import ij.ImagePlus;
import ij.io.Opener;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.nrg.xnat.plexiviewer.utils.UnzipFile;

public class DicomInfo {
    
    private String fileName;
    private String directory;
    private String tag;
    private boolean zipped;
    
    public DicomInfo(String args[]) {
        String file = null;
        if (args == null || args.length <1 || args.length > 4 ){
            showUsage();
            return;
        }
        for(int i=0; i<args.length;){
            if (args[i].equalsIgnoreCase("-file") ) {
                file=args[i+1];    
                i = i+2;
            }else if (args[i].equalsIgnoreCase("-tag") ) {
                tag=args[i+1];
                tag = tag.trim();
                i = i+2;
            }
        }
        if (file == null) {
            showUsage(); System.exit(1);
        }
        zipped = false;
        sanityCheckTag();
        setPath(file);
    }

    public DicomInfo(String filePath, String tag) {
        String file = filePath;
        if (file == null) {
            showUsage(); System.exit(1);
        }
        this.tag = tag;
        zipped = false;
        sanityCheckTag();
        setPath(file);
    }

    private void setPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " doesnt exist");
            System.exit(2);
        }
        fileName = file.getName();
        directory = file.getParent();
        if (!directory.endsWith(File.separator)) directory += File.separator;
        if (fileName.endsWith(".gz")) {zipped = true; unzip(); }

    }
    
    private void sanityCheckTag() {
        if (tag != null && tag.indexOf(",") == -1) {
            System.err.println("Invalid Tag format...expecting tags of the format 0008,0008");
            System.exit(1);
        }
    }
    
    public String getTagValue() {
        String rtn = null;
        try {
            Opener opener = new Opener();
            opener.setSilentMode(true);
            ImagePlus img = opener.openImage(directory + fileName);
            if (img != null) {
                rtn = (String)img.getProperty("Info");
                if (tag != null) {
                    int tagIndex = rtn.indexOf(tag);
                    if (tagIndex != -1) {
                        System.out.println(rtn.substring(tagIndex));
                    	rtn = (rtn.substring(tagIndex).split("\n")[0]);
                    }else {
                        System.err.println("Tag " + tag +" not present in the header");
                        System.exit(2);
                    }
                }
            }else {
                System.err.println("Couldnt read dicom image from " + directory + fileName);
                System.exit(2);
            }
        }catch(Exception e) {
            System.err.println(e.getClass().getName() + " " + e.getMessage());
            System.exit(2);
        }
        return rtn;
    }
    
    public void showInfo() {
       String dcmHeader = getTagValue();
       if (dcmHeader != null) 
           System.out.println(dcmHeader);
       else
           System.exit(2);
    }
    
    private void showUsage() {
        System.out.println("Dicom File Info usage:");
        System.out.println("DicomInfo -file <Path to file> [-tag<Dicom Tag>]");
    }
    
    private void unzip() {
        if (zipped) {
            String suffix =  "_" + new Random().nextInt();
            File tempDir = new File(System.getProperty("user.home"));
            try {
                File dir = File.createTempFile( "NRG", suffix,  tempDir);
                if (dir.exists()) dir.delete();
                dir.mkdir();
                new UnzipFile().gunzip(directory + File.separator + fileName, dir.getPath());
                directory = dir.getPath();
            }catch (IOException ioe) {System.out.println("DicomSequence:: Unable to create temporary directory");}
        }
    }
    
    public static void main(String args[]) {
        DicomInfo dcmInfo = new DicomInfo(args);
        dcmInfo.showInfo();
        System.exit(0);
    }
}
