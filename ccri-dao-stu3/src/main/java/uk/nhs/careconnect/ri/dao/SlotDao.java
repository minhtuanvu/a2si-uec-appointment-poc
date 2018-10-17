package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.*;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceIdentifier;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleActor;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class SlotDao implements SlotRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    SlotEntityToFHIRSlotTransformer slotEntityToFHIRSlotTransformer;

    @Autowired
    ScheduleEntityToFHIRScheduleTransformer scheduleEntityToFHIRScheduleTransformer;

    @Autowired
    PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    @Autowired
    PractitionerRoleToFHIRPractitionerRoleTransformer practitionerRoleToFHIRPractitionerRoleTransformer;


    @Autowired
    LocationEntityToFHIRLocationTransformer locationEntityToFHIRLocationTransformer;

    @Autowired
    HealthcareServiceEntityToFHIRHealthcareServiceTransformer healthcareServiceEntityToFHIRHealthcareServiceTransformer;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    ScheduleRepository scheduleDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    List<Resource> results = null;

    private static final Logger log = LoggerFactory.getLogger(SlotDao.class);

    @Override
    public void save(FhirContext ctx, SlotEntity slotEntity) {
        em.persist(slotEntity);
    }

    @Override
    public Slot read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            SlotEntity slotEntity =  em.find(SlotEntity.class, Long.parseLong(theId.getIdPart()));
            return slotEntity == null
                    ? null
                    : slotEntityToFHIRSlotTransformer.transform(slotEntity);

        } else {
            return null;
        }
    }

    @Override
    public SlotEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            SlotEntity slotEntity = (SlotEntity) em.find(SlotEntity.class, Long.parseLong(theId.getIdPart()));

            return slotEntity;

        } else {
            return null;
        }
    }

    @Override
    public Slot create(FhirContext ctx, Slot slot, IdType theId, String theConditional) throws OperationOutcomeException  {
        log.debug("Slot.save");

        SlotEntity slotEntity = null;

        if (slot.hasId()) slotEntity = readEntity(ctx, slot.getIdElement());

        if (theConditional != null) {
            try {
                if (theConditional.contains("https://tools.ietf.org/html/rfc4122")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    log.info("** Scheme = "+scheme);
                    String host = uri.getHost();
                    log.info("** Host = "+host);
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    //List<Slot> results = searchSlot(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null,null,null,null,null); //,null
                    List<SlotEntity> results = searchSlotEntity(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null,null,null,null,null,null); //,null
                    for (SlotEntity con : results) {
                        slotEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (slotEntity == null) {
            slotEntity = new SlotEntity();
        }

        if(slot.hasSchedule()) {

            try{

                ScheduleEntity scheduleEntity = (ScheduleEntity) scheduleDao.readEntity(ctx, new IdType(slot.getSchedule().getReference()));
                if (scheduleEntity != null) {
                    slotEntity.setSchedule(scheduleEntity);
                }else {
                    String message = "Schedule/"+slot.getSchedule().getReference() + " does not exist";
                    log.error(message);
                    throw new OperationOutcomeException("Slot",message, OperationOutcome.IssueType.CODEINVALID);
                }

            }catch(Exception ex){

            }

        }

        if (slot.hasServiceCategory()) {

            ConceptEntity code = conceptDao.findCode(slot.getServiceCategory().getCoding().get(0));

            if (code != null) {
                slotEntity.setServiceCategory(code);
            }
        }

        if (slot.hasAppointmentType()) {

            ConceptEntity code = conceptDao.findCode(slot.getAppointmentType().getCoding().get(0));

            if (code != null) {
                slotEntity.setAppointmentType(code);
            }

        }

        if (slot.hasStatus()) {
            log.debug("Slot.Status" + slot.getStatus());
            slotEntity.setStatus(slot.getStatus());
        } else {
            slotEntity.setStatus(null);
        }

        if (slot.hasStart()) {

            slotEntity.setStart(slot.getStart());
        } else {
            slotEntity.setStart(null);
        }
        if (slot.hasEnd()) {
            slotEntity.setEnd(slot.getEnd());
        } else {
            slotEntity.setEnd(null);
        }

        em.persist(slotEntity);

        log.info("Slot.Transform");
        return slotEntityToFHIRSlotTransformer.transform(slotEntity);

    }

    @Override
    public List<Resource> searchSlot(FhirContext ctx, TokenParam identifier, DateParam start, StringParam status, StringParam res_id, ReferenceParam schedule, ReferenceParam service,TokenParam serviceIdentifier, Set<Include> includes) {
        List<SlotEntity> qryResults = searchSlotEntity(ctx, identifier, start, status, res_id, schedule, service, serviceIdentifier, includes);

        results = new ArrayList<>();

        for (SlotEntity slotEntity : qryResults) {
            Slot slot = slotEntityToFHIRSlotTransformer.transform(slotEntity);
            results.add(slot);

        }

/*        for (SlotEntity slotEntity : qryResults) {
            Slot slot;
            if (slotEntity.getResource() != null) {
                slot = (Slot) ctx.newJsonParser().parseResource(slotEntity.getResource());
            } else {
                slot = slotEntityToFHIRSlotTransformer.transform(slotEntity);
                String resourceStr = ctx.newJsonParser().encodeResourceToString(slot);
                slotEntity.setResource(resourceStr);
                em.persist(slotEntity);
            }
            results.add(slot);
        }*/


        if (includes != null) {

            for (Include include : includes) {

                for (SlotEntity slotEntity : qryResults) {

                    switch (include.getValue()) {

                        case "Slot:schedule":
                            ScheduleEntity scheduleEntity = slotEntity.getSchedule();
                            if(scheduleEntity != null) {
                                resultsAddIfNotPresent(scheduleEntityToFHIRScheduleTransformer.transform(scheduleEntity));
                            }
                            break;

                        case "Schedule:actor:Practitioner":
                            if(slotEntity != null && slotEntity.getSchedule() != null) {
                                if(slotEntity.getSchedule().getActors() != null) {
                                    for (ScheduleActor actor : slotEntity.getSchedule().getActors()) {
                                        if (actor.getPractitionerEntity() != null) {
                                            resultsAddIfNotPresent(practitionerEntityToFHIRPractitionerTransformer.transform(actor.getPractitionerEntity()));

                                        }
                                    }
                                }
                            }
                            break;

                        case "Schedule:actor:PractitionerRole":
                            if(slotEntity != null && slotEntity.getSchedule() != null) {
                                if(slotEntity.getSchedule().getActors() != null) {
                                    for (ScheduleActor actor : slotEntity.getSchedule().getActors()) {
                                        if (actor.getPractitionerRole() != null) {
                                            resultsAddIfNotPresent(practitionerRoleToFHIRPractitionerRoleTransformer.transform(actor.getPractitionerRole()));
                                        }
                                    }
                                }
                            }
                            break;


                        case "Schedule:actor:Location":
                            if(slotEntity != null && slotEntity.getSchedule() != null) {
                                if (slotEntity.getSchedule().getActors() != null) {
                                    for (ScheduleActor actor : slotEntity.getSchedule().getActors()) {
                                        if (actor.getLocationEntity() != null) {
                                            resultsAddIfNotPresent(locationEntityToFHIRLocationTransformer.transform(actor.getLocationEntity()));
                                        }
                                    }
                                }
                            }
                            break;

                        case "Schedule:actor:HealthcareService":
                            if(slotEntity != null && slotEntity.getSchedule() != null) {
                                if (slotEntity.getSchedule().getActors() != null) {
                                    for (ScheduleActor actor : slotEntity.getSchedule().getActors()) {
                                        if (actor.getHealthcareServiceEntity() != null) {
                                            resultsAddIfNotPresent(healthcareServiceEntityToFHIRHealthcareServiceTransformer.transform(actor.getHealthcareServiceEntity()));
                                        }
                                    }
                                }
                            }
                            break;

                    }
                }
            }
        }
        return results;
    }

    private void resultsAddIfNotPresent(Resource resource) {
        boolean found = false;
        for (Resource resource1 : results) {
            if (resource1.getId().equals(resource.getId()) && resource.getClass().getSimpleName().equals(resource1.getClass().getSimpleName())) found=true;
        }
        if (!found) results.add(resource);
    }

    @Override
    public List<SlotEntity> searchSlotEntity(FhirContext ctx, TokenParam identifier, DateParam start, StringParam status, StringParam resid, ReferenceParam schedule,ReferenceParam service,TokenParam serviceIdentifier,Set<Include> includes) {
        List<SlotEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<SlotEntity> criteria = builder.createQuery(SlotEntity.class);
        Root<SlotEntity> root = criteria.from(SlotEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Schedule> results = new ArrayList<Schedule>();


        if (identifier !=null)
        {
            Join<SlotEntity, SlotIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        //ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (start !=null)
        {

            Predicate p = builder.greaterThanOrEqualTo(root.get("Start"), parameterLower);
            predList.add(p);

        }

        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }

        if (status != null) {

            Integer intStatus = 0;

            switch (status.getValue()) {

                case "busy" :
                    intStatus = 0;
                    break;
                case "free" :
                    intStatus = 1;
                    break;

                default:
                    intStatus = 1;

            }

            Predicate p = builder.equal(root.get("Status"), intStatus);
            predList.add(p);

        }

        if (schedule != null) {

            if (daoutils.isNumeric(schedule.getIdPart())) {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule" , JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), schedule.getIdPart());
                predList.add(p);
            } else {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }

        if (serviceIdentifier != null) {

            if (daoutils.isNumeric(serviceIdentifier.getValue())) {

                Join<SlotEntity, ScheduleEntity> join = root.join("schedule" , JoinType.LEFT);
                Join<ScheduleEntity, ScheduleActor> join1 = join.join("actors" , JoinType.LEFT);
                Join<ScheduleActor, HealthcareServiceEntity> join2 = join1.join("healthcareServiceEntity" , JoinType.LEFT);
                Join<HealthcareServiceEntity, HealthcareServiceIdentifier> join3 = join2.join("identifiers" , JoinType.LEFT);

                //System.out.println("serviceIdentifier.getValue():" + serviceIdentifier.getValue());
                //System.out.println("join3 : " + join3);

                Predicate p = builder.equal(join3.get("identifierId"), serviceIdentifier.getValue());
                predList.add(p);
            } else {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule", JoinType.LEFT);

                Predicate p = builder.equal(join.get("identifierId"), -1);
                predList.add(p);
            }

        }

        if (service != null) {

            if (daoutils.isNumeric(service.getIdPart())) {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule" , JoinType.LEFT);
                Join<ScheduleEntity, ScheduleActor> join1 = join.join("actors" , JoinType.LEFT);
                Join<ScheduleActor, HealthcareServiceEntity> join2 = join1.join("healthcareServiceEntity" , JoinType.LEFT);

                Predicate p = builder.equal(join2.get("id"), service.getIdPart());
                predList.add(p);
            } else {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }

        }

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);

        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        TypedQuery<SlotEntity> typedQuery = em.createQuery(criteria);

        if (start != null) {

                typedQuery.setParameter(parameterLower, start.getValue(), TemporalType.TIMESTAMP);
        }

        qryResults = typedQuery.setMaxResults(daoutils.MAXROWS).getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(SlotEntity.class)));
        System.out.println("Counting resources : " + em.createQuery(cq).getSingleResult());
        return em.createQuery(cq).getSingleResult();
    }




}
