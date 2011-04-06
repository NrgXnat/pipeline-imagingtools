/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.dicom;

import java.io.IOException;

import org.nrg.imagingtools.utils.FileUtils;

/*
 * This class will generate the format as required by the qnt_4dfp program.
 *   
 *   
 */
public class FormatExtractor {
	public static void main(String args[]) {
		if (args.length == 2) {
			String skip = args[0];
			String concfile = args[1];
			try {
				String format = FileUtils.getFormatFromConcFile(skip, concfile);
				System.out.println(format);
			}catch(IOException ioe) {
				System.out.println("ERROR");
			}
		}else {
			System.out.println("Purpose: FormatExtractor generates the format as required by the 4dfp routines like qnt_4dfp");
			System.out.println("Usage: FormatExtractor <skip> <path to confile>");
		}
		System.exit(0);
	}
}
