/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is OpenELIS code.
 *
 * Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
 */

package org.openelisglobal.reports.action.implementation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.form.BaseForm;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.ConfigurationProperties.Property;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.observationhistory.service.ObservationHistoryService;
import org.openelisglobal.observationhistory.service.ObservationHistoryServiceImpl.ObservationType;
import org.openelisglobal.patient.service.PatientService;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.reports.action.implementation.reportBeans.ActivityReportBean;
import org.openelisglobal.result.service.ResultService;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.sample.service.SampleService;
import org.openelisglobal.sample.util.AccessionNumberUtil;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.service.SampleHumanService;
import org.openelisglobal.spring.util.SpringContext;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public abstract class ActivityReport extends Report implements IReportCreator {
    private int PREFIX_LENGTH = AccessionNumberUtil.getAccessionNumberValidator().getInvarientLength();
    protected List<ActivityReportBean> testsResults;
    protected String reportPath = "";
    protected DateRange dateRange;

    @Override
    public JRDataSource getReportDataSource() throws IllegalStateException {
        return errorFound ? new JRBeanCollectionDataSource(errorMsgs) : new JRBeanCollectionDataSource(testsResults);
    }

    @Override
    protected void createReportParameters() {
        reportParameters.put("activityLabel", getActivityLabel());
        reportParameters.put("accessionPrefix", AccessionNumberUtil.getAccessionNumberValidator().getPrefix());
        reportParameters.put("labNumberTitle", MessageUtil.getContextualMessage("quick.entry.accession.number"));
        reportParameters.put("labName", ConfigurationProperties.getInstance().getPropertyValue(Property.SiteName));
        reportParameters.put("SUBREPORT_DIR", reportPath);
        reportParameters.put("startDate", dateRange.getLowDateStr());
        reportParameters.put("endDate", dateRange.getHighDateStr());
        reportParameters.put("isReportByTest", isReportByTest());

    }

    protected boolean isReportByTest() {
        return false;
    }

    protected abstract String getActivityLabel();

    protected abstract void buildReportContent(ReportSpecificationList testSelection);

    @Override
    public void initializeReport(BaseForm form) {
        initialized = true;
        ReportSpecificationList selection = (ReportSpecificationList) form.get("selectList");
        String lowDateStr = form.getString("lowerDateRange");
        String highDateStr = form.getString("upperDateRange");
        dateRange = new DateRange(lowDateStr, highDateStr);

        super.createReportParameters();
        errorFound = !validateSubmitParameters(selection);
        if (errorFound) {
            return;
        }

        buildReportContent(selection);
        if (testsResults.size() == 0) {
            add1LineErrorMessage("report.error.message.noPrintableItems");
        }
    }

    private boolean validateSubmitParameters(ReportSpecificationList selectList) {

        return (dateRange.validateHighLowDate("report.error.message.date.received.missing")
                && validateSelection(selectList));
    }

    private boolean validateSelection(ReportSpecificationList selectList) {
        boolean complete = !GenericValidator.isBlankOrNull(selectList.getSelection())
                && !"0".equals(selectList.getSelection());

        if (!complete) {
            add1LineErrorMessage("report.error.message.activity.missing");
        }

        return complete;
    }

    protected ActivityReportBean createActivityReportBean(Result result, boolean useTestName) {
        ActivityReportBean item = new ActivityReportBean();

        ResultService resultService = SpringContext.getBean(ResultService.class);
        SampleService sampleService = SpringContext.getBean(SampleService.class);
        Sample sample = result.getAnalysis().getSampleItem().getSample();
        PatientService patientService = SpringContext.getBean(PatientService.class);
        SampleHumanService sampleHumanService = SpringContext.getBean(SampleHumanService.class);
        Patient patient = sampleHumanService.getPatientForSample(sample);
        item.setResultValue(resultService.getResultValue(result, "\n", true, true));
        item.setTechnician(resultService.getSignature(result));
        item.setAccessionNumber(sampleService.getAccessionNumber(sample).substring(PREFIX_LENGTH));
        item.setReceivedDate(sampleService.getReceivedDateWithTwoYearDisplay(sample));
        item.setResultDate(DateUtil.convertTimestampToTwoYearStringDate(result.getLastupdated()));
        item.setCollectionDate(
                DateUtil.convertTimestampToTwoYearStringDate(result.getAnalysis().getSampleItem().getCollectionDate()));

        List<String> values = new ArrayList<>();
        values.add(
                patientService.getLastName(patient) == null ? "" : patientService.getLastName(patient).toUpperCase());
        values.add(patientService.getNationalId(patient));

        String referringPatientId = SpringContext.getBean(ObservationHistoryService.class)
                .getValueForSample(ObservationType.REFERRERS_PATIENT_ID, sample.getId());
        values.add(referringPatientId == null ? "" : referringPatientId);

        String name = StringUtil.buildDelimitedStringFromList(values, " / ", true);

        if (useTestName) {
            item.setPatientOrTestName(resultService.getReportingTestName(result));
            item.setNonPrintingPatient(name);
        } else {
            item.setPatientOrTestName(name);
        }

        return item;
    }

    @Override
    protected String reportFileName() {
        return "ActivityReport";
    }

    protected ActivityReportBean createIdentityActivityBean(ActivityReportBean item, boolean blankCollectionDate) {
        ActivityReportBean filler = new ActivityReportBean();

        filler.setAccessionNumber(item.getAccessionNumber());
        filler.setReceivedDate(item.getReceivedDate());
        filler.setCollectionDate(blankCollectionDate ? " " : item.getCollectionDate());
        filler.setPatientOrTestName(item.getNonPrintingPatient());

        return filler;
    }
}
