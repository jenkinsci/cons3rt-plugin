package io.jenkins.plugins.datatype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.Cons3rtPublisher;
import io.jenkins.plugins.Cons3rtSite;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import jenkins.model.Jenkins;

public class HostOption extends AbstractDescribableImpl<HostOption> {

	public static final Logger LOGGER = Logger.getLogger(HostOption.class.getName());
	
	private String systemRole;
	
	private Integer cpus;
	
	private Integer ram;
	
	private List<Disk> additionalDisks;
	
	private List<NetworkInterface> networkInterfaces;
	
	@DataBoundConstructor
	public HostOption(String systemRole, Integer cpus, Integer ram, List<Disk> additionalDisks, List<NetworkInterface> networkInterfaces) {
		super();
		this.systemRole = systemRole;
		this.cpus = cpus;
		this.ram = ram;
		this.additionalDisks = additionalDisks != null ? new ArrayList<Disk>(additionalDisks) : null;
		this.networkInterfaces = networkInterfaces != null ? new ArrayList<NetworkInterface>(networkInterfaces) : null;
	}

	public String getSystemRole() {
		return systemRole;
	}

	public Integer getCpus() {
		return cpus;
	}

	public Integer getRam() {
		return ram;
	}

	public List<Disk> getAdditionalDisks() {
		return additionalDisks;
	}

	public List<NetworkInterface> getNetworkInterfaces() {
		return networkInterfaces;
	}

	public void setSystemRole(String systemRole) {
		this.systemRole = systemRole;
	}

	public void setCpus(Integer cpus) {
		this.cpus = cpus;
	}

	public void setRam(Integer ram) {
		this.ram = ram;
	}

	public void setAdditionalDisks(List<Disk> additionalDisks) {
		this.additionalDisks = additionalDisks;
	}

	public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
		this.networkInterfaces = networkInterfaces;
	}

	@Extension 
    public static class DescriptorImpl extends Descriptor<HostOption> { 
        public String getDisplayName() { 
        	return "Host Option"; 
        	} 
        
		public ListBoxModel doFillSystemRoleItems() {
			
			ListBoxModel m = new ListBoxModel();
			
			for (String role : Cons3rtPublisher.availableRoles) {
				m.add(role);
			}

			return m;
		}
        
        public FormValidation doGetRoles(
    			@QueryParameter("deploymentId") Integer deploymentId,
    			@RelativePath("../../site") @QueryParameter String url,
				@RelativePath("../../site") @QueryParameter String tokenId,
				@RelativePath("../../site") @QueryParameter String authenticationType,
				@RelativePath("../../site") @QueryParameter String certificateId,
				@RelativePath("../../site") @QueryParameter String username) {

        	Jenkins.getInstance().checkPermission(Permission.UPDATE);
        	
        	// Attempt to determine authenticationType as it appears it wont come across:
        				final Cons3rtSite site;
        				if (certificateId != null && !certificateId.isEmpty()) {
        					site = new Cons3rtSite(url, tokenId, Cons3rtSite.certificateAuthentication, certificateId, username);
        				} else {
        					site = new Cons3rtSite(url, tokenId, Cons3rtSite.usernameAuthentication, certificateId, username);
        				}
        	
    		if (site == null || deploymentId == null) {
    			return FormValidation.warning("Please provide a site and deployment id");
    		}
    		
    		try {
    			try {
    				Cons3rtPublisher.setAvailableRoles(site.getHostRoles(LOGGER, deploymentId));
    			} catch (HTTPException e) {
    				LOGGER.log(Level.SEVERE, e.getMessage());
    				throw new IOException("Fetch of available host roles failed.");
    			}
    		} catch (IOException e) {
    			LOGGER.log(Level.SEVERE, e.getMessage());
    			return FormValidation.error(e.getMessage());
    		}

    		return FormValidation.ok("Successfully Fetched Available Roles For Selection");
    	}
        
        public FormValidation doGetNetworks(@QueryParameter("site") Cons3rtSite site,
    			@QueryParameter("deploymentId") Integer deploymentId, @QueryParameter("cloudspaceName") String cloudspaceName) {

        	Jenkins.getInstance().checkPermission(Permission.UPDATE);
        	
    		if (site == null || deploymentId == null) {
    			return FormValidation.warning("Please provide a site and deployment id");
    		}
    		
    		try {
    			try {
					final Integer cloudspaceId = Cons3rtPublisher.getCloudspaceIdForName(cloudspaceName);
					LOGGER.log(Level.INFO, "Got cloudspace id " + cloudspaceId);
    				Cons3rtPublisher.setAvailableNetworks(site.getAvailableNetworks(LOGGER, deploymentId, cloudspaceId));
    			} catch (HTTPException e) {
    				LOGGER.log(Level.SEVERE, e.getMessage());
    				throw new IOException("Fetch of available networks failed.");
    			}
    		} catch (IOException e) {
    			LOGGER.log(Level.SEVERE, e.getMessage());
    			return FormValidation.error(e.getMessage());
    		}

    		return FormValidation.ok("Successfully Fetched Available Networks For Selection");
    	}
    } 
	
}
