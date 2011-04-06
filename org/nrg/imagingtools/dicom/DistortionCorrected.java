/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.dicom;

public class DistortionCorrected {

    String file;
    private static final String tag = "0008,0008";
    
    public DistortionCorrected(String args[]) {
        if (args.length == 0) {
            showUsage(); System.exit(1);
        }
        file = args[0];
        if (file==null) {
            showUsage(); System.exit(2);
        }
    }
    
    public String detect() {
        DicomInfo dcmInfo = new DicomInfo(file,tag);
        String mriCharacteristics = dcmInfo.getTagValue();
        String print = "ERR";
        if (mriCharacteristics != null) {
            String[] charParts = mriCharacteristics.split("\\\\");
            if (charParts.length >4) {
                print = charParts[3];
                System.out.println("Distortion Correction:: " + print);
            }
        }
        return print;
    }
    
    private void showUsage() {
        System.out.println("DistortionCorrected <diccm file path>");
        System.out.println("PURPOSE: This program tests to see if the scanner has corrected the distortion at aquisition");
        System.out.println("USAGE: DistortionCorrected <path to dicom file>");
        System.out.println("Return value: D if the scanner has corrected and ND if the scanner has not and ERR otherwise");
    }
    
    public static void main(String args[]) {
        DistortionCorrected distortionCorrected = new DistortionCorrected(args);
        distortionCorrected.detect();
        
    }
}
