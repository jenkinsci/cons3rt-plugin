package io.jenkins.plugins.datatype;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class FileObject extends AbstractDescribableImpl<FileObject> {

	private String path;
	
	@DataBoundConstructor
	public FileObject(String path) {
		super();
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Extension 
    public static class DescriptorImpl extends Descriptor<FileObject> { 
        public String getDisplayName() { return "File Path"; } 
    } 
	
}
