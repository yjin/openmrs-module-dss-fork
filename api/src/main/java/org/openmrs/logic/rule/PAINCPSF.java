/**
 * ******************************************************************
 * Translated from - PAINCPSF.mlm on Wed Nov 07 09:53:27 PST 2012
 *
 * Title: Pain Qualitative Child PSF Assessment Filename: PAINCPSF Version: 1.0
 * Institution: Indiana University School of Medicine Author: Paul Biondich
 * Specialist: Pediatrics Date: 2005-04-28T01:15:12-0400 Validation : Purpose:
 * The purpose of this PSF is to screen children for the presence of any pain.
 * Positive answers trigger a physician prompt that collects more detail.
 * Explanation: Keywords: pain, PSF, child Citations: Links: 
*******************************************************************
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

import org.openmrs.api.ConceptService;
import java.text.SimpleDateFormat;
import org.openmrs.Concept;
import org.openmrs.ConceptName;

public class PAINCPSF implements MlmRule {

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
        return "Paul Biondich";
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
        return "2005-04-28T01:15:12-0400";
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
        return "Indiana University School of Medicine";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getKeywords()
     */
    public String getKeywords() {
        return "pain, PSF, child";
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
        return "The purpose of this PSF is to screen children for the presence of any pain. Positive answers trigger a physician prompt that collects more detail.";
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
        return "Pain Qualitative Child PSF Assessment";
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
        return 0;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getData()
     */
    public String getData() {
        return "read read read read read read read If";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getLogic()
     */
    public String getLogic() {
        return "If conclude endif If If CALL endif If CALL CALL endif endif";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAction()
     */
    public String getAction() {
        return "write write";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMin()
     */
    public Integer getAgeMin() {
        return 0;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMinUnits()
     */
    public String getAgeMinUnits() {
        return "days";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMax()
     */
    public Integer getAgeMax() {
        return 12;
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAgeMaxUnits()
     */
    public String getAgeMaxUnits() {
        return "years";
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

            Result mode = new Result((String) parameters.get("mode"));
            resultLookup.put("mode", mode);
            Result Box1 = new Result((String) parameters.get("box1"));
            resultLookup.put("Box1", Box1);
            Result Box2 = new Result((String) parameters.get("box2"));
            resultLookup.put("Box2", Box2);
            Result Box3 = new Result((String) parameters.get("box3"));
            resultLookup.put("Box3", Box3);
            Result Box4 = new Result((String) parameters.get("box4"));
            resultLookup.put("Box4", Box4);
            Result Box5 = new Result((String) parameters.get("box5"));
            resultLookup.put("Box5", Box5);
            Result Box6 = new Result((String) parameters.get("box6"));
            resultLookup.put("Box6", Box6);
            if ((!mode.isNull() && mode.toString().equalsIgnoreCase("PRODUCE"))) {
            }

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
        Result Box1 = (Result) resultLookup.get("Box1");
        Result Box2 = (Result) resultLookup.get("Box2");
        Result Box5 = (Result) resultLookup.get("Box5");
        Result Box6 = (Result) resultLookup.get("Box6");
        Result Box3 = (Result) resultLookup.get("Box3");
        Result Box4 = (Result) resultLookup.get("Box4");
        Result mode = (Result) resultLookup.get("mode");

        Object value = null;
        String variable = null;
        int varLen = 0;
        if ((!mode.isNull() && mode.toString().equalsIgnoreCase("PRODUCE"))) {
            return true;
        }
        if ((!mode.isNull() && mode.toString().equalsIgnoreCase("CONSUME"))) {
            if ((!Box1.isNull() && Box1.toString().equalsIgnoreCase("true"))) {
                varLen = "PAIN QUALITATIVE".length();
                value = userVarMap.get("PAIN QUALITATIVE");
                if (value != null) {
                    parameters.put("param1", value);
                } // It must be a result value or date
                else if ("PAIN QUALITATIVE".endsWith("_value")) {
                    variable = "PAIN QUALITATIVE".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("PAIN QUALITATIVE".endsWith("_date")) {
                    variable = "PAIN QUALITATIVE".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("PAIN QUALITATIVE".endsWith("_object")) {
                    variable = "PAIN QUALITATIVE".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("PAIN QUALITATIVE") != null) {
                        value = resultLookup.get("PAIN QUALITATIVE").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param1", value);
                } else {
                    parameters.put("param1", "PAIN QUALITATIVE");
                }
                varLen = "yes".length();
                value = userVarMap.get("yes");
                if (value != null) {
                    parameters.put("param2", value);
                } // It must be a result value or date
                else if ("yes".endsWith("_value")) {
                    variable = "yes".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("yes".endsWith("_date")) {
                    variable = "yes".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("yes".endsWith("_object")) {
                    variable = "yes".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("yes") != null) {
                        value = resultLookup.get("yes").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param2", value);
                } else {
                    parameters.put("param2", "yes");
                }
                if (ruleProvider != null) {
                    ruleProvider.getRule("storeObs");
                }
                context.eval(patient.getPatientId(), "storeObs", parameters);
            }
            if ((!Box2.isNull() && Box2.toString().equalsIgnoreCase("true"))) {
                varLen = "PAIN QUALITATIVE".length();
                value = userVarMap.get("PAIN QUALITATIVE");
                if (value != null) {
                    parameters.put("param1", value);
                } // It must be a result value or date
                else if ("PAIN QUALITATIVE".endsWith("_value")) {
                    variable = "PAIN QUALITATIVE".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("PAIN QUALITATIVE".endsWith("_date")) {
                    variable = "PAIN QUALITATIVE".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("PAIN QUALITATIVE".endsWith("_object")) {
                    variable = "PAIN QUALITATIVE".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("PAIN QUALITATIVE") != null) {
                        value = resultLookup.get("PAIN QUALITATIVE").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param1", value);
                } else {
                    parameters.put("param1", "PAIN QUALITATIVE");
                }
                varLen = "no".length();
                value = userVarMap.get("no");
                if (value != null) {
                    parameters.put("param2", value);
                } // It must be a result value or date
                else if ("no".endsWith("_value")) {
                    variable = "no".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("no".endsWith("_date")) {
                    variable = "no".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("no".endsWith("_object")) {
                    variable = "no".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("no") != null) {
                        value = resultLookup.get("no").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param2", value);
                } else {
                    parameters.put("param2", "no");
                }
                if (ruleProvider != null) {
                    ruleProvider.getRule("storeObs");
                }
                context.eval(patient.getPatientId(), "storeObs", parameters);
                varLen = "PAIN QUALITATIVE CAREWEB".length();
                value = userVarMap.get("PAIN QUALITATIVE CAREWEB");
                if (value != null) {
                    parameters.put("param1", value);
                } // It must be a result value or date
                else if ("PAIN QUALITATIVE CAREWEB".endsWith("_value")) {
                    variable = "PAIN QUALITATIVE CAREWEB".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("PAIN QUALITATIVE CAREWEB".endsWith("_date")) {
                    variable = "PAIN QUALITATIVE CAREWEB".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("PAIN QUALITATIVE CAREWEB".endsWith("_object")) {
                    variable = "PAIN QUALITATIVE CAREWEB".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("PAIN QUALITATIVE CAREWEB") != null) {
                        value = resultLookup.get("PAIN QUALITATIVE CAREWEB").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param1", value);
                } else {
                    parameters.put("param1", "PAIN QUALITATIVE CAREWEB");
                }
                varLen = "0".length();
                value = userVarMap.get("0");
                if (value != null) {
                    parameters.put("param2", value);
                } // It must be a result value or date
                else if ("0".endsWith("_value")) {
                    variable = "0".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("0".endsWith("_date")) {
                    variable = "0".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("0".endsWith("_object")) {
                    variable = "0".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("0") != null) {
                        value = resultLookup.get("0").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param2", value);
                } else {
                    parameters.put("param2", "0");
                }
                if (ruleProvider != null) {
                    ruleProvider.getRule("storeObs");
                }
                context.eval(patient.getPatientId(), "storeObs", parameters);
            }
        }
        return false;
    }

    public ArrayList<String> initAction() {
        ArrayList<String> actions = new ArrayList<String>();
        actions.add("Is || firstname || having pain today?");
        actions.add("ÀTiene || firstname || dolor hoy?@Spanish");


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