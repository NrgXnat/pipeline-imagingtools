/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.utils;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.imagingtools.dicom.DicomFileReader;
import org.nrg.imagingtools.dicom.DistortionCorrected;
import org.nrg.imagingtools.dicom.SiemensMosaicInfoExtractor;
import org.nrg.pipeline.xmlbeans.catalog.DCMCatalogDocument;
import org.nrg.pipeline.xmlbeans.catalog.Entry;
import org.nrg.pipeline.xmlbeans.catalog.Catalog.Entries;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.plexiViewer.Reader.IFHReader;
import org.nrg.xdat.bean.ArcProjectBean;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.XnatAbstractresourceBean;
import org.nrg.xdat.bean.XnatDicomseriesBean;
import org.nrg.xdat.bean.XnatImageresourceBean;
import org.nrg.xdat.bean.XnatImageresourceseriesBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.bean.XnatResourceBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;
import org.nrg.xdat.bean.XnatResourceseriesBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xnattools.service.WebServiceClient;
import org.nrg.xnattools.xml.XMLSearch;
import org.xml.sax.SAXException;

public class FileUtils {
    
    /*
     * Returns 1 if the scan needs a distortion correction to be applied
     * Expects that a session xml exists in the user's home directory
     * 
     */
    public static int needsGradUnwarp(String scanFilesDir) {
        int rtn = 0;
        File scanFileFolder = new File(scanFilesDir);
        if (scanFileFolder.exists() && scanFileFolder.isDirectory()) {
            File[] files = scanFileFolder.listFiles();
            if (files != null && files.length > 0) {
                File aDicomFile = files[0];
    //            String isMosaic = isMosaic(aDicomFile.getAbsolutePath());
    //            if (!isMosaic.equals("0") && !isMosaic.equals("1") ) {
	                DistortionCorrected distortionCorrected = new DistortionCorrected(new String[]{aDicomFile.getAbsolutePath()});
	                String distortionStr = distortionCorrected.detect();
	                if (distortionStr.equalsIgnoreCase("ND")) rtn = 1;
    //            }
            }
        }
        return rtn;
    }
    
    /*
     * Returns first file found in a folder
     * 
     */
    public static String getSingleFile(String scanFilesDir) {
        String rtn = "";
        File scanFileFolder = new File(scanFilesDir);
        if (scanFileFolder.exists() && scanFileFolder.isDirectory()) {
            File[] files = scanFileFolder.listFiles();
            if (files != null && files.length > 0) {
                File aDicomFile = files[0];
                rtn = aDicomFile.getAbsolutePath();
            }
        }
        return rtn;
    }
    
    /*
     * Returns first file found in a folder
     * 
     */
    public static String getSingleFileName(String scanFilesDir) {
        String rtn = "";
        File scanFileFolder = new File(scanFilesDir);
        if (scanFileFolder.exists() && scanFileFolder.isDirectory()) {
        	File[] files = scanFileFolder.listFiles();
            if (files != null && files.length > 0) {
            	File aDicomFile = files[0];
                rtn = aDicomFile.getName();
            }
        }
        return rtn;
    }
    
    /*public static String getSingleFileNameFromCatalog(String catalogFilePath) {
        String rtn = "";
        try {
        	DCMCatalogDocument catalog = (DCMCatalogDocument)new XmlReader().read(catalogFilePath, false);
        	String uri = catalog.getDCMCatalog().getEntries().getEntryArray(0).getURI();
        	int indexofSlash = uri.lastIndexOf("/");
        	if (indexofSlash != -1) {
        		rtn = uri.substring(indexofSlash+1);
        	}else {
        		rtn = uri;
        	}
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }*/
    
    private static XnatAbstractresourceBean getFileByContent( XnatImagescandataBean imageScan, String content) {
    	XnatAbstractresourceBean rtn = null;
    	try {
				for (int i = 0; i < imageScan.getFile().size(); i++) {
					XnatAbstractresourceBean f = imageScan.getFile().get(i);
				    if (f instanceof XnatResourceBean) {
				    	XnatResourceBean resource = (XnatResourceBean)f;
				    	if (resource.getContent().equals(content)) {
				    		rtn = f;
				    		break;
				    	}
				    }else if (f instanceof XnatImageresourceBean) {
				    	XnatImageresourceBean imageResource = (XnatImageresourceBean)f;
				    	if (imageResource.getContent().equals(content)) {
				    		rtn = f;
				    		break;
				    	}
				    }else if (f instanceof XnatResourceseriesBean) {
				    	XnatResourceseriesBean resourceSeries = (XnatResourceseriesBean)f;
				    	if (resourceSeries.getContent().equals(content)) {
				    		rtn = f;
				    		break;
				    	}
				    }else if (f instanceof XnatDicomseriesBean) {
				    	XnatDicomseriesBean resourceSeries = (XnatDicomseriesBean)f;
				    	if (resourceSeries.getContent().equals(content)) {
				    		rtn = f;
				    		break;
				    	}
				    }else if (f instanceof XnatResourcecatalogBean) {
				    	XnatResourcecatalogBean resourceCat = (XnatResourcecatalogBean)f;
				    	if (resourceCat.getContent().equals(content)) {
				    		rtn = f;
				    		break;
				    	}
				    }
					
				}
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return rtn;
    }
    
    private static XnatAbstractresourceBean getScanFile(XnatImagescandataBean imagescan) {
    	XnatAbstractresourceBean file = null;
    	String scanType = imagescan.getType();
    	String rawScanContentCode = scanType + "_RAW";
    	ArrayList <XnatAbstractresourceBean> files = imagescan.getFile();
    	if (files.size() > 0) {
	    	if (files.size() == 1) {
	        	file = files.get(0);
	        }else  {
	        	file = getFileByContent(imagescan, rawScanContentCode);
	        	if (file == null)
	        		file = getFileByContent(imagescan, "RAW");
	        }
    	}
    	return file;
    }
    
    private static XnatImagescandataBean getScan(XnatImagesessiondataBean imageSession, String imageScanId) {
    	XnatImagescandataBean imageScan = null;
    ArrayList<XnatImagescandataBean> imageScans = imageSession.getScans_scan(); 
    if (imageScans != null && imageScans.size() > 0) {
        for (int i = 0; i < imageScans.size(); i++) {
            XnatImagescandataBean mrscan = imageScans.get(i); 
            if (mrscan.getId().equals(imageScanId)) {
            	imageScan = mrscan;
            	break;
            }
            }
        }
     return imageScan;
    }
    
    private static ArrayList<XnatImagescandataBean> getScanByType(XnatImagesessiondataBean imageSession, String imageScanType) {
    	ArrayList<XnatImagescandataBean> imageScan = new ArrayList<XnatImagescandataBean>();
    	if (imageScanType == null) return imageScan;
    	ArrayList<XnatImagescandataBean> imageScans = imageSession.getScans_scan(); 
    if (imageScans != null && imageScans.size() > 0) {
        for (int i = 0; i < imageScans.size(); i++) {
            XnatImagescandataBean mrscan = imageScans.get(i); 
            if (imageScanType.equals(mrscan.getType())) {
            	imageScan.add(mrscan);
            }
            }
        }
     return imageScan;
    }
    
  
    public static String GetPathToDynamicEmission(String host, String user, String pwd, String imageSessionId) {
        String rtn = null;
        try {
        	XnatPetsessiondataBean pet  = (XnatPetsessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
		   ArrayList<XnatImagescandataBean> scans = pet.getScans_scan();
		   for (int i=0; i < scans.size(); i++) {
			   XnatImagescandataBean scan = scans.get(i);
			   if (scan.getType().equals("Dynamic emission")) {
				   ArrayList<XnatAbstractresourceBean> files = scan.getFile();
				   if (files.size() == 1) {
						  rtn = getUriFromFile(files.get(0));
				   }else {
					   for (int j =0; j < files.size(); j++) {
						   XnatResourceBean file = (XnatResourceBean)files.get(j);
						   if (file.getContent().equals("Dynamic emission_RAW")) {
							   rtn = getUriFromFile(file);
							   break;
						   }
					   }
				   }
			   }
		   }
        }catch(Exception e) {e.printStackTrace();}
		   return rtn;
	   }
        
        
        private static String getUriFromFile(XnatAbstractresourceBean file) throws IOException, SAXException{
        	String rtn = null;
        	 if (file instanceof XnatImageresourceBean) {
				   XnatImageresourceBean imageRsc = (XnatImageresourceBean)file;
				   rtn = imageRsc.getUri();
			   }else if (file instanceof XnatResourcecatalogBean) {
				   XnatResourcecatalogBean rscCatalog = (XnatResourcecatalogBean)file;
	               String catalogPath = rscCatalog.getUri();
	               if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
	               CatCatalogBean catalogBean =  (CatCatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
	               ArrayList<CatEntryBean>  catalogEntries = catalogBean.getEntries_entry();
	               String uri = catalogEntries.get(0).getUri();
	               int i = catalogPath.lastIndexOf("/");
	               catalogPath = catalogPath.substring(0, i);
	               rtn =  catalogPath + "/" + uri;
			   }
        	 return rtn;
        }
    
        public static String GetFileSeparator() {
        	return File.separator;
        }
        
        public static String GetFile(String host, String user, String pwd,  String project, String collection, String file_label) {
        	String filename="";
        	WebServiceClient webClient = new WebServiceClient(host, user, pwd);
        	try {
        		ByteArrayOutputStream out = new ByteArrayOutputStream();

               	webClient.connect("REST/projects/" + project + "/files?format=json", out);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

               	
                String jsonTxt = org.apache.commons.io.IOUtils.toString( in);
                System.out.println(jsonTxt);
                JSONObject jsonObj = null;
                JSON json = JSONSerializer.toJSON( jsonTxt );
                 if (json instanceof JSONObject){
                	jsonObj = (JSONObject) json;
                    System.out.println(jsonObj.get("ResultSet"));
                    JSONObject internal = (JSONObject)JSONSerializer.toJSON(jsonObj.getString("ResultSet"));
                    JSONArray internalArray = (JSONArray)JSONSerializer.toJSON(internal.getString("Result"));
                    if (internalArray.size() == 1) {
                        internal = internalArray.getJSONObject(0);
                        if (internal.getString("file_content") != null && internal.getString("collection") != null && internal.getString("collection").equals(collection) && internal.getString("file_content").equals(file_label)) {
                        	filename = internal.getString("Name");
                        }
                    }else {
                    	for (int i=0; i< internalArray.size(); i++) {
                            internal = internalArray.getJSONObject(i);
                            if (internal.getString("file_content") != null && internal.getString("collection") != null && internal.getString("collection").equals(collection) && internal.getString("file_content").equals(file_label)) {
                            	filename = internal.getString("Name");
                            	break;	
                            }	
                    	}
                    }
                }
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	System.out.println("File name is: " + filename);
        	return filename;
        }
        
		public static String GetCachePath(String host, String user, String pwd,   String project) {
	       	WebServiceClient webClient = new WebServiceClient(host, user, pwd);
	       	String rtn = null;
        	try {
        		ByteArrayOutputStream out = new ByteArrayOutputStream();

               	webClient.connect("REST/projects/" + project + "/archive_spec", out);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                XDATXMLReader reader = new XDATXMLReader();
                BaseElement base = reader.parse(in);
                ArcProjectBean arcProject = (ArcProjectBean)base;
                rtn = arcProject.getPaths().getCachepath();
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	System.out.println("Cachepath " + rtn);
        	 return rtn;			
			
		}
        
        public static String GetColumn(String host, String user, String pwd,  String uri, String colname) {
        	String rtn="";
        	WebServiceClient webClient = new WebServiceClient(host, user, pwd);
        	try {
        		ByteArrayOutputStream out = new ByteArrayOutputStream();
               	webClient.connect(uri, out);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                
                String jsonTxt = org.apache.commons.io.IOUtils.toString( in);
                JSONObject jsonObj = null;
                JSON json = JSONSerializer.toJSON( jsonTxt );
                 if (json instanceof JSONObject){
                	jsonObj = (JSONObject) json;
                    System.out.println(jsonObj.get("ResultSet"));
                    JSONObject internal = (JSONObject)JSONSerializer.toJSON(jsonObj.getString("ResultSet"));
                    JSONArray internalArray = (JSONArray)JSONSerializer.toJSON(internal.getString("Result"));
                    if (internalArray.size() == 1) {
                        internal = internalArray.getJSONObject(0);
                        	rtn = internal.getString(colname);
                    }
                }
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	System.out.println(colname+"=" + rtn);
        	return rtn;
        }
        
        public static boolean FileExists(String host, String user, String pwd,  String project, String collection, String file_name) {
        	boolean exists=false;
        	WebServiceClient webClient = new WebServiceClient(host, user, pwd);
        	try {
        		ByteArrayOutputStream out = new ByteArrayOutputStream();

               	webClient.connect("REST/projects/" + project + "/files?format=json", out);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

               	
                String jsonTxt = org.apache.commons.io.IOUtils.toString( in);
                System.out.println(jsonTxt);
                JSONObject jsonObj = null;
                JSON json = JSONSerializer.toJSON( jsonTxt );
                 if (json instanceof JSONObject){
                	jsonObj = (JSONObject) json;
                    System.out.println(jsonObj.get("ResultSet"));
                    JSONObject internal = (JSONObject)JSONSerializer.toJSON(jsonObj.getString("ResultSet"));
                    JSONArray internalArray = (JSONArray)JSONSerializer.toJSON(internal.getString("Result"));
                    if (internalArray.size() == 1) {
                        internal = internalArray.getJSONObject(0);
                        if (internal.getString("file_content") != null && internal.getString("collection") != null && internal.getString("collection").equals(collection) && internal.getString("Name").equals(file_name)) {
                        	exists=true;
                        }
                    }else {
                    	for (int i=0; i< internalArray.size(); i++) {
                            internal = internalArray.getJSONObject(i);
                            if (internal.getString("file_content") != null && internal.getString("collection") != null && internal.getString("collection").equals(collection) && internal.getString("Name").equals(file_name)) {
                            	exists=true;
                            	break;	
                            }	
                    	}
                    }
                }
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        	System.out.println("File exists: " + exists);
        	return exists;
        }
        
        public static String getSingleDicomFileForScan(String host, String user, String pwd, String imageSessionId, String imageScanId) {
        String rtn = "";
        try {
        	XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
        	XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
        	XnatAbstractresourceBean file =  getScanFile(imageScan);
        	  if (file instanceof XnatResourcecatalogBean) {
           	   XnatResourcecatalogBean catalog = (XnatResourcecatalogBean)file;
               String catalogPath = catalog.getUri();
               if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
               CatDcmcatalogBean dcmCatalogBean =  (CatDcmcatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
               ArrayList<CatEntryBean>  catalogEntries = dcmCatalogBean.getEntries_entry();
               int lastIndexOfSlash = catalogPath.lastIndexOf("/");
               String uri = catalogEntries.get(0).getUri();               
               if (lastIndexOfSlash != -1) {
            	   catalogPath = catalogPath.substring(0,lastIndexOfSlash);
            	   rtn = catalogPath + "/" + uri;
               }else {
		    		rtn = uri;
		    	}
        	  }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
    public static String getSingleDicomFileNameForScan(String host, String user, String pwd, String imageSessionId, String imageScanId) {
        String rtn = "";
        try {
        	XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
        	XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
        	XnatAbstractresourceBean file =  getScanFile(imageScan);
        	  if (file instanceof XnatResourcecatalogBean) {
           	   XnatResourcecatalogBean catalog = (XnatResourcecatalogBean)file;
               String catalogPath = catalog.getUri();
               if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
               CatDcmcatalogBean dcmCatalogBean =  (CatDcmcatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
               ArrayList<CatEntryBean>  catalogEntries = dcmCatalogBean.getEntries_entry();
               String uri = catalogEntries.get(0).getUri();
               int lastIndexOfSlash = uri.lastIndexOf("/");
               if (lastIndexOfSlash != -1) {
            	   rtn = uri.substring(lastIndexOfSlash);
               }else {
		    		rtn =File.separator +  uri;
		    	}
        	  }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
    
    public static ArrayList<String> GetScanIdsByType(String host, String user, String pwd, String imageSessionId, String imageScanType) {
        ArrayList<String> rtn = null;
        String[] types= imageScanType.split(",");
        try {
        	XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
        	for (int i =0; i < types.length; i++) {
        		String scanType = types[i].trim();
            	ArrayList<XnatImagescandataBean> imageScan = getScanByType(imageSession, scanType);
            	if (imageScan.size() == 0)  continue;
            	if (rtn==null) rtn = new ArrayList<String>();
            	for (int j =0; j < imageScan.size(); j++) {
            		rtn.add(imageScan.get(j).getId());
            		System.out.println("Found scan " + imageScan.get(j).getId());
            	}
        	}
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
    public static String getSingleDicomFileNameForScan(XnatImagesessiondataBean imageSession, String imageScanId) {
        String rtn = "";
        try {
        	XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
        	XnatAbstractresourceBean file =  getScanFile(imageScan);
        	  if (file instanceof XnatResourcecatalogBean) {
           	   XnatResourcecatalogBean catalog = (XnatResourcecatalogBean)file;
               String catalogPath = catalog.getUri();
               if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
               CatDcmcatalogBean dcmCatalogBean =  (CatDcmcatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
               ArrayList<CatEntryBean>  catalogEntries = dcmCatalogBean.getEntries_entry();
               String uri = catalogEntries.get(0).getUri();
               int lastIndexOfSlash = uri.lastIndexOf("/");
               if (lastIndexOfSlash != -1) {
            	   rtn = uri.substring(lastIndexOfSlash);
               }else {
		    		rtn = File.separator +   uri;
		    	}
        	  }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
   
    
    public static String getSingleFileNameForScan(String host, String user, String pwd, String imageSessionId, String imageScanId) {
        String rtn = "";
        try {
        	XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
        	rtn = getSingleDicomFileNameForScan(imageSession, imageScanId);
        	if (rtn.equals("")) {
        		rtn = getSingleImaFileNameForScan(imageSession, imageScanId);
        	}
        }catch(Exception e) {
        	e.printStackTrace();
        }    	
        return rtn;
    }
    
    public static String getSingleImaFileNameForScan(String host, String user, String pwd, String imageSessionId, String imageScanId) {
        String rtn = "";
        try {
        	XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
        	XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
        	XnatAbstractresourceBean file =  getScanFile(imageScan);
        	  if (file instanceof XnatImageresourceseriesBean) {
        		  XnatImageresourceseriesBean imgRsc = (XnatImageresourceseriesBean)file;
        		  rtn = imgRsc.getName();
        	  }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
    public static String getSingleImaFileNameForScan(XnatImagesessiondataBean imageSession, String imageScanId) {
        String rtn = "";
        try {
        	XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
        	XnatAbstractresourceBean file =  getScanFile(imageScan);
        	  if (file instanceof XnatImageresourceseriesBean) {
        		  XnatImageresourceseriesBean imgRsc = (XnatImageresourceseriesBean)file;
        		  rtn = File.separator + imgRsc.getName();
        	  }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
    public static String getSingleFileNameFromCatalog(String catalogFilePath) {
    String rtn = "";
    try {
    	DCMCatalogDocument catalog = (DCMCatalogDocument)new XmlReader().read(catalogFilePath, false);
    	String uri = catalog.getDCMCatalog().getEntries().getEntryArray(0).getURI();
    	//This URI will be relative to the CatalogFilePath
    	int indexofSlash = catalogFilePath.lastIndexOf("/");
    	String rootDir = "";
    	if (indexofSlash != -1) {
    		rootDir = catalogFilePath.substring(0, indexofSlash);
    		rtn = rootDir + "/" + uri;
    	}else {
    		rtn = uri;
    	}
    }catch(Exception e) {
    	e.printStackTrace();
    }
    return rtn;
}


    


    public static void makeListFileFromCatalog(String catalogFilePath, String outFilePath) {
        try {
        	File catalogFile = new File(catalogFilePath);
        	String rootDir = catalogFile.getParent();
        	 FileWriter fstream = new FileWriter(outFilePath);
             BufferedWriter out = new BufferedWriter(fstream);
        	DCMCatalogDocument catalog = (DCMCatalogDocument)new XmlReader().read(catalogFilePath, false);
        	Entries entries = catalog.getDCMCatalog().getEntries();
        	for (int i = 0; i < entries.sizeOfEntryArray(); i++) {
        		Entry entry = entries.getEntryArray(i);
        		String uri = entry.getURI();
        		if (!uri.startsWith(File.separator)) {
        			uri = rootDir + File.separator + uri;
        		}
    			out.write(uri + "\n");
        	}
            out.close();
        }catch(Exception e) {
        	e.printStackTrace();
        }
    }

    
    public static String getNumberOfFramesFromCatalog(String catalogFilePath) {
        String rtn = "";
        try {
        	DCMCatalogDocument catalog = (DCMCatalogDocument)new XmlReader().read(catalogFilePath, false);
        	 rtn += catalog.getDCMCatalog().getEntries().getEntryArray().length;
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }
    
    public static String getNumberOfFunctionalFrames(int skipped, String catalogFilePath) {
        String rtn = "";
        String framesFromCatalog = getNumberOfFramesFromCatalog(catalogFilePath);
        rtn = "" + (Integer.parseInt(framesFromCatalog) - skipped);
        return rtn;
    }
    
    /*public static String getNumberOfFrames(String dicomFilePath) {
        String rtn = "";
        try {
        	SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
			siemensDicomFile.load();
			rtn = "" + siemensDicomFile.getNumberOfFrames() ;
			System.out.println("Number of  Frames " + rtn);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return rtn;
    }*/
    

    /*
     * This method returns the format as required by qnt_4dfp.
     * Each IFH file associated with the 4dfp file in the conc file
     * is read and the frames extracted from the IFH file to create the format 
     */
    public static String getFormatFromConcFile(String skip, String concfilePath) throws IOException{
    	String format = "ERROR";
    	File concfile = new File(concfilePath);
    	if (!concfile.exists()) {
    	   System.err.println("Conc file " + concfilePath + "  doesnt exist ");
    	   throw new IOException("File " + concfilePath + " not found");
    	}
    	int skipAsInt = Integer.parseInt(skip);
    	ArrayList<String> filePaths = getFilePathFromConcFile(concfilePath);
    	if (filePaths != null && filePaths.size()>0) {
    		format = "";
        	for (String filepath: filePaths) {
        		File ifhFile = new File(filepath);
        		IFHReader ifh = new IFHReader(ifhFile.getParent(), ifhFile.getName());
        		ifh.getFileInfo();
        		format += skip + "x" + (ifh.getVolumes() - skipAsInt)  + "+"; 
        	}
    	}
    	return format;
    }
    
    private static ArrayList<String> getFilePathFromConcFile(String concfilePath) {
    	ArrayList<String> files = new ArrayList<String>();
    	try {
            BufferedReader in = new BufferedReader(new FileReader(concfilePath));
            String str;
            while ((str = in.readLine()) != null) {
                String[] parts = str.split(":");
                if (parts[0].trim().equals("file")) {
                	files.add(parts[1]);
                }
            }
            in.close();
        } catch (IOException e) {
        }
    	return files;
    }
    
    public static String getArgumentsForUnpack4dfp(String dicomFilePath) {
		try {
			SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
			siemensDicomFile.load();
	        Rectangle tile = siemensDicomFile.getTileDimension();
	        if (tile != null) 
	        	return "  -nx" + (int)tile.getWidth() + " -ny" + (int)tile.getHeight();
	        else {
	        	System.out.println("SiemensMosaicInfoExtractor couldnt find the number of tiles in the mosaic");
	        	return " ";
	        }
        }catch(IOException e) {
        	return "";
        }
	}

    public static String getNXArgumentForUnpack4dfp(String dicomFilePath) {
		try {
			DicomFileReader dicomFileReader = new DicomFileReader(dicomFilePath);
			DicomObject dcmObj = dicomFileReader.getDicomObject();
			String manufacturer = dcmObj.getString(Tag.Manufacturer);
			if  (manufacturer != null) {
				if (manufacturer.equalsIgnoreCase("SIEMENS")) {
					SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
					siemensDicomFile.load();
			        Rectangle tile = siemensDicomFile.getTileDimension();
			        if (tile != null) 
			        	return "" + (int)tile.getWidth(); 
			        else {
			        	System.out.println("SiemensMosaicInfoExtractor couldnt find the number of tiles in the mosaic");
			        	return " ";
			        }
				}else {
					//For all Philips and GE scanners, each slice is a file - there are no MOSAICS
					//so nx is the image width and ny is the image height
					String rtn = dcmObj.getString(Tag.Columns);
					return rtn;
				}
			}else return "";
        }catch(IOException e) {
        	return "";
        }
	}

    public static String getNYArgumentForUnpack4dfp(String dicomFilePath) {
		try {
			DicomFileReader dicomFileReader = new DicomFileReader(dicomFilePath);
			DicomObject dcmObj = dicomFileReader.getDicomObject();
			String manufacturer = dcmObj.getString(Tag.Manufacturer);
			if  (manufacturer != null) {
				if (manufacturer.equalsIgnoreCase("SIEMENS")) {
					SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
					siemensDicomFile.load();
			        Rectangle tile = siemensDicomFile.getTileDimension();
			        if (tile != null) 
			        	return "" + (int)tile.getHeight(); 
			        else {
			        	System.out.println("SiemensMosaicInfoExtractor couldnt find the number of tiles in the mosaic");
			        	return " ";
			        }
				}else {
					//For all Philips and GE scanners, each slice is a file - there are no MOSAICS
					//so nx is the image width and ny is the image height
					String rtn = dcmObj.getString(Tag.Rows);
					return rtn;
				}
			}else return "";
        }catch(IOException e) {
        	return "";
        }
	}
    
    public static String isMosaic(String dicomFilePath) {
    	String rtn = "0";
		try {
			SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
			siemensDicomFile.load();
			if (siemensDicomFile.isMosaic()) {
		        Rectangle tile = siemensDicomFile.getTileDimension();
		        if (tile != null) 
		        	rtn = "" + (int)tile.getWidth();
			}else {
	        	System.out.println("SiemensMosaicInfoExtractor couldnt find the number of tiles in the mosaic");
	        	rtn = "1";
	        }
			return rtn;
        }catch(IOException e) {
        	return "";
        }
	}
    
    public static void main(String args[]) {
       System.out.println("STARTING");
    	GetScanIdsByType("https://cnda.wustl.edu/", "mohanar", "admin",  "CNDA_E23085", "CNTRACS_QA_80_n1, CNTRACS_QA_80_n2, CNTRACS_QA_10, FBIRN_QA_77_n1, FBIRN_QA_77_n2, FBIRN_QA_10, CNTRACS_QA_77");
    	System.out.println("DONE");
    	System.exit(0);
    }
    
    public static String getMosaicTRofSlice(String tr_vol, String dicomFilePath) {
    	String rtn = "0";
		try {
			DicomFileReader dicomFileReader = new DicomFileReader(dicomFilePath);
			DicomObject dcmObj = dicomFileReader.getDicomObject();
			String manufacturer = dcmObj.getString(Tag.Manufacturer);
			if  (manufacturer != null) {
				if (manufacturer.equalsIgnoreCase("SIEMENS")) {
					SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
					siemensDicomFile.load();
					if (siemensDicomFile.isMosaic()) {
						int noOfTiles = siemensDicomFile.getNumberOfTilesInMosaic();
						rtn = "" + Double.parseDouble(tr_vol)/noOfTiles;
					}
				}
			}
			System.out.println("TR_SLC " + rtn);
			return rtn;
		}catch(Exception e) {
			return null;
		}
    }
    
    public static String getMosaicTRofSliceQueryXNAT(String tr_vol, String host, String user, String pwd, String imageSessionId, String imageScanId) {
    	String rtn = "0";
		try {
	    	String dicomFilePath = getSingleDicomFileForScan(host, user, pwd, imageSessionId, imageScanId);
			DicomFileReader dicomFileReader = new DicomFileReader(dicomFilePath);
			DicomObject dcmObj = dicomFileReader.getDicomObject();
			String manufacturer = dcmObj.getString(Tag.Manufacturer);
			if  (manufacturer != null) {
				if (manufacturer.equalsIgnoreCase("SIEMENS")) {
					SiemensMosaicInfoExtractor siemensDicomFile = new SiemensMosaicInfoExtractor(dicomFilePath);
					siemensDicomFile.load();
					if (siemensDicomFile.isMosaic()) {
						int noOfTiles = siemensDicomFile.getNumberOfTilesInMosaic();
						rtn = "" + Double.parseDouble(tr_vol)/noOfTiles;
					}
				}
			}
			System.out.println("TR_SLC " + rtn);
			return rtn;
		}catch(Exception e) {
			return null;
		}
    }
    
}
