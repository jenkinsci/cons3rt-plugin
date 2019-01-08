package io.jenkins.plugins.datatype;

public enum ArchitectureType {
	 /** The X86. */
    X86("x86"),
    /** The X64. */
    X64("x64"),
    /** The arm. */
    ARM("ARM"),
    /** The sparc. */
    SPARC("SPARC"),
    /** The ppcle. */
    PPCLE("POWER PC Little Endian"), ;

	private String displayName;
	
	ArchitectureType(final String displayName) {
		this.displayName = displayName;
	}
	
	public static ArchitectureType valueFromDisplayName(final String displayName) {
		boolean found = false;
		ArchitectureType retval = null;
		for( ArchitectureType platform : ArchitectureType.values()) {
			if(displayName.equals(platform.getDisplayName())) {
				retval = platform;
				found = true;
				break;
			}
		}
		
		if(found) {
			return retval;
		} else {
			return null;
		}
	}

	public String getDisplayName() {
		return displayName;
	}

}
