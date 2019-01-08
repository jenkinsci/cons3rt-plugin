package io.jenkins.plugins.datatype;

public enum BitsType {
    /** The BIT s32. */
    BITS32("32-bit"),

    /** The BIT s64. */
    BITS64("64-bit");

	private String displayName;
	
	BitsType(final String displayName) {
		this.displayName = displayName;
	}
	
	public static BitsType valueFromDisplayName(final String displayName) {
		boolean found = false;
		BitsType retval = null;
		for( BitsType platform : BitsType.values()) {
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
