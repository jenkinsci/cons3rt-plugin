package io.jenkins.plugins.utils;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hudson.util.Secret;
import io.jenkins.plugins.Cons3rtPublisher;
import io.jenkins.plugins.datatype.DeploymentRunOptions;
import io.jenkins.plugins.datatype.Network;
import io.jenkins.plugins.datatype.RunConfiguration;

public class HttpWrapper {

	public static final Logger LOGGER = Logger.getLogger(HttpWrapper.class.getName());

	final String authenticationType;
	final String baseUrl;
	final String username;
	final String token;
	final StandardCertificateCredentials certificate;

	final SSLConnectionSocketFactory context;

	public static class HttpWrapperBuilder {
		private String baseUrl;
		private String token;
		private String authenticationType;
		private String username;
		private StandardCertificateCredentials certificate;

		public HttpWrapperBuilder(final String baseUrl, final String token, final String authenticationType) {
			this.baseUrl = baseUrl;
			this.token = token;
			this.authenticationType = authenticationType;
		}

		public HttpWrapperBuilder username(String username) {
			this.username = username;
			return this;
		}

		public HttpWrapperBuilder certificate(StandardCertificateCredentials certificate) {
			this.certificate = certificate;
			return this;
		}

		public HttpWrapper build() throws HTTPException {
			return new HttpWrapper(this.baseUrl, this.token, this.authenticationType, this.username, this.certificate);
		}
	}

	public HttpWrapper(final String url, final String token, final String authenticationType, final String username,
			final StandardCertificateCredentials certificate) throws HTTPException {
		this.baseUrl = url;
		this.token = token;
		this.authenticationType = authenticationType;
		this.username = username;
		this.certificate = certificate;

		this.context = buildConnectionContext();
	}

	public static class HTTPException extends Exception {

		private static final long serialVersionUID = 1L;

		public HTTPException(final String message) {
			super(message);
		}

		public HTTPException(final String message, final Exception e) {
			super(message, e);
		}
	}

	private SSLConnectionSocketFactory buildConnectionContext() throws HTTPException {
		try {
			final SSLContext sslContext;

			if (Cons3rtPublisher.isCeritificateAuthentication(this.authenticationType)) {
				sslContext = SSLContexts.custom()
						.loadKeyMaterial(this.certificate.getKeyStore(),
								this.certificate.getPassword().getPlainText().toCharArray())
						.loadTrustMaterial(null, new TrustStrategy() {
							@Override
							public boolean isTrusted(final X509Certificate[] chain, final String authType)
									throws CertificateException {
								return true;
							}
						}).build();
			} else if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
				sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
					@Override
					public boolean isTrusted(final X509Certificate[] chain, final String authType)
							throws CertificateException {
						return true;
					}
				}).build();
			} else {
				throw new HTTPException("Received unknown authentication type: " + this.authenticationType
						+ " could not construct wrapper.");
			}

			return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new HTTPException(e.getMessage());
		}

	}

	private void setHeaders(final HttpUriRequest request) {
		request.setHeader("token", this.token);

		if (Cons3rtPublisher.isUsernameAuthentication(this.authenticationType)) {
			request.setHeader("username", this.username);
		}
	}

	// HTTP GET
	private String get(final String url) throws HTTPException {

		try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(this.context).build()) {

			final HttpGet get = new HttpGet(url);
			setHeaders(get);

			final HttpResponse response = this.executeHttpRequest(client, get);
			return processResponse(response);
		} catch (IOException e) {
			throw new HTTPException(
					"get: caught " + e.getClass().getSimpleName() + " during http post with url parameters", e);
		}

	}

	// HTTP PUT for file upload
	private String putJson(final String url, final String body) throws HTTPException {

		try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(this.context).build()) {

			final HttpPut put = new HttpPut(url);
			this.setHeaders(put);

			put.setHeader("Content-Type", "application/json");
			
			if (body != null) {
                final StringEntity entity = new StringEntity(body);
                put.setEntity(entity);
            }

			final HttpResponse response = this.executeHttpRequest(client, put);
			return this.processResponse(response);
		} catch (IOException e) {
			throw new HTTPException(
					"post: caught " + e.getClass().getSimpleName() + " during http post with url parameters", e);
		}
	}

	// HTTP PUT for file upload
	private String putFile(final String url, File filePart) throws HTTPException {

		try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(this.context).build()) {

			final HttpPut put = new HttpPut(url);
			this.setHeaders(put);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			if (filePart != null && filePart.exists()) {
				builder.addTextBody("filename", filePart.getName());
				builder.addBinaryBody("file", filePart, ContentType.APPLICATION_OCTET_STREAM, filePart.getName());
				HttpEntity entity = builder.build();
				put.setEntity(entity);
			} else {
				throw new HTTPException("File to be uploaded was not found or does not exist");
			}

			final HttpResponse response = this.executeHttpRequest(client, put);
			return this.processResponse(response);
		} catch (IOException e) {
			throw new HTTPException(
					"post: caught " + e.getClass().getSimpleName() + " during http post with url parameters", e);
		}
	}

	// HTTP Post for file upload
	private String postFile(final String url, File filePart) throws HTTPException {

		try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(this.context).build()) {

			final HttpPost post = new HttpPost(url);
			this.setHeaders(post);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			if (filePart != null && filePart.exists()) {
				builder.addTextBody("filename", filePart.getName());
				builder.addBinaryBody("file", filePart, ContentType.APPLICATION_OCTET_STREAM, filePart.getName());
				HttpEntity entity = builder.build();
				post.setEntity(entity);
			} else {
				throw new HTTPException("File to be uploaded was not found or does not exist");
			}

			final HttpResponse response = this.executeHttpRequest(client, post);
			return this.processResponse(response);
		} catch (IOException e) {
			throw new HTTPException(
					"post: caught " + e.getClass().getSimpleName() + " during http post with url parameters", e);
		}

	}

	private HttpResponse executeHttpRequest(CloseableHttpClient client, HttpUriRequest request) throws HTTPException {
		try {
			final CloseableHttpResponse response = client.execute(request);

			final int status = response.getStatusLine().getStatusCode();

			if (status != 200 && status != 202) {
				LOGGER.log(Level.INFO, "Response code equaled : " + response.getStatusLine());
				String http = EntityUtils.toString(response.getEntity());
				LOGGER.log(Level.INFO, http);
				throw new HTTPException("Response Code returned did not equal 200 or 202. Response: " + http);
			} else {
				LOGGER.log(Level.INFO, "Response code equaled : " + response.getStatusLine());
				return response;
			}
		} catch (ClientProtocolException e) {
			final String message = "executeHttpRequest: caught " + e.getClass().getSimpleName()
					+ " while attempting to make request. Message: " + e.getMessage();
			LOGGER.log(Level.WARNING, message);
			throw new HTTPException(message);
		} catch (IOException e) {
			final String message = "executeHttpRequest: caught " + e.getClass().getSimpleName()
					+ " while attempting to make request. Message: " + e.getMessage();
			LOGGER.log(Level.WARNING, message);
			throw new HTTPException(message);
		}
	}

	private String processResponse(final HttpResponse response) throws HTTPException {
		try {
			String retval = null;
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				retval = EntityUtils.toString(responseEntity);
				LOGGER.log(Level.INFO, retval);
			}
			return retval;
		} catch (IOException | ParseException e) {
			final String message = "processResponse: caught " + e.getClass().getSimpleName()
					+ " while attempting to parse response. Message: " + e.getMessage();
			LOGGER.log(Level.WARNING, message);
			throw new HTTPException(message);
		}
	}

	@SuppressWarnings("unused")
	private JSONObject processJSONObjectFromResponse(final HttpResponse response) throws Exception {
		try {
			HttpEntity responseEntity = response.getEntity();
			JSONObject retval = new JSONObject();
			if (responseEntity != null) {
				final String returnString = EntityUtils.toString(responseEntity);
				LOGGER.log(Level.INFO, returnString);
				if (returnString != null && !returnString.isEmpty()) {
					retval = new JSONObject(returnString);
				}
			}
			return retval;
		} catch (Exception e) {
			final String message = "processResponse: caught " + e.getClass().getSimpleName()
					+ " while attempting to parse response. Message: " + e.getMessage();
			LOGGER.log(Level.WARNING, message);
			throw new Exception(message);
		}
	}

	public String validateCredentials() throws HTTPException {
		final String url = this.baseUrl + "/rest/api/validatecredentials";
		return this.get(url);
	}

	public String createAsset(final File file) throws HTTPException {
		final String url = this.baseUrl + "/rest/api/import/";
		return this.postFile(url, file);
	}

	public String updateAsset(final Integer assetId, final File file) throws HTTPException {
		final String url = this.baseUrl + "/rest/api/software/" + assetId + "/updatecontent";
		return this.putFile(url, file);
	}

	public Set<Entry<String, Integer>> getProjects() throws HTTPException {

		final Set<Entry<String, Integer>> retval = new HashSet<>();

		final String url = this.baseUrl + "/rest/api/projects";
		final String result = this.get(url);
		if (result != null && !result.isEmpty()) {
			final JSONArray projects = new JSONArray(result);

			for (int i = 0; i < projects.length(); ++i) {
				JSONObject rec = projects.getJSONObject(i);
				Integer id = rec.getInt("id");
				String name = rec.getString("name");

				final Entry<String, Integer> entry = new AbstractMap.SimpleEntry<String, Integer>(name, id);
				retval.add(entry);
			}
		}

		return retval;
	}

	public Set<Entry<String, Integer>> getCloudspaces(final Integer deploymentId) throws HTTPException {

		final Set<Entry<String, Integer>> retval = new HashSet<>();

		final String url = this.baseUrl + "/rest/api/deployments/" + deploymentId + "/validrealms/";
		final String result = this.get(url);
		if (result != null && !result.isEmpty()) {
			final JSONArray cloudspaces = new JSONArray(result);

			for (int i = 0; i < cloudspaces.length(); ++i) {
				JSONObject rec = cloudspaces.getJSONObject(i);
				Integer id = rec.getInt("id");
				String name = rec.getString("name");

				final Entry<String, Integer> entry = new AbstractMap.SimpleEntry<String, Integer>(name, id);
				retval.add(entry);
			}
		}

		return retval;
	}
	
	public Set<String> getRoles(Integer deploymentId) throws HTTPException {
		final Set<String> retval = new HashSet<>();

		final String url = this.baseUrl + "/rest/api/deployments/" + deploymentId;
		final String result = this.get(url);
		
		HttpWrapper.LOGGER.log(Level.INFO, result);
		
		if (result != null && !result.isEmpty()) {
			
			final JSONObject deployment = new JSONObject(result);
			final JSONArray deploymentHosts = deployment.getJSONArray("deploymentHosts");
			
			for (int i = 0; i < deploymentHosts.length(); ++i) {
				JSONObject deploymentHost = deploymentHosts.getJSONObject(i);
				String role = deploymentHost.getString("systemRole");
				HttpWrapper.LOGGER.log(Level.INFO, "Adding role: " + role);
				retval.add(role);
			}
		}

		return retval;
	}
	
	public Set<Network> getNetworks(Integer deploymentId, Integer cloudspaceId) throws HTTPException {
		
		final Set<Network> retval = new HashSet<>();

		final String url = this.baseUrl + "/rest/api/deployments/" + deploymentId + "/networks/" + cloudspaceId;
		final String result = this.get(url);
		
		HttpWrapper.LOGGER.log(Level.INFO, result);
		
		if (result != null && !result.isEmpty()) {
			
			final JSONArray networks = new JSONArray(result);
			
			for (int i = 0; i < networks.length(); ++i) {
				
				JSONObject network = networks.getJSONObject(i);
				String id = network.getString("identifier");
				String name = network.getString("name");
				String cidr = network.getString("cidr");
				
				String networkFunction = network.getString("networkFunction");
				if(!"CONS3RT".equals(networkFunction)) {
					final Network net = new Network(name, id, cidr);
					retval.add(net);
				}
			}
		}

		return retval;
	}

	public String launchDeployment(RunConfiguration launchRequest) throws HTTPException {
		final String url = this.baseUrl + "/rest/api/deployments/" + launchRequest.getDeploymentId() + "/execute";
		
		final String json = HttpWrapper.createJsonFromLaunchRequest(launchRequest);
		HttpWrapper.LOGGER.log(Level.INFO, json);
		return this.putJson(url, json);
	}

	public static String createJsonFromLaunchRequest(final RunConfiguration launchRequest) {
		final DeploymentRunOptions options = new DeploymentRunOptions(launchRequest);
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Secret.class, new SecretSerializer()) ;
        Gson gson = builder.create();
        return gson.toJson(options);
	}

}
