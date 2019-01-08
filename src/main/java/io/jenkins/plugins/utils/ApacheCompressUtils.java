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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * The Class ApacheCompressUtils.
 */
public class ApacheCompressUtils {
	
	/** The CONS3RT character set for everything except Java properties files *. */
    public static final Charset CONS3RT_CHARSET = StandardCharsets.UTF_8;

    /** The Constant COLON. */
    private static final CharSequence COLON = ":";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Remove the leading part of each entry that contains the source directory
     * name.
     *
     * @param baseDirPath    the base dir path
     * @param fullFilePath   the file that is about to be added
     * @param includeBaseDir the include base dir
     * @return the name of an archive entry
     */
    private static String getEntryName(final Path baseDirPath, Path fullFilePath, boolean includeBaseDir) {
        final String entryName;
        if (includeBaseDir) {
            entryName = baseDirPath.getParent().relativize(fullFilePath).toString();
        } else {
            entryName = baseDirPath.relativize(fullFilePath).toString();
        }
        return entryName;
    }

    /**
     * Gets the zip file entry as string.
     *
     * @param zipFilePath the zip file path
     * @param entryToGet  the entry to get
     * @return the zip file entry as string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String getZipFileEntryAsString(final Path zipFilePath, final String entryToGet) throws IOException {
        try (final ZipFile commonsZipFile = new ZipFile(zipFilePath.toFile())) {
            for (final Enumeration<ZipArchiveEntry> e = commonsZipFile.getEntries(); e.hasMoreElements();) {
                final ZipArchiveEntry entry = e.nextElement();
                final String entryName = entry.getName();
                if (entryName.equals(entryToGet)) {
                    try (final InputStream in = commonsZipFile.getInputStream(entry);
                            final BufferedInputStream bin = new BufferedInputStream(in)) {
                        return IOUtils.toString(bin, CONS3RT_CHARSET);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets the zip file listing.
     *
     * @param zipFilePath the zip file path
     * @return the zip file listing
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static List<String> getZipFileListing(final Path zipFilePath) throws IOException {
        final List<String> listing = new ArrayList<>();
        try (final ZipFile commonsZipFile = new ZipFile(zipFilePath.toFile())) {
            for (final Enumeration<ZipArchiveEntry> e = commonsZipFile.getEntries(); e.hasMoreElements();) {
                listing.add(e.nextElement().getName());
            }
        }
        return listing;
    }

    /**
     * Unzip into dir from zip file.
     *
     * @param outputDirPath  the output dir path
     * @param zipFilePath    the zip file path
     * @param filesToInclude the files to include
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws InvalidPathException the invalid path exception
     */
    public static void unzipIntoDirFromZipFile(final Path outputDirPath, final Path zipFilePath,
            final List<String> filesToInclude) throws IOException, InvalidPathException {

        LOGGER.info("unzipIntoDirFromZipFile: starting unzip of {} into directory {} (filesToInclude = {})",
                zipFilePath, outputDirPath, filesToInclude);

        if (!outputDirPath.toFile().exists()) {
            LOGGER.info("unzipIntoDirFromZipFile: unzip destination directory {} does not exist - will try to create",
                    outputDirPath);
            Files.createDirectories(outputDirPath);
        }

        if (!outputDirPath.toFile().canWrite()) {
            final String msg = "unzip destination directory " + outputDirPath + " is not writable";
            LOGGER.warn("unzipIntoDirFromZipFile: {}", msg);
            throw new InvalidPathException(msg, outputDirPath.toString());
        }

        // did caller provide a populated include list? If so, we need to set some stuff
        // up
        final boolean includeListProvided = filesToInclude != null && !filesToInclude.isEmpty();
        final List<String> localIncludeList;
        if (includeListProvided) {
            localIncludeList = new ArrayList<>(filesToInclude);
        } else {
            localIncludeList = new ArrayList<>();
        }

        try (final ZipFile commonsZipFile = new ZipFile(zipFilePath.toFile())) {

            for (final Enumeration<ZipArchiveEntry> e = commonsZipFile.getEntries(); e.hasMoreElements();) {

                // if we were provided an include list, and that is now empty, we are done
                if (includeListProvided) {
                    if (localIncludeList.isEmpty()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("unzipIntoDirFromZipFile: include file list empty - exiting loop");
                        }
                        break;
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("unzipIntoDirFromZipFile: files left to extract {}", localIncludeList);
                        }
                    }
                }

                final ZipArchiveEntry entry = e.nextElement();
                final String entryName = entry.getName();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("unzipIntoDirFromZipFile: found entry {} (directory = {})", entryName,
                            entry.isDirectory());
                }

                final boolean skip = Platform.isWindows() && entryName.contains(COLON);
                if (skip) {
                    LOGGER.info("unzipIntoDirFromZipFile: skipping entry {} containing {} on Windows platform",
                            entryName, COLON);
                    continue;
                }

                final Path entryOutputPath = outputDirPath.resolve(entryName);
                if (entry.isDirectory()) {
                    if (!includeListProvided || localIncludeList.contains(entryName)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("unzipIntoDirFromZipFile: creating directory {})", entryOutputPath);
                        }
                        Files.createDirectories(entryOutputPath);
                    } else {
                        LOGGER.info(
                                "unzipIntoDirFromZipFile: skipping directory entry {} as it is not on provided list of files to include",
                                entryName);
                    }
                } else if (entry.isUnixSymlink()) {

                    if (!includeListProvided || localIncludeList.contains(entryName)) {

                        // now copy from the zip stream to a string to get the link target path
                        final String targetString;
                        try (final InputStream in = commonsZipFile.getInputStream(entry);
                                final ByteArrayOutputStream result = new ByteArrayOutputStream()) {

                            final byte[] buffer = new byte[1024];
                            int length;
                            while ((length = in.read(buffer)) != -1) {
                                result.write(buffer, 0, length);
                            }
                            targetString = result.toString(CONS3RT_CHARSET.name());
                        }

                        final Path targetPath = FileSystems.getDefault().getPath(targetString);

                        // create the sym link
                        Files.createSymbolicLink(entryOutputPath, targetPath);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("unzipIntoDirFromZipFile: created sym link at {} pointing to {})",
                                    entryOutputPath, targetPath);
                        }

                        // remove file from list to include
                        if (includeListProvided) {
                            localIncludeList.remove(entryName);
                        }

                    } else {
                        LOGGER.info(
                                "unzipIntoDirFromZipFile: skipping sym link entry {} as it is not on provided list of files to include",
                                entryName);
                    }
                } else {
                    // file...
                    if (!includeListProvided || localIncludeList.contains(entryName)) {

                        // we might need to create the parent dir(s), depending on the way the zip file
                        // was made
                        final Path entryOutputPathParent = entryOutputPath.getParent();
                        if (!entryOutputPathParent.toFile().exists()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("unzipIntoDirFromZipFile: creating output path parent {})",
                                        entryOutputPathParent);
                            }
                            Files.createDirectories(entryOutputPathParent);
                        }

                        // now copy from the zip stream to the output path

                        try (final InputStream in = commonsZipFile.getInputStream(entry);
                                final BufferedInputStream bin = new BufferedInputStream(in);
                                final OutputStream out = new FileOutputStream(entryOutputPath.toFile())) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("unzipIntoDirFromZipFile: copying entry {} to {})", entryName,
                                        entryOutputPath);
                            }
                            IOUtils.copy(bin, out);
                        }
                        // remove file from list to include
                        if (includeListProvided) {
                            localIncludeList.remove(entryName);
                        }

                    } else {
                        LOGGER.info(
                                "unzipIntoDirFromZipFile: skipping file entry {} as it is not on provided list of files to include",
                                entryName);
                    }
                }
            }

            if (includeListProvided && !localIncludeList.isEmpty()) {
                LOGGER.warn(
                        "unzipIntoDirFromZipFile: non-empty include list was provided, but one or more entries not found so zip was entirely traversed (remaining entries = {})",
                        localIncludeList);
            }
        }
    }

    /**
     * Zip into zip file from dir.
     *
     * @param destZipFilePath  the dest zip file path
     * @param sourceDirPath    the source dir path
     * @param includeSourceDir include source dir as top level dir of zip file
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws InvalidPathException the invalid path exception
     */
    public static void zipIntoZipFileFromDir(final Path destZipFilePath, final Path sourceDirPath,
            final boolean includeSourceDir, final String extensionToMatch) throws IOException, InvalidPathException {

        // make sure we can write to the dest dir
        final Path parentDir = destZipFilePath.getParent();
        if ((parentDir == null) || (!Files.isWritable(parentDir))) {
            final String msg = "zip destination directory " + parentDir + " failed writability test";
            LOGGER.warn("zipIntoZipFileFromDir: {}", msg);
            throw new InvalidPathException(msg, (parentDir == null) ? "null" : parentDir.toString());
        }

        try (final OutputStream fos = new FileOutputStream(destZipFilePath.toFile());
                final BufferedOutputStream bfos = new BufferedOutputStream(fos);
                final ZipArchiveOutputStream zaos = (ZipArchiveOutputStream) new ArchiveStreamFactory()
                        .createArchiveOutputStream(ArchiveStreamFactory.ZIP, bfos)) {

            // See
            // http://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/archivers/zip/ZipArchiveOutputStream.html#setUseZip64-org.apache.commons.compress.archivers.zip.Zip64Mode-
            zaos.setUseZip64(Zip64Mode.AsNeeded);

            Files.walkFileTree(sourceDirPath, new SimpleFileVisitor<Path>() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
                 * java.nio.file.attribute.BasicFileAttributes)
                 */
                @Override
                public FileVisitResult preVisitDirectory(final Path dirPath, final BasicFileAttributes attrs)
                        throws IOException {

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("preVisitDirectory: entered for {} (isDirectory = {} isRegularFile = {})", dirPath,
                                attrs.isDirectory(), attrs.isRegularFile());
                    }

                    // get the entry name for our zip file entry based on path + desire to include
                    // source dir
                    final String entryName = getEntryName(sourceDirPath, dirPath, includeSourceDir);

                    if (!entryName.isEmpty()) {

                        // need to put a / on the end to force archive entry to be a directory
                        final String dirEntryName = entryName.concat("/");

                        // create a new entry object - iff name ends with "/" , this is a directory
                        final ZipArchiveEntry entry = new ZipArchiveEntry(dirEntryName);

                        // put the headers for the entry in the archive file
                        try {
                            zaos.putArchiveEntry(entry);
                        } finally {
                            // close this entry of the archive
                            zaos.closeArchiveEntry();
                        }

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("preVisitDirectory: added zip dir entry {} (source path {})", dirEntryName,
                                    dirPath);
                        }

                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(
                                    "preVisitDirectory: skipping add of zip dir entry for {} (entry name is empty)",
                                    dirPath);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
                 * java.nio.file.attribute.BasicFileAttributes)
                 */
                @Override
                public FileVisitResult visitFile(final Path filePath, final BasicFileAttributes attrs)
                        throws IOException {

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                                "visitFile: entered for {} (isDirectory = {} isRegularFile = {} isSymbolicLink = {})",
                                filePath, attrs.isDirectory(), attrs.isRegularFile(), attrs.isSymbolicLink());
                    }

                    // if we only want files with certain extensions, check that
                    if (extensionToMatch != null && !extensionToMatch.isEmpty()
                            && !filePath.getFileName().toString().endsWith(".".concat(extensionToMatch))) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("visitFile: not adding {} - did not match extension {}", filePath,
                                    extensionToMatch);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    // we will need to handle sym links - hard links are mostly invisible to us at
                    // this level of abstraction, and zip files do not support them

                    // get the entry name for our zip file entry based on path + desire to include
                    // source dir
                    final String entryName = getEntryName(sourceDirPath, filePath, includeSourceDir);

                    // create a new entry object
                    final ZipArchiveEntry entry = new ZipArchiveEntry(entryName);

                    // if this is a sym link...
                    if (attrs.isSymbolicLink()) {

                        // copy the content for this entry into the archive - either the contents of the
                        // target if not a sym link,
                        // or the target path if a sym link
                        Path realPath = null;
                        try {
                            realPath = filePath.toRealPath();
                        } catch (final IOException ioe) {
                            LOGGER.warn("visitFile: could not find target for symbolic link entry {} - skipping",
                                    entryName);
                        }

                        if (realPath != null) {

                            // get the target path, adjusted for our zip file path
                            final String realPathEntryName = getEntryName(sourceDirPath, realPath, includeSourceDir);

                            // we need to set a flag in the entry
                            entry.setUnixMode(UnixStat.LINK_FLAG | UnixStat.DEFAULT_LINK_PERM);

                            // put the headers for the entry in the archive file
                            zaos.putArchiveEntry(entry);

                            // write the target path string as the entry contents
                            try {
                                zaos.write(realPathEntryName.getBytes(CONS3RT_CHARSET));
                            } finally {
                                zaos.closeArchiveEntry();
                            }

                            LOGGER.info("visitFile: added zip file symbolic link entry {} pointing to target path {}",
                                    entryName, realPathEntryName);
                        }
                    } else {

                        // set the size
                        entry.setSize(filePath.toFile().length());

                        // put the headers for the entry in the archive file
                        zaos.putArchiveEntry(entry);

                        try (final BufferedInputStream input = new BufferedInputStream(
                                new FileInputStream(filePath.toFile()))) {
                            IOUtils.copy(input, zaos);
                        } finally {
                            // close this entry of the archive
                            zaos.closeArchiveEntry();
                        }

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("visitFile: added zip file entry {} (source path {})", entryName, filePath);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final ArchiveException e) {
            final String msg = " caught " + e.getClass().getSimpleName() + " creating archive stream into "
                    + destZipFilePath + " (message = " + e.getMessage() + ")";
            LOGGER.info("zipIntoZipFileFromDir: {}", msg);
            throw new IOException(msg);
        }
    }

    /**
     * Instantiates a new apache compress utils.
     */
    private ApacheCompressUtils() {
        // private ktor
    }
}
