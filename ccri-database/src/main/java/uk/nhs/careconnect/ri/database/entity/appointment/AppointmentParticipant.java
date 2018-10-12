package uk.nhs.careconnect.ri.database.entity.appointment;


import org.hl7.fhir.dstu3.model.Appointment;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(name = "AppointmentParticipant",uniqueConstraints = @UniqueConstraint(name="PK_APPOINTMENT_PARTICIPANT", columnNames={"APPOINTMENT_PARTICIPANT_ID"})
		,indexes = { @Index(name="IDX_APPOINTMENT_PARTICIPANT", columnList = "APPOINTMENT_PARTICIPANT_ID")}
)
public class AppointmentParticipant extends BaseResource {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="APPOINTMENT_PARTICIPANT_ID")
	private Long Id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "APPOINTMENT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_PARTICIPANT_APPOINTMENT_ID"))
	private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="TYPE_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_PARTICIPANT_TYPE_CONCEPT_ID"))
    private ConceptEntity type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PARTICIPANT_ACTOR_ID",foreignKey= @ForeignKey(name="FK_APPOINTMENT_PARTICIPANT_PARTICIPANT_ACTOR_ID"))
    private PatientEntity actor;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PARTICIPANT_STATUS")
    private Appointment.ParticipationStatus status;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PARTICIPANT_REQUIRED")
    private Appointment.ParticipantRequired required;

    public ConceptEntity getType() {
        return type;
    }

    public void setType(ConceptEntity type) {
        this.type = type;
    }

    public Appointment.ParticipationStatus getStatus() {
        return status;
    }

    public void setStatus(Appointment.ParticipationStatus status) {
        this.status = status;
    }

    @Override
	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public AppointmentEntity getAppointment() {
		return appointment;
	}

	public void setAppointment(AppointmentEntity appointment) {
		this.appointment = appointment;
	}

    public PatientEntity getActor() { return actor; }

    public void setActor(PatientEntity actor) { this.actor = actor; }

    public Appointment.ParticipantRequired getRequired() { return required; }

    public void setRequired(Appointment.ParticipantRequired required) { this.required = required; }
}
