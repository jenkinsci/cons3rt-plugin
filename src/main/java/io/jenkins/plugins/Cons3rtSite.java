package io.jenkins.plugins;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.datatype.Network;
import io.jenkins.plugins.utils.HttpWrapper;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import io.jenkins.plugins.utils.HttpWrapper.HttpWrapperBuilder;

public class Cons3rtSite extends AbstractDescribableImpl<Cons3rtSite> {

	public static final Logger LOGGER = Logger.getLogger(Cons3rtSite.class.getName());

	public static final List<DomainRequirement> NO_REQUIREMENTS = Collections.<DomainRequirement>emptyList();

	public static final String certificateAuthentication = "certificate";

	public static final String usernameAuthentication = "username";

	String url;

	String tokenId;
	String token;

	String authenticationType;

	String username;

	String certificateId;
	StandardCertificateCredentials certificate;

	@DataBoundConstructor
	public Cons3rtSite(final String url, final String tokenId, final String authenticationType,
			final String certificateId, final String username) {

		this.url = url;
		this.tokenId = tokenId;

		LOGGER.log(Level.INFO, "Set site url to: " + this.url + " and tokenId to: " + this.tokenId);

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
			LOGGER.log(Level.INFO, "Based on username authentication. Set username to: " + this.username
					+ " and purged certificate information.");
			break;
		case Cons3rtSite.certificateAuthentication:
			this.setUsername(null);
			this.certificateId = certificateId;
			LOGGER.log(Level.INFO, "Based on certificate authentication. Set certificateId to: " + this.certificateId
					+ " and purged username information.");

			final StandardCertificateCredentials certificateCredential = lookupCertificateCredentialsById(
					certificateId);
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
			LOGGER.log(Level.INFO, "Based on username authentication. Set username to: " + this.username
					+ " and purged certificate information.");
			break;
		}

		LOGGER.log(Level.INFO, "Site: authentication type " + authenticationType + " username " + username
				+ " certificate id " + certificateId);
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

	public void setAuthenticationType(final String authenticationType) {
		this.authenticationType = authenticationType;
	}

	public void setCertificate(final StandardCertificateCredentials certificate) {
		this.certificate = certificate;
	}

	public void setCertificateId(final String certificateId) {
		this.certificateId = certificateId;
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

	public boolean isAuthenticationType(String given) {
		return this.authenticationType.equals(given);
	}

	public String authenticationTypeEquals(String given) {
		final String retval = String
				.valueOf((this.authenticationType != null) && (this.authenticationType.equals(given)));
		Cons3rtPublisher.LOGGER.log(Level.INFO, "given: " + given + " authentication equals: " + retval);
		return retval;
	}

	public void testConnection(final Logger logger) throws HTTPException {

		final HttpWrapperBuilder builder = new HttpWrapper.HttpWrapperBuilder(this.url, this.token,
				this.authenticationType);

		if (Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
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

	public Set<Entry<String, Integer>> getAvailableCloudspaces(Logger logger, Integer deploymentId)
			throws HTTPException {
		LOGGER.info("Attempting to get available cloudspaces for deployment id: " + deploymentId + " in site with url: "
				+ this.getUrl());
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

	public Set<Network> getAvailableNetworks(Logger logger, Integer deploymentId, Integer cloudspaceId)
			throws HTTPException {
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

	public static Cons3rtSite fromStapler(@RelativePath("../site") @QueryParameter String url,
			@RelativePath("../site") @QueryParameter String tokenId,
			@RelativePath("../site") @QueryParameter String authenticationType,
			@RelativePath("../site") @QueryParameter String certificateId,
			@RelativePath("../site") @QueryParameter String username) {

		LOGGER.info("From Stapler got url: " + url + " and auth type: "
				+ ((certificateId != null) ? "certificate" : "username"));

		if (url != null && tokenId != null) {
			// Attempt to determine authenticationType as it appears it wont come across:
			if (certificateId != null && !certificateId.isEmpty()) {
				return new Cons3rtSite(url, tokenId, Cons3rtSite.certificateAuthentication, certificateId, username);
			} else if (username != null) {
				return new Cons3rtSite(url, tokenId, Cons3rtSite.usernameAuthentication, certificateId, username);
			} else {
				return null;
			}
		} else {
			return null;
		}

	}

	@Extension
	public static class DescriptorImpl extends Descriptor<Cons3rtSite> {

		public static final Logger LOGGER = Logger.getLogger(Cons3rtSite.class.getName());

		@Override
		public String getDisplayName() {
			return "CONS3RT Site";
		}

		public ListBoxModel doFillCertificateIdItems(@AncestorInPath Item owner) {

			return new StandardListBoxModel().includeMatchingAs(ACL.SYSTEM, owner, StandardCertificateCredentials.class,
					Cons3rtSite.NO_REQUIREMENTS, CredentialsMatchers.always());
		}

		public ListBoxModel doFillTokenIdItems(@AncestorInPath Item owner) {

			return new StandardListBoxModel().includeMatchingAs(ACL.SYSTEM, owner, StringCredentials.class,
					Cons3rtSite.NO_REQUIREMENTS, CredentialsMatchers.always());
		}

		public FormValidation doUsernameLoginCheck(@QueryParameter("url") String url,
				@QueryParameter("tokenId") String tokenId, @QueryParameter("username") String username) {

			LOGGER.log(Level.INFO, "Received url " + url + " tokenId " + tokenId);

			if (url == null || tokenId == null) {
				return FormValidation.warning("Please provide a url and token");
			}

			if (username == null) {
				return FormValidation.warning("Please provide username");
			}

			final Cons3rtSite site = new Cons3rtSite(url, tokenId, Cons3rtSite.usernameAuthentication, null, username);
			try {
				try {
					site.testConnection(LOGGER);
				} catch (HTTPException e) {
					LOGGER.log(Level.SEVERE, e.getMessage());
					throw new IOException("Connection Failed.");
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage());
				return FormValidation.error(e.getMessage());
			}

			return FormValidation.ok("Successful connection");
		}

		public FormValidation doCertificateLoginCheck(@QueryParameter("url") String url,
				@QueryParameter("tokenId") String tokenId, @QueryParameter("certificateId") String certificateId) {

			LOGGER.log(Level.INFO, "Received url " + url + " tokenId " + tokenId);

			if (url == null || tokenId == null) {
				return FormValidation.warning("Please provide a url and token");
			}

			if (certificateId == null) {
				return FormValidation.warning("Please provide certificate");
			}

			final Cons3rtSite site = new Cons3rtSite(url, tokenId, Cons3rtSite.certificateAuthentication, certificateId,
					null);
			try {
				try {
					site.testConnection(LOGGER);
				} catch (HTTPException e) {
					LOGGER.log(Level.SEVERE, e.getMessage());
					throw new IOException("Connection Failed.");
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage());
				return FormValidation.error(e.getMessage());
			}

			return FormValidation.ok("Successful connection");
		}

	}

}
