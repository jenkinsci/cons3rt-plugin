package io.jenkins.plugins.datatype;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class Disk extends AbstractDescribableImpl<Disk> {

	private Integer capacityInMegabytes;
	
	@DataBoundConstructor
	public Disk(Integer capacityInMegabytes) {
		super();
		this.capacityInMegabytes = capacityInMegabytes;
	}

	public Integer getCapacityInMegabytes() {
		return capacityInMegabytes;
	}

	public void setCapacityInMegabytes(Integer capacityInMegabytes) {
		this.capacityInMegabytes = capacityInMegabytes;
	}

	@Extension 
    public static class DescriptorImpl extends Descriptor<Disk> { 
        public String getDisplayName() { return "Disk"; } 
    } 
	
}
