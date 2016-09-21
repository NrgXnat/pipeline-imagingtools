/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.dicom;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.nrg.xnat.plexiviewer.utils.UnzipFile;

public class DicomFileReader {
	
	  private String filePath;
	  private DicomObject dcmObj ;
	  private String fileName;
	  private String directory;
	  private boolean zipped;

	  
	public DicomFileReader(String filePath) {
	    	this.filePath = filePath;
	    	zipped = false;
	}
	
	public DicomObject getDicomObject() throws IOException {
		setPath();
		readFile();
		return dcmObj;
	}
	
	 private void setPath() throws IOException {
	       File file = new File(filePath);
	       if (!file.exists()) {
	    	   File zipped = new File(filePath+".gz");
	    	   if (zipped.exists()) {
			       fileName = zipped.getName();
			       directory = zipped.getParent();
	    	   } else {
	    		   System.out.println("File " + filePath + " doesnt exist");
	    		   throw new IOException("File " + filePath + " doesnt exist");
	    	   }
	       }else {
		       fileName = file.getName();
		       directory = file.getParent();
	       }
	       if (!directory.endsWith(File.separator)) directory += File.separator;
	       if (fileName.endsWith(".gz")) {zipped = true; unzip(); }
	   }
	 
	 private void unzip() {
	       if (zipped) {
	           String suffix = "" + new Random(System.currentTimeMillis()).nextInt() ;
	           File tempDir = new File(System.getProperty("user.home"));
	           try {
	               File dir = File.createTempFile( "NRG", suffix,  tempDir);
	               if (dir.exists()) dir.delete();
	               dir.mkdir();
	               new UnzipFile().gunzip(directory + File.separator + fileName, dir.getPath());
	               directory = dir.getPath();
	               fileName = fileName.substring(0, fileName.length() - 3);
	               if (!directory.endsWith(File.separator)) directory += File.separator;
	           }catch (IOException ioe) {System.out.println("DicomInfoExtractor:: Unable to create temporary directory");}
	       }
	   }
	
	private void readFile() throws IOException{
		   dcmObj = new BasicDicomObject();
		   DicomInputStream din = null;
		   try {
			   File dcmFile = new File(directory +  fileName);
		       din = new DicomInputStream(dcmFile);
		       din.readDicomObject(dcmObj, -1);
		   }
		   catch (IOException e) {
		       e.printStackTrace();
		       throw e;
		   }
		   finally {
		       try {
		           if (din != null) din.close();
		           if (zipped) {
		        	   File unzippedFile = new File(directory + fileName);
		        	   unzippedFile.delete();
		        	   unzippedFile = new File(directory);
		        	   unzippedFile.delete();
		           }
		       }
		       catch (IOException ignore) {
		       }
		   }
	   }
}
