package io.jenkins.plugins.datatype;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Property extends AbstractDescribableImpl<Property> {

	private String key;
	
	private String value;

	@DataBoundConstructor
	public Property(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Extension 
    public static class DescriptorImpl extends Descriptor<Property> { 
        public String getDisplayName() { return "Property"; } 
    } 
	
}
