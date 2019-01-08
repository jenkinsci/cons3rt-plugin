import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.Secret;
import io.jenkins.plugins.datatype.HostOption;
import io.jenkins.plugins.datatype.RunConfiguration;
import io.jenkins.plugins.utils.HttpWrapper;
import io.jenkins.plugins.utils.HttpWrapper.HTTPException;

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

}
