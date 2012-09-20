/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author Lorenzzaccio
 */
public class util {

    public static boolean  isNum(String s) {
    try {
    Double.parseDouble(s);
    }
    catch (NumberFormatException nfe) {
    return false;
    }
    return true;
    }

    public static boolean  isPositive(String s) {
    Double val;
    try {
    val = Double.parseDouble(s);
    }
    catch (NumberFormatException nfe) {
    return false;
    }
    if(val <0)
        return false;
    return true;
    }

}
