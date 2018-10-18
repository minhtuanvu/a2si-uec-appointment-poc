# a2si-uec-appointment-poc
This application is a FHIR Server that has been for a proof of concept to demonstrate Urgent and Emergency Care Appointment Booking. It has been built using the [Care Connect Reference Implementation]{https://nhsconnect.github.io/CareConnectAPI/build_ri_overview.html}


## Running the application
Running this application requires that docker and docker-compose are installed and this git repository is cloned to your local machine


To run, simply issue the following commands

```
Docker-compose pull
docker-compose up -d
```

Once it has started up, run the following to load some sample data:

```
docker cp ccri-dataload/src/main/resources/Examples/dataload.sql ccrisql:home/dataload.sql
docker exec -it ccrisql psql -U fhirjpa -d careconnect -f home/dataload.sql
```

A basic test is to issue a GET to the following URL which will retrieve the CapabilityStatement:
```
http://<installation-path-and-port>/fhir/STU3/Metadata
```

## Appointment Slot Retrieval
Appointment slots along with referenced resources can be retrieved by issuing the following request, using the current date for the start parameter:
```
http://<installation-path-and-port>/ccri-fhirserver/STU3/Slot?status=free&start=2018-10-17&_include=Slot:schedule&_include:recurse=Schedule:actor:Practitioner&_include:recurse=Schedule:actor:PractitionerRole&_include:recurse=Schedule:actor:Location&_include:recurse=Schedule:actor:HealthcareService&serviceidentifier=123456789011
```

Removing the serviceidentifier parameter will return all slots for the given date

## Appointment Booking
To book an appointment, a FHIR Appointment resource will need to be POSTed to the following URL
```
http://<installation-path-and-port>/fhir/STU3/Appointment
```

The following is an example Appointment Resource which can be used for posting:

```
{
  "resourceType": "Appointment",
  "meta": {
    "versionId": "1",
    "lastUpdated": "2018-10-17T08:51:40.071+00:00"
  },
  "status": "booked",
  "description": "Emergency appointment",
	"slot": [
    {
      "reference": "Slot/80001"
    }
  ],
  "participant": [
    {
      "actor": {
        "reference": "Patient/1168"
      },
      "status": "accepted"
    }
  ]
}
```

