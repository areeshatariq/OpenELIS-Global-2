<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="org.openelisglobal.common.action.IActionConstants,
                 org.openelisglobal.common.provider.validation.AccessionNumberValidatorFactory,
                 org.openelisglobal.common.provider.validation.IAccessionNumberValidator" %>
                 
<%@ page isELIgnored="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags" %>

      

<%!
String path = "";
String basePath = "";
IAccessionNumberValidator accessionNumberValidator;
%>

<%
path = request.getContextPath();
basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
accessionNumberValidator = new AccessionNumberValidatorFactory().getValidator();
%>

<script type="text/javascript">
var lineSeparator = "";
var accessionLength = <%= accessionNumberValidator.getMaxAccessionLength()%>;
inPrintState = false;

//Adds warning when leaving page
window.onbeforeunload = formWarning;
function formWarning(){ 
	//firefox overwrites any message that is put as a page closing message
	return "Are you sure you want to leave this page?";
}

function saveLabel() {
	postBatchSample(onPostBatchSampleSuccess, defaultFailure);
}

function onPostBatchSampleSuccess() {
	setPatient();
	moveAccessionToRecentArea();
	jQuery("#labNo").val("");
    jQuery("#labNo").trigger('keyup');
	setSave();
}

//Add accession number to recent area
function moveAccessionToRecentArea() {
	var $recentTextArea = jQuery("#recentSummary");
	if ($recentTextArea.val()) {
		lineSeparator = "\n";
	}
	var newRecent = jQuery("#labNo").val() + lineSeparator + $recentTextArea.val();
	if ((newRecent.match(/\n/g)||[]).length >= 3) {
		newRecent = newRecent.slice(0,newRecent.lastIndexOf("\n"));
	}
	$recentTextArea.val(newRecent);
}

//check if labNo is valid accession number when a character key is pressed and length is accession length
function checkAccessionNumber(accessionNumber, event) {
	var charPressed = (event.keyCode >=48 && event.keyCode <=57) || (event.keyCode >=65 && event.keyCode <=90) || (event.keyCode >=96 && event.keyCode <=105)
	if (accessionNumber.value.length >= accessionLength && charPressed) {
      	validateAccessionNumberOnServer(false, false, accessionNumber.id, accessionNumber.value, processAccessionSuccess, null);
	} else if (accessionNumber.value.length < accessionLength) {
		inPrintState = false;
		setSave();
	}
}

//check response from accession number validation
function processAccessionSuccess(xhr) {
    var formField = xhr.responseXML.getElementsByTagName("formfield").item(0);
    var message = xhr.responseXML.getElementsByTagName("message").item(0);
    if (message.firstChild.nodeValue == "valid") {
        inPrintState = true;
        setSave()
    } else {
        alert(message.firstChild.nodeValue);
    }
    var labElement = formField.firstChild.nodeValue;
    selectFieldErrorDisplay(success, document.getElementById(labElement));
}
</script>
<h2><spring:message code="sample.batchentry.preprinted.header.entry" /></h2>

<table>
<tr>
	<td>
		<spring:message code="sample.batchentry.preprinted.labno" />:
	</td>
</tr>
<tr>
	<td>
		<form:input path="sampleOrderItems.labNo"
			maxlength='<%= Integer.toString(accessionNumberValidator.getMaxAccessionLength())%>'
            onkeyup="checkAccessionNumber(this, event);"
            styleClass="text"
            id="labNo"/>
		<button type="button" onclick="saveLabel();"
			id="saveButtonId" 
			disabled="disabled">
		<spring:message code="sample.batchentry.preprinted.save" />
		</button>
	</td>
</tr>
<tr>
	<td> <br></td>
</tr>
<tr>
	<td>
		<table>
			<tr>
				<td>
					<spring:message code="sample.batchentry.preprinted.summary" />:
				</td>
			</tr>
			<tr>
				<td>
					<textarea id="recentSummary" 
						rows="5" 
						cols="50"
						readonly="readonly"></textarea>
				</td>
			</tr>
		</table>
	</td>
</tr>
</table>