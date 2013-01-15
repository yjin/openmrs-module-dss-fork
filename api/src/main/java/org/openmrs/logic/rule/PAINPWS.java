/**
 * ******************************************************************
 * Translated from - PAINPWS.mlm on Wed Nov 07 09:53:26 PST 2012
 *
 * Title: Pain Identified Qualitiatve Pain PWS Assessment Filename: PAINPWS
 * Version: 1.0 Institution: Indiana University School of Medicine Author: Paul
 * Biondich Specialist: Pediatrics Date: 2005-04-28T11:37:28-0400 Validation :
 * Purpose: The purpose of this PWS is to respond to a patient who was
 * identified with pain on screening. A more granular quantification of the pain
 * is captured by the physician. Explanation: Keywords: pain, PWS Citations:
 * Links: ******************************************************************
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

public class PAINPWS implements MlmRule {

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
        return "2005-04-28T11:37:28-0400";
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
        return "pain, PWS";
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
        return "The purpose of this PWS is to respond to a patient who was identified with pain on screening. A more granular quantification of the pain is captured by the physician.";
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
        return "Pain Identified Qualitiatve Pain PWS Assessment";
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
        return "read read read read read read read If endif";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getLogic()
     */
    public String getLogic() {
        return "If If conclude conclude endif If If || If || If || If || If || If || If || If || If || If CALL endif If CALL conclude endif If CALL CALL endif If CALL CALL endif If CALL CALL endif If CALL CALL endif If CALL CALL endif If CALL CALL endif endif";
    }

    /**
     * * @see org.openmrs.arden.MlmRule#getAction()
     */
    public String getAction() {
        return "write write write write write write write";
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
        return 21;
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

                Result pain = context.read(
                        patient.getPatientId(), context.getLogicDataSource("CHICA"),
                        new LogicCriteriaImpl("PAIN QUALITATIVE").within(Duration.days(-2)).last());
                resultLookup.put("pain", pain);
            }

            if (evaluate_logic(parameters, context, ruleProvider, patient, userVarMap, resultLookup)) {
                Result ruleResult = new Result();
                Result pain = (Result) resultLookup.get("pain");

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
        Result pain = (Result) resultLookup.get("pain");
        Result Box5 = (Result) resultLookup.get("Box5");
        Result Box6 = (Result) resultLookup.get("Box6");
        Result Box3 = (Result) resultLookup.get("Box3");
        Result Box4 = (Result) resultLookup.get("Box4");
        Result mode = (Result) resultLookup.get("mode");

        Object value = null;
        String variable = null;
        int varLen = 0;
        if ((!mode.isNull() && mode.toString().equalsIgnoreCase("PRODUCE"))) {
            if ((!pain.isNull() && pain.toString().equalsIgnoreCase("yes"))) {
                return true;
            }
            return false;
        }
        if ((!mode.isNull() && mode.toString().equalsIgnoreCase("CONSUME"))) {
            if ((!Gender.isNull() && Gender.toString().equalsIgnoreCase("M"))) {
                //preprocess any || operator ;
                String val = doAction("he", userVarMap, resultLookup);
                userVarMap.put("heshe", val);
            }
            if ((!Gender.isNull() && Gender.toString().equalsIgnoreCase("F"))) {
                //preprocess any || operator ;
                String val = doAction("she", userVarMap, resultLookup);
                userVarMap.put("heshe", val);
            }
            if ((!Box1.isNull() && Box1.toString().equalsIgnoreCase("true"))
                    || (!Box2.isNull() && Box2.toString().equalsIgnoreCase("true"))
                    || (!Box3.isNull() && Box3.toString().equalsIgnoreCase("true"))
                    || (!Box4.isNull() && Box4.toString().equalsIgnoreCase("true"))
                    || (!Box5.isNull() && Box5.toString().equalsIgnoreCase("true"))
                    || (!Box6.isNull() && Box6.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("According to information collected today on screening, || firstname || seemed to be in pain.", userVarMap, resultLookup);
                userVarMap.put("description", val);
            }
            if ((!Box1.isNull() && Box1.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("I confirmed || hisher || pain score was 1-2.", userVarMap, resultLookup);
                userVarMap.put("note1", val);
            }
            if ((!Box2.isNull() && Box2.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("I confirmed || hisher || pain score was 3-4.", userVarMap, resultLookup);
                userVarMap.put("note2", val);
            }
            if ((!Box3.isNull() && Box3.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("I confirmed || hisher || pain score was 5-6.", userVarMap, resultLookup);
                userVarMap.put("note3", val);
            }
            if ((!Box4.isNull() && Box4.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("I confirmed || hisher || pain score was 7-8.", userVarMap, resultLookup);
                userVarMap.put("note4", val);
            }
            if ((!Box5.isNull() && Box5.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("I confirmed || hisher || pain score was 9-10.", userVarMap, resultLookup);
                userVarMap.put("note5", val);
            }
            if ((!Box6.isNull() && Box6.toString().equalsIgnoreCase("true"))) {
                //preprocess any || operator ;
                String val = doAction("I confirmed || heshe || was not in pain, though.", userVarMap, resultLookup);
                userVarMap.put("note6", val);
            }
            if ((!Box1.isNull() && Box1.toString().equalsIgnoreCase("true"))
                    || (!Box2.isNull() && Box2.toString().equalsIgnoreCase("true"))
                    || (!Box3.isNull() && Box3.toString().equalsIgnoreCase("true"))
                    || (!Box4.isNull() && Box4.toString().equalsIgnoreCase("true"))
                    || (!Box5.isNull() && Box5.toString().equalsIgnoreCase("true"))
                    || (!Box6.isNull() && Box6.toString().equalsIgnoreCase("true"))) {
                varLen = "description".length();
                value = userVarMap.get("description");
                if (value != null) {
                    parameters.put("param1", value);
                } // It must be a result value or date
                else if ("description".endsWith("_value")) {
                    variable = "description".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("description".endsWith("_date")) {
                    variable = "description".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("description".endsWith("_object")) {
                    variable = "description".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("description") != null) {
                        value = resultLookup.get("description").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param1", value);
                } else {
                    parameters.put("param1", "description");
                }
                varLen = "CHICA NOTES".length();
                value = userVarMap.get("CHICA NOTES");
                if (value != null) {
                    parameters.put("param2", value);
                } // It must be a result value or date
                else if ("CHICA NOTES".endsWith("_value")) {
                    variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("CHICA NOTES".endsWith("_date")) {
                    variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("CHICA NOTES".endsWith("_object")) {
                    variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("CHICA NOTES") != null) {
                        value = resultLookup.get("CHICA NOTES").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param2", value);
                } else {
                    parameters.put("param2", "CHICA NOTES");
                }
                if (ruleProvider != null) {
                    ruleProvider.getRule("storeNote");
                }
                context.eval(patient.getPatientId(), "storeNote", parameters);
            }
            if ((!Box1.isNull() && Box1.toString().equalsIgnoreCase("true"))
                    && (!Box2.isNull() && Box2.toString().equalsIgnoreCase("true"))
                    && (!Box3.isNull() && Box3.toString().equalsIgnoreCase("true"))
                    && (!Box4.isNull() && Box4.toString().equalsIgnoreCase("true"))
                    && (!Box5.isNull() && Box5.toString().equalsIgnoreCase("true"))
                    && (!Box6.isNull() && Box6.toString().equalsIgnoreCase("true"))) {
                varLen = "PWS READ ERROR".length();
                value = userVarMap.get("PWS READ ERROR");
                if (value != null) {
                    parameters.put("param1", value);
                } // It must be a result value or date
                else if ("PWS READ ERROR".endsWith("_value")) {
                    variable = "PWS READ ERROR".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("PWS READ ERROR".endsWith("_date")) {
                    variable = "PWS READ ERROR".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("PWS READ ERROR".endsWith("_object")) {
                    variable = "PWS READ ERROR".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("PWS READ ERROR") != null) {
                        value = resultLookup.get("PWS READ ERROR").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param1", value);
                } else {
                    parameters.put("param1", "PWS READ ERROR");
                }
                varLen = "MISSCAN".length();
                value = userVarMap.get("MISSCAN");
                if (value != null) {
                    parameters.put("param2", value);
                } // It must be a result value or date
                else if ("MISSCAN".endsWith("_value")) {
                    variable = "MISSCAN".substring(0, varLen - 6); // -6 for _value
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).toString();
                    }
                } else if ("MISSCAN".endsWith("_date")) {
                    variable = "MISSCAN".substring(0, varLen - 5); // -5 for _date
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable).getResultDate().toString();
                    }
                } else if ("MISSCAN".endsWith("_object")) {
                    variable = "MISSCAN".substring(0, varLen - 7); // -5 for _object
                    if (resultLookup.get(variable) != null) {
                        value = resultLookup.get(variable);
                    }
                } else {
                    if (resultLookup.get("MISSCAN") != null) {
                        value = resultLookup.get("MISSCAN").toString();
                    }
                }
                if (value != null) {
                    parameters.put("param2", value);
                } else {
                    parameters.put("param2", "MISSCAN");
                }
                if (ruleProvider != null) {
                    ruleProvider.getRule("storeObs");
                }
                context.eval(patient.getPatientId(), "storeObs", parameters);
                return false;
            }
        }
        if ((!Box1.isNull() && Box1.toString().equalsIgnoreCase("true"))) {
            varLen = "PAIN QUANTITATIVE".length();
            value = userVarMap.get("PAIN QUANTITATIVE");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("PAIN QUANTITATIVE".endsWith("_value")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_date")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_object")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("PAIN QUANTITATIVE") != null) {
                    value = resultLookup.get("PAIN QUANTITATIVE").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "PAIN QUANTITATIVE");
            }
            varLen = "1-2".length();
            value = userVarMap.get("1-2");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("1-2".endsWith("_value")) {
                variable = "1-2".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("1-2".endsWith("_date")) {
                variable = "1-2".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("1-2".endsWith("_object")) {
                variable = "1-2".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("1-2") != null) {
                    value = resultLookup.get("1-2").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "1-2");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeObs");
            }
            context.eval(patient.getPatientId(), "storeObs", parameters);
            varLen = "note1".length();
            value = userVarMap.get("note1");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("note1".endsWith("_value")) {
                variable = "note1".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("note1".endsWith("_date")) {
                variable = "note1".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("note1".endsWith("_object")) {
                variable = "note1".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("note1") != null) {
                    value = resultLookup.get("note1").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "note1");
            }
            varLen = "CHICA NOTES".length();
            value = userVarMap.get("CHICA NOTES");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("CHICA NOTES".endsWith("_value")) {
                variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("CHICA NOTES".endsWith("_date")) {
                variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("CHICA NOTES".endsWith("_object")) {
                variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("CHICA NOTES") != null) {
                    value = resultLookup.get("CHICA NOTES").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "CHICA NOTES");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeNote");
            }
            context.eval(patient.getPatientId(), "storeNote", parameters);
        }
        if ((!Box2.isNull() && Box2.toString().equalsIgnoreCase("true"))) {
            varLen = "PAIN QUANTITATIVE".length();
            value = userVarMap.get("PAIN QUANTITATIVE");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("PAIN QUANTITATIVE".endsWith("_value")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_date")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_object")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("PAIN QUANTITATIVE") != null) {
                    value = resultLookup.get("PAIN QUANTITATIVE").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "PAIN QUANTITATIVE");
            }
            varLen = "3-4".length();
            value = userVarMap.get("3-4");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("3-4".endsWith("_value")) {
                variable = "3-4".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("3-4".endsWith("_date")) {
                variable = "3-4".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("3-4".endsWith("_object")) {
                variable = "3-4".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("3-4") != null) {
                    value = resultLookup.get("3-4").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "3-4");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeObs");
            }
            context.eval(patient.getPatientId(), "storeObs", parameters);
            varLen = "note2".length();
            value = userVarMap.get("note2");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("note2".endsWith("_value")) {
                variable = "note2".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("note2".endsWith("_date")) {
                variable = "note2".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("note2".endsWith("_object")) {
                variable = "note2".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("note2") != null) {
                    value = resultLookup.get("note2").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "note2");
            }
            varLen = "CHICA NOTES".length();
            value = userVarMap.get("CHICA NOTES");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("CHICA NOTES".endsWith("_value")) {
                variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("CHICA NOTES".endsWith("_date")) {
                variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("CHICA NOTES".endsWith("_object")) {
                variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("CHICA NOTES") != null) {
                    value = resultLookup.get("CHICA NOTES").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "CHICA NOTES");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeNote");
            }
            context.eval(patient.getPatientId(), "storeNote", parameters);
        }
        if ((!Box3.isNull() && Box3.toString().equalsIgnoreCase("true"))) {
            varLen = "PAIN QUANTITATIVE".length();
            value = userVarMap.get("PAIN QUANTITATIVE");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("PAIN QUANTITATIVE".endsWith("_value")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_date")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_object")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("PAIN QUANTITATIVE") != null) {
                    value = resultLookup.get("PAIN QUANTITATIVE").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "PAIN QUANTITATIVE");
            }
            varLen = "5-6".length();
            value = userVarMap.get("5-6");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("5-6".endsWith("_value")) {
                variable = "5-6".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("5-6".endsWith("_date")) {
                variable = "5-6".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("5-6".endsWith("_object")) {
                variable = "5-6".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("5-6") != null) {
                    value = resultLookup.get("5-6").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "5-6");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeObs");
            }
            context.eval(patient.getPatientId(), "storeObs", parameters);
            varLen = "note3".length();
            value = userVarMap.get("note3");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("note3".endsWith("_value")) {
                variable = "note3".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("note3".endsWith("_date")) {
                variable = "note3".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("note3".endsWith("_object")) {
                variable = "note3".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("note3") != null) {
                    value = resultLookup.get("note3").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "note3");
            }
            varLen = "CHICA NOTES".length();
            value = userVarMap.get("CHICA NOTES");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("CHICA NOTES".endsWith("_value")) {
                variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("CHICA NOTES".endsWith("_date")) {
                variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("CHICA NOTES".endsWith("_object")) {
                variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("CHICA NOTES") != null) {
                    value = resultLookup.get("CHICA NOTES").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "CHICA NOTES");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeNote");
            }
            context.eval(patient.getPatientId(), "storeNote", parameters);
        }
        if ((!Box4.isNull() && Box4.toString().equalsIgnoreCase("true"))) {
            varLen = "PAIN QUANTITATIVE".length();
            value = userVarMap.get("PAIN QUANTITATIVE");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("PAIN QUANTITATIVE".endsWith("_value")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_date")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_object")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("PAIN QUANTITATIVE") != null) {
                    value = resultLookup.get("PAIN QUANTITATIVE").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "PAIN QUANTITATIVE");
            }
            varLen = "7-8".length();
            value = userVarMap.get("7-8");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("7-8".endsWith("_value")) {
                variable = "7-8".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("7-8".endsWith("_date")) {
                variable = "7-8".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("7-8".endsWith("_object")) {
                variable = "7-8".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("7-8") != null) {
                    value = resultLookup.get("7-8").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "7-8");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeObs");
            }
            context.eval(patient.getPatientId(), "storeObs", parameters);
            varLen = "note4".length();
            value = userVarMap.get("note4");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("note4".endsWith("_value")) {
                variable = "note4".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("note4".endsWith("_date")) {
                variable = "note4".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("note4".endsWith("_object")) {
                variable = "note4".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("note4") != null) {
                    value = resultLookup.get("note4").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "note4");
            }
            varLen = "CHICA NOTES".length();
            value = userVarMap.get("CHICA NOTES");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("CHICA NOTES".endsWith("_value")) {
                variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("CHICA NOTES".endsWith("_date")) {
                variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("CHICA NOTES".endsWith("_object")) {
                variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("CHICA NOTES") != null) {
                    value = resultLookup.get("CHICA NOTES").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "CHICA NOTES");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeNote");
            }
            context.eval(patient.getPatientId(), "storeNote", parameters);
        }
        if ((!Box5.isNull() && Box5.toString().equalsIgnoreCase("true"))) {
            varLen = "PAIN QUANTITATIVE".length();
            value = userVarMap.get("PAIN QUANTITATIVE");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("PAIN QUANTITATIVE".endsWith("_value")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_date")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_object")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("PAIN QUANTITATIVE") != null) {
                    value = resultLookup.get("PAIN QUANTITATIVE").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "PAIN QUANTITATIVE");
            }
            varLen = "9-10".length();
            value = userVarMap.get("9-10");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("9-10".endsWith("_value")) {
                variable = "9-10".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("9-10".endsWith("_date")) {
                variable = "9-10".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("9-10".endsWith("_object")) {
                variable = "9-10".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("9-10") != null) {
                    value = resultLookup.get("9-10").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "9-10");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeObs");
            }
            context.eval(patient.getPatientId(), "storeObs", parameters);
            varLen = "note5".length();
            value = userVarMap.get("note5");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("note5".endsWith("_value")) {
                variable = "note5".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("note5".endsWith("_date")) {
                variable = "note5".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("note5".endsWith("_object")) {
                variable = "note5".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("note5") != null) {
                    value = resultLookup.get("note5").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "note5");
            }
            varLen = "CHICA NOTES".length();
            value = userVarMap.get("CHICA NOTES");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("CHICA NOTES".endsWith("_value")) {
                variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("CHICA NOTES".endsWith("_date")) {
                variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("CHICA NOTES".endsWith("_object")) {
                variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("CHICA NOTES") != null) {
                    value = resultLookup.get("CHICA NOTES").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "CHICA NOTES");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeNote");
            }
            context.eval(patient.getPatientId(), "storeNote", parameters);
        }
        if ((!Box6.isNull() && Box6.toString().equalsIgnoreCase("true"))) {
            varLen = "PAIN QUANTITATIVE".length();
            value = userVarMap.get("PAIN QUANTITATIVE");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("PAIN QUANTITATIVE".endsWith("_value")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_date")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("PAIN QUANTITATIVE".endsWith("_object")) {
                variable = "PAIN QUANTITATIVE".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("PAIN QUANTITATIVE") != null) {
                    value = resultLookup.get("PAIN QUANTITATIVE").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "PAIN QUANTITATIVE");
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
            varLen = "note6".length();
            value = userVarMap.get("note6");
            if (value != null) {
                parameters.put("param1", value);
            } // It must be a result value or date
            else if ("note6".endsWith("_value")) {
                variable = "note6".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("note6".endsWith("_date")) {
                variable = "note6".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("note6".endsWith("_object")) {
                variable = "note6".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("note6") != null) {
                    value = resultLookup.get("note6").toString();
                }
            }
            if (value != null) {
                parameters.put("param1", value);
            } else {
                parameters.put("param1", "note6");
            }
            varLen = "CHICA NOTES".length();
            value = userVarMap.get("CHICA NOTES");
            if (value != null) {
                parameters.put("param2", value);
            } // It must be a result value or date
            else if ("CHICA NOTES".endsWith("_value")) {
                variable = "CHICA NOTES".substring(0, varLen - 6); // -6 for _value
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).toString();
                }
            } else if ("CHICA NOTES".endsWith("_date")) {
                variable = "CHICA NOTES".substring(0, varLen - 5); // -5 for _date
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable).getResultDate().toString();
                }
            } else if ("CHICA NOTES".endsWith("_object")) {
                variable = "CHICA NOTES".substring(0, varLen - 7); // -5 for _object
                if (resultLookup.get(variable) != null) {
                    value = resultLookup.get(variable);
                }
            } else {
                if (resultLookup.get("CHICA NOTES") != null) {
                    value = resultLookup.get("CHICA NOTES").toString();
                }
            }
            if (value != null) {
                parameters.put("param2", value);
            } else {
                parameters.put("param2", "CHICA NOTES");
            }
            if (ruleProvider != null) {
                ruleProvider.getRule("storeNote");
            }
            context.eval(patient.getPatientId(), "storeNote", parameters);
        }
        return false;
    }

    public ArrayList<String> initAction() {
        ArrayList<String> actions = new ArrayList<String>();
        actions.add("* ATTENTION *  According to information collected today on screening, || firstname || seems to be in pain.  Please rate pain on a scale of 1-10 below and counsel appropriately:");
        actions.add("Score: 1 - 2");
        actions.add("Score: 3 - 4");
        actions.add("Score: 5 - 6");
        actions.add("Score: 7 - 8");
        actions.add("Score: 9 - 10");
        actions.add("Not in pain");


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