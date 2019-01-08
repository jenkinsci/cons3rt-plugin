/**********************************************************************************************************************************
* Jackpine Technologies Corporation ("JACKPINE") CONFIDENTIAL
* Unpublished Copyright (c) 2009-2017 Jackpine Technologies Corporation, All Rights Reserved.
*
* NOTICE:  This source file is part of JACKPINE's CONS3RT product.
* All information contained herein is, and remains the property of JACKPINE. The intellectual and technical concepts 
* contained herein are proprietary to JACKPINE and may be covered by U.S. and Foreign Patents, patents in process, and are 
* protected by trade secret or copyright law.
*
* Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
* from JACKPINE.  Access to the source code contained herein is hereby forbidden to anyone except current JACKPINE employees,
* managers or contractors who have executed Confidentiality and Non-disclosure agreements explicitly covering such access.
*
* The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes  
* information that is confidential and/or proprietary, and is a trade secret, of JACKPINE.
*
* ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE 
* WITHOUT THE EXPRESS WRITTEN CONSENT OF COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE LAWS AND
* INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY
* ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT
* MAY DESCRIBE, IN WHOLE OR IN PART.
* 
* Contact: Web - http://www.jackpinetech.com Email - legal@jackpinetech.com
* $Id$
************************************************************************************************************************************/

package io.jenkins.plugins.utils;

import java.util.HashSet;
import java.util.Set;

import io.jenkins.plugins.exceptions.InvalidNameException;

/**
 * The Class AssetNameUtils.
 */
public final class NameUtils {

    /**
     * Instantiates a new asset name utils.
     */
    private NameUtils() {
    }

    /**
     * Check to see that the given name passes any and all restrictions on an asset's name, except uniqueness.
     *
     * @param name
     *            the name to check
     * @throws InvalidNameException
     *             thrown if any problems encountered, and populated with description(s) of violation(s)
     */
    public static void checkAssetNameRestrictions(final String name) throws InvalidNameException {
        if (name == null) {
            throw new InvalidNameException("Asset name cannot be null", "(null)");
        } else if (name.length() == 0) {
            throw new InvalidNameException("Asset name cannot be blank", "");
        } else if (name.trim().length() == 0) {
            throw new InvalidNameException("Asset name cannot consist entirely of whitespace", "<" + name + ">");
        } else if (name.trim().length() != name.length()) {
            throw new InvalidNameException("Asset name cannot have leading or trailing whitespace", "<" + name + ">");
        }

        final String invalidChars = "!@#$%^&*;:\"<>?/\\'`~+={}[],";

        // does the name contain invalid characters
        if (!NameUtils.containsNone(name, invalidChars.toCharArray())) {
            throw new InvalidNameException(name + " contains one or more of following invalid characters: " + invalidChars, name);
        }
    }
    
    public static void checkNullOrEmpty(final String string) throws InvalidNameException {
        if (string == null) {
            throw new InvalidNameException("A value must be provided", "(null)");
        } else if (string.length() == 0) {
            throw new InvalidNameException("The value provided cannot be blank", "");
        } else if (string.trim().length() == 0) {
            throw new InvalidNameException("The value provided cannot consist entirely of whitespace", "<" + string + ">");
        } else if (string.trim().length() != string.length()) {
            throw new InvalidNameException("The value provided cannot have leading or trailing whitespace", "<" + string + ">");
        }
    }
    
    public static void checkCreatedUsernameRestrictions(String deploymentRunUsername) throws InvalidNameException {
    	 if (deploymentRunUsername == null) {
             throw new InvalidNameException("Username cannot be null", "(null)");
         } else if (deploymentRunUsername.length() == 0) {
             throw new InvalidNameException("Username cannot be blank", "");
         } else if (deploymentRunUsername.trim().length() == 0) {
             throw new InvalidNameException("Username cannot consist entirely of whitespace", "<" + deploymentRunUsername + ">");
         } else if (deploymentRunUsername.trim().length() != deploymentRunUsername.length()) {
             throw new InvalidNameException("Username cannot have leading or trailing whitespace", "<" + deploymentRunUsername + ">");
         }
    	 
    	 final Set<String> invalidUsernames = new HashSet<>();
    	 invalidUsernames.add("cons3rt");
    	 invalidUsernames.add("root");
         invalidUsernames.add("admin");
         invalidUsernames.add("administrator");
         
         
         if(invalidUsernames.contains(deploymentRunUsername.toLowerCase())) {
        	 throw new InvalidNameException("You have entered a restricted username: " + deploymentRunUsername, deploymentRunUsername);
         }
	}
    
    public static void checkDeploymentRunNameRestrictions(final String name) throws InvalidNameException {
        if (name == null) {
            throw new InvalidNameException("Deployment run name cannot be null", "(null)");
        } else if (name.length() == 0) {
            throw new InvalidNameException("Deployment run name cannot be blank", "");
        } else if (name.trim().length() == 0) {
            throw new InvalidNameException("Deployment run name cannot consist entirely of whitespace", "<" + name + ">");
        } else if (name.trim().length() != name.length()) {
            throw new InvalidNameException("Deployment run name cannot have leading or trailing whitespace", "<" + name + ">");
        }

        final String invalidChars = "!@#$%^&*;:\"<>?/\\'`~+={}[],";

        // does the name contain invalid characters
        if (!NameUtils.containsNone(name, invalidChars.toCharArray())) {
            throw new InvalidNameException(name + " contains one or more of following invalid characters: " + invalidChars, name);
        }
    }

    /**
     * Method to check for existence of invalid chars in a string. Originally from Apache StringUtils
     *
     * @param str
     *            the string to check
     * @param invalidChars
     *            the invalid characters
     * @return true if string does not contain any of the chars, false if it does
     */
    private static boolean containsNone(final String str, final char[] invalidChars) {
        if ((str == null) || (invalidChars == null)) {
            return true;
        }
        final int strSize = str.length();
        final int validSize = invalidChars.length;
        for (int i = 0; i < strSize; i++) {
            final char ch = str.charAt(i);
            for (int j = 0; j < validSize; j++) {
                if (invalidChars[j] == ch) {
                    return false;
                }
            }
        }
        return true;
    }

}
