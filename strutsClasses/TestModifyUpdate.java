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

package org.openelisglobal.testconfiguration.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.GenericValidator;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.openelisglobal.common.action.BaseAction;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.localization.service.LocalizationServiceImpl;
import org.openelisglobal.test.service.TestSectionServiceImpl;
import org.openelisglobal.common.services.TestService;
import org.openelisglobal.typeofsample.service.TypeOfSampleServiceImpl;
import org.openelisglobal.typeoftestresult.service.TypeOfTestResultServiceImpl;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.hibernate.HibernateUtil;
import org.openelisglobal.localization.daoimpl.LocalizationDAOImpl;
import org.openelisglobal.localization.valueholder.Localization;
import org.openelisglobal.panel.daoimpl.PanelDAOImpl;
import org.openelisglobal.panelitem.dao.PanelItemDAO;
import org.openelisglobal.panelitem.daoimpl.PanelItemDAOImpl;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.resultlimits.dao.ResultLimitDAO;
import org.openelisglobal.resultlimits.daoimpl.ResultLimitDAOImpl;
import org.openelisglobal.resultlimits.valueholder.ResultLimit;
import org.openelisglobal.test.dao.TestDAO;
import org.openelisglobal.test.daoimpl.TestDAOImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.test.valueholder.TestSection;
import org.openelisglobal.testresult.dao.TestResultDAO;
import org.openelisglobal.testresult.daoimpl.TestResultDAOImpl;
import org.openelisglobal.testresult.valueholder.TestResult;
import org.openelisglobal.typeofsample.dao.TypeOfSampleDAO;
import org.openelisglobal.typeofsample.dao.TypeOfSampleTestDAO;
import org.openelisglobal.typeofsample.daoimpl.TypeOfSampleDAOImpl;
import org.openelisglobal.typeofsample.daoimpl.TypeOfSampleTestDAOImpl;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.openelisglobal.unitofmeasure.daoimpl.UnitOfMeasureDAOImpl;
import org.openelisglobal.unitofmeasure.valueholder.UnitOfMeasure;

public class TestModifyUpdate extends BaseAction {
    private TypeOfSampleDAO typeOfSampleDAO = new TypeOfSampleDAOImpl();
    @Override
    protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String currentUserId = getSysUserId(request);
        String jsonString = ((DynaValidatorForm)form).getString("jsonWad");
//        System.out.println(jsonString);

        JSONParser parser=new JSONParser();

        JSONObject obj = (JSONObject)parser.parse(jsonString);
        TestAddParams testAddParams = extractTestAddParms(obj, parser);
       
        Localization nameLocalization = createNameLocalization(testAddParams);
        Localization reportingNameLocalization = createReportingNameLocalization(testAddParams);
        
        List<TestSet> testSets = createTestSets(testAddParams);
        TestDAO testDAO = new TestDAOImpl();
        
        TypeOfSampleTestDAO typeOfSampleTestDAO = new TypeOfSampleTestDAOImpl();
        PanelItemDAO panelItemDAO = new PanelItemDAOImpl();
        
        TestResultDAO testResultDAO = new TestResultDAOImpl();
        ResultLimitDAO resultLimitDAO = new ResultLimitDAOImpl();

        Transaction tx = HibernateUtil.getSession().beginTransaction();
        try{
        
        	
            List<TypeOfSampleTest> typeOfSampleTest = typeOfSampleTestDAO.getTypeOfSampleTestsForTest(testAddParams.testId);
            String[] typeOfSamplesTestIDs = new String[typeOfSampleTest.size()];
            for ( int i = 0; i < typeOfSampleTest.size(); i++) {
            	typeOfSamplesTestIDs[i] = typeOfSampleTest.get(i).getId();
            }
            typeOfSampleTestDAO.deleteData(typeOfSamplesTestIDs, currentUserId);
            
            List<PanelItem> panelItems = panelItemDAO.getPanelItemByTestId(testAddParams.testId);
        	for( PanelItem item : panelItems){
        		item.setSysUserId(currentUserId);
        	}
            panelItemDAO.deleteData(panelItems);
            
            List<ResultLimit> resultLimitItems = resultLimitDAO.getAllResultLimitsForTest(testAddParams.testId);
            for( ResultLimit item : resultLimitItems){
        		item.setSysUserId(currentUserId);
        	}
            resultLimitDAO.deleteData(resultLimitItems);
        	
            for( TestSet set : testSets){
                set.test.setSysUserId(currentUserId);
                set.test.setLocalizedTestName(nameLocalization);
                set.test.setLocalizedReportingName(reportingNameLocalization);
                
//	gnr: based on testAddUpdate, 
//  added existing testId to process in createTestSets using testAddParams.testId, delete then insert to modify for most elements
              
                for( Test test :set.sortedTests){
                    test.setSysUserId(currentUserId);
                    //if (!test.getId().equals( set.test.getId() )) {
                    	testDAO.updateData(test);
                    //}
                }
                
            	updateTestNames(testAddParams.testId, nameLocalization.getEnglish(), nameLocalization.getFrench(), reportingNameLocalization.getEnglish(), reportingNameLocalization.getFrench(), currentUserId);
            	updateTestEntities(testAddParams.testId, testAddParams.loinc, currentUserId);

                set.sampleTypeTest.setSysUserId(currentUserId);
                set.sampleTypeTest.setTestId(set.test.getId());
               	typeOfSampleTestDAO.insertData(set.sampleTypeTest);
               	
               	for( PanelItem item : set.panelItems){
                    item.setSysUserId(currentUserId);
                    Test nonTransiantTest = testDAO.getTestById(set.test.getId());
                    item.setTest(nonTransiantTest);
                    panelItemDAO.insertData(item);
                }

                for( TestResult testResult : set.testResults){
                    testResult.setSysUserId(currentUserId);
                    Test nonTransiantTest = testDAO.getTestById(set.test.getId());
                    testResult.setTest(nonTransiantTest);
                    testResultDAO.insertData(testResult);
                }

                for( ResultLimit resultLimit : set.resultLimits){
                    resultLimit.setSysUserId(currentUserId);
                    resultLimit.setTestId(set.test.getId());
                    resultLimitDAO.insertData(resultLimit);
                }
            }

            tx.commit();
        }catch( HibernateException e ){
            tx.rollback();
        }finally{
            HibernateUtil.closeSession();
        }

        TestServiceImpl.refreshTestNames();
        SpringContext.getBean(TypeOfSampleService.class).clearCache();
        return mapping.findForward(FWD_SUCCESS);
    }
    
    private void updateTestEntities( String testId, String loinc, String userId) {
    	 Test test = new TestService( testId ).getTest();

         if( test != null ){
        	 test.setSysUserId(userId);
        	 test.setLoinc(loinc);
        	 new TestDAOImpl().updateData ( test );
         }
    }
    
    private void updateTestNames( String testId, String nameEnglish, String nameFrench, String reportNameEnglish, String reportNameFrench, String userId ){
        Test test = new TestService( testId ).getTest();

        if( test != null ){
            Localization name = test.getLocalizedTestName();
            Localization reportingName = test.getLocalizedReportingName();
            name.setEnglish( nameEnglish.trim() );
            name.setFrench( nameFrench.trim() );
            name.setSysUserId( userId );
            reportingName.setEnglish( reportNameEnglish.trim() );
            reportingName.setFrench( reportNameFrench.trim() );
            reportingName.setSysUserId( userId );

            new LocalizationDAOImpl().updateData( name );
            new LocalizationDAOImpl().updateData( reportingName );

        }

        //Refresh test names
        DisplayListService.getInstance().getFreshList( DisplayListService.ListType.ALL_TESTS );
        DisplayListService.getInstance().getFreshList( DisplayListService.ListType.ORDERABLE_TESTS );
    }

    private void createPanelItems(ArrayList<PanelItem> panelItems, TestAddParams testAddParams) {
        PanelDAOImpl panelDAO = new PanelDAOImpl();
        for( String panelId : testAddParams.panelList) {
            PanelItem panelItem = new PanelItem();
            panelItem.setPanel(panelDAO.getPanelById(panelId));
            panelItems.add(panelItem);
        }
    }

    private void createTestResults(ArrayList<TestResult> testResults, String significantDigits, TestAddParams testAddParams) {
        TypeOfTestResultServiceImpl.ResultType type = TypeOfTestResultServiceImpl.getResultTypeById(testAddParams.resultTypeId);

        if (TypeOfTestResultServiceImpl.ResultType.isTextOnlyVariant(type) ||
                TypeOfTestResultServiceImpl.ResultType.isNumeric(type)){
            TestResult testResult = new TestResult();
            testResult.setTestResultType(type.getCharacterValue());
            testResult.setSortOrder("1");
            testResult.setIsActive(true);
            testResult.setSignificantDigits(significantDigits);
            testResults.add(testResult);
        }else if(TypeOfTestResultServiceImpl.ResultType.isDictionaryVariant(type.getCharacterValue())){
            int sortOrder = 10;
            for(DictionaryParams params : testAddParams.dictionaryParamList){
                TestResult testResult = new TestResult();
                testResult.setTestResultType(type.getCharacterValue());
                testResult.setSortOrder(String.valueOf(sortOrder));
                sortOrder += 10;
                testResult.setIsActive(true);
                testResult.setValue( params.dictionaryId);
                testResult.setIsQuantifiable(params.isQuantifiable);
                testResults.add(testResult);
            }
        }
    }
    private Localization createNameLocalization(TestAddParams testAddParams) {
        return LocalizationServiceImpl.createNewLocalization(testAddParams.testNameEnglish,
                testAddParams.testNameFrench, LocalizationServiceImpl.LocalizationType.TEST_NAME);
    }

    private Localization createReportingNameLocalization(TestAddParams testAddParams) {
        return  LocalizationServiceImpl.createNewLocalization(testAddParams.testReportNameEnglish,
                testAddParams.testReportNameFrench, LocalizationServiceImpl.LocalizationType.REPORTING_TEST_NAME);
    }

    private List<TestSet> createTestSets(TestAddParams testAddParams) {
        Double lowValid = null;
        Double highValid = null;
        String significantDigits = testAddParams.significantDigits;
        boolean numericResults = TypeOfTestResultServiceImpl.ResultType.isNumericById(testAddParams.resultTypeId);
        boolean dictionaryResults = TypeOfTestResultServiceImpl.ResultType.isDictionaryVarientById(testAddParams.resultTypeId);
        List<TestSet> testSets = new ArrayList<TestSet>();
        UnitOfMeasure uom = null;
        if(!GenericValidator.isBlankOrNull(testAddParams.uomId) || "0".equals(testAddParams.uomId)) {
            uom = new UnitOfMeasureDAOImpl().getUnitOfMeasureById(testAddParams.uomId);
        }
        TestSection testSection = new TestSectionServiceImpl( testAddParams.testSectionId).getTestSection();

        if( numericResults ){
            lowValid = StringUtil.doubleWithInfinity(testAddParams.lowValid);
            highValid = StringUtil.doubleWithInfinity(testAddParams.highValid);
        }
        //The number of test sets depend on the number of sampleTypes
        for( int i = 0; i < testAddParams.sampleList.size(); i++){
            TypeOfSample typeOfSample = typeOfSampleDAO.getTypeOfSampleById(testAddParams.sampleList.get(i).sampleTypeId);
            if (typeOfSample == null) {
                continue;
            }
            TestSet testSet = new TestSet();
            Test test = new Test();
           
            test.setId(testAddParams.testId);
            
            test.setUnitOfMeasure(uom);
            test.setDescription(testAddParams.testNameEnglish + "(" + typeOfSample.getDescription() + ")");
            test.setTestName(testAddParams.testNameEnglish);
            test.setLocalCode(testAddParams.testNameEnglish);
            test.setIsActive(testAddParams.active);
            test.setOrderable("Y".equals(testAddParams.orderable));
            test.setIsReportable("N");
            test.setTestSection(testSection);
            test.setGuid(String.valueOf(UUID.randomUUID()));
            ArrayList<String> orderedTests = testAddParams.sampleList.get(i).orderedTests;
            for( int j = 0; j < orderedTests.size(); j++){
                if( "0".equals(orderedTests.get(j))){
                    test.setSortOrder(String.valueOf(j));
                }else {
                    Test orderedTest = new TestService(orderedTests.get(j)).getTest();
                    orderedTest.setSortOrder(String.valueOf(j));
                    testSet.sortedTests.add(orderedTest);
                }
            }

            testSet.test = test;

            TypeOfSampleTest typeOfSampleTest = new TypeOfSampleTest();
            typeOfSampleTest.setTypeOfSampleId(typeOfSample.getId());
            testSet.sampleTypeTest = typeOfSampleTest;

            createPanelItems(testSet.panelItems, testAddParams);
            createTestResults(testSet.testResults, significantDigits, testAddParams);
            if( numericResults) {
                testSet.resultLimits = createResultLimits(lowValid, highValid, testAddParams);
            }else if( dictionaryResults){
                testSet.resultLimits = createDictionaryResultLimit( testAddParams);
            }

            testSets.add( testSet);
        }

        return testSets;
    }

    private ArrayList<ResultLimit> createDictionaryResultLimit(TestAddParams testAddParams) {
        ArrayList<ResultLimit> resultLimits = new ArrayList<ResultLimit>();
        if( !GenericValidator.isBlankOrNull(testAddParams.dictionaryReferenceId)){
            ResultLimit limit = new ResultLimit();
            limit.setResultTypeId(testAddParams.resultTypeId);
            limit.setDictionaryNormalId(testAddParams.dictionaryReferenceId);
            resultLimits.add(limit);
        }

        return resultLimits;
    }

    private ArrayList<ResultLimit> createResultLimits(Double lowValid, Double highValid, TestAddParams testAddParams) {
        ArrayList<ResultLimit> resultLimits = new ArrayList<ResultLimit>();
        for( ResultLimitParams params : testAddParams.limits){
            ResultLimit limit = new ResultLimit();
            limit.setResultTypeId(testAddParams.resultTypeId);
            limit.setGender(params.gender);
            limit.setMinAge(StringUtil.doubleWithInfinity(params.lowAge));
            limit.setMaxAge(StringUtil.doubleWithInfinity(params.highAge));
            limit.setLowNormal(StringUtil.doubleWithInfinity(params.lowLimit));
            limit.setHighNormal(StringUtil.doubleWithInfinity(params.highLimit));
            limit.setLowValid(lowValid);
            limit.setHighValid(highValid);
            resultLimits.add(limit);
        }

        return resultLimits;
    }


    private TestAddParams extractTestAddParms(JSONObject obj, JSONParser parser) {
        TestAddParams testAddParams = new TestAddParams();
        try {

        	testAddParams.testId = (String) obj.get("testId");
            testAddParams.testNameEnglish = (String) obj.get("testNameEnglish");
            testAddParams.testNameFrench = (String) obj.get("testNameFrench");
            testAddParams.testReportNameEnglish = (String) obj.get("testReportNameEnglish");
            testAddParams.testReportNameFrench = (String) obj.get("testReportNameFrench");
            testAddParams.testSectionId = (String) obj.get("testSection");
            testAddParams.dictionaryReferenceId = (String) obj.get("dictionaryReference");
            extractPanels(obj, parser, testAddParams);
            testAddParams.uomId = (String)obj.get("uom");
            testAddParams.loinc = (String)obj.get("loinc");
            testAddParams.resultTypeId = (String)obj.get("resultType");
            extractSampleTypes(obj, parser, testAddParams);
            testAddParams.active = (String) obj.get("active");
            testAddParams.orderable = (String) obj.get("orderable");
            if( TypeOfTestResultServiceImpl.ResultType.isNumericById(testAddParams.resultTypeId)){
                testAddParams.lowValid = (String)obj.get("lowValid");
                testAddParams.highValid = (String)obj.get("highValid");
                testAddParams.significantDigits = (String)obj.get("significantDigits");
                extractLimits( obj, parser, testAddParams);
            }else if( TypeOfTestResultServiceImpl.ResultType.isDictionaryVarientById(testAddParams.resultTypeId)){
                String dictionary = (String)obj.get("dictionary");
                JSONArray dictionaryArray = (JSONArray) parser.parse(dictionary);
                for( int i = 0; i < dictionaryArray.size(); i++){
                    DictionaryParams params = new DictionaryParams();
                    params.dictionaryId = (String)((JSONObject) dictionaryArray.get(i)).get("value");
                    params.isQuantifiable = "Y".equals((String)((JSONObject) dictionaryArray.get(i)).get("qualified"));
                    testAddParams.dictionaryParamList.add(params);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return testAddParams;
    }

    private void extractLimits(JSONObject obj, JSONParser parser, TestAddParams testAddParams) throws ParseException{
        String lowAge = "0";
        String limits = (String)obj.get("resultLimits");
        JSONArray limitArray = (JSONArray) parser.parse(limits);
        for( int i = 0; i < limitArray.size(); i++){
            ResultLimitParams params = new ResultLimitParams();
            Boolean gender = (Boolean)((JSONObject) limitArray.get(i)).get("gender");
            if( gender ){
                params.gender = "M";
            }
            String highAge = (String)(((JSONObject) limitArray.get(i)).get("highAgeRange"));
            params.displayRange = (String)(((JSONObject) limitArray.get(i)).get("reportingRange"));
            params.lowLimit = (String)(((JSONObject) limitArray.get(i)).get("lowNormal"));
            params.highLimit = (String)(((JSONObject) limitArray.get(i)).get("highNormal"));
            params.lowAge = lowAge;
            params.highAge = highAge;
            testAddParams.limits.add(params);

            if( gender ){
                params = new ResultLimitParams();
                params.gender = "F";
                params.displayRange = (String)(((JSONObject) limitArray.get(i)).get("reportingRangeFemale"));
                params.lowLimit = (String)(((JSONObject) limitArray.get(i)).get("lowNormalFemale"));
                params.highLimit = (String)(((JSONObject) limitArray.get(i)).get("highNormalFemale"));
                params.lowAge = lowAge;
                params.highAge = highAge;
                testAddParams.limits.add(params);
            }

            lowAge = highAge;
        }
    }

    private void extractPanels(JSONObject obj, JSONParser parser, TestAddParams testAddParams) throws ParseException {
        String panels = (String)obj.get("panels");
        JSONArray panelArray = (JSONArray) parser.parse(panels);

        for (int i = 0; i < panelArray.size(); i++) {
            testAddParams.panelList.add((String) (((JSONObject) panelArray.get(i)).get("id")));
        }

    }

    private void extractSampleTypes(JSONObject obj, JSONParser parser, TestAddParams testAddParams) throws ParseException {
        String sampleTypes = (String)obj.get("sampleTypes");
        JSONArray sampleTypeArray = (JSONArray) parser.parse(sampleTypes);

        for (int i = 0; i < sampleTypeArray.size(); i++) {
            SampleTypeListAndTestOrder sampleTypeTests = new SampleTypeListAndTestOrder();
            sampleTypeTests.sampleTypeId = (String) (((JSONObject) sampleTypeArray.get(i)).get("typeId"));

            JSONArray testArray = (JSONArray) (((JSONObject)sampleTypeArray.get(i)).get("tests"));
            for( int j = 0; j < testArray.size(); j++){
                sampleTypeTests.orderedTests.add( String.valueOf(((JSONObject) testArray.get(j)).get("id")));
            }
            testAddParams.sampleList.add(sampleTypeTests);
        }
    }

    @Override
    protected String getPageTitleKey() {
        return null;
    }

    @Override
    protected String getPageSubtitleKey() {
        return null;
    }

    public class TestAddParams{
    	String testId;
        public String testNameEnglish;
        public String testNameFrench;
        public String testReportNameEnglish;
        public String testReportNameFrench;
        String testSectionId;
        ArrayList<String> panelList = new ArrayList<String>();
        String uomId;
        String loinc;
        String resultTypeId;
        ArrayList<SampleTypeListAndTestOrder> sampleList = new ArrayList<SampleTypeListAndTestOrder>();
        String active;
        String orderable;
        String lowValid;
        String highValid;
        public String significantDigits;
        String dictionaryReferenceId;
        ArrayList<ResultLimitParams> limits = new ArrayList<ResultLimitParams>();
        public ArrayList<DictionaryParams> dictionaryParamList = new ArrayList<DictionaryParams>();
    }

    public class SampleTypeListAndTestOrder{
        String sampleTypeId;
        ArrayList<String> orderedTests = new ArrayList<String>();
    }

    public class ResultLimitParams{
        String gender;
        String lowAge;
        String highAge;
        String lowLimit;
        String highLimit;
        String displayRange;
    }
    public class TestSet{
        Test test;
        TypeOfSampleTest sampleTypeTest;
        ArrayList<Test> sortedTests = new ArrayList<Test>();
        ArrayList<PanelItem> panelItems = new ArrayList<PanelItem>();
        ArrayList<TestResult> testResults = new ArrayList<TestResult>();
        ArrayList<ResultLimit> resultLimits = new ArrayList<ResultLimit>();
    }

    public class DictionaryParams{
        public String dictionaryId;
        public boolean isQuantifiable = false;
    }
}
