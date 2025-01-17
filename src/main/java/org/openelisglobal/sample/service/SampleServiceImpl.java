package org.openelisglobal.sample.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.observationhistory.service.ObservationHistoryService;
import org.openelisglobal.observationhistory.service.ObservationHistoryServiceImpl;
import org.openelisglobal.observationhistory.valueholder.ObservationHistory;
import org.openelisglobal.organization.service.OrganizationService;
import org.openelisglobal.organization.valueholder.Organization;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.person.service.PersonService;
import org.openelisglobal.person.valueholder.Person;
import org.openelisglobal.referencetables.service.ReferenceTablesService;
import org.openelisglobal.requester.service.RequesterTypeService;
import org.openelisglobal.requester.service.SampleRequesterService;
import org.openelisglobal.requester.valueholder.RequesterType;
import org.openelisglobal.requester.valueholder.SampleRequester;
import org.openelisglobal.sample.dao.SampleDAO;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.service.SampleHumanService;
import org.openelisglobal.sampleqaevent.service.SampleQaEventService;
import org.openelisglobal.sampleqaevent.valueholder.SampleQaEvent;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@DependsOn({ "springContext" })
public class SampleServiceImpl extends BaseObjectServiceImpl<Sample, String> implements SampleService {

    public static String TABLE_REFERENCE_ID;

    private static Long PERSON_REQUESTER_TYPE_ID;
    private static Long ORGANIZATION_REQUESTER_TYPE_ID;

    @Autowired
    protected SampleDAO sampleDAO;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private SampleHumanService sampleHumanService;
    @Autowired
    private SampleQaEventService sampleQaEventService;
    @Autowired
    private SampleRequesterService sampleRequesterService;
    @Autowired
    private PersonService personService;
    @Autowired
    private ReferenceTablesService referenceTableService;
    @Autowired
    private RequesterTypeService requesterTypeService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private TestService testService;

    @PostConstruct
    private void initializeGlobalVariables() {
        TABLE_REFERENCE_ID = referenceTableService.getReferenceTableByName("SAMPLE").getId();
        RequesterType type = requesterTypeService.getRequesterTypeByName("provider");
        PERSON_REQUESTER_TYPE_ID = type != null ? Long.parseLong(type.getId()) : Long.MIN_VALUE;
        type = requesterTypeService.getRequesterTypeByName("organization");
        ORGANIZATION_REQUESTER_TYPE_ID = type != null ? Long.parseLong(type.getId()) : Long.MIN_VALUE;
    }

    public SampleServiceImpl() {
        super(Sample.class);
    }

    @Override
    protected SampleDAO getBaseObjectDAO() {
        return sampleDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public Sample getSampleByAccessionNumber(String labNumber) {
        return getMatch("accessionNumber", labNumber).orElse(null);
    }

    @Override
    @Transactional
    public boolean insertDataWithAccessionNumber(Sample sample) {
        insert(sample);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesReceivedOn(String recievedDate) {
        return sampleDAO.getSamplesReceivedOn(recievedDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesForPatient(String patientID) {
        return sampleHumanService.getSamplesForPatient(patientID);
    }

    /**
     * Gets the date of when the order was completed
     *
     * @return The date of when it was completed, null if it was not yet completed
     */
    @Override
    @Transactional(readOnly = true)
    public Date getCompletedDate(Sample sample) {
        Date date = null;
        List<Analysis> analysisList = analysisService.getAnalysesBySampleId(sample.getId());

        for (Analysis analysis : analysisList) {
            if (!isCanceled(analysis)) {
                if (analysis.getCompletedDate() == null) {
                    return null;
                } else if (date == null) {
                    date = analysis.getCompletedDate();
                } else if (analysis.getCompletedDate().after(date)) {
                    date = analysis.getCompletedDate();
                }
            }
        }
        return date;
    }

    private boolean isCanceled(Analysis analysis) {
        return StatusService.getInstance().getStatusID(StatusService.AnalysisStatus.Canceled)
                .equals(analysis.getStatusId());
    }

    @Override
    @Transactional(readOnly = true)
    public Timestamp getOrderedDate(Sample sample) {
        if (sample == null) {
            return null;
        }
        ObservationHistory observation = SpringContext.getBean(ObservationHistoryService.class)
                .getObservationForSample(ObservationHistoryServiceImpl.ObservationType.REQUEST_DATE, sample.getId());
        if (observation != null && observation.getValue() != null) {
            return DateUtil.convertStringDateToTruncatedTimestamp(observation.getValue());
        } else { // If ordered date is not given then use received date
            return sample.getReceivedTimestamp();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getAccessionNumber(Sample sample) {
        return sample.getAccessionNumber();
    }

    @Override
    @Transactional(readOnly = true)
    public String getReceivedDateForDisplay(Sample sample) {
        return sample.getReceivedDateForDisplay();
    }

    @Override
    @Transactional(readOnly = true)
    public String getTwoYearReceivedDateForDisplay(Sample sample) {
        String fourYearDate = getReceivedDateForDisplay(sample);
        int lastSlash = fourYearDate.lastIndexOf("/");
        return fourYearDate.substring(0, lastSlash + 1) + fourYearDate.substring(lastSlash + 3);
    }

    @Override
    @Transactional(readOnly = true)
    public String getReceivedDateWithTwoYearDisplay(Sample sample) {
        return DateUtil.convertTimestampToTwoYearStringDate(sample.getReceivedTimestamp());
    }

    @Override
    @Transactional(readOnly = true)
    public String getReceivedTimeForDisplay(Sample sample) {
        return sample.getReceivedTimeForDisplay();
    }

    @Override
    @Transactional(readOnly = true)
    public String getReceived24HourTimeForDisplay(Sample sample) {
        return sample.getReceived24HourTimeForDisplay();
    }

    @Override
    public boolean isConfirmationSample(Sample sample) {
        return sample != null && sample.getIsConfirmation();
    }

    @Override
    @Transactional(readOnly = true)
    public String getId(Sample sample) {
        return sample.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getPatient(Sample sample) {
        return sampleHumanService.getPatientForSample(sample);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysis(Sample sample) {
        return sample == null ? new ArrayList<>() : analysisService.getAnalysesBySampleId(sample.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SampleQaEvent> getSampleQAEventList(Sample sample) {
        return sample == null ? new ArrayList<>() : sampleQaEventService.getSampleQaEventsBySample(sample);
    }

    @Override
    @Transactional(readOnly = true)
    public Person getPersonRequester(Sample sample) {
        if (sample == null) {
            return null;
        }

        List<SampleRequester> requesters = sampleRequesterService.getRequestersForSampleId(sample.getId());

        for (SampleRequester requester : requesters) {
            if (PERSON_REQUESTER_TYPE_ID == requester.getRequesterTypeId()) {
                Person person = new Person();
                person.setId(String.valueOf(requester.getRequesterId()));
                personService.getData(person);
                return person.getId() != null ? person : null;
            }
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getOrganizationRequester(Sample sample) {
        if (sample == null) {
            return null;
        }

        List<SampleRequester> requesters = sampleRequesterService.getRequestersForSampleId(sample.getId());

        for (SampleRequester requester : requesters) {
            if (ORGANIZATION_REQUESTER_TYPE_ID == requester.getRequesterTypeId()) {
                Organization org = organizationService.getOrganizationById(String.valueOf(requester.getRequesterId()));
                return org != null ? org : null;
            }
        }

        return null;
    }

    @Transactional(readOnly = true)
    public Sample getPatientPreviousSampleForTestName(Sample sample, Patient patient, String testName) {
        List<Sample> patientSampleList = sampleHumanService.getSamplesForPatient(patient.getId());
        Sample previousSample = null;
        List<Integer> sampIDList = new ArrayList<>();
        List<Integer> testIDList = new ArrayList<>();

        testIDList.add(Integer.parseInt(testService.getTestByName(testName).getId()));

        if (patientSampleList.isEmpty()) {
            return previousSample;
        }

        for (Sample patientSample : patientSampleList) {
            sampIDList.add(Integer.parseInt(patientSample.getId()));
        }

        List<Integer> statusList = new ArrayList<>();
        statusList.add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized)));

        List<Analysis> analysisList = analysisService.getAnalysesBySampleIdTestIdAndStatusId(sampIDList, testIDList,
                statusList);

        if (analysisList.isEmpty()) {
            return previousSample;
        }

        for (int j = 0; j < analysisList.size(); j++) {
            if (j < analysisList.size() && sample.getAccessionNumber()
                    .equals(analysisList.get(j).getSampleItem().getSample().getAccessionNumber())) {
                previousSample = analysisList.get(j + 1).getSampleItem().getSample();
            }

        }

        /*
         * for(int j=0;j<analysisList.size();j++){
         *
         * if(j<analysisList.size() &&
         * sample.getAccessionNumber().equals(analysisList.get(j).getSampleItem().
         * getSample().getAccessionNumber())) return
         * analysisList.get(j+1).getSampleItem().getSample();
         *
         * }
         */
        return previousSample;

    }

    @Override
    @Transactional(readOnly = true)
    public void getData(Sample sample) {
        getBaseObjectDAO().getData(sample);

    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getConfirmationSamplesReceivedInDateRange(Date receivedDateStart, Date receivedDateEnd) {
        return getBaseObjectDAO().getConfirmationSamplesReceivedInDateRange(receivedDateStart, receivedDateEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesByProjectAndStatusIDAndAccessionRange(List<Integer> inclusiveProjectIdList,
            List<Integer> inclusiveStatusIdList, String minAccession, String maxAccession) {
        return getBaseObjectDAO().getSamplesByProjectAndStatusIDAndAccessionRange(inclusiveProjectIdList,
                inclusiveStatusIdList, minAccession, maxAccession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesByProjectAndStatusIDAndAccessionRange(String projectId,
            List<Integer> inclusiveStatusIdList, String minAccession, String maxAccession) {
        return getBaseObjectDAO().getSamplesByProjectAndStatusIDAndAccessionRange(projectId, inclusiveStatusIdList,
                minAccession, maxAccession);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLargestAccessionNumberWithPrefix(String prefix) {
        return getBaseObjectDAO().getLargestAccessionNumberWithPrefix(prefix);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLargestAccessionNumberMatchingPattern(String startingWith, int size) {
        return getBaseObjectDAO().getLargestAccessionNumberMatchingPattern(startingWith, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesWithPendingQaEventsByService(String serviceId) {
        return getBaseObjectDAO().getSamplesWithPendingQaEventsByService(serviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List getSamplesByStatusAndDomain(List statuses, String domain) {
        return getBaseObjectDAO().getSamplesByStatusAndDomain(statuses, domain);
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousSampleRecord(String id) {
        return getBaseObjectDAO().getPreviousSampleRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesCollectedOn(String collectionDate) {
        return getBaseObjectDAO().getSamplesCollectedOn(collectionDate);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLargestAccessionNumber() {
        return getBaseObjectDAO().getLargestAccessionNumber();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesWithPendingQaEvents(Sample sample, boolean filterByCategory, String qaEventCategoryId,
            boolean filterByDomain) {
        return getBaseObjectDAO().getSamplesWithPendingQaEvents(sample, filterByCategory, qaEventCategoryId,
                filterByDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextSampleRecord(String id) {
        return getBaseObjectDAO().getNextSampleRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Sample getSampleByReferringId(String referringId) {
        return getBaseObjectDAO().getSampleByReferringId(referringId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesReceivedInDateRange(String receivedDateStart, String receivedDateEnd) {
        return getBaseObjectDAO().getSamplesReceivedInDateRange(receivedDateStart, receivedDateEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesByAccessionRange(String minAccession, String maxAccession) {
        return getBaseObjectDAO().getSamplesByAccessionRange(minAccession, maxAccession);
    }

    @Override
    @Transactional(readOnly = true)
    public void getSampleByAccessionNumber(Sample sample) {
        getBaseObjectDAO().getSampleByAccessionNumber(sample);

    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfSamples(int startingRecNo) {
        return getBaseObjectDAO().getPageOfSamples(startingRecNo);
    }

    @Override
    public String generateAccessionNumberAndInsert(Sample sample) {
        sample.setAccessionNumber(getBaseObjectDAO().getNextAccessionNumber());
        return insert(sample);
    }

}
