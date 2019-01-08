package io.jenkins.plugins;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.model.Item;
import hudson.security.ACL;
import io.jenkins.plugins.datatype.Network;
import io.jenkins.plugins.utils.HttpWrapper;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import io.jenkins.plugins.utils.HttpWrapper.HttpWrapperBuilder;

public class Cons3rtSite {

	public static final Logger LOGGER = Logger.getLogger(Cons3rtSite.class.getName());

	public static final List<DomainRequirement> NO_REQUIREMENTS = Collections.<DomainRequirement>emptyList();

	public static final String certificateAuthentication = "certificate";

	public static final String usernameAuthentication = "username";

	String name;
	
	String url;

	String tokenId;
	String token;

	String authenticationType;
	
	String username;
	
	String certificateId;
	StandardCertificateCredentials certificate;
	
	boolean launchEnabled;

	@DataBoundConstructor
	public Cons3rtSite(final String name, final String url, final String tokenId, final String authenticationType,
			final String certificateId, final String username, final boolean launchEnabled) {
		
		this.name = name;
		
		this.url = url;
		this.tokenId = tokenId;
		
		this.launchEnabled = launchEnabled;

		LOGGER.log(Level.INFO, "Set name to: " + this.name + " site url to: " + this.url + " and tokenId to: " + this.tokenId);

		final StringCredentials tokenCredential = lookupTokenCredentialsById(tokenId);
		if (tokenCredential != null) {
			this.token = tokenCredential.getSecret().getPlainText();
			LOGGER.log(Level.INFO, "Set token to: " + this.token);
		} else {
			LOGGER.log(Level.WARNING, "Could not find token credential for id");
		}
		
		this.authenticationType = authenticationType;
		LOGGER.log(Level.INFO, "Received Authentication type of: " + this.authenticationType);
		
		switch (this.authenticationType) {
		case Cons3rtSite.usernameAuthentication:
			this.setCertificate(null);
			this.setCertificateId(null);
			this.username = username;
			LOGGER.log(Level.INFO, "Based on username authentication. Set username to: " + this.username + " and purged certificate information.");
			break;
		case Cons3rtSite.certificateAuthentication:
			this.setUsername(null);
			this.certificateId = certificateId;
			LOGGER.log(Level.INFO, "Based on certificate authentication. Set certificateId to: " + this.certificateId + " and purged username information.");
			
			final StandardCertificateCredentials certificateCredential = lookupCertificateCredentialsById(certificateId);
			if (certificateCredential != null) {
				this.certificate = certificateCredential;
				LOGGER.log(Level.INFO, "Set certificate to " + this.certificate.getDescription());
			} else {
				LOGGER.log(Level.WARNING, "Could not find certificate credential for id");
			}
			break;
		default:
			LOGGER.log(Level.INFO, "Default case.");
			this.setCertificate(null);
			this.setCertificateId(null);
			this.username = username;
			LOGGER.log(Level.INFO, "Based on username authentication. Set username to: " + this.username + " and purged certificate information.");
			break;	
		}
		
		LOGGER.log(Level.INFO, "Site: authentication type " + authenticationType + " username " + username + " certificate id " + certificateId);
	}

	public String getAuthenticationType() {
		return this.authenticationType;
	}

	public StandardCertificateCredentials getCertificate() {
		return this.certificate;
	}

	public String getCertificateId() {
		return this.certificateId;
	}
	
	public String getName() {
		return this.name;
	}

	public String getToken() {
		return this.token;
	}

	public String getTokenId() {
		return this.tokenId;
	}

	public String getUrl() {
		return this.url;
	}

	public String getUsername() {
		return this.username;
	}

	public String getSitename() {

		if(this.name != null) {
			return this.name;
		} else {
			final StringBuilder sb = new StringBuilder();
			if (this.username != null) {
				sb.append(this.username);
				sb.append('@');
			} else if(this.certificateId != null) {
				sb.append("certificate");
				sb.append('@');
			}
	
			sb.append(this.url);
			return sb.toString();
		}
	}

	public boolean isLaunchEnabled() {
		return launchEnabled;
	}

	public void setLaunchEnabled(boolean launchEnabled) {
		this.launchEnabled = launchEnabled;
	}

	public void setAuthenticationType(final String authenticationType) {
		this.authenticationType = authenticationType;
	}

	public void setCertificate(final StandardCertificateCredentials certificate) {
		this.certificate = certificate;
	}

	public void setCertificateId(final String certificateId) {
		this.certificateId = certificateId;
	}
	
	public void setName(final String name) {
		this.name = name;
	}

	public void setToken(final String token) {
		this.token = token;
	}

	public void setTokenId(final String tokenId) {
		this.tokenId = tokenId;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	private StringCredentials lookupTokenCredentialsById(final String tokenCredentialId) {
		final List<StringCredentials> all = CredentialsProvider.lookupCredentials(StringCredentials.class, (Item) null,
				ACL.SYSTEM, NO_REQUIREMENTS);

		return CredentialsMatchers.firstOrNull(all, CredentialsMatchers.withId(tokenCredentialId));
	}

	private StandardCertificateCredentials lookupCertificateCredentialsById(final String certificateCredentialId) {
		final List<StandardCertificateCredentials> all = CredentialsProvider
				.lookupCredentials(StandardCertificateCredentials.class, (Item) null, ACL.SYSTEM, NO_REQUIREMENTS);

		return CredentialsMatchers.firstOrNull(all, CredentialsMatchers.withId(certificateCredentialId));
	}

	public String authenticationTypeEquals(String given) {
		final String retval = String
				.valueOf((this.authenticationType != null) && (this.authenticationType.equals(given)));
		Cons3rtPublisher.LOGGER.log(Level.INFO, "given: " + given + " authentication equals: " + retval);
		return retval;
	}

	public void testConnection(final Logger logger) throws HTTPException {
		
		final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(this.url, this.token, this.authenticationType);
		
		if(Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
			builder.certificate(this.certificate);
		} else if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
			builder.username(this.username);
		}
		
		final HttpWrapper wrapper = builder.build();
		
		final String result = wrapper.validateCredentials();
		logger.log(Level.INFO, "Attempt to validate connection returned: " + result);
	}

	public Set<Entry<String, Integer>> getAvailableProjects(Logger logger) throws HTTPException {
		final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(this.url, this.token,
				this.authenticationType);

		if (Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
			builder.certificate(this.certificate);
		} else if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
			builder.username(this.username);
		}

		final HttpWrapper wrapper = builder.build();

		return wrapper.getProjects();
	}

	public Set<Entry<String, Integer>> getAvailableCloudspaces(Logger logger, Integer deploymentId) throws HTTPException {
		final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(this.url, this.token,
				this.authenticationType);

		if (Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
			builder.certificate(this.certificate);
		} else if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
			builder.username(this.username);
		}

		final HttpWrapper wrapper = builder.build();

		return wrapper.getCloudspaces(deploymentId);
	}

	public Set<String> getHostRoles(Logger logger, Integer deploymentId) throws HTTPException {
		final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(this.url, this.token,
				this.authenticationType);

		if (Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
			builder.certificate(this.certificate);
		} else if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
			builder.username(this.username);
		}

		final HttpWrapper wrapper = builder.build();

		return wrapper.getRoles(deploymentId);
	}

	public Set<Network> getAvailableNetworks(Logger logger, Integer deploymentId, Integer cloudspaceId) throws HTTPException {
		final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(this.url, this.token,
				this.authenticationType);

		if (Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
			builder.certificate(this.certificate);
		} else if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
			builder.username(this.username);
		}

		final HttpWrapper wrapper = builder.build();

		return wrapper.getNetworks(deploymentId, cloudspaceId);
	}

}
