package org.openmrs.module.dss.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class RuleMapperController extends SimpleFormController {

    protected final Log log = LogFactory.getLog(getClass());

    /*
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception {
        return "testing";
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        // DssService
        DssService dssService = Context.getService(DssService.class);

        // get rule name
        String ruleName = request.getParameter("ruleName");
        String mrn = request.getParameter("mrn");

        // get rule id
        Integer ruleId = null;
        if (request.getParameter("ruleId") != null) {
            try {
                ruleId = Integer.parseInt(request.getParameter("ruleId"));
            } catch (NumberFormatException e) {
            }
        }

        // collect concept ids
        ArrayList<Integer> conceptIds = new ArrayList<Integer>();
        if (request.getParameter("conceptIds") != null) {
            String[] cIds = request.getParameter("conceptIds").split(",");
            for (String id : cIds) {
                try {
                    conceptIds.add(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                }
            }
        }

        // do mapping
        if (ruleId != null && !conceptIds.isEmpty()) {
            System.out.print("Mapping result: ");
            System.out.println(mapRuleToConcepts(ruleId, conceptIds));
        }

        // get current mappings

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ruleName", ruleName);
        map.put("mrn", mrn);
        map.put("lastMRN", mrn);

        // get list of rules for the UI
        List<Rule> rules = dssService.getRules(new Rule(), true, true, "tokenName");
        map.put("rules", rules);

        // get all concepts mapped
        HashMap<Concept, List<Rule>> rulesByConcept = new HashMap<Concept, List<Rule>>();
        for (Rule rule : rules) {
            for (Concept c : dssService.getMappings(rule)) {
                rulesByConcept.put(c, null);
            }
        }

        for (Concept concept : rulesByConcept.keySet()) {
            rulesByConcept.put(concept, dssService.getMappings(concept));
            System.out.println(concept);
            System.out.println(rulesByConcept.get(concept).size());
            System.out.println(rulesByConcept.get(concept));
        }

        // mappings.add(rule.getTokenName() + " -> " + c.getBestShortName(Locale.ENGLISH));

        System.out.println(rulesByConcept);
        System.out.println(rulesByConcept.size());
        System.out.println(rulesByConcept.entrySet().size());
        map.put("mappings", rulesByConcept);


        return map;
    }

    private boolean mapRuleToConcepts(Integer ruleId, List<Integer> conceptIds) {
        DssService dssService = Context.getService(DssService.class);

        // get rule
        Rule r = dssService.getRule(ruleId);
        if (r == null) {
            return false;
        }

        // get concepts
        Concept c;
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        for (Integer id : conceptIds) {
            c = Context.getConceptService().getConcept(id);
            if (c != null) {
                concepts.add(c);
            }
        }

        if (concepts.isEmpty()) {
            return false;
        }

        return dssService.addMapping(r, concepts);
    }
}
