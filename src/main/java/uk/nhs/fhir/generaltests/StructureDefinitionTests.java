package uk.nhs.fhir.generaltests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import uk.nhs.fhir.FHIRServerTests;

public class StructureDefinitionTests extends FHIRServerTests {
	@Before
	public void setUp() throws IOException, InterruptedException {
		removeAllProfilesFromServer();
		client = ctx.newRestfulGenericClient(fhirServerURL);
	}
	
	@Test
	public void LoadAndRetrieveStructureDefinitions() throws IOException, InterruptedException {
		copyTestFileIntoFHIRServer("GeneralResources/StructureDefinitions/GPConnect-Appointment-1.xml");
		copyTestFileIntoFHIRServer("GeneralResources/StructureDefinitions/GPConnect-Patient-1.xml");
		copyTestFileIntoFHIRServer("GeneralResources/StructureDefinitions/GPConnect-Device-1.xml");
		copyTestFileIntoFHIRServer("GeneralResources/StructureDefinitions/CareConnect-Medication-1.xml");
		Thread.sleep(10000); // Wait 10 seconds for the server to pick the files up
		
		// Now, use the HAPI FHIR client to retrieve the profiles from the FHIR server so we can check them...
		
		// Perform a search
		Bundle profiles = client
		      .search()
		      .forResource(StructureDefinition.class)
		      .where(StructureDefinition.NAME.matches().value("GPConnect-Patient-1"))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
		      .execute();
		 
		// Check we have exactly one profile
		assertEquals(1, profiles.getEntry().size());
		
		Entry entry = profiles.getEntry().get(0);
		StructureDefinition sd = (StructureDefinition)entry.getResource();
		
		// Check it is right resource
		assertEquals("1.0", sd.getVersion());
		assertEquals("GPConnect-Patient-1", sd.getName());
		
		// Now, try a search for names containing "GPConnect"
		Bundle profileNameMatch = client
		      .search()
		      .forResource(StructureDefinition.class)
		      .where(StructureDefinition.NAME.matches().value("GPConnect"))
		      .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
		      .execute();
		 
		// Check we have three matches
		assertEquals(3, profileNameMatch.getEntry().size());
	}
}
