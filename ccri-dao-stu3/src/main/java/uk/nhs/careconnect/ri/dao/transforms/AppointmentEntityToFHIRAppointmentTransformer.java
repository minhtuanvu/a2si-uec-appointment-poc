package uk.nhs.careconnect.ri.dao.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentEntity;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentIdentifier;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentParticipant;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentReason;


@Component
public class AppointmentEntityToFHIRAppointmentTransformer implements Transformer<AppointmentEntity, Appointment> {

    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppointmentEntityToFHIRAppointmentTransformer.class);
    

    @Override
    public Appointment transform(final AppointmentEntity appointmentEntity) {
        final Appointment appointment = new Appointment();

        Meta meta = new Meta(); //.addProfile(CareConnectProfile.Location_1);

        if (appointmentEntity.getUpdated() != null) {
            meta.setLastUpdated(appointmentEntity.getUpdated());
        }
        else {
            if (appointmentEntity.getCreated() != null) {
                meta.setLastUpdated(appointmentEntity.getCreated());
            }
        }
        appointment.setMeta(meta);

        if(appointmentEntity.getId() != null){
            appointment.setId(appointmentEntity.getId().toString());
        }

        for(AppointmentIdentifier identifier : appointmentEntity.getIdentifiers())
        {
            appointment.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        if (appointmentEntity.getStatus() != null) {
            appointment.setStatus(appointmentEntity.getStatus());
        }

        if (appointmentEntity.getApointmentType() != null) {
            appointment.getAppointmentType()
                    .addCoding()
                    .setDisplay(appointmentEntity.getApointmentType().getDisplay())
                    .setSystem(appointmentEntity.getApointmentType().getSystem())
                    .setCode(appointmentEntity.getApointmentType().getCode());
        }

        for (AppointmentReason appointmentReasonEntity : appointmentEntity.getReasons()) {
            CodeableConcept concept = appointment.addReason();
            concept.addCoding()
                    .setSystem(appointmentReasonEntity.getReason().getSystem())
                    .setCode(appointmentReasonEntity.getReason().getCode())
                    .setDisplay(appointmentReasonEntity.getReason().getDisplay());
        }

        if (appointmentEntity.getPriority() != 0) {
            appointment.setPriority(appointment.getPriority());
        }

        if (appointmentEntity.getDescription() != null) {
            appointment.setDescription(appointmentEntity.getDescription());
        }

        if (appointmentEntity.getStart() != null) {
            appointment.setStart(appointmentEntity.getStart());
        }

        if (appointmentEntity.getEnd() != null) {
            appointment.setEnd(appointmentEntity.getEnd());
        }

        if (appointmentEntity.getSlot() != null) {
            appointment.addSlot(new Reference("Slot/"+ appointmentEntity.getSlot().getId()));
        }

        if (appointmentEntity.getCreated() != null) {
            appointment.setCreated(appointmentEntity.getCreated());
        }

        if (appointmentEntity.getComment() != null) {
            appointment.setComment(appointmentEntity.getComment());
        }

        for (AppointmentParticipant appointmentParticipant : appointmentEntity.getParticipants()) {
            Appointment.AppointmentParticipantComponent appointmentComponent = appointment.addParticipant();

            if (appointmentParticipant.getActor() != null) {
                appointmentComponent
                        .setActor(new Reference("Patient/" + appointmentParticipant.getActor().getId()))
                         .getActor().setDisplay(appointmentParticipant.getActor().getNames().get(0).getDisplayName());
            }

            if (appointmentParticipant.getType() != null) {
                appointmentComponent.addType().addCoding()
                        .setCode(appointmentParticipant.getType().getCode())
                        .setSystem(appointmentParticipant.getType().getSystem())
                        .setDisplay(appointmentParticipant.getType().getDisplay());
            }

            if (appointmentParticipant.getStatus() != null) {
                appointmentComponent.setStatus(Appointment.ParticipationStatus.ACCEPTED);
            }

            if(appointmentParticipant.getRequired() != null){
                appointmentComponent.setRequired(Appointment.ParticipantRequired.REQUIRED);
            }


        }

        return appointment;

    }
}
