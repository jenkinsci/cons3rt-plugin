package io.jenkins.plugins.datatype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.Cons3rtPublisher;
import io.jenkins.plugins.Cons3rtSite;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class RunConfiguration extends AbstractDescribableImpl<RunConfiguration> {
	
	public static final Logger LOGGER = Logger.getLogger(RunConfiguration.class.getName());
	
	private Integer cloudspaceId;
	
	private String cloudspaceName;
	
	private Integer deploymentId;
	
	private String deploymentRunName;
	
	private Secret password;
	
	private boolean releaseResources;
	
	private String createdUsername;
	
	private boolean locked;
	
	private boolean endExisting;
	
	private boolean retainOnError;
	
	private List<Property> properties;
	
	private List<HostOption> hostOptions;
	
	public RunConfiguration() {};
	
	@DataBoundConstructor
	public RunConfiguration(final Integer deploymentId, final String deploymentRunName, final String cloudspaceName, 
			final boolean releaseResources, final String createdUsername, final Secret password, final boolean locked,
			final boolean endExisting, final boolean retainOnError, List<Property> properties, List<HostOption> hostOptions) {
		this.deploymentId = deploymentId;
		this.deploymentRunName = deploymentRunName;
		this.cloudspaceName = cloudspaceName;
		this.releaseResources = releaseResources;
		this.createdUsername = createdUsername;
		this.password = password;
		this.locked = locked;
		this.endExisting = endExisting;
		this.retainOnError = retainOnError;
		this.properties = properties != null ? new ArrayList<Property>(properties) : Collections.<Property>emptyList();
		this.hostOptions = hostOptions != null ? new ArrayList<HostOption>(hostOptions) : null;
	}

	public Integer getCloudspaceId() {
		return cloudspaceId;
	}

	public String getCloudspaceName() {
		return cloudspaceName;
	}

	public Integer getDeploymentId() {
		return deploymentId;
	}

	public String getDeploymentRunName() {
		return deploymentRunName;
	}

	public Secret getPassword() {
		return password;
	}

	public String getCreatedUsername() {
		return createdUsername;
	}

	public boolean isLocked() {
		return locked;
	}

	public boolean isEndExisting() {
		return endExisting;
	}

	public boolean isRetainOnError() {
		return retainOnError;
	}
	
	public List<Property> getProperties() {
		return properties;
	}
	
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setEndExisting(boolean endExisting) {
		this.endExisting = endExisting;
	}

	public void setRetainOnError(boolean retainOnError) {
		this.retainOnError = retainOnError;
	}

	public boolean isReleaseResources() {
		return releaseResources;
	}

	public void setCloudspaceId(Integer cloudspaceId) {
		this.cloudspaceId = cloudspaceId;
	}

	public void setCloudspaceName(String cloudspaceName) {
		this.cloudspaceName = cloudspaceName;
	}

	public void setDeploymentId(Integer deploymentId) {
		this.deploymentId = deploymentId;
	}

	public void setDeploymentRunName(String deploymentRunName) {
		this.deploymentRunName = deploymentRunName;
	}

	public void setPassword(Secret password) {
		this.password = password;
	}

	public void setReleaseResources(boolean releaseResources) {
		this.releaseResources = releaseResources;
	}

	public void setCreatedUsername(String createdUsername) {
		this.createdUsername = createdUsername;
	}
	
	public List<HostOption> getHostOptions() {
		return hostOptions;
	}

	public void setHostOptions(List<HostOption> hostOptions) {
		this.hostOptions = hostOptions;
	}

	@Extension 
    public static class DescriptorImpl extends Descriptor<RunConfiguration> { 
        public String getDisplayName() {
        	return "Run Configuration";
        	} 
        
        public ListBoxModel doFillCloudspaceNameItems(
        		@QueryParameter("deploymentId") Integer deploymentId,
        		Cons3rtSite site) {
        	
    		ListBoxModel m = new ListBoxModel();
    		
    		LOGGER.info("Calling do fill cloudspaces with dep id: " + deploymentId + " and site value: " + ((site != null) ? "non-null" : "null"));
    		
    		if(Cons3rtPublisher.availableCloudspaces.containsKey(deploymentId)) {
    			
    			final Set<Entry<String, Integer>> cloudspaces = Cons3rtPublisher.availableCloudspaces.get(deploymentId);
    			
    			for (Entry<String, Integer> cloudspace : cloudspaces) {
        			m.add(cloudspace.getKey());
        		}
    			
    		} else if( site != null && deploymentId != null) {
				try {
					Cons3rtPublisher.addAvailableCloudspaces(deploymentId, site.getAvailableCloudspaces(LOGGER, deploymentId));
				
					for (Entry<String, Integer> cloudspace : Cons3rtPublisher.availableCloudspaces.get(deploymentId)) {
		    			m.add(cloudspace.getKey());
		    		}
				} catch (HTTPException e) {
					LOGGER.info("Caught error fetching cloudspaces. " + e.getMessage());
				}
    		}
				
    		return m;
    	}
        
        public FormValidation doGetCloudspaces(
        		@QueryParameter("deploymentId") Integer deploymentId,
        		@RelativePath("../../site") @QueryParameter String url,
				@RelativePath("../../site") @QueryParameter String tokenId,
				@RelativePath("../../site") @QueryParameter String authenticationType,
				@RelativePath("../../site") @QueryParameter String certificateId,
				@RelativePath("../../site") @QueryParameter String username) throws ServletException, IOException {

        	Jenkins.getInstance().checkPermission(Item.CONFIGURE);

			if (deploymentId != null) {
				// Attempt to determine authenticationType as it appears it wont come across:
				final Cons3rtSite site;
				if (certificateId != null && !certificateId.isEmpty()) {
					site = new Cons3rtSite(url, tokenId, Cons3rtSite.certificateAuthentication, certificateId,
							username);
				} else {
					site = new Cons3rtSite(url, tokenId, Cons3rtSite.usernameAuthentication, certificateId, username);
				}

				try {
					Cons3rtPublisher.addAvailableCloudspaces(deploymentId, site.getAvailableCloudspaces(LOGGER, deploymentId));
				} catch (HTTPException e) {
					Cons3rtPublisher.addAvailableCloudspaces(deploymentId, new HashSet<>());
					LOGGER.log(Level.SEVERE, e.getMessage());
					return FormValidation.error("Fetch of available cloud spaces for deployment id: " + deploymentId + " failed. " + e.getMessage());
				}

				return FormValidation.ok("Successfully Fetched Available Run Resources");
			} else {
				return FormValidation.error("A deployment id must be provided in order to fetch available cloudspaces for the deployment.");
			}

		}
        
        @Override
		public RunConfiguration newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			LOGGER.info("New run conf instance: " + formData.toString());
        	
        	return req.bindJSON(clazz, formData);
		}
        
		public FormValidation doGetResources(@QueryParameter("deploymentId") Integer deploymentId,
				@QueryParameter("cloudspaceName") String cloudspaceName,
				@RelativePath("../../site") @QueryParameter String url,
				@RelativePath("../../site") @QueryParameter String tokenId,
				@RelativePath("../../site") @QueryParameter String authenticationType,
				@RelativePath("../../site") @QueryParameter String certificateId,
				@RelativePath("../../site") @QueryParameter String username) throws IOException {

			Jenkins.getInstance().checkPermission(Item.CONFIGURE);

			if (deploymentId != null && cloudspaceName != null) {

				// Attempt to determine authenticationType as it appears it wont come across:
				final Cons3rtSite site;
				if (certificateId != null && !certificateId.isEmpty()) {
					site = new Cons3rtSite(url, tokenId, Cons3rtSite.certificateAuthentication, certificateId,
							username);
				} else {
					site = new Cons3rtSite(url, tokenId, Cons3rtSite.usernameAuthentication, certificateId, username);
				}

				try {
					Cons3rtPublisher.setAvailableRoles(site.getHostRoles(LOGGER, deploymentId));
					Cons3rtPublisher.setAvailableNetworks(site.getAvailableNetworks(LOGGER, deploymentId,
							Cons3rtPublisher.getCloudspaceIdForName(cloudspaceName)));
				} catch (HTTPException e) {
					LOGGER.log(Level.SEVERE, e.getMessage());
					throw new IOException("Fetch of available host roles failed.");
				}

				return FormValidation.ok("Successfully Fetched Available Run Resources");
			}

			return FormValidation.ok("Successfully Fetched Available Run Resources");
		}

	}
	
	
	
}
