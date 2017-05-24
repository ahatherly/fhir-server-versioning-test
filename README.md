# Introduction

This is an exploratory test of using FHIR versioning in the NHS Digital FHIR reference server (https://fhir.nhs.uk), including:

- How we manage changes to profiles in Github
- How we apply versions to profile resources
- How these are held and represented in the FHIR server
- How implementations reference the relevant versioned profiles
- How these are displayed in the FHIR server itself

Changes to the FHIR reference server as part of this investigation will be checked into a feature branch for now - feature/versioned-profiles

# Investigation

- Major versions are currently part of the profile name itself.
- The FHIR server currently stores:
	- A "name" - declared in the "name" element in the profile: "name": "GPConnect-Appointment-1"
	- An "ID" - this is only declared at the end of the URL path: "url": "http://fhir-test.nhs.uk/StructureDefinition/gpconnect-appointment-1"
- In the above example name and ID are the same apart from case, and both have the major version number in them

- **Working assumption 1** : Major version can be retained in the filename, but NOT in the resource content
- **Working assumption 2** : Major and minor versions will be held in the server using FHIR versioning - patch versions will not
- **Working assumption 3** : Patch versions (for bug fixes, wording tweaks, etc) will over-write the relevant minor version (i.e. not retained in the FHIR server)
- **Working assumption 4** : Maturity of a profile (Draft, Active, Retired) will be held in the status element to differentiate between draft and live profiles
- **Working assumption 5** : The profile index pages should show each profile once, with decorators to indicate the latest active version, and latest draft version if newer

- To achieve this, we can alter the way the FHIR server loads profiles, so it can retain previous versions as required.

## Test steps

### STEP 1

**Create a new profile "Adam-Patient-1" with version 0.1**

- Test data:
	- name="Adam-Patient"
	- version="0.1"
	- status="draft"
	- url in the profile for all tests is https://fhir.nhs.uk/StructureDefinition/adam-patient
	- [Link to test profile](src/main/resources/VersioningProfiles/Step1-Adam-Patient-1.xml)
- Add the file into the FHIR server directory
	- Expected:
		- Server creates a "versioned" directory **DONE**
		- Server reads the major and minor version from inside the profile itself **DONE**
		- Server checks the version is in the form NN or NN.NN or NN.NN.NN - otherwise it gets rejected **DONE**
		- Major, minor and patch versions parsed into integers (note: filename is ignored) **DONE**
		- The ID of the resource is taken from the URL (adam-patient) and NOT the name **DONE**
		- Server writes a copy of the file into this directory in the format [id]-versioned-[version] - i.e. (adam-patient-versioned-0.1) (dropping patch version) **DONE**
		- After this pre-process step, all versioned profiles are loaded into the FHIR server index from the versioned directory only **DONE**
		- Rejected profiles (those that can't be loaded) should be reported somehow - perhaps on a "secret" URL **DONE**
		- FHIR server profile index shows a single entry for "Adam-Patient" with a [Draft-0.1] decorator
		- FHIR bundle for [baseurl]/StructureDefinition?name=Adam-Patient bundle returned contains a single entry for "Adam-Patient" with version 0.1 in it
		- FHIR bundle for [baseurl]/StructureDefinition/adam-patient/_history bundle returned contains a single entry for "Adam-Patient" with version 0.1 in it
		- FHIR versioned read on [baseurl]/StructureDefinition/adam-patient/_history/0.1 returns the profile with version 0.1 **DONE**
		- FHIR server rendered entry for "Adam-Patient" includes a version history on the right, showing just [Draft-0.1] **DONE**
		- Each version in the history list links to the versioned FHIR URL for that version **DONE**

- Changes required to FHIR server to achieve this:
	- TBC


### STEP 2

**Create a new profile "Adam-Patient-1" with version 1.0**

- Test data:
	- name="Adam-Patient"
	- version="1.0"
	- status="active"
	- [Link to test profile](src/main/resources/VersioningProfiles/Step2-Adam-Patient-1.xml)
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-1.0"
		- Versioned directory now contains two files - 0.1 and 1.0 (note: this is still a single file in Git)
		- FHIR server profile index shows a single entry for "Adam-Patient" with a [Live-1.0] decorator
		- FHIR server rendered entry for "Adam-Patient" includes a version history on the right, showing [Draft-0.1] and [Active-1.0]
		- Each version in the history list links to the versioned FHIR URL for that version
### STEP 3

**Update the profile "Adam-Patient-1"**

- Test data:
	- Change some values
	- Update version to "1.1"
	- status="draft"
	- [Link to test profile](src/main/resources/VersioningProfiles/Step3-Adam-Patient-1.xml)
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-1.1"
		- Versioned directory now contains three files - 0.1, 1.0 and 1.1 (note: this is still a single file in Git)
		- FHIR server profile index shows a single entry for "Adam-Patient" with two decorators: [Live-1.0] and [Draft-1.1]
		- FHIR server rendered entry for "Adam-Patient" includes a version history on the right, showing [Draft-0.1], [Active-1.0], [Active-1.1]
		- Each version in the history list links to the versioned FHIR URL for that version

### STEP 4

**Update the profile "Adam-Patient-1"**

- Test data:
	- Change some values
	- Update version to "1.1.1"
	- status="draft"
	- [Link to test profile](src/main/resources/VersioningProfiles/Step4-Adam-Patient-1.xml)
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-1.1" over-writing previous file with this name
		- If both 1.1 and 1.1.1 files are in the incoming directory from Github for some reason, these will need to be processed in ascending version order to avoid the older version overwriting the newer version
		- Versioned directory still contains three files - 0.1, 1.0 and 1.1 (1.1 file is now actually 1.1.1 patch version)
		- FHIR server profile index still shows a single entry for "Adam-Patient" with two decorators: [Live-1.0] and [Draft-1.1.1]
		- FHIR server rendered entry for "Adam-Patient" includes a version history on the right, showing [Draft-0.1], [Active-1.0], [Active-1.1.1]
		- Each version in the history list links to the versioned FHIR URL for that version
		- NOTE: URL for version 1.1.1 ends with 1.1 as the patch version has been discarded, so the versioned URL never changes between patch versions
### STEP 5

**Create a new profile file "Adam-Patient-2"**

- Test data:
	- Change some values
	- Update version to "2.0"
	- status="active"
	- [Link to test profile](src/main/resources/VersioningProfiles/Step5-Adam-Patient-1.xml)
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-2.0"
		- Versioned directory now contains four files - 0.1, 1.0, 1.1 and 2.0
		- FHIR server profile index still shows a single entry for "Adam-Patient" with one decorators: [Live-2.0]
		- FHIR server rendered entry for "Adam-Patient" includes a version history on the right, showing [Draft-0.1], [Active-1.0], [Active-1.1.1], [Active-2.0]
		- Each version in the history list links to the versioned FHIR URL for that version


