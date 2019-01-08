package io.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.datatype.ArchitectureType;
import io.jenkins.plugins.datatype.BitsType;
import io.jenkins.plugins.datatype.FileObject;
import io.jenkins.plugins.datatype.PlatformType;
import io.jenkins.plugins.exceptions.InvalidNameException;
import io.jenkins.plugins.utils.AssetFileUtils;
import io.jenkins.plugins.utils.NameUtils;
import io.jenkins.plugins.utils.ContextLogger;
import net.sf.json.JSONObject;

public class AssetBuilder extends Builder {

	public static final Logger LOGGER = Logger.getLogger(AssetBuilder.class.getName());
	
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
		
		public FormValidation doCheckName(@QueryParameter("name") String name) {
			
			//Test that name satisfies cons3rt standards:
			try {
				NameUtils.checkAssetNameRestrictions(name);
			} catch (InvalidNameException e ) {
				return FormValidation.error(e.getMessage());
			}
			
			return FormValidation.ok();
		}
		
		public ListBoxModel doFillArchitectureItems() {
			final StandardListBoxModel retval = new StandardListBoxModel();
			retval.includeEmptyValue();
			
			for (ArchitectureType arch : ArchitectureType.values()) {
				retval.add(arch.getDisplayName());
			}
			return retval;
		}

		public ListBoxModel doFillBitsItems() {
			final StandardListBoxModel retval = new StandardListBoxModel();
			retval.includeEmptyValue();

			for (BitsType bits : BitsType.values()) {
				retval.add(bits.getDisplayName());
			}
			return retval;
		}

		public ListBoxModel doFillPlatformItems() {
			final StandardListBoxModel retval = new StandardListBoxModel();
			retval.includeEmptyValue();

			for (PlatformType plat : PlatformType.values()) {
				retval.add(plat.getDisplayName());
			}
			return retval;
		}

		@Override
		public String getDisplayName() {
			return "Create a CONS3RT Asset";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public Builder newInstance(StaplerRequest req, JSONObject formData) {
			if(req != null) {
				return req.bindJSON(clazz, formData);
			} else {
				return null;
			}
		}

	}

	private String name;
	private String description;
	private String version;
	private String vendor;

	private String platform;
	private String architecture;
	private String bits;
	private Integer cpu;
	private Integer memory;
	private Integer storage;

	private String documentationFilePath;
	private String installationScriptFilePath;
	private String licenseFilePath;
	private List<FileObject> mediaFilePaths;

	// Utility Fields:
	private boolean hasLicense = false;
	private boolean hasDocumentation = false;
	private String licenseFileName;
	private String documentationFileName;
	private String installScriptFileName;

	@DataBoundConstructor
	public AssetBuilder(String name, String description, String version, String vendor, String platform,
			String architecture, String bits, Integer cpu, Integer memory, Integer storage,
			String installationScriptFilePath, List<FileObject> mediaFilePaths, String licenseFilePath,
			String documentationFilePath) {
		super();
		this.setName(name);
		this.description = description;
		this.version = version;
		this.vendor = vendor;
		this.platform = platform;
		this.architecture = architecture;
		this.bits = bits;
		this.cpu = cpu;
		this.memory = memory;
		this.storage = storage;
		this.installationScriptFilePath = installationScriptFilePath;
		this.mediaFilePaths = mediaFilePaths;
		this.setLicenseFilePath(licenseFilePath);
		this.setDocumentationFilePath(documentationFilePath);
	}

	public String getArchitecture() {
		return architecture;
	}

	public String getBits() {
		return bits;
	}

	public Integer getCpu() {
		return cpu;
	}

	public String getDescription() {
		return description;
	}

	public String getDocumentationFilePath() {
		return documentationFilePath;
	}

	public String getInstallationScriptFilePath() {
		return installationScriptFilePath;
	}

	public String getLicenseFilePath() {
		return licenseFilePath;
	}

	public List<FileObject> getMediaFilePaths() {
		return mediaFilePaths;
	}

	public Integer getMemory() {
		return memory;
	}

	public String getName() {
		return name;
	}

	public String getPlatform() {
		return platform;
	}

	public Integer getStorage() {
		return storage;
	}

	public String getVendor() {
		return vendor;
	}

	public String getVersion() {
		return version;
	}

	public String getLicenseFileName() {
		return licenseFileName;
	}

	public String getDocumentationFileName() {
		return documentationFileName;
	}

	public String getInstallScriptFileName() {
		return installScriptFileName;
	}

	public void setLicenseFileName(String licenseFileName) {
		this.licenseFileName = licenseFileName;
	}

	public void setDocumentationFileName(String documentationFileName) {
		this.documentationFileName = documentationFileName;
	}

	public void setInstallScriptFileName(String installScriptFileName) {
		this.installScriptFileName = installScriptFileName;
	}

	public boolean hasLicense() {
		return hasLicense;
	}

	public boolean hasDocumentation() {
		return hasDocumentation;
	}

	public void setHasLicense(boolean hasLicense) {
		this.hasLicense = hasLicense;
	}

	public void setHasDocumentation(boolean hasDocumentation) {
		this.hasDocumentation = hasDocumentation;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

		final ContextLogger log = new ContextLogger(listener.getLogger(), "CONS3RT Asset Builder", Level.INFO);
		
		FilePath workDir = null;
		try {

			LOGGER.log(Level.INFO, "Received: " + printAsset());

			log.log("Beginning build...", Level.INFO);

			log.log("Checking for installation file....");

			FilePath installFile = null;
			if (this.getInstallationScriptFilePath() == null || this.getInstallationScriptFilePath().isEmpty()) {
				log.log("No Installation Script file was provided.", Level.SEVERE);
				// TODO: throw error
			} else {
				installFile = AssetFileUtils.verifyFileExists(build.getWorkspace(),
						this.getInstallationScriptFilePath());
				log.log("Found installation file " + this.getInstallationScriptFilePath());
				this.installScriptFileName = installFile.getName();
			}

			log.log("Checking for license file....");

			FilePath licenseFile = null;
			if (this.licenseFilePath == null || this.licenseFilePath.isEmpty()) {
				this.setHasLicense(false);
				log.log("No License file was provided.");
			} else {
				licenseFile = AssetFileUtils.verifyFileExists(build.getWorkspace(), this.getLicenseFilePath());

				log.log("Found license file " + this.getLicenseFilePath());

				this.licenseFileName = licenseFile.getName();
				this.setHasLicense(true);
			}

			log.log("Checking for documentation file....");

			FilePath documentationFile = null;
			if (this.documentationFilePath == null || this.documentationFilePath.isEmpty()) {
				this.setHasLicense(false);
				log.log("No Documentation file was provided.");
			} else {
				documentationFile = AssetFileUtils.verifyFileExists(build.getWorkspace(), this.documentationFilePath);

				log.log("Found documentation file " + this.getDocumentationFilePath());

				this.documentationFileName = documentationFile.getName();
				this.setHasDocumentation(true);
			}

			// Create working directory:
			log.log("Creating working directory...");
			workDir = AssetFileUtils.createWorkingDirectory(build.getWorkspace(), this.getName());
			log.log("Created working directory: " + workDir.getName());

			// Create asset.properties file at top level with known info:
			log.log("Creating asset properties file...");
			AssetFileUtils.createAssetPropertiesFile(workDir, this);
			log.log("Created asset properties file.");

			// Create scripts directory:
			log.log("Creating scripts directory...");
			final FilePath scriptsDirectory = new FilePath(workDir, "scripts");
			scriptsDirectory.mkdirs();

			log.log("Created scripts directory. Copying Install script...");

			// Copy install script into scripts directory:
			final FilePath installScriptFilePath = new FilePath(scriptsDirectory, this.installScriptFileName);
			installFile.copyTo(installScriptFilePath);

			log.log("Copied Install script into scripts directory.");

			// Copy Documentation to top level if provided:
			if (this.hasDocumentation) {
				log.log("Copying documentation file into working directory...");
				final FilePath documentationFilePath = new FilePath(workDir, this.documentationFileName);
				documentationFile.copyTo(documentationFilePath);
				log.log("Copied documentation file.");
			}

			// Copy License to top level if provided:
			if (this.hasLicense) {
				log.log("Copying license file into working directory...");
				final FilePath licenseFilePath = new FilePath(workDir, this.licenseFileName);
				licenseFile.copyTo(licenseFilePath);
				log.log("Copied license file.");
			}

			// Create media directory (if media files provided):
			if (this.mediaFilePaths != null && !this.mediaFilePaths.isEmpty()) {
				log.log("Media file(s) provided. Creating media directory...");
				final FilePath mediaDirectory = new FilePath(workDir, "media");
				mediaDirectory.mkdirs();

				log.log("Media directory created.");

				// Copy media file(s) into media directory, if provided:
				for (final FileObject mf : this.mediaFilePaths) {
					final FilePath workspace = build.getWorkspace();
					final String relativePath = mf.getPath();
					
					log.log("Received media file path: " + relativePath);
					
					//Detect if wildcard:
					if(relativePath.contains(AssetFileUtils.wildCard)) {
						
						LOGGER.info("Wild card found in path: " + relativePath);

						final Set<String> files = AssetFileUtils.findWildcardMatches(workspace, relativePath);
						
						for( final String file : files ) {
							AssetFileUtils.copyAssetFile(log, workspace, file, mediaDirectory);
						}
					} else {
						AssetFileUtils.copyAssetFile(log, workspace, relativePath, mediaDirectory);
					}
				}
			} else {
				log.log("No media files provided.");
			}

			// Asset now created, create asset zip:
			log.log("Asset contents copied, creating asset zip.");
			final File assetZip = AssetFileUtils.getAssetZipFromPath(build.getWorkspace(), workDir);
			log.log("Asset zip " + assetZip.getName() + " created.");
			
			return true;

		} catch (IOException | InterruptedException e) {
			log.log("Caught Exception: " + e.getClass().getSimpleName() + " with message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
			return false;
		} finally {
			// If the working directory was created, delete it.
			if(workDir != null) {
				try {
					log.log("Cleaning up working directory...");
					workDir.deleteRecursive();
					log.log("Cleaned.");
				} catch (IOException | InterruptedException e) {
					log.log("Attemping to clean up working directory, Caught Exception: " + e.getClass().getSimpleName() + " with message: " + e.getMessage(),Level.SEVERE);
				}
			}
		}
	}

	private String printAsset() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Name: " + this.name + ", ");
		sb.append("Platform: " + this.platform + ", ");
		sb.append("Installation Script: " + this.installationScriptFilePath + ", ");
		return sb.toString();
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public void setBits(String bits) {
		this.bits = bits;
	}

	public void setCpu(Integer cpu) {
		this.cpu = cpu;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDocumentationFilePath(String documentationFilePath) {
		this.documentationFilePath = documentationFilePath;
	}

	public void setInstallationScriptFilePath(String installationScriptFilePath) {
		this.installationScriptFilePath = installationScriptFilePath;
	}

	public void setLicenseFilePath(String licenseFilePath) {
		this.licenseFilePath = licenseFilePath;
	}

	public void setMediaFilePaths(List<FileObject> mediaFilePaths) {
		this.mediaFilePaths = mediaFilePaths;
	}

	public void setMemory(Integer memory) {
		this.memory = memory;
	}

	public void setName(String name) {
		BuildStepAssets.INSTANCE.removeName(this.name);
		LOGGER.info("Removed old Name: " + this.name + " Adding new name via setter: " + name);
		BuildStepAssets.INSTANCE.addName(name);
		this.name = name;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setStorage(Integer storage) {
		this.storage = storage;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
