/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.imagingtools.utils;

public class Converter {
	
	 /** Mapping from character values to their hexadecimal evaluations. */
	  private static int[] _eval = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0, 0, 10, 11, 12,13, 14, 15};


	 public static final byte[] toByteArray(String hexString)
	    {
		 if ( hexString.length() % 2 == 1 ) {
	    	  String msg = "Hexadecimal string does not have an even length.";
	    	  //throw new IllegalArgumentException(msg);
	      }

	      // Allocate room for the bytes
	      byte[] byteArray = new byte[hexString.length()/2];

	      // Convert each 2 String characters to a byte
	      for (int i = 0; i < hexString.length()-1; i+=2) {
	    	  int loc1 = hexString.charAt(i)-48 ;
	    	  int loc2 = hexString.charAt(i+1)-48;
	    	  System.out.print("L= " + hexString.length() + " i= " + i + " " + "  " + ((loc1 << 4) & 0xF0)+ " " + loc2 + " " + hexString.charAt(i) + " " + hexString.charAt(i+1));
	    	  int digit = ((_eval[loc1] << 4) & 0xF0) + _eval[loc2];
	    	  System.out.println(" Digit " + digit);
	    	  byteArray[i/2] = (byte)digit;
	      }
	      // Return the byte array
	      return byteArray;
	    }

}
