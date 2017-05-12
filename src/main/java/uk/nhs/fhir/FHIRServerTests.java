package uk.nhs.fhir;

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
import uk.nhs.fhir.util.PropertyReader;

public class FHIRServerTests {
	
	public static String profilePath = PropertyReader.getProperty("profilePath");
    public static String valueSetPath = PropertyReader.getProperty("valusetPath");
    public static String operationsPath = PropertyReader.getProperty("operationsPath");
    public static String guidesPath = PropertyReader.getProperty("guidesPath");
    public static String testDataPath = PropertyReader.getProperty("testDataPath");
	public static String fhirServerURL = PropertyReader.getProperty("fhirServerURL");
	
	protected FhirContext ctx = FhirContext.forDstu2();
	protected IGenericClient client;
	
	public static void copyTestFileIntoFHIRServer(String filename) throws IOException {
		
		String filenameOnly = filename;
		if (filenameOnly.contains("/")) {
			filenameOnly = filenameOnly.substring(filenameOnly.lastIndexOf('/'));
		}
		
		Path from = Paths.get(testDataPath + "/" + filename);
		Path to = Paths.get(profilePath + "/" + filenameOnly);
		CopyOption[] options = new CopyOption[]{
	      StandardCopyOption.REPLACE_EXISTING,
	    }; 
		Files.copy(from, to, options);
	}
	
	public static void removeAllProfilesFromServer() throws IOException {
		FileUtils.deleteDirectory(new File(profilePath));
		FileUtils.forceMkdir(new File(profilePath));
	}
	
	public static void removeAllValueSetsFromServer() throws IOException {
		FileUtils.deleteDirectory(new File(valueSetPath));
		FileUtils.forceMkdir(new File(valueSetPath));
	}
	
	public static void removeAllOperationDefinitionsFromServer() throws IOException {
		FileUtils.deleteDirectory(new File(operationsPath));
		FileUtils.forceMkdir(new File(operationsPath));
	}
	
	public static void removeAllGuidesFromServer() throws IOException {
		FileUtils.deleteDirectory(new File(guidesPath));
		FileUtils.forceMkdir(new File(guidesPath));
	}
}
