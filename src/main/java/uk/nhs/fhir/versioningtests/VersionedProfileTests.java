package uk.nhs.fhir.versioningtests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;

public class VersionedProfileTests {
	
	public static final String PROFILE_DIRECTORY = "/opt/fhir/profiles/";
	public static final String TEST_DATA_DIRECTORY = "./src/main/resources/";
	public static final String FHIR_SERVER_URL = "http://localhost:8080";
	
	protected FhirContext ctx = FhirContext.forDstu2();
	protected IGenericClient client;
	
	public static void copyTestFileIntoFHIRServer(String filename) throws IOException {
		Path from = Paths.get(TEST_DATA_DIRECTORY + filename);
		Path to = Paths.get(PROFILE_DIRECTORY + filename);
		CopyOption[] options = new CopyOption[]{
	      StandardCopyOption.REPLACE_EXISTING,
	    }; 
		Files.copy(from, to, options);
	}
	
	public static void removeAllProfilesFromServer() throws IOException {
		FileUtils.deleteDirectory(new File(PROFILE_DIRECTORY));
		FileUtils.forceMkdir(new File(PROFILE_DIRECTORY));
	}
	
}
