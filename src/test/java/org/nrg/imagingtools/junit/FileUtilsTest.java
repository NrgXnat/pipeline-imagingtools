/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.junit;

import org.junit.Assert;
import org.junit.Test;
import org.nrg.imagingtools.utils.FileUtils;

public class FileUtilsTest {
	
	String[] filepaths = {
			"C:\\foo.dcm",
			"C:\\Archive\\BS002_VB19161\\RAW\\VB19161.MR.HEAD_RAICHLE.13.194.2005.10.06.16.41.50.796875.57059119.IMA",
		    "C:\\Archive\\BS002_VB19161\\RAW\\VB19161.MR.HEAD_RAICHLE.13.194.2005.10.06.16.41.50.796875.57059119.IMA.gz",
		    "C:\\Archive\\BS016_subject_12\\RAW\\MEDITATION_SUBJECT5_DOR.MR.WIETSKE_NILSR.10.33.2008.02.06.18.15.05.125000.43022163.IMA.gz"
	};
	
	@Test
	public void testUnpack4dfpArgumnets() {
		String unpackArgs = FileUtils.getArgumentsForUnpack4dfp(filepaths[0]);
		Assert.assertEquals("",unpackArgs);
		unpackArgs = FileUtils.getArgumentsForUnpack4dfp(filepaths[1]);
		Assert.assertEquals("  -nx  64 -ny 64",unpackArgs);
		unpackArgs = FileUtils.getArgumentsForUnpack4dfp(filepaths[2]);
		Assert.assertEquals("  -nx  64 -ny 64",unpackArgs);
		unpackArgs = FileUtils.getArgumentsForUnpack4dfp(filepaths[3]);
		Assert.assertEquals("  -nx  112 -ny 112",unpackArgs);
	}
	
}
