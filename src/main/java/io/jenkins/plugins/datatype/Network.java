package io.jenkins.plugins.datatype;

public class Network {

	private final String name;
	
	private final String id;
	
	private final String cidr;

	public Network(String name, String id, String cidr) {
		super();
		this.name = name;
		this.id = id;
		this.cidr = cidr;
	}

	public String getCidr() {
		return cidr;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
	
}
