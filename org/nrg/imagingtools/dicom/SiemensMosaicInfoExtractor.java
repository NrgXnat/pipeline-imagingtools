/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

* Extracts information about the Siemens MOSAIC dimensions
*/

package org.nrg.imagingtools.dicom;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.zip.DataFormatException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class SiemensMosaicInfoExtractor {
	
	String dicomFilePath ;
	static final String MOSAIC_INFO_BEGIN = "### ASCCONV BEGIN ###";
	                                                                      
	static final String MOSAIC_INFO_END = "### ASCCONV END ###";
	static final String SSLICEARRAY_ASSLICE = "sSliceArray.asSlice";
	DicomObject dcmObj;
	
	
	public SiemensMosaicInfoExtractor(String dicomFilePath){
		this.dicomFilePath = dicomFilePath;
		dcmObj = null;
	}
	



	public void load() throws IOException {
		DicomFileReader dicomFileReader = new DicomFileReader(dicomFilePath);
		try {
			 dcmObj = dicomFileReader.getDicomObject();
		}catch(IOException e) {
			System.out.println("Encountered  " + e.getClass() + " "  + e.getMessage());
			throw e;
		}
	}
	
	
	public boolean isMosaic() {
	 boolean isMosaic = false;
	 String[] imageType = dcmObj.getStrings(Tag.ImageType);
	 if (imageType != null && imageType.length > 0) {
		 for (int i = 0; i < imageType.length; i++) {
			 if (imageType[i].toUpperCase().indexOf("MOSAIC")!= -1) {
				 isMosaic = true;
				 break;
			 }
		 }
	 }
	 return isMosaic;
	}
	
	public boolean isSiemensDicom() {
		boolean isSiemens = false;
		String manufacturer = dcmObj.getString(Tag.Manufacturer);
		if  (manufacturer != null) {
			if (manufacturer.equalsIgnoreCase("SIEMENS"))
				isSiemens = true;
		}
		return isSiemens;
	}
	
	public int getRows() {
		return dcmObj.getInt(Tag.Rows);
	}
	
	public int getColumns() {
		return dcmObj.getInt(Tag.Columns);
	}
	
	public int getNumberOfFrames() {
		System.out.println("Number of Frames " + dcmObj.getString(Tag.NumberOfFrames));
		return dcmObj.getInt(Tag.NumberOfFrames);
	}
	
	public int getBytesPerPixel() throws DataFormatException{
		int bitsAllocated =  getBitsAllocated();
		if (!(bitsAllocated == 8 || bitsAllocated == 16 || bitsAllocated == 32 )) {
			throw new DataFormatException("Invalid bits allocated...found " + bitsAllocated);
		}
		return bitsAllocated/8;
	}
	
	public int getBitsAllocated() {
		return dcmObj.getInt(Tag.BitsAllocated);
	}

	public String getSiemensAddendum1() {
		//Ref:: http://atonal.ucdavis.edu/matlab/fmri/spm5/spm_dicom_headers.m
		byte[] bytes =  dcmObj.getBytes(Tag.toTag("00291010"));
		System.out.println(dcmObj.bigEndian() + " " + new String(bytes));
		 //First Fours Bytes are SV10
		//Next 4 sets of 4 Bytes are unused
		 int pos = 16;
		 for (int i = 0; i < 1; i++) {
			 byte[] tagName = new byte[64];
			 System.arraycopy(bytes, pos, tagName, 0, tagName.length);
			 SiemensCSAInfo csa = new SiemensCSAInfo();
			 csa.setName(tagName);
			 System.out.println("CSA " + csa.getName());
		 }
		 
		 return "";
	}
	
	public String getSiemensAddendum2() {
		String ascconv = null;
		//String hex_siemensPrivateInfo = dcmObj.getString(Tag.toTag("00291020"));
		//byte[] bytes = Converter.toByteArray(hex_siemensPrivateInfo.replace("\\",""));
		byte[] bytes =  dcmObj.getBytes(Tag.toTag("00291020"));
		String siemensPrivateInfoAscii = new String(bytes);
		//System.out.println(siemensPrivateInfoAscii);
		//int i = siemensPrivateInfoAscii.indexOf("BEGIN");
		//System.out.println(i +"="+i + "      " + siemensPrivateInfoAscii.substring(i, i+22));
		 int start_index =siemensPrivateInfoAscii.indexOf(MOSAIC_INFO_BEGIN);
		 //The following code was required as the string for NP806 had different values
		 int end_index = siemensPrivateInfoAscii.substring(start_index).indexOf(MOSAIC_INFO_END);
	    // No ASCCONV information
	    if (start_index == -1 || end_index == -1) { return ascconv; }
	    // Get the ASCCONV string
	    ascconv =siemensPrivateInfoAscii.substring(start_index + MOSAIC_INFO_BEGIN.length(),start_index+end_index);
	    return ascconv;
	}
	
	private LinkedHashMap extractProperties(String str) {
		LinkedHashMap metadata = new LinkedHashMap();
		try {
			// Separate the names from the values
		    BufferedReader reader=new BufferedReader(new StringReader(str));
		    String line = reader.readLine();
		    while (line != null) {
		    	// Find the "=" delimiter
		    	String equalDelimiter = " = ";
		    	int equalIndex = line.indexOf(equalDelimiter);
		    	if (equalIndex != -1) {
		    		// Get the name and value
		    		String name = line.substring(0, equalIndex).trim();
		    		String value = line.substring(equalIndex +  equalDelimiter.length(),  line.length());
		    		metadata.put(name, value);
		    	}
		    	// Get the next line
		    	line = reader.readLine();
		    }
		}catch(Exception e) {
			
		}
	    return metadata;
	}
	
	public int getNumberOfTilesInMosaic() {
		int mosaic_num = -1;
		 if (!isSiemensDicom() || !isMosaic()) {
			 return mosaic_num;
		 }

		String ascconv = getSiemensAddendum2();
		LinkedHashMap metadata = extractProperties(ascconv);
		//Iterate over the sSliceArray.asSlice[].* property. 
		//The number of tiles in a mosaic is the max index of this array. However, siemens mosaic always has
		//a square number of images.
		Iterator keyIterator = metadata.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = (String)keyIterator.next();
			if (key.startsWith(SSLICEARRAY_ASSLICE)) {
				int index = getIndex(key);
				if (index > mosaic_num) mosaic_num = index;
			}
		}
		//Index starts at 0 so return =  + 1;
		return (mosaic_num + 1) ;
	}
	
	public Rectangle getTileDimension() {
		int tilesInMosaic = getNumberOfTilesInMosaic();
		if (tilesInMosaic <= 0) return null;
		int tileWidth, tileHeight;
		int tx, ty;
		/* compute size of mosaic layout as 1st integer whose square is >= # of tiles in mosaic */
		for(  tx=1 ; tx*tx < tilesInMosaic ; tx++ ) ; 
		ty = tx ;
		tileWidth = getColumns() /tx ;
		tileHeight = getRows() / ty;
		return new Rectangle(tileWidth,tileHeight);
	}
	
	private int getIndex(String str) {
		try {
		    int leftIndex = str.indexOf("[");
		    int rightIndex = str.indexOf("]");
		    if (leftIndex == -1 || rightIndex == -1 || leftIndex > rightIndex) {
		    	return -1;
		    }
		    String index = str.substring(leftIndex+1, rightIndex);
		    return Integer.parseInt(index);
		}
		catch (Exception e) { return -1; }
	}
	
	
	public String displayInfo() {
			String rtn = "";
			rtn = "File Path " + dicomFilePath + "\n";
			rtn += "Manufactured by Siemens " + isSiemensDicom() +"\n";
			rtn += "Is Mosaic " + isMosaic()  +"\n" ;
			rtn += "Rows " + getRows()  +"\n";
			rtn += "Columns " + getColumns()  +"\n";
		    rtn += "Number of Frames: " + getNumberOfFrames()  +"\n";
		    try {
		        	rtn += "Bytes Per Pixel: " + getBytesPerPixel()  +"\n";
		     }catch(DataFormatException dfe) {
		        	rtn += "Bits Per Pixel: " + getBitsAllocated()  +"\n";
		    }
		    rtn += "Number of Tiles in Mosaic: " + getNumberOfTilesInMosaic() + "\n";
		    Rectangle tile = getTileDimension();
		     if (tile != null)
		       	rtn += "Tile Dimensions: " + tile.getWidth() +"x" + tile.getHeight() + "\n";
			return rtn;
	}
	
	
	
	public static void main(String args[]) {
		SiemensMosaicInfoExtractor siemensMosaic = new SiemensMosaicInfoExtractor(args[0]);
		try {
			siemensMosaic.load();
	        Rectangle tile = siemensMosaic.getTileDimension();
	        if (tile != null) 
	        	System.out.println("NX= " + (int)tile.getWidth()); 
	        else {
	        	System.out.println("SiemensMosaicInfoExtractor couldnt find the number of tiles in the mosaic");
	        	
	        }

			System.out.println("Number of frames is " + siemensMosaic.getNumberOfFrames());
		
		}catch(IOException ioe) {}
	}
	
	
	private class SiemensCSAInfo {
		String name;
		String vm;
		String vr;
		String syngodt;
		int  nitems;
		String xx;
		String[] itemsXX; 
		String[] itemsVal;
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		
		public void setName(byte[] tagName) {
			 int firstZeroIndex = 0;
			 for (int j = 0; j < tagName.length; j++) {
				 if (tagName[j] == 0)  {
					 firstZeroIndex =j;
					 break;
				 }
			 }
			 byte[] tagName1 = new byte[firstZeroIndex];
			 System.arraycopy(tagName, 0, tagName1, 0, firstZeroIndex);
			 name = new String(tagName1);
		}
		
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the vm
		 */
		public String getVm() {
			return vm;
		}
		/**
		 * @param vn the vn to set
		 */
		public void setVm(String vn) {
			this.vm = vm;
		}
		/**
		 * @return the vr
		 */
		public String getVr() {
			return vr;
		}
		/**
		 * @param vr the vr to set
		 */
		public void setVr(String vr) {
			this.vr = vr;
		}
		/**
		 * @return the syngodt
		 */
		public String getSyngodt() {
			return syngodt;
		}
		/**
		 * @param syngodt the syngodt to set
		 */
		public void setSyngodt(String syngodt) {
			this.syngodt = syngodt;
		}
		/**
		 * @return the nitems
		 */
		public int getNitems() {
			return nitems;
		}
		/**
		 * @param nitems the nitems to set
		 */
		public void setNitems(int nitems) {
			this.nitems = nitems;
		}
		/**
		 * @return the xx
		 */
		public String getXx() {
			return xx;
		}
		/**
		 * @param xx the xx to set
		 */
		public void setXx(String xx) {
			this.xx = xx;
		}
		/**
		 * @return the itemsXX
		 */
		public String[] getItemsXX() {
			return itemsXX;
		}
		/**
		 * @param itemsXX the itemsXX to set
		 */
		public void setItemsXX(String[] itemsXX) {
			this.itemsXX = itemsXX;
		}
		/**
		 * @return the itemsVal
		 */
		public String[] getItemsVal() {
			return itemsVal;
		}
		/**
		 * @param itemsVal the itemsVal to set
		 */
		public void setItemsVal(String[] itemsVal) {
			this.itemsVal = itemsVal;
		}

		public String toString() {
			String rtn = "";
			rtn += "Name: " + name + "\n";
			rtn += "VM: " + vm + "\n";
			rtn += "VR: " + vr + "\n";
			rtn += "Syngodt: " + syngodt + "\n";
			return rtn;
		}
	}
	
}