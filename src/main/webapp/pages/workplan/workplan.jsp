<%@ page language="java"
	contentType="text/html; charset=utf-8"
	import="java.util.List,
			org.openelisglobal.common.action.IActionConstants,
			java.util.Collection,
			org.openelisglobal.test.beanItems.TestResultItem,
			org.openelisglobal.common.util.IdValuePair,
			org.openelisglobal.internationalization.MessageUtil,
			org.openelisglobal.common.util.ConfigurationProperties,
			org.openelisglobal.common.util.ConfigurationProperties.Property,
			org.owasp.encoder.Encode" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="workplanType" value="${param.type}"/>
<c:set var="tests" value="${form.workplanTests}"/>
<c:set var="testCount" value="${fn:length(tests)}"/>
<c:set var="currentAccessionNumber" value=""/>
<c:set var="rowColorIndex" value="2"/>

<c:if test="${not (workplanType == 'test') && not (workplanType == 'panel')}">
<c:set var="testSectionsByName" value="${form.testSectionsByName}"/>
	<script type="text/javascript" >
	var testSectionNameIdHash = [];
	<c:forEach items="${testSectionsByName}" var="testSection">
		testSectionNameIdHash["${testSection.id}"] = "${testSection.value}";
	</c:forEach>
	</script>
</c:if>

<%

	String basePath = "";
	String path = request.getContextPath();
	basePath = request.getScheme() + "://" + request.getServerName() + ":"
	+ request.getServerPort() + path + "/";

%>

<script type="text/javascript" >

function  /*void*/ setMyCancelAction(form, action, validate, parameters)
{
	//first turn off any further validation
	setAction(document.getElementById("mainForm"), 'Cancel', 'no', '');
}

function disableEnableTest(checkbox, index){

	if (checkbox.checked) {
		$("row_" + index).style.background = "#cccccc";
	}else {
		checkbox.checked = "";
		$("row_" + index).style.backgroundColor = "";
	}
}

function submitTestSectionSelect( element ) {

	window.location.href = "WorkPlanByTestSection.do?testSectionId=" + element.value + "&type=" + testSectionNameIdHash[element.value] ;
}

function printWorkplan() {

	var form = document.getElementById("mainForm");
	form.action = "PrintWorkplanReport.do";
	form.target = "_blank";
	form.submit();
}

</script>
<form:hidden path="workplanType"/>
<form:hidden path="testTypeID"/>
<c:choose>
<c:when test="${not (workplanType == 'test') && not (workplanType == 'panel')}">
<div id="searchDiv" class="colorFill"  >
<div id="PatientPage" class="colorFill" style="display:inline" >
<input type="hidden" name="testName" value='<c:out value="${workplanType}"/>' />
<h2><spring:message code="sample.entry.search"/></h2>
	<table width="30%">
		<tr>
			<td width="50%" align="right" >
				<c:out value="${form.searchLabel}"/>
			</td>
			<td>
			<form:select path="testSectionId" 
				 onchange="submitTestSectionSelect(this);" >
				<option value=""></option>
				<form:options items="${form.testSections}" itemLabel="value" itemValue="id" />
			</form:select>
	   		</td>
		</tr>
	</table>
	<br/>
	<h1>
		
	</h1>
</div>
</div>
</c:when>
<c:otherwise>
	<form:hidden path="testName"/>
</c:otherwise>
</c:choose>

<br/>
<c:if test="${not (testCount == 0)}">
<c:set value="${fn:length(form.workplanTests)}" var="size" />
<button type="button" name="print" id="print"  onclick="printWorkplan();"  >
	<spring:message code="workplan.print"/>
</button>
<br/><br/>
<spring:message code="label.total.tests"/> = <c:out value="${size}"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="./images/nonconforming.gif" /> = <spring:message code="result.nonconforming.item"/>
<br/><br/>
<Table width="90%" border="0" cellspacing="0">
	<tr>
		<th width="5%" style="text-align: left;">
			<spring:message code="label.button.remove"/>
		</th>
		<c:if test="${workplanType == 'test'}">
			<th width="3%">&nbsp;</th>
		</c:if>
    	<th width="10%" style="text-align: left;">
    		<%= MessageUtil.getContextualMessage("quick.entry.accession.number") %>
		</th>
		<% if(ConfigurationProperties.getInstance().isPropertyValueEqual(Property.SUBJECT_ON_WORKPLAN, "true")){ %>
		<th width="5%" style="text-align: left;">
			<%= MessageUtil.getContextualMessage("patient.subject.number") %>
		</th>	
		<% } %>
		<% if(ConfigurationProperties.getInstance().isPropertyValueEqual(Property.NEXT_VISIT_DATE_ON_WORKPLAN, "true")){ %>
	    <th width="5%" style="text-align: left;">
	    	<spring:message code="sample.entry.nextVisit.date"/>
	    </th>
	    <% } %>
		<c:if test="${not (workplanType == 'test')}">
		<th width="3%">&nbsp;</th>
		<th width="30%" style="text-align: left;">
			<% if(ConfigurationProperties.getInstance().isPropertyValueEqual(Property.configurationName, "Haiti LNSP")){ %>
                <spring:message code="sample.entry.project.patient.and.testName"/>
	        <% } else { %>
	  		   <spring:message code="sample.entry.project.testName"/>
	  		<% } %>  
		</th>
		</c:if>
		<th width="20%" style="text-align: left;">
	  		<spring:message code="sample.receivedDate"/>&nbsp;&nbsp;
		</th>
  	</tr>

	<c:forEach items="${form.workplanTests}" var="workplanTests" varStatus="iter">
		<form:hidden path="workplanTests[${iter.index}].accessionNumber"/>
		<form:hidden path="workplanTests[${iter.index}].patientInfo"/>
		<form:hidden path="workplanTests[${iter.index}].receivedDate"/>
		<form:hidden path="workplanTests[${iter.index}].testName"/>
		<c:if test="${not (workplanTests.accessionNumber == currentAccessionNumber)}" var="showAccessionNumber">
			<c:set var="currentAccessionNumber" value="${workplanTests.accessionNumber}"/>
			<c:set var="rowColorIndex" value="${rowColorIndex + 1}"/>
		</c:if>
     		<tr id='row_${iter.index}' 
     		<c:choose>
     			<c:when test="${rowColorIndex % 2 == 0}"> class="evenRow"</c:when>
     			<c:otherwise> class="oddRow"</c:otherwise>
     		</c:choose>
     		>
     			<td id='cell_${iter.index}'>
     			<c:if test="${not workplanTests.servingAsTestGroupIdentifier}">
					<form:checkbox path="workplanTests[${iter.index}].notIncludedInWorkplan"
						   id='includedCheck_${iter.index}'
						   cssClass="includedCheck"
						   onclick='disableEnableTest(this,${iter.index});' />
			    </c:if>
				</td>
				<c:if test="${workplanType == 'test'}">
				<td>
					<c:if test="${workplanTests.nonconforming}">
						<img src="./images/nonconforming.gif" />
					</c:if>
				</td>	
				</c:if>
	    		<td>
	      		<c:if test="${showAccessionNumber}">
	      			<c:out value="${workplanTests.accessionNumber}"/>
				</c:if>
	    		</td>
	    		<% if( ConfigurationProperties.getInstance().isPropertyValueEqual(Property.SUBJECT_ON_WORKPLAN, "true")){ %>
		    		<td>
		      		<c:if test="${showAccessionNumber}">
		    			<c:out value="${workplanTests.patientInfo}"/>
		    		</c:if>
		    		</td>
	    		<% } %>
	    		<% if(ConfigurationProperties.getInstance().isPropertyValueEqual(Property.NEXT_VISIT_DATE_ON_WORKPLAN, "true")){ %>
	    			<td>
	      			<c:if test="${showAccessionNumber}">
		    			<c:out value="${workplanTests.nextVisitDate}"/>
		    		</c:if>
	    			</td>
	    		<% } %>
					<c:if test="${not (workplanType == 'test')}">
					<td>
						<c:if test="${workplanTests.nonconforming}">
							<img src="./images/nonconforming.gif" />
						</c:if>
				</td>
				<td>
					${workplanTests.testName} 
				</td>
				</c:if>
	    		<td>
	      			<c:out value="${workplanTests.receivedDate}"/>
	    		</td>
      		</tr>
  	</c:forEach>
	<tr>
	    <td colspan="8"><hr/></td>
    </tr>
	<tr>
		<td>
      		<button type="button" name="print" id="print"  onclick="printWorkplan();"  >
				<spring:message code="workplan.print"/>
			</button>
		</td>
	</tr>
</Table>
</c:if>
<c:if test="${workplanType == 'test' || workplanType == 'panel' }">
	<c:if test="${testCount == 0}">
		<h2><%= MessageUtil.getContextualMessage("result.noTestsFound") %></h2>
	</c:if>
</c:if>
<c:if test="${not (workplanType == 'test') && not (workplanType == 'panel') }">
	<c:if test="${testCount == 0}">
		<c:if test="${not empty form.testSectionId}">
		<h2><%=MessageUtil.getContextualMessage("result.noTestsFound") %></h2>
		</c:if>
	</c:if>
</c:if>


