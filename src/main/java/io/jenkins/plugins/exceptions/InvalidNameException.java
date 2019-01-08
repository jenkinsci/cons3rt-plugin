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

package io.jenkins.plugins.exceptions;

import java.util.List;

/**
 * Exception that is thrown when an invalid asset name is encountered.
 */

public final class InvalidNameException extends Exception {

    /** Serializable. */
    private static final long serialVersionUID = 1L;

    /** The invalid name. */
    private String invalidName;

    /** The its violations. */
    private List<String> itsViolations;

    /**
     * Default no-arg constructor.
     */
    public InvalidNameException() {
        super("InvalidNameException");
    }

    /**
     * Constructor.
     *
     * @param message
     *            The message
     * @param invalidName
     *            the invalid user name
     */
    public InvalidNameException(final String message, final String invalidName) {
        super(message);
        this.invalidName = invalidName;
    }

    /**
     * Constructor to be used with a caught exception.
     *
     * @param message
     *            The message
     * @param cause
     *            The exception used in creation
     * @param invalidName
     *            the invalid user name
     */
    public InvalidNameException(final String message, final Throwable cause, final String invalidName) {
        super(message, cause);
        this.invalidName = invalidName;
    }

    /**
     * Gets the invalid name.
     *
     * @return the invalidName
     */
    public String getInvalidName() {
        return this.invalidName;
    }

    /**
     * Gets the its violations.
     *
     * @return the itsViolations
     */
    public List<String> getItsViolations() {
        return this.itsViolations;
    }

    /**
     * Sets the invalid name.
     *
     * @param invalidName
     *            the invalidName to set
     */
    public void setInvalidName(final String invalidName) {
        this.invalidName = invalidName;
    }

    /**
     * Sets the its violations.
     *
     * @param itsViolations
     *            the itsViolations to set
     */
    public void setItsViolations(final List<String> itsViolations) {
        this.itsViolations = itsViolations;
    }

}
