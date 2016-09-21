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
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.imagingtools.dicom.DicomFileReader;
import org.nrg.imagingtools.dicom.DistortionCorrected;
import org.nrg.imagingtools.dicom.SiemensMosaicInfoExtractor;
import org.nrg.pipeline.xmlbeans.catalog.Catalog.Entries;
import org.nrg.pipeline.xmlbeans.catalog.DCMCatalogDocument;
import org.nrg.pipeline.xmlbeans.catalog.Entry;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.xnat.plexiviewer.reader.IFHReader;
import org.nrg.xdat.bean.ArcProjectBean;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.ValProtocoldataBean;
import org.nrg.xdat.bean.XnatAbstractresourceBean;
import org.nrg.xdat.bean.XnatDicomseriesBean;
import org.nrg.xdat.bean.XnatImageassessordataBean;
import org.nrg.xdat.bean.XnatImageresourceBean;
import org.nrg.xdat.bean.XnatImageresourceseriesBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.bean.XnatQcmanualassessordataBean;
import org.nrg.xdat.bean.XnatResourceBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;
import org.nrg.xdat.bean.XnatResourceseriesBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatQcscandataI;
import org.nrg.xnattools.SessionManager;
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

    private static XnatAbstractresourceI getFileByContent( XnatImagescandataBean imageScan, String content) {
        XnatAbstractresourceI rtn = null;
        try {
            for (int i = 0; i < imageScan.getFile().size(); i++) {
                XnatAbstractresourceI f = imageScan.getFile().get(i);
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

    private static XnatAbstractresourceI getFileLikeContent( XnatImagescandataBean imageScan, String content) {
        XnatAbstractresourceI rtn = null;
        try {
            for (int i = 0; i < imageScan.getFile().size(); i++) {
                XnatAbstractresourceI f = imageScan.getFile().get(i);
                if (f instanceof XnatResourceBean) {
                    XnatResourceBean resource = (XnatResourceBean)f;
                    if (resource.getContent().endsWith(content)) {
                        rtn = f;
                        break;
                    }
                }else if (f instanceof XnatImageresourceBean) {
                    XnatImageresourceBean imageResource = (XnatImageresourceBean)f;
                    if (imageResource.getContent().endsWith(content)) {
                        rtn = f;
                        break;
                    }
                }else if (f instanceof XnatResourceseriesBean) {
                    XnatResourceseriesBean resourceSeries = (XnatResourceseriesBean)f;
                    if (resourceSeries.getContent().endsWith(content)) {
                        rtn = f;
                        break;
                    }
                }else if (f instanceof XnatDicomseriesBean) {
                    XnatDicomseriesBean resourceSeries = (XnatDicomseriesBean)f;
                    if (resourceSeries.getContent().endsWith(content)) {
                        rtn = f;
                        break;
                    }
                }else if (f instanceof XnatResourcecatalogBean) {
                    XnatResourcecatalogBean resourceCat = (XnatResourcecatalogBean)f;
                    if (resourceCat.getContent().endsWith(content)) {
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


    public static String GetScanFileLabel(String host, String user, String pwd, String imageSessionId, String imageScanId) {
        String rtn = null;
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
            XnatAbstractresourceI file =  getScanFile(imageScan);
            //System.out.println("File uri " + imageSessionId + " " + imageScanId + "  " + file.getXSIType());
            rtn = file.getLabel();
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (rtn==null) rtn="null";

        return rtn;
    }

    private static XnatAbstractresourceI getScanFile(XnatImagescandataBean imagescan) {
        XnatAbstractresourceI file = null;
        String scanType = imagescan.getType();
        String rawScanContentCode = scanType + "_RAW";
        List <XnatAbstractresourceI> files = imagescan.getFile();
        if (files.size() > 0) {
            if (files.size() == 1) {
                file = files.get(0);
            }else  {
                file = getFileByContent(imagescan, rawScanContentCode);
                if (file == null)
                    file = getFileByContent(imagescan, "RAW");
                if (file ==null)
                    file = getFileLikeContent(imagescan, "_RAW");
            }
        }
        return file;
    }

    private static XnatImagescandataBean getScan(XnatImagesessiondataBean imageSession, String imageScanId) {
        XnatImagescandataBean imageScan = null;
        List<XnatImagescandataBean> imageScans = imageSession.getScans_scan();
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
        List<XnatImagescandataBean> imageScans = imageSession.getScans_scan();
        String[] scanTypes = imageScanType.trim().split(",");
        Hashtable scanTypeHash = new Hashtable();
        for (int k=0; k< scanTypes.length; k++) {
            scanTypeHash.put(scanTypes[k], "1");
        }
        if (imageScans != null && imageScans.size() > 0) {
            for (int i = 0; i < imageScans.size(); i++) {
                XnatImagescandataBean mrscan = imageScans.get(i);
                if (scanTypeHash.containsKey(mrscan.getType())) {
                    imageScan.add(mrscan);
                }
            }
        }
        return imageScan;
    }

    private static ArrayList<XnatImagescandataBean> getScanBySeriesDescription(XnatImagesessiondataBean imageSession, String imageScanSeriesDescription) {
        ArrayList<XnatImagescandataBean> imageScan = new ArrayList<XnatImagescandataBean>();
        if (imageScanSeriesDescription == null) return imageScan;
        List<XnatImagescandataBean> imageScans = imageSession.getScans_scan();
        String[] scanTypes = imageScanSeriesDescription.trim().split(",");
        Hashtable scanTypeHash = new Hashtable();
        for (int k=0; k< scanTypes.length; k++) {
            scanTypeHash.put(scanTypes[k], "1");
        }
        if (imageScans != null && imageScans.size() > 0) {
            for (int i = 0; i < imageScans.size(); i++) {
                XnatImagescandataBean mrscan = imageScans.get(i);
                if (scanTypeHash.containsKey(mrscan.getSeriesDescription())) {
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
            List<XnatImagescandataBean> scans = pet.getScans_scan();
            for (int i=0; i < scans.size(); i++) {
                XnatImagescandataBean scan = scans.get(i);
                if (scan.getType().equals("Dynamic emission")) {
                    List<XnatAbstractresourceBean> files = scan.getFile();
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
            List<CatEntryBean>  catalogEntries = catalogBean.getEntries_entry();
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
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebServiceClient webClient = new WebServiceClient(host, user, pwd);
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
        String rtn = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebServiceClient webClient = new WebServiceClient(host, user, pwd);
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
        String [] columnHuntOrder = colname.split(",");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebServiceClient webClient = new WebServiceClient(host, user, pwd);
            webClient.connect(uri, out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            String jsonTxt = org.apache.commons.io.IOUtils.toString( in);
            System.out.println(uri + " JSONTXT " + jsonTxt);
            JSONObject jsonObj = null;
            JSON json = JSONSerializer.toJSON( jsonTxt );
            if (json instanceof JSONObject){
                jsonObj = (JSONObject) json;
                System.out.println(jsonObj.get("ResultSet"));
                JSONObject internal = (JSONObject)JSONSerializer.toJSON(jsonObj.getString("ResultSet"));
                JSONArray internalArray = (JSONArray)JSONSerializer.toJSON(internal.getString("Result"));
                if (internalArray.size() == 1) {
                    internal = internalArray.getJSONObject(0);
                    for (int i=0; i<columnHuntOrder.length; i++) {
                        rtn = internal.getString(columnHuntOrder[i].trim());
                        if (rtn != null && !rtn.equals("")){
                            System.out.println(columnHuntOrder[i].trim()+"=" + rtn);
                            break;
                        }
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public static ArrayList GetColumnList(String host, String user, String pwd,  String uri, String column) {
        ArrayList<String> returnList = Lists.newArrayList();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebServiceClient webClient = new WebServiceClient(host, user, pwd);
            webClient.connect(uri, out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            String jsonTxt = org.apache.commons.io.IOUtils.toString( in);
            System.out.println(uri + " JSONTXT " + jsonTxt);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode     response     = objectMapper.readTree(jsonTxt);

            if (response != null && response.has("ResultSet")) {
                JsonNode resultArray = response.get("ResultSet").get("Result");
                if (resultArray.isArray()) {
                    for (int i=0; i < resultArray.size(); i++) {
                        JsonNode result = resultArray.get(i);
                        JsonNode resultCol = result.get(column.trim());
                        if (resultCol != null && StringUtils.isNotBlank(resultCol.textValue())) {
                            returnList.add(i, resultCol.textValue());
                        }
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return returnList;
    }


    public static boolean FileExists(String host, String user, String pwd,  String project, String collection, String file_name) {
        boolean exists=false;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            WebServiceClient webClient = new WebServiceClient(host, user, pwd);
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
            XnatAbstractresourceI file =  getScanFile(imageScan);
            if (file instanceof XnatResourcecatalogBean) {
                XnatResourcecatalogBean catalog = (XnatResourcecatalogBean)file;
                String catalogPath = catalog.getUri();
                if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
                CatDcmcatalogBean dcmCatalogBean =  (CatDcmcatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
                List<CatEntryBean>  catalogEntries = dcmCatalogBean.getEntries_entry();
                int lastIndexOfSlash = catalogPath.lastIndexOf("/");
                String uri = catalogEntries.get(0).getUri();
                if (lastIndexOfSlash != -1) {
                    catalogPath = catalogPath.substring(0,lastIndexOfSlash);
                    rtn = catalogPath + "/" + uri;
                } else {
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
            XnatAbstractresourceI file =  getScanFile(imageScan);
            if (file instanceof XnatResourcecatalogBean) {
                XnatResourcecatalogBean catalog = (XnatResourcecatalogBean)file;
                String catalogPath = catalog.getUri();
                if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
                CatDcmcatalogBean dcmCatalogBean =  (CatDcmcatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
                List<CatEntryBean>  catalogEntries = dcmCatalogBean.getEntries_entry();
                String uri = catalogEntries.get(0).getUri();
                int lastIndexOfSlash = uri.lastIndexOf("/");
                if (lastIndexOfSlash != -1) {
                    rtn = uri.substring(lastIndexOfSlash);
                } else {
                    rtn =File.separator +  uri;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }


    public static ArrayList<String> GetScanIdsByType(String host, String user, String pwd, String imageSessionId, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanType) {
        ArrayList<String> rtn = new ArrayList<String>();
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageScanType.size(); i++) {
                String scanType = imageScanType.get(i).getStringValue().trim();
                String[] scanTypes = scanType.split(",");
                for (int k=0; k< scanTypes.length; k++) {
                    String scan_type = scanTypes[k].trim();
                    System.out.println("Scantype is " + scan_type);
                    ArrayList<XnatImagescandataBean> imageScan = getScanByType(imageSession, scan_type);
                    if (imageScan.size() == 0)  continue;
                    if (rtn==null) rtn = new ArrayList<String>();
                    for (int j =0; j < imageScan.size(); j++) {
                        rtn.add(imageScan.get(j).getId());
                        System.out.println("Found scan " + imageScan.get(j).getId());
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }



    private static ArrayList<XnatImagescandataBean> getScanById(XnatImagesessiondataBean imageSession, String imageScanId) {
        ArrayList<XnatImagescandataBean> imageScan = new ArrayList<XnatImagescandataBean>();
        if (imageScanId == null) return imageScan;
        List<XnatImagescandataBean> imageScans = imageSession.getScans_scan();
        if (imageScans != null && imageScans.size() > 0) {
            for (int i = 0; i < imageScans.size(); i++) {
                XnatImagescandataBean mrscan = imageScans.get(i);
                if (imageScanId.equals(mrscan.getId())) {
                    imageScan.add(mrscan);
                }
            }
        }
        return imageScan;
    }



    public static String FilterScansFromTypeByQuality_GetFirst(String host, String user, String pwd, String imageSessionId, String quality, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanType) {
        String firstUsableScanId = null;
        try {
            ArrayList<String> rtn = FilterScansFromTypeByQuality(host,user,pwd,imageSessionId,quality,imageScanType);
            if (rtn.size() > 0) {
                firstUsableScanId = rtn.get(0);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return firstUsableScanId;
    }


    public static int IsDicom(String host, String user, String pwd, String imageSessionId, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanIds) {
        int rtn = 0;
        boolean breakLoop = false;
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageScanIds.size(); i++) {
                String scanId = imageScanIds.get(i).getStringValue();
                ArrayList<XnatImagescandataBean> imageScan = getScanById(imageSession, scanId);
                for (int j =0; j <imageScan.size(); j++) {
                    if (imageScan != null && imageScan.size() > 0 ) {
                        List files = imageScan.get(0).getFile();
                        if (files.size() > 0) {
                            for (int k =0; k < files.size(); k++) {
                                XnatAbstractresourceBean absFile = (XnatAbstractresourceBean) files.get(k);
                                System.out.println("Scan: " + scanId + " " + absFile.getClass());
                                if (absFile instanceof  XnatDicomseriesBean ) {
                                    rtn = 1;
                                    breakLoop = true;
                                    break;
                                }else if (absFile instanceof  XnatResourcecatalogBean){
                                    XnatResourcecatalogBean rsccat = (XnatResourcecatalogBean) absFile;
                                    if (rsccat.getContent().equalsIgnoreCase("RAW")) {
                                        if (rsccat.getFormat().equalsIgnoreCase("DICOM")) {
                                            rtn = 1;
                                            breakLoop = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (breakLoop) break;
                }
                if (breakLoop) break;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }


    public static ArrayList<String> FilterScansFromTypeByQuality(String host, String user, String pwd, String imageSessionId, String quality, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanType) {
        ArrayList<String> rtn = new ArrayList<String>();
        if (imageScanType.size()==0) return rtn;
        Hashtable<String,String> qualityHash = new Hashtable<String,String>();

        try {
            String[] qualityList = quality.split(",");
            for (int i=0; i< qualityList.length;i++) {
                qualityHash.put(qualityList[i].toUpperCase(), "1");
            }
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageScanType.size(); i++) {
                String scanType = imageScanType.get(i).getStringValue().trim();
                String[] scanTypes = scanType.split(",");
                for (int k=0; k<scanTypes.length; k++) {
                    String scan_type= scanTypes[k].trim();
                    ArrayList<XnatImagescandataBean> imageScan = getScanByType(imageSession, scan_type);
                    if (imageScan.size() == 0) {
                        imageScan = getScanById(imageSession, scan_type);
                        if (imageScan.size() == 0)  continue;
                    }
                    if (rtn==null) rtn = new ArrayList<String>();
                    for (int j =0; j < imageScan.size(); j++) {
                        if (qualityHash.containsKey(imageScan.get(j).getQuality().toUpperCase()))
                            rtn.add(imageScan.get(j).getId());
                        //System.out.println("Found scan " + imageScan.get(j).getId());
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }




    public static List<String> FilterScansFromTypeByQuality(String host, String user, String pwd, String imageSessionId, String quality, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanType, int returnQuantity) {
        ArrayList<String> rtn = new ArrayList<String>();
        if (imageScanType.size()==0) return rtn;
        Hashtable<String,String> qualityHash = new Hashtable<String,String>();

        try {
            String[] qualityList = quality.split(",");
            for (int i=0; i< qualityList.length;i++) {
                qualityHash.put(qualityList[i].toUpperCase(), "1");
            }
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageScanType.size(); i++) {
                String scanType = imageScanType.get(i).getStringValue().trim();
                String[] scanTypes = scanType.split(",");
                for (int k=0; k<scanTypes.length; k++) {
                    String scan_type= scanTypes[k].trim();
                    ArrayList<XnatImagescandataBean> imageScan = getScanByType(imageSession, scan_type);
                    if (imageScan.size() == 0) {
                        imageScan = getScanById(imageSession, scan_type);
                        if (imageScan.size() == 0)  continue;
                    }
                    if (rtn==null) rtn = new ArrayList<String>();
                    for (int j =0; j < imageScan.size(); j++) {
                        if (qualityHash.containsKey(imageScan.get(j).getQuality().toUpperCase()))
                            rtn.add(imageScan.get(j).getId());
                        //System.out.println("Found scan " + imageScan.get(j).getId());
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (returnQuantity < 0)
            return rtn;
        else if (returnQuantity < rtn.size()) {
            return rtn.subList(0, returnQuantity);
        }else  {
            return rtn;
        }
    }

    public static List<String> FilterScansFromSeriesDescriptionByQuality(String host, String user, String pwd, String imageSessionId, String quality, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanSeriesDescription, int returnQuantity) {
        ArrayList<String> rtn = new ArrayList<String>();
        if (imageScanSeriesDescription.size()==0) return rtn;
        Hashtable<String,String> qualityHash = new Hashtable<String,String>();

        try {
            String[] qualityList = quality.split(",");
            for (int i=0; i< qualityList.length;i++) {
                qualityHash.put(qualityList[i].toUpperCase(), "1");
            }
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageScanSeriesDescription.size(); i++) {
                String scanType = imageScanSeriesDescription.get(i).getStringValue().trim();
                String[] scanTypes = scanType.split(",");
                for (int k=0; k<scanTypes.length; k++) {
                    String scan_type= scanTypes[k].trim();
                    ArrayList<XnatImagescandataBean> imageScan = getScanBySeriesDescription(imageSession, scan_type);
                    if (imageScan.size() == 0) {
                        imageScan = getScanById(imageSession, scan_type);
                        if (imageScan.size() == 0)  continue;
                    }
                    if (rtn==null) rtn = new ArrayList<String>();
                    for (int j =0; j < imageScan.size(); j++) {
                        if (qualityHash.containsKey(imageScan.get(j).getQuality().toUpperCase()))
                            rtn.add(imageScan.get(j).getId());
                        //System.out.println("Found scan " + imageScan.get(j).getId());
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (returnQuantity < 0)
            return rtn;
        else if (returnQuantity < rtn.size()) {
            return rtn.subList(0, returnQuantity);
        } else {
            return rtn;
        }
    }


    public static String GetPreferredScanByManualQCQuality(String host, String user, String pwd, String imageSessionId, String quality, String tempDir, String rating, String ratingScaleType, ArrayList<net.sf.saxon.tinytree.TinyNodeImpl> imageScanType) {
        String rtn = null;
        Hashtable<String,String> scanids = null;
        ArrayList<XnatImagescandataBean> imageScan = new ArrayList<XnatImagescandataBean>();
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageScanType.size(); i++) {
                String scanType = imageScanType.get(i).getStringValue();
                String[] scanTypes = scanType.split(",");
                //System.out.println("Scantypes:"+scanTypes.length);
                for (int k=0; k<scanTypes.length; k++) {
                    String scan_type = scanTypes[k].trim();
                    ArrayList<XnatImagescandataBean> iScan = getScanByType(imageSession, scan_type);
                    if (iScan.size() == 0) {
                        iScan = getScanById(imageSession, scan_type);
                        if (iScan.size() == 0)  continue;
                    }
                    if (scanids==null) scanids = new Hashtable<String,String>();
                    if (iScan!=null && iScan.size()>0) {
                        imageScan.addAll(iScan);
                    }
                    for (int j =0; j < iScan.size(); j++) {
                        scanids.put(iScan.get(j).getId(), iScan.get(j).getId());
                        //System.out.println("Found scan " + iScan.get(j).getId());
                    }
                }
            }
            //System.out.println("Rating Scale Type:" + ratingScaleType + ":"+imageScan.size());
            if (rating != null && ratingScaleType != null && !ratingScaleType.equals("")) {
                XMLSearch search = new XMLSearch(host, user, pwd );
                ArrayList<String> files = search.searchAll("xnat:qcManualAssessorData.imageSession_ID",imageSessionId,"=","xnat:qcManualAssessorData",tempDir);
                if (files.size()>0) {
                    XnatQcmanualassessordataBean latestQC = null;
                    Calendar currentcal = Calendar.getInstance();
                    boolean done = false;

                    for (int i = 0; i < files.size(); i++) {
                        String path = files.get(i);
                        File f = null;
                        try {
                            f = new File(path);
                            if (f.exists()) {
                                path = f.getAbsolutePath();
                            }else {
                                f = new File(new URI(path));
                                path = f.getAbsolutePath();
                            }
                        } catch(Exception e) {e.printStackTrace();}
                        XDATXMLReader reader = new XDATXMLReader();
                        BaseElement base = reader.parse(f);
                        XnatQcmanualassessordataBean qc = (XnatQcmanualassessordataBean)base;
                        Date date = qc.getDate();
                        //Get the latest manual QC
                        //If that exists use the best from here
                        //If no QC exists, pick the best MR by quality.
                        if (i==0) {
                            currentcal.setTime(date);
                            latestQC = qc;
                        } else {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            if (currentcal.before(cal)) {
                                latestQC = qc;
                                currentcal.setTime(latestQC.getDate());
                            }
                        }
                    }
                    if (latestQC != null) {
                        List<XnatQcscandataI> qcscans = latestQC.getScans_scan();
                        for (int j=0; j < qcscans.size(); j++) {
                            XnatQcscandataI qcscan = qcscans.get(j);
                            if (scanids.containsKey(qcscan.getImagescanId()) ) {
                                if (ratingScaleType.equals(qcscan.getRating_scale())) {
                                    if (rating.equals(qcscan.getRating())) {
                                        rtn = qcscan.getImagescanId();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                for (int j =0; j < imageScan.size(); j++) {
                    //	System.out.println("Found scan " + imageScan.get(j).getId());
                    if (imageScan.get(j).getQuality().equalsIgnoreCase(quality))
                        rtn = imageScan.get(j).getId();
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Scan selected: " + rtn);
        return rtn;
    }


    public static String GetValidationStatus(String host, String user, String pwd, String imageSessionId, String validationId) {
        String rtn = null;
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            for (int i =0; i < imageSession.getAssessors_assessor().size(); i++) {
                XnatImageassessordataBean assessor = (XnatImageassessordataBean)imageSession.getAssessors_assessor().get(i);
                if (assessor instanceof ValProtocoldataBean) {
                    if (assessor.getLabel().equals(validationId)) {
                        ValProtocoldataBean validationAssessor = (ValProtocoldataBean)assessor;
                        rtn = validationAssessor.getCheck_status();
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public static String GetArchiveDirRootPath(String host, String user, String pwd, String imageSessionId) {
        String rtn = null;
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
            List<XnatImagescandataBean> scans = imageSession.getScans_scan();
            if (scans.size()>0) {
                XnatImagescandataBean scan = scans.get(0);
                List<XnatAbstractresourceBean> files = scan.getFile();
                if (files.size()>0) {
                    XnatAbstractresourceBean file = files.get(0);
                    if (file instanceof XnatResourcecatalogBean) {
                        XnatResourcecatalogBean rsc = (XnatResourcecatalogBean)file;
                        String uri = rsc.getUri();
                        int index = uri.indexOf(imageSession.getLabel());
                        if (index > 0) {
                            rtn = uri.substring(0,index);
                            if (rtn.endsWith(File.separator)) {
                                rtn = rtn.substring(0,rtn.length()-1);
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Archive Root Path " + rtn);
        return rtn;
    }

    public static String getSingleDicomFileNameForScan(XnatImagesessiondataBean imageSession, String imageScanId) {
        String rtn = "";
        try {
            XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
            XnatAbstractresourceI file =  getScanFile(imageScan);
            if (file instanceof XnatResourcecatalogBean) {
                XnatResourcecatalogBean catalog = (XnatResourcecatalogBean)file;
                String catalogPath = catalog.getUri();
                if (catalogPath.endsWith("/")) catalogPath = catalogPath.substring(0,catalogPath.length()-1);
                CatDcmcatalogBean dcmCatalogBean =  (CatDcmcatalogBean)new XmlReader().getBeanFromXml(catalogPath, false);
                List<CatEntryBean>  catalogEntries = dcmCatalogBean.getEntries_entry();
                String uri = catalogEntries.get(0).getUri();
                int lastIndexOfSlash = uri.lastIndexOf("/");
                if (lastIndexOfSlash != -1) {
                    rtn = uri.substring(lastIndexOfSlash);
                } else {
                    rtn = File.separator +   uri;
                }
            }
        } catch(Exception e) {
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
            XnatAbstractresourceI file =  getScanFile(imageScan);

            if (file instanceof XnatImageresourceseriesBean) {
                XnatImageresourceseriesBean imgRsc = (XnatImageresourceseriesBean)file;
                rtn = imgRsc.getName();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public static String getSingleImaFileNameForScan(XnatImagesessiondataBean imageSession, String imageScanId) {
        String rtn = "";
        try {
            XnatImagescandataBean imageScan = getScan(imageSession, imageScanId);
            XnatAbstractresourceI file =  getScanFile(imageScan);
            if (file instanceof XnatImageresourceseriesBean) {
                XnatImageresourceseriesBean imgRsc = (XnatImageresourceseriesBean)file;
                rtn = File.separator + imgRsc.getName();
            }
        } catch(Exception e) {
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
            } else {
                rtn = uri;
            }
        } catch(Exception e) {
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


    public static String getTagAsInts(String dicomFilePath, String tag, int index) {
        try {
            DicomElement dcmElt = getTag(dicomFilePath,tag);
            int[] ints = dcmElt.getInts(true);
            String rtn = "" + ints[index];
            return rtn;
        }catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTagAsFloats(String dicomFilePath, String tag, int index) {
        try {
            DicomElement dcmElt = getTag(dicomFilePath,tag);
            float[] floats = dcmElt.getFloats(true);
            String rtn = "" + floats[index];
            return rtn;
        }catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTagAsString(String dicomFilePath, String tag) {
        try {
            DicomElement dcmElt = getTag(dicomFilePath,tag);
            String rtn = dcmElt.getString(null, true);
            return rtn;
        }catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTagAsString(String dicomFilePath, String tag, int index) {
        try {
            DicomElement dcmElt = getTag(dicomFilePath,tag);
            String[] rtn = dcmElt.getStrings(null, true);
            return rtn[index];
        }catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static DicomElement getTag(String dicomFilePath, String tag) {
        try {
            DicomFileReader dicomFileReader = new DicomFileReader(dicomFilePath);
            DicomObject dcmObj = dicomFileReader.getDicomObject();
            DicomElement dcmElt = dcmObj.get(Tag.toTag(tag));
            return dcmElt;
        }catch(IOException e) {
            e.printStackTrace();
            return null;
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
   /*   	String rtn = getTagAsString(args[0],"00181312");
      	System.out.println("Tag value is " + rtn); */
        //boolean label = GetScanIdsByType("HOST","USER","PWD","XNATID","dMRI");
        //System.out.println("Get Column " + label);
        //String label = "";
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

    public static String getJSESSION(String dummy) {
        String rtn = null;
        try {
            //Assumed that the SessionManager has been inited already
            rtn = SessionManager.GetInstance().getJSESSION();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return rtn;
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

    public static String GetClosestInAcquisitionTime(String host, String user, String pwd, String imageSessionId, String inscanId, String closestScanType) {
        String rtn = null;
        try {
            XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) new XMLSearch(host, user, pwd).getBeanFromHost(imageSessionId, true);
        }catch(Exception e) {
            e.printStackTrace();
        }

        return rtn;
    }

}
