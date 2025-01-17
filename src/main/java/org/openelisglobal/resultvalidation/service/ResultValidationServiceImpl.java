package org.openelisglobal.resultvalidation.service;

import java.util.ArrayList;
import java.util.List;

import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.services.IResultSaveService;
import org.openelisglobal.common.services.ResultSaveService;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.services.StatusService.OrderStatus;
import org.openelisglobal.common.services.registration.interfaces.IResultUpdate;
import org.openelisglobal.note.service.NoteService;
import org.openelisglobal.note.valueholder.Note;
import org.openelisglobal.result.service.ResultService;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;
import org.openelisglobal.sample.service.SampleService;
import org.openelisglobal.sample.valueholder.Sample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultValidationServiceImpl implements ResultValidationService {

    private AnalysisService analysisService;
    private ResultService resultService;
    private NoteService noteService;
    private SampleService sampleService;

    public ResultValidationServiceImpl(AnalysisService analysisService, ResultService resultService,
            NoteService noteService, SampleService sampleService) {
        this.analysisService = analysisService;
        this.resultService = resultService;
        this.noteService = noteService;
        this.sampleService = sampleService;
    }

    @Override
    @Transactional
    public void persistdata(List<Result> deletableList, List<Analysis> analysisUpdateList,
            ArrayList<Result> resultUpdateList, List<AnalysisItem> resultItemList, ArrayList<Sample> sampleUpdateList,
            ArrayList<Note> noteUpdateList, IResultSaveService resultSaveService, List<IResultUpdate> updaters,
            String sysUserId) {
        ResultSaveService.removeDeletedResultsInTransaction(deletableList, sysUserId);

        // update analysis
        for (Analysis analysis : analysisUpdateList) {
            analysisService.update(analysis);
        }

        for (Result resultUpdate : resultUpdateList) {
            if (resultUpdate.getId() != null) {
                resultService.update(resultUpdate);
            } else {
                resultService.insert(resultUpdate);
            }
        }

        checkIfSamplesFinished(resultItemList, sampleUpdateList);

        // update finished samples
        for (Sample sample : sampleUpdateList) {
            sampleService.update(sample);
        }

        // create or update notes
        for (Note note : noteUpdateList) {
            if (note != null) {
                if (note.getId() == null) {
                    noteService.insert(note);
                } else {
                    noteService.update(note);
                }
            }
        }

        for (IResultUpdate updater : updaters) {
            updater.transactionalUpdate(resultSaveService);
        }
    }

    private void checkIfSamplesFinished(List<AnalysisItem> resultItemList, List<Sample> sampleUpdateList) {
        String currentSampleId = "";
        boolean sampleFinished = true;
        List<Integer> sampleFinishedStatus = getSampleFinishedStatuses();

        for (AnalysisItem analysisItem : resultItemList) {

            String analysisSampleId = sampleService.getSampleByAccessionNumber(analysisItem.getAccessionNumber())
                    .getId();
            if (!analysisSampleId.equals(currentSampleId)) {

                currentSampleId = analysisSampleId;

                List<Analysis> analysisList = analysisService.getAnalysesBySampleId(currentSampleId);

                for (Analysis analysis : analysisList) {
                    if (!sampleFinishedStatus.contains(Integer.parseInt(analysis.getStatusId()))) {
                        sampleFinished = false;
                        break;
                    }
                }

                if (sampleFinished) {
                    Sample sample = sampleService.get(currentSampleId);
                    sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Finished));
                    sampleUpdateList.add(sample);
                }

                sampleFinished = true;

            }

        }
    }

    private List<Integer> getSampleFinishedStatuses() {
        ArrayList<Integer> sampleFinishedStatus = new ArrayList<>();
        sampleFinishedStatus.add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized)));
        sampleFinishedStatus.add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Canceled)));
        sampleFinishedStatus.add(
                Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.NonConforming_depricated)));
        return sampleFinishedStatus;
    }

}
