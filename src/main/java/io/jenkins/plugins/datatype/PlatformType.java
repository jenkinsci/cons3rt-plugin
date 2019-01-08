package io.jenkins.plugins.datatype;

public enum PlatformType {
	 /** The linux. */
    LINUX("Linux"),

    /** The os x. */
    OS_X("OS X"),

    /** The solaris. */
    SOLARIS("Solaris"),

    /** The windows. */
    WINDOWS("Windows");

	private String displayName;
	
	PlatformType(final String displayName) {
		this.displayName = displayName;
	}
	
	public static PlatformType valueFromDisplayName(final String displayName) {
		boolean found = false;
		PlatformType retval = null;
		for( PlatformType platform : PlatformType.values()) {
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
