/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.junit;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.nrg.imagingtools.dicom.SiemensMosaicInfoExtractor;

public class SiemensMosaicInfoExtractorTest {

		String[] filepaths = {
					"C:\\foo.dcm",
					"C:\\Archive\\BS002_VB19161\\RAW\\VB19161.MR.HEAD_RAICHLE.13.194.2005.10.06.16.41.50.796875.57059119.IMA",
				    "C:\\Archive\\BS002_VB19161\\RAW\\VB19161.MR.HEAD_RAICHLE.13.194.2005.10.06.16.41.50.796875.57059119.IMA.gz",
				    "C:\\Archive\\BS016_subject_12\\RAW\\MEDITATION_SUBJECT5_DOR.MR.WIETSKE_NILSR.10.33.2008.02.06.18.15.05.125000.43022163.IMA.gz",
				    "C:\\Documents and Settings\\mohanar\\Desktop\\080213_TC26543_2_20_2008_12_37_4\\080213_TC26543\\RAW\\1.MR.head_DHead.12.1.20080213.084007.937000.4752856922.dcm.gz"
			};

		@Test(expected=IOException.class)
		public void handleFileDoesntExist() throws IOException{
			//try {
				SiemensMosaicInfoExtractor fileDoesNotExistExtractor = new SiemensMosaicInfoExtractor(filepaths[0]);
				fileDoesNotExistExtractor.load();
		//	}catch(IOException success) {}
		}

		@Test
		public void readsUncompressedFiles1()  {
				SiemensMosaicInfoExtractor uncompressedExtractor = new SiemensMosaicInfoExtractor(filepaths[1]);
				try {
					uncompressedExtractor.load();
					File zippedFile =  new File(filepaths[3]);
					Assert.assertTrue(zippedFile.exists());
					Rectangle tiles = uncompressedExtractor.getTileDimension();
					Assert.assertEquals(tiles,new Rectangle(64,64));
				}catch(IOException ioe) {Assert.assertFalse(true);}
		}

		@Test
		public void readsUncompressedFiles2()  {
				SiemensMosaicInfoExtractor uncompressedExtractor = new SiemensMosaicInfoExtractor(filepaths[4]);
				try {
					uncompressedExtractor.load();
					System.out.println(uncompressedExtractor.displayInfo());
					File zippedFile =  new File(filepaths[3]);
					Assert.assertTrue(zippedFile.exists());
					//Rectangle tiles = uncompressedExtractor.getTileDimension();
					//Assert.assertEquals(tiles,new Rectangle(64,64));
				}catch(IOException ioe) {Assert.assertFalse(true);}
		}

		
		@Test
		public void readsCompressedFiles1()  {
				SiemensMosaicInfoExtractor compressedExtractor = new SiemensMosaicInfoExtractor(filepaths[2]);
				try {
					compressedExtractor.load();
					Rectangle tiles = compressedExtractor.getTileDimension();
					Assert.assertEquals(new Rectangle(64,64), tiles);
				}catch(IOException ioe) {Assert.assertFalse(true);}
		}

		@Test
		public void readsCompressedFiles2()  {
				SiemensMosaicInfoExtractor compressedExtractor = new SiemensMosaicInfoExtractor(filepaths[3]);
				try {
					compressedExtractor.load();
					Rectangle tiles = compressedExtractor.getTileDimension();
					Assert.assertEquals(new Rectangle(112,112), tiles);
				}catch(IOException ioe) {Assert.assertFalse(true);}
		}

		@Test
		public void zippedTempFileContinuesToExist()  {
				SiemensMosaicInfoExtractor compressedExtractor = new SiemensMosaicInfoExtractor(filepaths[3]);
				try {
					compressedExtractor.load();
					File zippedFile =  new File(filepaths[3]);
					Assert.assertTrue(zippedFile.exists());
				}catch(IOException ioe) {Assert.assertFalse(true);}
		}
		
		@Test
		public void handlesNonDicomFiles()  {
				SiemensMosaicInfoExtractor compressedExtractor = new SiemensMosaicInfoExtractor(filepaths[3]);
				try {
					compressedExtractor.load();
					File zippedFile =  new File(filepaths[3]);
					Assert.assertTrue(zippedFile.exists());
				}catch(IOException ioe) {Assert.assertFalse(true);}
		}
		

		
}
