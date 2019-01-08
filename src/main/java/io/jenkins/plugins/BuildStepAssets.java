package io.jenkins.plugins;

import java.util.HashSet;
import java.util.Set;

public enum BuildStepAssets {

    /** The instance *. */
    INSTANCE;

	private Set<String> names = new HashSet<>();
	
    /**
     * Private constructor to ensure singleton nature.
     */
    private BuildStepAssets() {}

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		if(names != null) {
			this.names.clear();
			this.names.addAll(names);
		} else {
			this.names.clear();
		}
	}
	
	public void addName(final String name) {
		this.names.add(name);
	}
	
	public void removeName(final String name) {
		this.names.remove(name);
	}
    
    
}