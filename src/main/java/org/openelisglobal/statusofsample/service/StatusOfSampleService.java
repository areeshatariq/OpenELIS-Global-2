package org.openelisglobal.statusofsample.service;

import java.util.List;

import org.openelisglobal.common.service.BaseObjectService;
import org.openelisglobal.statusofsample.valueholder.StatusOfSample;

public interface StatusOfSampleService extends BaseObjectService<StatusOfSample, String> {
    void getData(StatusOfSample sourceOfSample);

//	void updateData(StatusOfSample sourceOfSample);

//	boolean insertData(StatusOfSample sourceOfSample);

    List getPreviousStatusOfSampleRecord(String id);

    List getPageOfStatusOfSamples(int startingRecNo);

    Integer getTotalStatusOfSampleCount();

    StatusOfSample getDataByStatusTypeAndStatusCode(StatusOfSample statusofsample);

    List getAllStatusOfSamples();

    List getNextStatusOfSampleRecord(String id);
}
