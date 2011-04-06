/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.dicom;

import java.io.IOException;
import java.lang.reflect.Field;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.TagUtils;

public class DicomInfoExtractor {
	
	String filePath;
	private int tag;
	  
   public DicomInfoExtractor(String args[]) {
       if (args == null || args.length <1 || args.length > 4 ){
           showUsage();
           return;
       }
       for(int i=0; i<args.length;){
           if (args[i].equalsIgnoreCase("-file") ) {
               filePath=args[i+1];    
               i = i+2;
           }else if (args[i].equalsIgnoreCase("-tag") ) {
               String tagStr=args[i+1];
               tagStr = tagStr.trim();
               sanityCheckTag(tagStr);
               tag =Tag.toTag(tagStr);
               i = i+2;
           }
       }
       if (filePath == null) {
           showUsage(); System.exit(1);
       }
   }
   
   public String getFilePath() {
	   return filePath;
   }
   
  
   
   private void sanityCheckTag(String tagStr) {
       if (tagStr != null && tagStr.indexOf(",") == -1) {
           System.err.println("Invalid Tag format...expecting tags of the format ****,****");
           System.exit(1);
       }
   }
   
   private void showUsage() {
       System.out.println("DicomInfoExtractor Usage:");
       System.out.println("DicomInfoExtractor -file <Path to file> [-tag<Dicom Tag>]");
   }
   
   private String getTagValue(DicomObject dcmObj) {
	   return getTagValue(dcmObj, tag);
   }
   
   public  String getTagValue(DicomObject dcmObj, int tag) {
	   return dcmObj.getString(tag);
   }
   
   
   public String getTagValue(DicomObject dcmObj, String tagStr) {
       String tagString = tagStr.trim();
       int tagInt =Tag.toTag(tagString);
       return getTagValue(dcmObj, tagInt);
   }
   
   
   public static void main(String args[]) {
	   try {
	       DicomInfoExtractor dcmInfo = new DicomInfoExtractor(args);
	       //dcmInfo.showInfo();
	       dcmInfo.showAll();
	       System.out.println("Siemens Private Tag: ");
	       String filePath = dcmInfo.getFilePath();
	       SiemensMosaicInfoExtractor siemensMosaic = new SiemensMosaicInfoExtractor(filePath);
			try {
				siemensMosaic.load();
				//String addendum1 = siemensMosaic.getSiemensAddendum1();
				String addendum2 = siemensMosaic.getSiemensAddendum2();
				//System.out.println("(0029,1010): " + addendum1);
				System.out.println("(0029,1020): " + addendum2);
			}catch(IOException ioe) {}
	       System.exit(0);
	   }catch(IOException ioe) {
		   ioe.printStackTrace();
		   System.exit(1);
	   }
   }
   
   public void showInfo() throws IOException{
	   DicomFileReader dcmFileReader = new DicomFileReader(filePath);
	   DicomObject dcmObj = dcmFileReader.getDicomObject();
	   Object dcmHeader = getTagValue(dcmObj);
       if (dcmHeader != null) 
           System.out.println(dcmHeader);
       else
           System.exit(2);
    }
   
   public void showAll() throws IOException{
	   DicomFileReader dcmFileReader = new DicomFileReader(filePath);
	   DicomObject dcmObj = dcmFileReader.getDicomObject();
	   final Field fields[] = Tag.class.getFields();
       for (int i = 0; i < fields.length; ++i) {
         String tag = fields[i].getName();
         try {
	         Object dcmHeader = getTagValue(dcmObj, tag);
	         if (dcmHeader != null) {
	        	 System.out.println(tag +  TagUtils.toString(Tag.toTag(tag)) + " : " + dcmHeader);
	         }
         }catch(UnsupportedOperationException ue) {
        	 System.out.println("Tag " + tag + " not supported ");
         }
       }
   }
   
   
   
   
   
   
   
}
