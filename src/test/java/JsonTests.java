import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hudson.util.Secret;
import io.jenkins.plugins.datatype.HostOption;
import io.jenkins.plugins.datatype.RunConfiguration;
import io.jenkins.plugins.utils.HttpWrapper;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;
import io.jenkins.plugins.utils.SecretSerializer;

public class JsonTests {
	
	@Rule public JenkinsRule j = new JenkinsRule();
	
	@Ignore
	@Test
	public void printRunOptions() throws HTTPException {
		final RunConfiguration runConf = new RunConfiguration();
		runConf.setPassword(Secret.fromString("testpass"));
		runConf.setHostOptions(new ArrayList<HostOption>());
		
		final String json = HttpWrapper.createJsonFromLaunchRequest(runConf);
		
		System.out.println(json);
	}
	
	@Ignore
	@Test
	public void prettyPrint() {

String jsonString = "{\"site\":{\"url\":\"https://dev.cons3rt.io\",\"tokenId\":\"d82a6383-1bbc-49f7-b5c9-23529c03f4b1\",\"certificateId\":\"\",\"authenticationType\":\"username\",\"username\":\"jpaulo\"},\"actionType\":\"createAsset\",\"assetId\":\"\",\"attemptUploadOnBuildFailure\":false,\"prebuiltAssetName\":\"\",\"deleteCreatedAssetAfterUpload\":false,\"assetStyle\":\"filepath\",\"filepath\":\"test\",\"launchRequest\":{\"deploymentId\":\"1617\",\"deploymentRunName\":\"test\",\"cloudspaceName\":\"DEV-vCloud-1816\",\"username\":\"test\",\"password\":\"PuKo/8IdVMzskU0ZKTDX7G+wLe6CaY7ht7/C1KaOrds=\",\"releaseResources\":false,\"locked\":false,\"endExisting\":false,\"retainOnError\":false,\"hostOptions\":{\"systemRole\":\"\",\"cpus\":\"\",\"ram\":\"\",\"additionalDisks\":{\"capacityInMegabytes\":\"\"},\"networkInterfaces\":{\"networkName\":\"\",\"internalIpAddress\":\"\",\"isPrimaryConnection\":false}}},\"stapler-class\":\"io.jenkins.plugins.Cons3rtPublisher\",\"$class\":\"io.jenkins.plugins.Cons3rtPublisher\"}";
		JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(jsonString).getAsJsonObject();
		
		GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Secret.class, new SecretSerializer()) ;
        Gson gson = builder.create();
        
        System.out.println(gson.toJson(json));
	}

}
