package io.jenkins.plugins.datatype;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.Cons3rtPublisher;
import io.jenkins.plugins.Cons3rtSite;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import jenkins.model.Jenkins;

public class NetworkInterface extends AbstractDescribableImpl<NetworkInterface> {

	public static final Logger LOGGER = Logger.getLogger(NetworkInterface.class.getName());
	
	private String networkName;
	
	private String internalIpAddress;
	
	private boolean isPrimaryConnection;
	
	private static Set<Network> availableCloudspaceNetworks = new HashSet<>();
	
	@DataBoundConstructor
	public NetworkInterface(final String networkName, final String internalIpAddress, final boolean isPrimaryConnection) {
		super();
		this.networkName = networkName;
		this.isPrimaryConnection = isPrimaryConnection;
		
		// Null out empty value to avoid mapping empty
		if(internalIpAddress.isEmpty()) {
			this.internalIpAddress = null;
		} else {
			this.internalIpAddress = internalIpAddress;
		}
	}

	public String getNetworkName() {
		return networkName;
	}
	
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public static Set<Network> getAvailableCloudspaceNetworks() {
		return availableCloudspaceNetworks;
	}

	public String getInternalIpAddress() {
		return internalIpAddress;
	}

	public void setInternalIpAddress(String internalIpAddress) {
		this.internalIpAddress = internalIpAddress;
	}

	public boolean isPrimaryConnection() {
		return isPrimaryConnection;
	}

	public void setIsPrimaryConnection(boolean primaryNetwork) {
		this.isPrimaryConnection = primaryNetwork;
	}

	public static void setAvailableCloudspaceNetworks(Set<Network> availableCloudspaceNetworks) {
		NetworkInterface.availableCloudspaceNetworks = availableCloudspaceNetworks;
	}
	

	@Extension 
    public static class DescriptorImpl extends Descriptor<NetworkInterface> { 
        public String getDisplayName() { 
        	return "Network Interface"; 
        	} 
        
        public ListBoxModel doFillNetworkNameItems() {
    		
        	Jenkins.getInstance().checkPermission(Permission.UPDATE);
        	
        	ListBoxModel m = new ListBoxModel();
        	
    		for (Network network : Cons3rtPublisher.getAvailableNetworks()) {
    			m.add(network.getName());
    		}
    		
    		return m;
    	}
        
    } 
	
}
