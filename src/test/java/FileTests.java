import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.tools.ant.DirectoryScanner;
import org.junit.Ignore;
import org.junit.Test;

public class FileTests {
	
	public static final Logger LOGGER = Logger.getLogger(FileTests.class.getName());

	@Ignore
	@Test
	public void test() {
		
		final Set<String> results = new HashSet<>();
		
		final String baseDir = "";
		final String relativePath = "test-media/*-dir/*";
		
		
		LOGGER.info("Searching with basedir: " + baseDir);
		LOGGER.info("Finding all files returned by search for: " + relativePath);
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[] { relativePath });
		scanner.setBasedir(baseDir);
		scanner.setCaseSensitive(false);
		scanner.scan();

		String[] dirs = scanner.getIncludedDirectories();
		String[] files = scanner.getIncludedFiles();
		
		for( final String dir : dirs ) {
			LOGGER.info("Search found directory: " + dir);
			results.add(dir);
		}
		
		for( final String file : files ) {
			LOGGER.info("Search found file: " + file);
			results.add(file);
		}
		
	}

}
