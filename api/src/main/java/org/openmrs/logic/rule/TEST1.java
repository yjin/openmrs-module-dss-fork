/**
 * ******************************************************************
 * Translated from - TEST1.mlm on Sat Nov 24 19:37:14 PST 2012
 *
 * Filename: TEST1 Version: 1.0 Institution: SFSU Author: Yan Jin Specialist:
 * Pediatrics Date: 2012-11-11 Validation : testing Explanation: Keywords:
 * Arden, Test Citations: Links:
 * ******************************************************************
 */
package org.openmrs.logic.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.arden.MlmRule;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.impl.LogicCriteriaImpl;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.logic.rule.provider.RuleProvider;
import org.openmrs.logic.Duration;
import java.util.StringTokenizer;
// import org.openmrs.module.dss.util.DebugFileWrite;  //debug code
import java.io.IOException;  //debug code

import org.openmrs.api.ConceptService;
import java.text.SimpleDateFormat;
import org.openmrs.Concept;
import org.openmrs.ConceptName;

public class TEST1 implements MlmRule {

    private Log log = LogFactory.getLog(this.getClass());

    /**
     * * @see org.openmrs.logic.rule.Rule#getDuration()
     */
    public int getDuration() {
        return 60 * 30;   // 30 minutes
    }

    /**
     * * @see org.openmrs.logic.rule.Rule#getDatatype(String)
     */
    public Datatype getDatatype(String token) {
        return Datatype.TEXT;
    }

    /**
     * * @see org.openmrs.logic.rule.Rule#getParameterList()
     */
    public Set<RuleParameterInfo> getParameterList() {
        return null;
    }

    /**
     * * @see org.openmrs.logic.rule.Rule#getDependencies()
     */
    public String[] getDependencies() {
        return new String[]{};
    }

    /**
     * * @see org.openmrs.logic.rule.Rule#getTTL()
     */
    public int getTTL() {
        return 0; //60 * 30; // 30 minutes
    }

    /**
     * * @see org.openmrs.logic.rule.Rule#getDatatype(String)
     */
    public Datatype getDefaultDatatype() {
        return Datatype.CODED;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAuthor()
     */
    public String getAuthor() {
        return "Yan Jin";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getCitations()
     */
    public String getCitations() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getDate()
     */
    public String getDate() {
        return "2012-11-11";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getExplanation()
     */
    public String getExplanation() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getInstitution()
     */
    public String getInstitution() {
        return "SFSU";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getKeywords()
     */
    public String getKeywords() {
        return "Arden, Test";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getLinks()
     */
    public String getLinks() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getPurpose()
     */
    public String getPurpose() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getSpecialist()
     */
    public String getSpecialist() {
        return "Pediatrics";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getTitle()
     */
    public String getTitle() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getVersion()
     */
    public Double getVersion() {
        return 1.0;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getType()
     */
    public String getType() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getPriority()
     */
    public Integer getPriority() {
        return 10;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getData()
     */
    public String getData() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getLogic()
     */
    public String getLogic() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAction()
     */
    public String getAction() {
        return "write";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMin()
     */
    public Integer getAgeMin() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMinUnits()
     */
    public String getAgeMinUnits() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMax()
     */
    public Integer getAgeMax() {
        return null;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMaxUnits()
     */
    public String getAgeMaxUnits() {
        return null;
    }

    private static boolean containsIgnoreCase(Result key, List<Result> lst) {
        if (key == null) {
            return false;
        }
        String keyString = "";
        if (key.getDatatype() == Result.Datatype.CODED) {
            Concept keyConcept = key.toConcept();
            if (keyConcept != null) {
                keyString = ((ConceptName) keyConcept.getNames().toArray()[0]).getName();
            }
        } else {
            keyString = key.toString();
        }
        for (Result element : lst) {
            Concept concept = element.toConcept();
            if (concept == null) {
                continue;
            }
            String elementString = ((ConceptName) concept.getNames().toArray()[0]).getName();
            if (keyString.equalsIgnoreCase(elementString)) {
                return true;
            }
        }
        return false;
    }

    private static String toProperCase(String str) {

        if (str == null || str.length() < 1) {
            return str;
        }

        StringBuffer resultString = new StringBuffer();
        String delimiter = " ";
        StringTokenizer tokenizer = new StringTokenizer(str, delimiter, true);
        String currToken = null;

        while (tokenizer.hasMoreTokens()) {
            currToken = tokenizer.nextToken();
            if (!currToken.equals(delimiter)) {
                if (currToken.length() > 0) {
                    currToken = currToken.substring(0, 1).toUpperCase()
                            + currToken.substring(1).toLowerCase();
                }
            }
            resultString.append(currToken);
        }
        return resultString.toString();
    }

    public Result eval(LogicContext context, Integer patientId,
            Map<String, Object> parameters) throws LogicException {



        String actionStr = "";
        PatientService patientService = Context.getPatientService();
        Patient patient = patientService.getPatient(patientId);
        HashMap<String, Result> resultLookup = new HashMap<String, Result>();
        Boolean ageOK = null;

        try {
            RuleProvider ruleProvider = (RuleProvider) parameters.get("ruleProvider");
            HashMap<String, String> userVarMap = new HashMap<String, String>();
            String firstname = patient.getPersonName().getGivenName();
            userVarMap.put("firstname", toProperCase(firstname));
            String lastName = patient.getFamilyName();
            userVarMap.put("lastName", lastName);
            String gender = patient.getGender();
            userVarMap.put("Gender", gender);
            if (gender.equalsIgnoreCase("M")) {
                userVarMap.put("gender", "his");
                userVarMap.put("hisher", "his");
            } else {
                userVarMap.put("gender", "her");
                userVarMap.put("hisher", "her");
            }
            ArrayList<String> actions = initAction();


            if (evaluate_logic(parameters, context, ruleProvider, patient, userVarMap, resultLookup)) {
                Result ruleResult = new Result();

                for (String currAction : actions) {
                    currAction = doAction(currAction, userVarMap, resultLookup);
                    ruleResult.add(new Result(currAction));
                }
                return ruleResult;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.emptyResult();
        }
        return Result.emptyResult();
    }

    private boolean evaluate_logic(Map<String, Object> parameters, LogicContext context, RuleProvider ruleProvider, Patient patient, HashMap<String, String> userVarMap, HashMap<String, Result> resultLookup) throws LogicException {

        Result Gender = new Result(userVarMap.get("Gender"));

        return false;
    }

    public ArrayList<String> initAction() {


        ArrayList<String> actions = new ArrayList<String>();
        actions.add("Hello, It is Arden test!");


        return actions;
    }

    private String substituteString(String variable, String outStr, HashMap<String, String> userVarMap, HashMap<String, Result> resultLookup) {
        //see if the variable is in the user map
        String value = userVarMap.get(variable);
        if (value != null) {
        } // It must be a result value or date
        else if (variable.contains("_value")) {
            variable = variable.replace("_value", "").trim();
            if (resultLookup.get(variable) != null) {
                value = resultLookup.get(variable).toString();
            }
        } // It must be a result date
        else if (variable.contains("_date")) {
            String pattern = "MM/dd/yy";
            SimpleDateFormat dateForm = new SimpleDateFormat(pattern);
            variable = variable.replace("_date", "").trim();
            if (resultLookup.get(variable) != null) {
                value = dateForm.format(resultLookup.get(variable).getResultDate());
            }
        } else {
            if (resultLookup.get(variable) != null) {
                value = resultLookup.get(variable).toString();
            }
        }
        if (value != null) {
            outStr += value;
        }
        return outStr;
    }

    public String doAction(String inStr, HashMap<String, String> userVarMap, HashMap<String, Result> resultLookup) {


        int startindex = -1;
        int endindex = -1;
        int index = -1;
        String outStr = "";
        while ((index = inStr.indexOf("||")) > -1) {
            if (startindex == -1) {
                startindex = 0;
                outStr += inStr.substring(0, index);
            } else if (endindex == -1) {
                endindex = index - 1;
                String variable = inStr.substring(startindex, endindex).trim();
                outStr = substituteString(variable, outStr, userVarMap, resultLookup);
                startindex = -1;
                endindex = -1;
            }
            inStr = inStr.substring(index + 2);
        }
        outStr += inStr;
        return outStr;
    }
}