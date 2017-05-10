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
- The FHIR server currently stores two resource names:
	- The name declared in the "name" element in the profile: "name": "GPConnect-Appointment-1"
	- The name at the end of the URL path: "url": "http://fhir-test.nhs.uk/StructureDefinition/gpconnect-appointment-1"
- In the above example the names are the same apart from case, and both have the major version in

- *Working assumption 1* : Major version can be retained in the filename, but NOT in the resource content
- *Working assumption 2* : Major and minor versions will be held in the server using FHIR versioning - patch versions will not
- *Working assumption 3* : Patch versions (for bug fixes, wording tweaks, etc) will over-write the relevant minor version (i.e. not retained in the FHIR server)
- *Working assumption 4* : Maturity of a profile (Draft, Active, Retired) will be held in the status element to differentiate between draft and live profiles
- *Working assumption 5* : The profile index pages should show each profile once, with decorators to indicate the latest active version, and latest draft version if newer

- To achieve this, we can alter the way the FHIR server loads profiles, so it can retain previous versions as required.

## Test steps

- Create a new profile "Adam-Patient-1" with version 0.1:
	- name="Adam-Patient"
	- version="0.1"
	- status="draft"
- Add the file into the FHIR server directory
	- Expected:
		- Server creates a "versioned" directory
		- Server reads the major and minor version from inside the profile itself
		- Server checks the version is in the form NN or NN.NN or NN.NN.NN - otherwise it gets rejected
		- Major, minor and patch versions parsed into integers (note: filename is ignored)
		- Server writes a copy of the file into this directory with "-versioned-0.1" (dropping patch version)
		- After this pre-process step, all versioned profiles are loaded into the FHIR server index from the versioned directory only
		- Rejected profiles (those that can't be loaded) should be reported somehow - perhaps on a "secret" URL
		- FHIR server profile index shows a single entry for "Adam-Patient-1" with a [Draft-0.1] decorator
		- FHIR server rendered entry for "Adam-Patient-1" includes a version history on the right, showing just [Draft-0.1]
		- Each version in the history list links to the versioned FHIR URL for that version
- Create a new profile "Adam-Patient-1" with version 1.0:
	- name="Adam-Patient"
	- version="1.0"
	- status="active"
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-1.0"
		- Versioned directory now contains two files - 0.1 and 1.0 (note: this is still a single file in Git)
		- FHIR server profile index shows a single entry for "Adam-Patient-1" with a [Live-1.0] decorator
		- FHIR server rendered entry for "Adam-Patient-1" includes a version history on the right, showing [Draft-0.1] and [Active-1.0]
		- Each version in the history list links to the versioned FHIR URL for that version
- Update the profile "Adam-Patient-1":
	- Change some values
	- Update version to "1.1"
	- status="draft"
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-1.1"
		- Versioned directory now contains three files - 0.1, 1.0 and 1.1 (note: this is still a single file in Git)
		- FHIR server profile index shows a single entry for "Adam-Patient-1" with two decorators: [Live-1.0] and [Draft-1.1]
		- FHIR server rendered entry for "Adam-Patient-1" includes a version history on the right, showing [Draft-0.1], [Active-1.0], [Active-1.1]
		- Each version in the history list links to the versioned FHIR URL for that version
- Update the profile "Adam-Patient-1":
	- Change some values
	- Update version to "1.1.1"
	- status="draft"
- Add the file into the FHIR server directory
	- Expected:
		- Server reads the major and minor version from inside the profile, parses, etc
		- Server writes a copy of the file into this directory with "-versioned-1.1" over-writing previous file with this name
		- Versioned directory still contains two files - 1.0 and 1.1 (1.1 file is now actually 1.1.1 patch version)
		- FHIR server rendered entry for "Adam-Patient-1" includes a version history on the right, showing [Draft-0.1], [Active-1.0], [Active-1.1.1]
		- Each version in the history list links to the versioned FHIR URL for that version
