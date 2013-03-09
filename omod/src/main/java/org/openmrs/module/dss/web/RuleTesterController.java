package org.openmrs.module.dss.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * @author tmdugan
 *
 */
public class RuleTesterController extends SimpleFormController {

    protected final Log log = LogFactory.getLog(getClass());

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception {
        return "testing";
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        DssService dssService = Context
                .getService(DssService.class);
     
        String mode = request.getParameter("mode");
        if(mode == null){
            mode = "PRODUCE";
        }
        map.put("mode", mode);
       
        String ruleName = request.getParameter("ruleName");
        String mrn = request.getParameter("mrn");
 //       Integer encounterId = Integer.parseInt(request.getParameter("encounterId"));

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        // default mode
        parameters.put("mode", "PRODUCE");

        Set<String> keys = request.getParameterMap().keySet();
        for (String key : keys) {
            parameters.put(key, (String) request.getParameter(key));
        }

        map.put("ruleName", ruleName);
        map.put("mrn", mrn);
        map.put("lastMRN", mrn);
        map.put("progress", 0);

        if (mrn != null) {
            try {
                PatientService patientService = Context.getPatientService();
                List<Patient> patients = patientService
                        .getPatientsByIdentifier(mrn, false);
                Patient patient = null;
                if (patients != null && patients.size() > 0) {
                    patient = patients.get(0);
                    map.put("patient", patient);
                } else {
                    map.put("patient", "NULL");
                }
                if (patient != null) {
                    Rule rule = new Rule();
                    rule.setTokenName(ruleName);
                    List<Rule> rules = dssService.getRules(rule, false, false,
                            null);
                    Rule currRule = null;
                    map.put("numberOfRules", rules.size());
                    if (rules.size() > 0) {
                        currRule = rules.get(0);
                    }
                    map.put("ruleToEvaluate", currRule);
                    map.put("ageRestriction", currRule.checkAgeRestrictions(patient));

                    map.put("progress", 1);
                    if (currRule != null
                            && currRule.checkAgeRestrictions(patient)) {
                        map.put("progress", 2);
                        currRule.setParameters(parameters);
                        Result result = dssService.runRule(patient, currRule);
                        // result.add(new Result("DIEGO"));
                        map.put("progress", 3);

                        if (result == null) {
                            map.put("resultIsNull", true);
                        } else {
                            map.put("resultIsNull", false);
                        }

                        map.put("progress", 4);
                        if (result.size() < 2) {
                            map.put("resultSize", result.size());
                            map.put("runResult", result.toString());
                        } else {
                            map.put("resultSize", result.size());
                            String resultString = "";
                            for (Result currResult : result) {
                                resultString += currResult.toString();
                                resultString += "<br/><br/>";
                            }
                            map.put("runResult", resultString);
                        }
                    }
                }

            } catch (Exception e) {
                this.log.error(e.getMessage());
                this.log.error(org.openmrs.module.dss.util.Util.getStackTrace(e));
            }
        }
        if (ruleName != null && ruleName.length() > 0) {
            map.put("lastRuleName", ruleName);
        }

        List<Rule> rules = dssService.getRules(new Rule(), true, true,
                "tokenName");

        map.put("rules", rules);
        map.put("debug", "diego");

        return map;
    }
}
