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

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/**
 * The Enum Cons3rtCompressUtils.
 */
public enum Cons3rtCompressUtils {

    INSTANCE;

    /**
     * Gets the zip file entry as string.
     *
     * @param zipFilePath the zip file path
     * @param entryToGet  the entry to get
     * @return the zip file entry as string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String getZipFileEntryAsString(final Path zipFilePath, final String entryToGet) throws IOException {
        return ApacheCompressUtils.getZipFileEntryAsString(zipFilePath, entryToGet);
    }

    /**
     * Gets the zip file listing.
     *
     * @param zipFilePath the zip file path
     * @return the zip file listing
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public List<String> getZipFileListing(final Path zipFilePath) throws IOException {
        return ApacheCompressUtils.getZipFileListing(zipFilePath);
    }

    /**
     * Unzip into dir from zip file.
     *
     * @param destDirPath the dest dir path
     * @param zipFilePath the zip file path
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws InvalidPathException the invalid path exception
     */
    public void unzipIntoDirFromZipFile(final Path destDirPath, final Path zipFilePath)
            throws IOException, InvalidPathException {
        ApacheCompressUtils.unzipIntoDirFromZipFile(destDirPath, zipFilePath, null);
    }

    /**
     * Unzip into dir from zip file.
     *
     * @param destDirPath    the dest dir path
     * @param zipFilePath    the zip file path
     * @param filesToInclude the files to include
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws InvalidPathException the invalid path exception
     */
    public void unzipIntoDirFromZipFile(final Path destDirPath, final Path zipFilePath,
            final List<String> filesToInclude) throws IOException, InvalidPathException {
        ApacheCompressUtils.unzipIntoDirFromZipFile(destDirPath, zipFilePath, filesToInclude);
    }

    /**
     * Zip into zip file from dir.
     *
     * @param destZipFilePath  the dest zip file path
     * @param sourceDirPath    the source dir path
     * @param includeSourceDir include the source dir itself as the root dir in the
     *                         resulting zip file
     * @param extensionToMatch the extension to match - ignored if null or empty
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws InvalidPathException the invalid path exception
     */
    public void zipIntoZipFileFromDir(final Path destZipFilePath, final Path sourceDirPath,
            final boolean includeSourceDir, final String extensionToMatch) throws IOException, InvalidPathException {
        ApacheCompressUtils.zipIntoZipFileFromDir(destZipFilePath, sourceDirPath, includeSourceDir, extensionToMatch);
    }
}
