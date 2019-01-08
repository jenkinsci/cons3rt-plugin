package io.jenkins.plugins.datatype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.Cons3rtPublisher;
import io.jenkins.plugins.Cons3rtSite;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import jenkins.model.Jenkins;

public class RunConfiguration extends AbstractDescribableImpl<RunConfiguration> {
	
	public static final Logger LOGGER = Logger.getLogger(RunConfiguration.class.getName());
	
	private Integer cloudspaceId;
	
	private String cloudspaceName;
	
	private Integer deploymentId;
	
	private String deploymentRunName;
	
	private Secret password;
	
	private boolean releaseResources;
	
	private String username;
	
	private boolean locked;
	
	private boolean endExisting;
	
	private boolean retainOnError;
	
	private List<Property> properties;
	
	private List<HostOption> hostOptions;
	
	public RunConfiguration() {};
	
	@DataBoundConstructor
	public RunConfiguration(final Integer deploymentId, final String deploymentRunName, final String cloudspaceName, 
			final boolean releaseResources, final String username, final Secret password, final boolean locked,
			final boolean endExisting, final boolean retainOnError, List<Property> properties, List<HostOption> hostOptions) {
		this.deploymentId = deploymentId;
		this.deploymentRunName = deploymentRunName;
		this.cloudspaceName = cloudspaceName;
		this.releaseResources = releaseResources;
		this.username = username;
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

	public String getUsername() {
		return username;
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

	public void setUsername(String username) {
		this.username = username;
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
        
        public ListBoxModel doFillCloudspaceNameItems(@QueryParameter("siteName") String siteName,
    			@QueryParameter("deploymentId") Integer deploymentId) {
    		
    		ListBoxModel m = new ListBoxModel();
    		if (siteName != null || deploymentId != null) {
    			final Cons3rtSite site = Cons3rtPublisher.DESCRIPTOR.getSite(siteName);
    			if (site != null) {
    				try {
    					Cons3rtPublisher.setAvailableCloudspaces(site.getAvailableCloudspaces(LOGGER, deploymentId));
    				} catch (HTTPException e) {
    					return m;
    				}
    			}
    		}
    		
    		for (Entry<String, Integer> cloudspace : Cons3rtPublisher.availableCloudspaces) {
    			m.add(cloudspace.getKey());
    		}
    		
    		return m;
    	}
        
        public FormValidation doGetCloudspaces(@QueryParameter("siteName") String siteName,
    			@QueryParameter("deploymentId") Integer deploymentId) {

        	Jenkins.getInstance().checkPermission(Permission.UPDATE);
        	
    		if (siteName == null || deploymentId == null) {
    			return FormValidation.warning("Please provide a site and deployment id");
    		}
    		
    		final Cons3rtSite site = Cons3rtPublisher.DESCRIPTOR.getSite(siteName);
    		if( site == null ) {
    			return FormValidation.warning("A site was not found. This is likely a configuration issue.");
    		}
    		
    		try {
    			try {
    				Cons3rtPublisher.setAvailableCloudspaces(site.getAvailableCloudspaces(LOGGER, deploymentId));
    			} catch (HTTPException e) {
    				LOGGER.log(Level.SEVERE, e.getMessage());
    				throw new IOException("Fetch of available cloudspaces failed.");
    			}
    		} catch (IOException e) {
    			LOGGER.log(Level.SEVERE, e.getMessage());
    			return FormValidation.error(e.getMessage());
    		}

    		return FormValidation.ok("Successfully Fetched Available Clouspaces For Selection");
    	}
    } 
	
	
}
