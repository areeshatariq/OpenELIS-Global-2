/**
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
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*
* Contributor(s): CIRG, University of Washington, Seattle WA.
*/
package org.openelisglobal.analysis.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.dao.BaseDAO;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.sampleitem.valueholder.SampleItem;
import org.openelisglobal.test.valueholder.Test;

/**
 * @author diane benz
 *
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public interface AnalysisDAO extends BaseDAO<Analysis, String> {

//	boolean insertData(Analysis analysis, boolean duplicateCheck) throws LIMSRuntimeException;

//	@SuppressWarnings("rawtypes")
//	void deleteData(List analysiss) throws LIMSRuntimeException;

//	@SuppressWarnings("rawtypes")
//	List getAllAnalyses() throws LIMSRuntimeException;

//	@SuppressWarnings("rawtypes")
//	List getPageOfAnalyses(int startingRecNo) throws LIMSRuntimeException;

    void getData(Analysis analysis) throws LIMSRuntimeException;

//	void updateData(Analysis analysis) throws LIMSRuntimeException;

//	@SuppressWarnings("rawtypes")
//	List getAnalyses(String filter) throws LIMSRuntimeException;

//	@SuppressWarnings("rawtypes")
//	List getNextAnalysisRecord(String id) throws LIMSRuntimeException;
//
//	@SuppressWarnings("rawtypes")
//	List getPreviousAnalysisRecord(String id) throws LIMSRuntimeException;

//	@SuppressWarnings("rawtypes")
//	List getAllAnalysesPerTest(Test test) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllAnalysisByTestAndStatus(String testId, List<Integer> statusIdList) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllAnalysisByTestsAndStatus(List<String> testIdList, List<Integer> statusIdList)
            throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllAnalysisByTestAndExcludedStatus(String testId, List<Integer> statusIdList) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllAnalysisByTestSectionAndStatus(String testSectionId, List<Integer> statusIdList,
            boolean sortedByDateAndAccession) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllAnalysisByTestSectionAndExcludedStatus(String testSectionId, List<Integer> statusIdList)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleItem(SampleItem sampleItem) throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleItemsExcludingByStatusIds(SampleItem sampleItem, Set<Integer> statusIds)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleStatusId(String statusId) throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleStatusIdExcludingByStatusId(String statusId, Set<Integer> statusIds)
            throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAnalysesReadyToBeReported() throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllChildAnalysesByResult(Result result) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionAnalysesReadyToBeReported() throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionAnalysesReadyForReportPreviewBySample(List accessionNumbers) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAnalysesAlreadyReportedBySample(Sample sample) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionAnalysesBySample(SampleItem sampleItem) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionAnalysesBySampleIncludeCanceled(SampleItem sampleItem) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getRevisionHistoryOfAnalysesBySample(SampleItem sampleItem) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getRevisionHistoryOfAnalysesBySampleAndTest(SampleItem sampleItem, Test test, boolean includeLatestRevision)
            throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getAllMaxRevisionAnalysesPerTest(Test test) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionPendingAnalysesReadyToBeReportedBySample(Sample sample) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionPendingAnalysesReadyForReportPreviewBySample(Sample sample) throws LIMSRuntimeException;

    Analysis getPreviousAnalysisForAmendedAnalysis(Analysis analysis) throws LIMSRuntimeException;

    void getMaxRevisionAnalysisBySampleAndTest(Analysis analysis) throws LIMSRuntimeException;

    @SuppressWarnings("rawtypes")
    List getMaxRevisionParentTestAnalysesBySample(SampleItem sampleItem) throws LIMSRuntimeException;

    List<Analysis> getAnalysesForStatusId(String statusId) throws LIMSRuntimeException;

    List<Analysis> getAnalysisStartedOnExcludedByStatusId(Date collectionDate, Set<Integer> statusIds)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisStartedOn(Date collectionDate) throws LIMSRuntimeException;

    List<Analysis> getAnalysisCollectedOnExcludedByStatusId(Date collectionDate, Set<Integer> statusIds)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisCollectedOn(Date collectionDate) throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleId(String id) throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleIdExcludedByStatusId(String id, Set<Integer> statusIds)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisBySampleAndTestIds(String sampleKey, List<Integer> testIds);

    List<Analysis> getAnalysesBySampleIdTestIdAndStatusId(List<Integer> sampleIdList, List<Integer> testIdList,
            List<Integer> statusIdList);

//	Analysis getPatientPreviousAnalysisForTestName(Patient patient, Sample currentSample, String testName);

    List<Analysis> getAnalysisByTestSectionAndCompletedDateRange(String sectionID, Date lowDate, Date highDate)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisStartedOrCompletedInDateRange(Date lowDate, Date highDate) throws LIMSRuntimeException;

    List<Analysis> getAllAnalysisByTestSectionAndStatus(String testSectionId, List<Integer> analysisStatusList,
            List<Integer> sampleStatusList) throws LIMSRuntimeException;

    List<Analysis> getAnalysisStartedOnRangeByStatusId(Date lowDate, Date highDate, String statusID)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisCompleteInRange(Timestamp lowDate, Timestamp highDate) throws LIMSRuntimeException;

    List<Analysis> getAnalysisEnteredAfterDate(Timestamp latestCollectionDate) throws LIMSRuntimeException;

    List<Analysis> getAnalysisByAccessionAndTestId(String accessionNumber, String testId) throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleIdAndStatusId(String id, Set<Integer> analysisStatusIds)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisByTestNamesAndCompletedDateRange(List<String> testNames, Date lowDate, Date highDate)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysesBySampleItemIdAndStatusId(String sampleItemId, String statusId)
            throws LIMSRuntimeException;

    List<Analysis> getAnalysisByTestDescriptionAndCompletedDateRange(List<String> descriptions, Date sqlDayOne,
            Date sqlDayTwo) throws LIMSRuntimeException;

    Analysis getAnalysisById(String analysisId) throws LIMSRuntimeException;

//	void updateData(Analysis analysis, boolean skipAuditTrail) throws LIMSRuntimeException;
}
