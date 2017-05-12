package uk.nhs.fhir.versioningtests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.rest.client.IGenericClient;
import uk.nhs.fhir.FHIRServerTests;

public class VersionedProfileTest2 extends FHIRServerTests {
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		removeAllProfilesFromServer();
		client = ctx.newRestfulGenericClient(fhirServerURL);
	}
	
	@Test
	public void LoadProfileStep1() throws IOException, InterruptedException {
		copyTestFileIntoFHIRServer("VersioningProfiles/Step1-Adam-Patient-1.xml");
		copyTestFileIntoFHIRServer("VersioningProfiles/Step2-Adam-Patient-1.xml");
		Thread.sleep(10000); // Wait 10 seconds for the server to pick the files up
		
		// Now, use the HAPI FHIR client to retrieve the profile from the FHIR server so we can check it
		// Perform a search
		Bundle profiles = client
		      .search()
		      .forResource(StructureDefinition.class)
		      .where(StructureDefinition.NAME.matches().value("Adam-Patient"))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
		      .execute();
		 
		// Check we have exactly one profile
		assertEquals(1, profiles.getEntry().size());
		
		Entry entry = profiles.getEntry().get(0);
		StructureDefinition sd = (StructureDefinition)entry.getResource();
		
		// Check it is version 1.0
		assertEquals("1.0", sd.getVersion());
	}
	
}
