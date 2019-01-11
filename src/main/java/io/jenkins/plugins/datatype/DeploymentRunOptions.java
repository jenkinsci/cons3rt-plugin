package io.jenkins.plugins.datatype;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hudson.util.Secret;

public class DeploymentRunOptions {
	
	private String deploymentRunName;
	
	// TESTS_EXECUTED_RESOURCES_RELEASED | TESTS_EXECUTED_RESOURCES_RESERVED (toggle from a boolean)
	private String endState;
	
	private Secret password;
	
	private String username;
	
	private boolean locked;
	
	private boolean endExisting;
	
	private boolean retainOnError;
	
	private Integer virtualizationRealmId;
	
	private List<Property> properties;
	
	private Set<HostOption> hostOptions;
	
	public DeploymentRunOptions(final RunConfiguration request) {
		this.deploymentRunName = request.getDeploymentRunName();
		this.virtualizationRealmId = request.getCloudspaceId();
		this.endState = (request.isReleaseResources()) ? "TESTS_EXECUTED_RESOURCES_RELEASED" : "TESTS_EXECUTED_RESOURCES_RESERVED";
		this.username = request.getCreatedUsername();
		this.password = request.getPassword();
		this.locked = request.isLocked();
		this.endExisting = request.isEndExisting();
		this.retainOnError = request.isRetainOnError();
		this.properties = request.getProperties();
		
		if( request.getHostOptions() != null ) {
			this.setHostOptions(new HashSet<>(request.getHostOptions()));
		} else {
			this.setHostOptions(new HashSet<HostOption>());
		}
	}

	public String getDeploymentRunName() {
		return deploymentRunName;
	}

	public String getEndState() {
		return endState;
	}

	public Secret getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public Integer getVirtualizationRealmId() {
		return virtualizationRealmId;
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

	public Set<HostOption> getHostOptions() {
		return hostOptions;
	}

	public void setHostOptions(Set<HostOption> hostOptions) {
		this.hostOptions = hostOptions;
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

	public void setDeploymentRunName(String deploymentRunName) {
		this.deploymentRunName = deploymentRunName;
	}

	public void setEndState(String endState) {
		this.endState = endState;
	}

	public void setPassword(Secret password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setVirtualizationRealmId(Integer virtualizationRealmId) {
		this.virtualizationRealmId = virtualizationRealmId;
	}
	
}
