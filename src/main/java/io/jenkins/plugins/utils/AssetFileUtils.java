package io.jenkins.plugins.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.DirectoryScanner;

import hudson.FilePath;
import io.jenkins.plugins.AssetBuilder;
import io.jenkins.plugins.datatype.ArchitectureType;
import io.jenkins.plugins.datatype.BitsType;
import io.jenkins.plugins.datatype.PlatformType;

public class AssetFileUtils {

	public static final Logger LOGGER = Logger.getLogger(AssetFileUtils.class.getName());

	public static final String cons3rtAssetBuilderPrefix = "CONS3RT-Asset-Builder";

	public static final String cons3rtAssetPrefix = "CONS3RT-Asset";

	public static final String wildCard = "*";

	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
	}

	static FilenameFilter assetPropertiesFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.equalsIgnoreCase("asset.properties");
		}
	};

	static FilenameFilter prebuiltAssetFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.startsWith(cons3rtAssetPrefix);
		}
	};

	public static File getAssetZipFromPath(final FilePath buildWorkspace, FilePath providedPath)
			throws IOException, InterruptedException {

		final File providedFile = new File(providedPath.toURI());

		final File assetZipForUpload;

		// Test if the provided file path points to a zip or a directory:
		if (providedFile.exists()) {
			if (providedFile.isDirectory()) {
				LOGGER.log(Level.INFO, "Provided file " + providedPath
						+ " is a directory. Checking for top level asset.properties file", "INFO");

				// Make sure an asset.properties file (and only 1) exists at the top level of
				// the directory
				
				
				final String[] fileList = providedFile.list(assetPropertiesFilter);
				
				final List<String> assetPropsFiles;
				if(fileList != null) {
					assetPropsFiles = Arrays.asList(fileList);
				} else {
					final String message = "Could not find asset.properties file at top level of directory "
							+ providedPath;
					LOGGER.log(Level.WARNING, message);
					throw new IOException(message);
				} 
				
				if (assetPropsFiles.size() > 1) {
					final String message = "Found " + assetPropsFiles.size()
							+ " asset.properties files at top level of directory " + providedPath;
					LOGGER.warning(message);
					throw new IOException(message);
				} else {
					LOGGER.info("Found asset.properties file at top level of directory " + providedPath
							+ ". Attempting to create zip for asset import");
				}

				final StringBuilder assetZipName = new StringBuilder();
				assetZipName.append(AssetFileUtils.cons3rtAssetPrefix);
				assetZipName.append("-");

				if (providedPath.getBaseName().startsWith(cons3rtAssetBuilderPrefix)) {
					LOGGER.info(
							"Asset to be zipped is an asset built by the cons3rt asset builder plugin. Removing prefix and timestamp to get name");
					final String baseName = providedPath.getBaseName().replace(cons3rtAssetBuilderPrefix + "-", "");
					final String name = baseName.substring(0, baseName.lastIndexOf("-"));
					assetZipName.append(name);
				} else {
					assetZipName.append(providedPath.getBaseName());
				}

				final String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
				assetZipName.append("-");
				assetZipName.append(timeStamp);
				assetZipName.append(".zip");

				final FilePath zipFilePath = new FilePath(buildWorkspace, assetZipName.toString());

				// final FilePath zipFilePath =
				// buildWorkspace.createTempFile("cons3rt-jenkins-plugin-asset-", ".zip");
				LOGGER.info("Zipping directory " + providedPath.toURI().toString() + " to file "
						+ zipFilePath.toURI().toString());
				Cons3rtCompressUtils.INSTANCE.zipIntoZipFileFromDir(Paths.get(zipFilePath.toURI()),
						Paths.get(providedPath.toURI()), false, null);
				LOGGER.info("Zip created.");
				assetZipForUpload = new File(zipFilePath.toURI());
			} else {
				LOGGER.log(Level.INFO,
						"Provided file " + providedPath + " is not a directory. Checking if file is a zip file");
				if (providedFile.getName().endsWith(".zip")) {
					LOGGER.info("Provided file " + providedPath
							+ " is a zip file ending in .zip, this file will be used for asset upload.");
					assetZipForUpload = providedFile;
				} else {
					final String message = "Provided file " + providedPath + " is not a zip file ending in .zip";
					LOGGER.warning(message);
					throw new IOException(message);
				}
			}
		} else {
			final String message = "Provided file " + providedPath + " does not exist.";
			LOGGER.warning(message);
			throw new IOException(message);
		}

		return assetZipForUpload;
	}

	public static FilePath createWorkingDirectory(final FilePath workspace, final String name)
			throws IOException, InterruptedException {
		try {
			// Convert name to clean name:
			final String cleanedName = name.replaceAll(" ", "_");
			final StringBuilder workDirName = new StringBuilder();
			workDirName.append(AssetFileUtils.cons3rtAssetBuilderPrefix);
			workDirName.append("-");
			workDirName.append(cleanedName);
			workDirName.append("-");

			final String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			workDirName.append(timeStamp);

			final FilePath workDir = new FilePath(workspace, workDirName.toString());
			workDir.mkdirs();
			return workDir;
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		}
	}

	public static void createAssetPropertiesFile(final FilePath workDir, final AssetBuilder assetBuilder)
			throws IOException, InterruptedException {
		// Based on fields in assetBuilder create asset properties file
		final String newline = System.getProperty("line.separator");
		final String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		final StringBuilder sb = new StringBuilder();

		// Add header:
		sb.append('#');
		sb.append("#Created by CONS3RT Jenkins Builder Plugin");
		sb.append(newline);

		// Add timestamp as header:
		sb.append('#');
		sb.append(timeStamp);
		sb.append(newline);

		// Add software type info:
		sb.append("assetType=SOFTWARE");
		sb.append(newline);
		sb.append("softwareAssetType=APPLICATION");
		sb.append(newline);
		sb.append(newline);

		sb.append("name=");
		sb.append(assetBuilder.getName());
		sb.append(newline);

		sb.append("installScript=");
		sb.append(assetBuilder.getInstallScriptFileName());
		sb.append(newline);

		if (assetBuilder.getDescription() != null && !assetBuilder.getDescription().isEmpty()) {
			sb.append("description=");
			sb.append(assetBuilder.getDescription());
			sb.append(newline);
		}

		if (assetBuilder.getPlatform() != null && !assetBuilder.getPlatform().isEmpty()) {
			sb.append("applicationOsPlatform=");
			sb.append(PlatformType.valueFromDisplayName(assetBuilder.getPlatform()).name());
			sb.append(newline);
		}

		if (assetBuilder.getArchitecture() != null && !assetBuilder.getArchitecture().isEmpty()) {
			sb.append("applicationArchitecture=");
			sb.append(ArchitectureType.valueFromDisplayName(assetBuilder.getArchitecture()).name());
			sb.append(newline);
		}

		if (assetBuilder.getBits() != null && !assetBuilder.getBits().isEmpty()) {
			sb.append("applicationBits=");
			sb.append(BitsType.valueFromDisplayName(assetBuilder.getBits()).name());
			sb.append(newline);
		}

		if (assetBuilder.getCpu() != null) {
			sb.append("applicationRequiredCpuCount=");
			sb.append(assetBuilder.getCpu());
			sb.append(newline);
		}

		if (assetBuilder.getMemory() != null) {
			sb.append("requiredRam=");
			sb.append(assetBuilder.getMemory());
			sb.append(newline);
		}

		if (assetBuilder.getStorage() != null) {
			sb.append("requiredDisk=");
			sb.append(assetBuilder.getStorage());
			sb.append(newline);
		}

		if (assetBuilder.hasDocumentation()) {
			sb.append("documentationFile=");
			sb.append(assetBuilder.getDocumentationFileName());
			sb.append(newline);
		}

		if (assetBuilder.hasLicense()) {
			sb.append("licenseFile=");
			sb.append(assetBuilder.getLicenseFileName());
			sb.append(newline);
		}

		// Write the properties file
		final FilePath propertiesFile = new FilePath(workDir, "asset.properties");
		propertiesFile.write(sb.toString(), null);
	}

	public static FilePath verifyFileExists(final FilePath workspace, final String pathToFile)
			throws IOException, InterruptedException {
		final FilePath providedPath = new FilePath(workspace, pathToFile);

		// Test if the file exists, if so return file path
		if (providedPath.exists()) {
			return providedPath;
		} else {
			throw new IOException(
					"Could not find file " + providedPath.getName() + " in build workspace " + workspace.getBaseName());
		}
	}

	public static void copyAssetFile(final ContextLogger log, final FilePath workspace, final String relativePath,
			final FilePath targetDirectory) throws IOException, InterruptedException {
		final FilePath currentfile = AssetFileUtils.verifyFileExists(workspace, relativePath);
		final FilePath targetPath = new FilePath(targetDirectory, currentfile.getName());

		log.log("Copying file: " + currentfile.getName() + " to " + targetPath.toURI());

		if (currentfile.isDirectory()) {
			log.log("Directory detected, copying contents");
			currentfile.copyRecursiveTo(targetPath);
		} else {
			currentfile.copyTo(targetPath);
		}

		log.log("Media file copied.");
	}

	public static Set<String> findWildcardMatches(FilePath workspace, String relativePath)
			throws IOException, InterruptedException {

		final Set<String> results = new HashSet<>();

		final String baseDir = Paths.get(workspace.toURI()).toString();

		LOGGER.info("Searching with basedir: " + baseDir);
		LOGGER.info("Finding all files returned by search for: " + relativePath);
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[] { relativePath });
		scanner.setBasedir(baseDir);
		scanner.setCaseSensitive(true);
		scanner.scan();

		String[] dirs = scanner.getIncludedDirectories();
		String[] files = scanner.getIncludedFiles();

		for (final String dir : dirs) {
			LOGGER.info("Search found directory: " + dir);
			results.add(dir);
		}

		for (final String file : files) {
			LOGGER.info("Search found file: " + file);
			results.add(file);
		}

		return results;
	}

	public static FilePath findPrebuiltAsset(FilePath workspace, String prebuiltAssetName)
			throws IOException, InterruptedException, ParseException {
		final String cleanedName = prebuiltAssetName.replaceAll(" ", "_");

		final File workspaceDir = new File(workspace.toURI());

		final String[] fileList = workspaceDir.list(prebuiltAssetFilter);

		final List<String> prebuiltAssets;
		if (fileList != null) {
			prebuiltAssets = Arrays.asList(fileList);
		} else {
			final String message = "Could not find " + cleanedName + " in workspace.";
			LOGGER.log(Level.WARNING, message);
			throw new IOException(message);
		}

		LOGGER.log(Level.INFO, "Found " + prebuiltAssets.size() + " prebuilt assets in workspace.");

		final Map<Date, String> datedFiles = new HashMap<>();

		// Scan all prebuild assets
		for (final String file : prebuiltAssets) {
			LOGGER.log(Level.INFO, "Found file: " + file);
			final String baseName = file.replace(cons3rtAssetPrefix + "-", "");
			final String name = baseName.substring(0, baseName.lastIndexOf("-"));
			final String dateString = baseName.substring(baseName.lastIndexOf("-") + 1, baseName.length());

			// Determing name and date
			LOGGER.log(Level.INFO, "Found prebuilt asset with name: " + name + " and date: " + dateString);

			// If name matches request, parse date and add to results map
			if (cleanedName.equals(name)) {
				LOGGER.log(Level.INFO, "Pre-Built asset has desired name, adding: " + file);
				Date date = getDateFormat().parse(dateString);
				datedFiles.put(date, file);
			}

		}

		// If results map is empty error, else determine newest and return
		if (datedFiles.isEmpty()) {
			throw new IOException("A prebuilt asset for name: " + prebuiltAssetName + " was not found in workspace.");
		} else {
			final Date newest = Collections.max(datedFiles.keySet());
			final String timestamp = getDateFormat().format(newest);
			LOGGER.log(Level.INFO, "Newest prebuilt asset: " + cleanedName + " dated: " + timestamp);
			final String newestFile = datedFiles.get(newest);
			return new FilePath(workspace, newestFile);
		}

	}
}
