package io.jenkins.plugins;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.datatype.Network;
import io.jenkins.plugins.datatype.RunConfiguration;
import io.jenkins.plugins.utils.AssetFileUtils;
import io.jenkins.plugins.utils.ContextLogger;
import io.jenkins.plugins.utils.HttpWrapper;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import io.jenkins.plugins.utils.HttpWrapper.HttpWrapperBuilder;
import io.jenkins.plugins.utils.NameUtils;
import net.sf.json.JSONObject;

public class Cons3rtPublisher extends Recorder {

	public static final Logger LOGGER = Logger.getLogger(Cons3rtPublisher.class.getName());

	private static final String createAssetAction = "createAsset";

	private static final String updateAssetAction = "updateAsset";
	
	private static final String prebuiltAssetType = "prebuilt";

	private static final String filepathAssetType = "filepath";
	
	private Cons3rtSite site;
	
	private Integer assetId;
	
	private String assetStyle;
	
	private String prebuiltAssetName;
	
	private String filepath;

	private String actionType;
	
	private boolean attemptUploadOnBuildFailure;
	
	private boolean deleteCreatedAssetAfterUpload;
	
	private RunConfiguration launchRequest;
	
	public static final Map<Integer, Set<Entry<String, Integer>>> availableCloudspaces = new HashMap<>();
	
	public static final Set<String> availableRoles = new HashSet<>();

	public static final Set<Network> availableNetworks = new HashSet<>();;

	@DataBoundConstructor
	public Cons3rtPublisher(final Cons3rtSite site, Integer assetId, String assetStyle, String filepath, final String prebuiltAssetName, final String actionType, final boolean attemptUploadOnBuildFailure,
			final boolean deleteCreatedAssetAfterUpload, final RunConfiguration launchRequest) {
		
		this.setSite(site);
		
		Cons3rtPublisher.LOGGER.log(Level.INFO, "Entering constructor with values: " + assetStyle + " " + actionType);
		
		this.setAssetStyle(assetStyle);
		
		if(this.assetStyle == null || this.assetStyle.isEmpty() ) {
			this.setAssetStyle(prebuiltAssetType);
		}
		
		switch (this.getAssetStyle()) {
		case Cons3rtPublisher.prebuiltAssetType:
			this.prebuiltAssetName = prebuiltAssetName;
			this.filepath = null;
			break;
		case Cons3rtPublisher.filepathAssetType:
			this.filepath = filepath;
			this.prebuiltAssetName = null;
			break;
		default:
			this.filepath = filepath;
			this.prebuiltAssetName = null;
			break;
		}
		
		this.actionType = actionType;
		
		if(this.actionType == null || this.actionType.isEmpty() ) {
			this.setActionType(createAssetAction);
		}
		
		switch (this.getActionType()) {
		case Cons3rtPublisher.createAssetAction:
			this.setAssetId(null);
			break;
		case Cons3rtPublisher.updateAssetAction:
			this.assetId = assetId;
			break;
		default:
			this.setAssetId(null);
			break;
		}
		
		this.attemptUploadOnBuildFailure = attemptUploadOnBuildFailure;
		this.deleteCreatedAssetAfterUpload = deleteCreatedAssetAfterUpload;
		this.launchRequest = launchRequest;
		
		if(this.launchRequest != null) {
			this.launchRequest.setCloudspaceId(Cons3rtPublisher.getCloudspaceIdForName(this.launchRequest.getCloudspaceName()));
		}
		
		LOGGER.log(Level.INFO, "Received Site: " + this.site.getUrl() + " with action type: " + this.actionType + " and asset id: " + this.assetId);
	}

	public static Integer getCloudspaceIdForName(final String cloudspaceName) {
		Integer retval = null;
		if(cloudspaceName != null) {
			for( final Entry<Integer, Set<Entry<String, Integer>>> entry : availableCloudspaces.entrySet()) {
				
				final Set<Entry<String, Integer>> cloudspaces = entry.getValue();
				
				for( final Entry<String, Integer> cloudspace : cloudspaces ) {
					if( cloudspace.getKey().equals(cloudspaceName)) {
						retval = cloudspace.getValue();
						break;
					}
				}
			}
		}
		return retval;
	}

	public String getActionType() {
		return this.actionType;
	}

	public boolean isDeleteCreatedAssetAfterUpload() {
		return deleteCreatedAssetAfterUpload;
	}

	public void setDeleteCreatedAssetAfterUpload(boolean deleteCreatedAssetAfterUpload) {
		this.deleteCreatedAssetAfterUpload = deleteCreatedAssetAfterUpload;
	}

	public void setActionType(final String actionType) {
		this.actionType = actionType;
	}

	public Integer getAssetId() {
		return this.assetId;
	}

	public String getFilepath() {
		return this.filepath;
	}

	public static Map<Integer, Set<Entry<String, Integer>>> getAvailableCloudspaces() {
		return Cons3rtPublisher.availableCloudspaces;
	}
	
	public RunConfiguration getLaunchRequest() {
		return launchRequest;
	}

	public String getPrebuiltAssetName() {
		return prebuiltAssetName;
	}

	public String getAssetStyle() {
		return assetStyle;
	}

	public Cons3rtSite getSite() {
		return site;
	}

	public void setSite(Cons3rtSite site) {
		this.site = site;
	}

	public void setAssetStyle(String assetStyle) {
		this.assetStyle = assetStyle;
	}

	public void setPrebuiltAssetName(String prebuiltAssetName) {
		this.prebuiltAssetName = prebuiltAssetName;
	}

	public void setLaunchRequested(RunConfiguration launchRequest) {
		this.launchRequest = launchRequest;
	}

	
	public static void addAvailableCloudspaces(final Integer key, final Set<Entry<String, Integer>> cloudspaces) {
		Cons3rtPublisher.availableCloudspaces.put(key, cloudspaces);
	}
	
	public static void setAvailableNetworks(Set<Network> availableNetworks) {
		Cons3rtPublisher.availableNetworks.clear();
		Cons3rtPublisher.availableNetworks.addAll(availableNetworks);
	}
	
	public static void setAvailableRoles(Set<String> availableRoles) {
		Cons3rtPublisher.availableRoles.clear();
		Cons3rtPublisher.availableRoles.addAll(availableRoles);
	}

	public boolean isAttemptUploadOnBuildFailure() {
		return attemptUploadOnBuildFailure;
	}

	public void setAttemptUploadOnBuildFailure(boolean attemptUploadOnBuildFailure) {
		this.attemptUploadOnBuildFailure = attemptUploadOnBuildFailure;
	}

	public void setAssetId(Integer assetId) {
		this.assetId = assetId;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}
	
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		
		final ContextLogger log = new ContextLogger(listener.getLogger(), "CONS3RT Plugin", Level.INFO);

		LOGGER.log(Level.INFO, "attemptUploadOnBuildFailure: " + this.attemptUploadOnBuildFailure);
		
		if (Result.ABORTED.equals(build.getResult())) {
            log.log("Skipping asset upload or update as build was aborted.", Level.SEVERE);
            return true;
        }
		
		if (this.site == null) {
			log.log("No CONS3RT site found. This is likely a configuration problem.", Level.SEVERE);
			build.setResult(Result.UNSTABLE);
			return true;
		}
		
		if(!this.attemptUploadOnBuildFailure && build.getResult() != null && build.getResult().equals(Result.FAILURE) ) {
			log.log("Skipping asset upload or update as build failed and user did not request to attempt upload on build failure.", Level.SEVERE);
            return false;
		} else if(this.attemptUploadOnBuildFailure && build.getResult() != null && build.getResult().equals(Result.FAILURE)){
			log.log("Attempting to upload or update despite build failure, as requested.");
		}
		
		final String baseUrl = this.site.getUrl();
		final String token = this.site.getToken();
		final String authenticationType = this.site.getAuthenticationType();

		try {
			log.log("Site Url: " + baseUrl + " authentication type: " + authenticationType + " action type: " + this.getActionType());

			final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(baseUrl, token, authenticationType);
			
			if(Cons3rtPublisher.isCeritificateAuthentication(authenticationType)) {
				builder.certificate(this.site.getCertificate());
			} else if (Cons3rtPublisher.isUsernameAuthentication(authenticationType)) {
				builder.username(this.site.getUsername());
			}
			
			final HttpWrapper wrapper = builder.build();
			
			final FilePath providedPath;
			switch (this.getAssetStyle()) {
			case Cons3rtPublisher.prebuiltAssetType:
				providedPath = AssetFileUtils.findPrebuiltAsset(build.getWorkspace(), this.prebuiltAssetName);
				break;
			case Cons3rtPublisher.filepathAssetType:
				providedPath = new FilePath(build.getWorkspace(), this.getFilepath());
				break;
			default:
				final String message = "Invalid asset type requested: " + this.getAssetStyle();
				log.log(message, Level.SEVERE);
				throw new IOException(message);
			}
			
			final File assetZipForUpload = AssetFileUtils.getAssetZipFromPath(build.getWorkspace(), providedPath);

			log.log("Using asset zip file: " + assetZipForUpload.getAbsolutePath());
			
			log.log("Received action type: " + this.getActionType());
			
			final String result;
			switch (this.getActionType()) {
			case Cons3rtPublisher.createAssetAction:
				result = wrapper.createAsset(assetZipForUpload);
				break;
			case Cons3rtPublisher.updateAssetAction:
				result = wrapper.updateAsset(this.assetId, assetZipForUpload);
				break;
			default:
				final String message = "Invalid action type requested: " + this.getActionType();
				log.log(message, Level.SEVERE);
				throw new IOException(message);
			}
			
			log.log(result);
			
			if(this.isRunRequested()) {
				log.log("Launch of deployment " + this.launchRequest.getDeploymentId() + " into cloudspace " + this.launchRequest.getCloudspaceName() + " id " + this.getLaunchRequest().getCloudspaceId() + " was requested.");
				final String deploymentRunId= wrapper.launchDeployment(this.launchRequest);
				log.log("Launch was successful. Deployment run id: " + deploymentRunId);
			} else {
				log.log("No deployment launch was requested.");
			}
			
			// Attempt to delete pre-built asset:
			if(prebuiltAssetType.equals(this.assetStyle) && this.deleteCreatedAssetAfterUpload) {
				log.log("Deletion of created asset was requested. Attempting to delete: " + assetZipForUpload.getName());
				final boolean deleted = assetZipForUpload.delete();
				
				if(!deleted) {
					log.log("Failed to delete: " + assetZipForUpload.getName());
				}
			}
			
		} catch (HTTPException | ParseException | SecurityException e) {
			log.log("Caught: " + e.getClass().getName() + " message: " + e.getMessage(), Level.SEVERE);
			build.setResult(Result.FAILURE);
			return false;
		} 

		return true;
	}
	
	public boolean isActionType(String given) {
		return this.actionType.equals(given);
	}
	
	public boolean isAssetStyle(String given) {
		return this.assetStyle.equals(given);
	}

	public String runRequested() {
		final String retval = ( this.getLaunchRequest() != null ) ? "true" : "false";
		Cons3rtPublisher.LOGGER.log(Level.INFO, "runRequested equals: " + retval);
		return retval;
	}
	
	public boolean isRunRequested() {
		return Boolean.valueOf(this.runRequested());
	}

	public static boolean isCeritificateAuthentication(final String authenticationType) {
		return authenticationType.equals(Cons3rtSite.certificateAuthentication);
	}

	public static boolean isUsernameAuthentication(final String authenticationType) {
		return authenticationType.equals(Cons3rtSite.usernameAuthentication);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public DescriptorImpl() {
			super(Cons3rtPublisher.class);
			load();
		}

		protected DescriptorImpl(Class<? extends Publisher> clazz) {
			super(clazz);
		}

		@Override
		public String getDisplayName() {
			return "Create or Update a CONS3RT Asset";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			
			final String output = formData.toString();
        	LOGGER.info(output);
			
			if(req != null) {
				final Cons3rtPublisher instance = (Cons3rtPublisher) req.bindJSON(clazz, formData);
				
				Cons3rtPublisher.LOGGER.log(Level.INFO, "Entering new instance with values: " + instance.assetStyle + " " + instance.actionType);
				Cons3rtPublisher.LOGGER.log(Level.INFO, "Entering new instance with values: " + instance.getSite().getUrl());

				return instance;
			} else {
				return null;
			}
		}

		public FormValidation doCheckUrl(@QueryParameter("url") String url) {
			if ((url == null) || (url.trim().isEmpty())) {
				return FormValidation.error("Url not specified!");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckCertificateId(@QueryParameter("certificateId") String certificateId) {
			if ((certificateId == null) || (certificateId.trim().isEmpty())) {
				return FormValidation.error("Certificate not specified!");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckDeploymentId(@QueryParameter("deploymentId") Integer deploymentId) {
			if ((deploymentId == null) || (deploymentId < 0)) {
				return FormValidation.error("An existing deployment id must be provided");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckDeploymentRunUsername(@QueryParameter("deploymentRunUsername") String deploymentRunUsername) {
			if ((deploymentRunUsername == null) || (deploymentRunUsername.trim().isEmpty())) {
				return FormValidation.error("A username must be provided");
			}
			
			try {
				NameUtils.checkCreatedUsernameRestrictions(deploymentRunUsername);
			} catch (io.jenkins.plugins.exceptions.InvalidNameException e) {
				return FormValidation.error(e.getMessage());
			}
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckDeploymentRunPassword(@QueryParameter("deploymentRunPassword") String deploymentRunPassword) {
			if ((deploymentRunPassword == null) || (deploymentRunPassword.trim().isEmpty())) {
				return FormValidation.error("A password must be provided");
			}
			return FormValidation.ok();
		}
		
		public FormValidation doCheckDeploymentRunName(@QueryParameter("deploymentRunName") String deploymentRunName) {
			try {
				NameUtils.checkDeploymentRunNameRestrictions(deploymentRunName);
			} catch (io.jenkins.plugins.exceptions.InvalidNameException e) {
				return FormValidation.error(e.getMessage());
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckTokenId(@QueryParameter("tokenId") String tokenId) {
			if ((tokenId == null) || (tokenId.trim().isEmpty())) {
				return FormValidation.error("Token not specified!");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckUsername(@QueryParameter("username") String username) {
			if ((username == null) || (username.trim().isEmpty())) {
				return FormValidation.error("Username not specified!");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckProperty(@QueryParameter final String value) {
			if ((value == null) || (value.trim().isEmpty())) {
				return FormValidation.error("A value must be provided");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckFilepath(@QueryParameter final String filepath) {
			if ((filepath == null) || (filepath.trim().isEmpty())) {
				return FormValidation.error("A filepath was not provided");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckAssetId(@QueryParameter("assetId") Integer assetId) {
			if ((assetId == null) || (assetId < 1)) {
				return FormValidation.error("An existing asset id must be provided");
			}
			return FormValidation.ok();
		}
		
		public ListBoxModel doFillPrebuiltAssetNameItems() {
			LOGGER.info("Entering load prebuild names");
			final StandardListBoxModel retval = new StandardListBoxModel();
			for (String name : BuildStepAssets.INSTANCE.getNames()) {
				LOGGER.info("Adding name: " + name);
				retval.add(name);
			}
			return retval;
		}
		
	}

}
