package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Slot;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;

import java.util.List;
import java.util.Set;

public interface SlotRepository extends BaseRepository<SlotEntity,Slot> {
    void save(FhirContext ctx, SlotEntity slotEntity) throws OperationOutcomeException;

    Slot read(FhirContext ctx, IdType theId);

    SlotEntity readEntity(FhirContext ctx, IdType theId);

    Slot create(FhirContext ctx, Slot slot, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Resource> searchSlot(FhirContext ctx,
                              @OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam identifier,
                              @OptionalParam(name = Slot.SP_START) DateParam start,
                              @OptionalParam(name = Slot.SP_STATUS) StringParam status,
                              @OptionalParam(name = Slot.SP_RES_ID) StringParam id,
                              @OptionalParam(name = Slot.SP_SCHEDULE) ReferenceParam schedule,
                              @OptionalParam(name = "service") ReferenceParam service,
                              @IncludeParam(allow= {"Slot:schedule",
                                                    "Schedule:actor:Practitioner",
                                                    "Schedule:actor:PractitionerRole",
                                                    "Schedule:actor:Location",
                                                    "Schedule:actor:HealthcareService"}) Set<Include> includes
    );

    List<SlotEntity> searchSlotEntity(FhirContext ctx,
                                      @OptionalParam(name = Slot.SP_IDENTIFIER) TokenParam identifier,
                                      @OptionalParam(name = Slot.SP_START) DateParam start,
                                      @OptionalParam(name = Slot.SP_STATUS) StringParam status,
                                      @OptionalParam(name = Slot.SP_RES_ID) StringParam id,
                                      @OptionalParam(name = Slot.SP_SCHEDULE) ReferenceParam schedule,
                                      @OptionalParam(name = "service") ReferenceParam service,
                                      @IncludeParam(allow= {"Slot:schedule",
                                                            "Schedule:actor:Practitioner",
                                                            "Schedule:actor:PractitionerRole",
                                                            "Schedule:actor:Location",
                                                            "Schedule:actor:HealthcareService"}) Set<Include> includes
    );

}
