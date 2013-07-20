package org.openmrs.module.dss.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.arden.MlmRule;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.impl.LogicCriteriaImpl;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.token.TokenService;
import org.openmrs.module.dss.CompilingClassLoader;
import org.openmrs.module.dss.DssRuleProvider;
import org.openmrs.module.dss.db.DssDAO;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.dss.util.IOUtil;
import org.openmrs.module.dss.util.Util;

/**
 * Defines implementations of services used by this module
 *
 * @author Tammy Dugan
 */
public class DssServiceImpl implements DssService {

    private Log log = LogFactory.getLog(this.getClass());
    private DssDAO dao;
    private static Map<String, org.openmrs.logic.Rule> loadedRuleMap = new HashMap<String, org.openmrs.logic.Rule>();

    /**
     * Empty constructor
     */
    public DssServiceImpl() {
    }

    /**
     * @return DssDAO
     */
    public DssDAO getDssDAO() {
        return this.dao;
    }

    /**
     * Sets the DAO for this service. The dao allows interaction with the database.
     *
     * @param dao
     */
    public void setDssDAO(DssDAO dao) {
        this.dao = dao;
    }

    @Override
    public String runRulesAsString(Patient p, List<Rule> ruleList) {
        ArrayList<Result> results = this.runRules(p, ruleList);
        String reply = "";

        if (results == null || results.isEmpty()) {
            return "No rules run!!!!";
        }

        for (Result result : results) {
            if (result != null) {
                reply += result + "\n";
            }
        }
        return reply;
    }

    @Override
    public Result runRule(Patient p, Rule rule) {
        log.info("Running rule...");
        ArrayList<Rule> ruleList = new ArrayList<Rule>();
        ruleList.add(rule);

        ArrayList<Result> results = this.runRules(p, ruleList);

        //Since we ran only one rule, we will only have
        //one result object in the list of returned results
        //the single result object could have multiple results
        //depending on how the single rule was evaluated
        if (results != null && results.size() > 0) {
            return results.get(0);
        }

        return Result.emptyResult();
    }

    @Override
    public ArrayList<Result> runRules(Patient patient, List<Rule> ruleList) {
        log.info("Running rules...");
        ArrayList<Result> results = new ArrayList<Result>();
        Map<String, Object> parameters;
        String ruleName = null;

        log.info("Going to get the LogicService...");
        LogicService logicSvc = Context.getLogicService();

        log.info("Going to instantiate a DssRuleProvider...");
        DssRuleProvider ruleProvider = new DssRuleProvider();
        String threadName = Thread.currentThread().getName();


        try {
            for (Rule rule : ruleList) {

                // check if we have a valid rule
                if (rule == null) {
                    log.warn("Attempted to run a NULL Rule");
                    continue;
                }

                log.debug("Rule data:" + " title=" + rule.getTitle() + " ruleType=" + rule.getRuleType() + " toString="
                        + rule.toString() + " tokenName=" + rule.getTokenName());

                // check if patient is within age restrictions
                if (rule.checkAgeRestrictions(patient) == false) {
                    log.info("Skipping rule " + rule.getTitle() + " because patient is not within age restriction.");
                    continue;
                }

                ruleName = rule.getTokenName();

                // prepare parameters
                parameters = rule.getParameters();
                if (parameters == null) {
                    parameters = new HashMap<String, Object>();
                }
                if (!parameters.containsKey("mode")) {
                    parameters.put("mode", "PRODUCE");
                }
                parameters.put("ruleProvider", ruleProvider);

                // load rule
                try {
                    this.loadRule(ruleName, false);
                } catch (APIAuthenticationException e) {
                    // ignore a privilege exception
                    log.warn("There was a privilege exception when attempting to load rule " + ruleName);
                } catch (Exception e1) {
                    log.error("Error loading rule: " + ruleName);
                    log.error(e1.getMessage());
                    log.error(Util.getStackTrace(e1));
                    results.add(null);
                    continue;
                }

                long startTime = System.currentTimeMillis();
                Result result = null;

                // run rule
                try {
                    log.info("Going to evaluate the rule");
                    String serializedParams = "";
                    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                        serializedParams += entry.getKey() + "=" + entry.getValue() + "; ";
                    }
                    log.info("Parameters are: " + serializedParams);
                    result = logicSvc.eval(patient.getPatientId(), new LogicCriteriaImpl(ruleName), parameters);
                    results.add(result);
                } catch (APIAuthenticationException e) {
                    //ignore a privilege exception
                } catch (Exception e) {
                    log.error("Error evaluating rule: " + ruleName);
                    log.error(e.getMessage());
                    log.error(Util.getStackTrace(e));
                    results.add(null);
                    continue;
                }

                if (result != null) {
                    // print result with actions (debug only)
                    StringBuilder sb = new StringBuilder();
                    sb.append("Result data:");
                    sb.append(" size=").append(result.size());
                    sb.append(" toString=").append(result.toString());
                    sb.append(" actions=");
                    for (Result currResult : result) {
                        sb.append(currResult.toString()).append("|");
                        results.add(result);
                    }
                    log.debug(sb.toString());
                }

                long elapsedTime = System.currentTimeMillis() - startTime;

                if (elapsedTime > 100) {
                    System.out.println("logicSvc.eval time(" + ruleName + ", " + threadName + "): " + elapsedTime);
                }
            }
            return results;

        } catch (Exception e) {
            log.error("Error running rules.");
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            return null;
        }
    }

    @Override
    public org.openmrs.logic.Rule loadRule(String rule, boolean updateRule) throws Exception {
        log.info("Going to load rule '" + rule + "' from HashMap...");
        org.openmrs.logic.Rule loadedRule = loadedRuleMap.get(rule);

        if (loadedRule != null && !updateRule) {
            // the rule has already been loaded, and an update is not needed.
            log.info("Rule '" + rule + "' found in HashMap. Not forcing update.");
            return loadedRule;
        }

        // Create a CompilingClassLoader
        log.info("Creating a " + CompilingClassLoader.class.getName() + "...");
        CompilingClassLoader ccl = CompilingClassLoader.getInstance();

        AdministrationService adminService = Context.getAdministrationService();
        String rulePackagePrefix = Util.formatPackagePrefix(adminService
                .getGlobalProperty("dss.rulePackagePrefix"));

        Class<?> classObject = null;

        // try to load the class dynamically
        if (!rule.contains(rulePackagePrefix)) {
            try {
                classObject = ccl.loadClass(rulePackagePrefix + rule);
            } catch (Exception e) {
                // ignore this exception
            }
        } else {
            try {
                classObject = ccl.loadClass(rule);
            } catch (Exception e) {
                // ignore this exception
            }
        }

        // try to load the class from the class library
        if (classObject == null) {
            String defaultPackagePrefixProp = adminService.getGlobalProperty("dss.defaultPackagePrefix");
            List<String> defaultPackagePrefixes = Util.formatPackagePrefixes(defaultPackagePrefixProp, ",");
            if (defaultPackagePrefixes.size() > 0) {
                int cnt = 0;
                while ((classObject == null) && (cnt < defaultPackagePrefixes.size())) {
                    String defaultPackagePrefix = defaultPackagePrefixes.get(cnt++);
                    if (!rule.contains(defaultPackagePrefix)) {
                        try {
                            classObject = ccl.loadClass(defaultPackagePrefix + rule);
                        } catch (Exception e) {
                            //ignore this exception
                        }
                    }
                }
            }
        }

        // try to load the class as it is
        if (classObject == null) {
            try {
                classObject = ccl.loadClass(rule);
            } catch (Exception e) {
                //ignore this exception
            }
        }

        if (classObject == null) {
            log.info("Class was not found.");
            throw new Exception("Could not load class for rule: " + rule);
        }

        Object obj = null;

        try {
            obj = classObject.newInstance();
        } catch (Exception e) {
            log.error("", e);
        }

        if (!(obj instanceof org.openmrs.logic.Rule)) {
            throw new Exception("Could not load class for rule: " + rule
                    + ". The rule must implement the Rule interface.");
        }

        loadedRule = (org.openmrs.logic.Rule) obj;

        try {
            // register token
            Context.getService(TokenService.class).registerToken(rule, new DssRuleProvider(), classObject.getName());
            loadedRuleMap.put(rule, loadedRule);
        } catch (Exception e) {
            log.error("", e);
        }

        return loadedRule;
    }

    @Override
    public Rule getRule(int ruleId) throws APIException {
        return getDssDAO().getRule(ruleId);
    }

    @Override
    public List<Rule> getPrioritizedRules() throws DAOException {
        return getDssDAO().getPrioritizedRules();
    }

    @Override
    public List<Rule> getPrioritizedRules(String type) throws DAOException {
        return getDssDAO().getPrioritizedRules(type);
    }

    @Override
    public List<Rule> getPrioritizedRulesByConcept(Concept concept) throws DAOException {
        return getDssDAO().getPrioritizedRulesByConcept(concept);
    }

    @Override
    public List<Rule> getPrioritizedRulesByConcepts(Set<Concept> concepts) throws DAOException {
        return getDssDAO().getPrioritizedRulesByConcepts(concepts);
    }

    @Override
    public List<Rule> getPrioritizedRulesByConceptsInEncounter(Encounter encounter) throws DAOException {
        return getDssDAO().getPrioritizedRulesByConceptsInEncounter(encounter);
    }

    @Override
    public List<Rule> getNonPrioritizedRules(String type) throws DAOException {
        return getDssDAO().getNonPrioritizedRules(type);
    }

    @Override
    public List<Rule> getRules(Rule rule, boolean ignoreCase, boolean enableLike, String sortColumn) {
        return getDssDAO().getRules(rule, ignoreCase, enableLike, sortColumn);
    }

    @Override
    public void deleteRule(int ruleId) {
        getDssDAO().deleteRule(ruleId);
    }

    @Override
    public Rule addRule(String classFilename, MlmRule rule) throws APIException {
        String tokenName = IOUtil.getFilenameWithoutExtension(classFilename);
        Rule databaseRule = getDssDAO().getRule(tokenName);

        if (databaseRule != null) {
            databaseRule.setLastModified(new java.util.Date());
        } else {
            databaseRule = new Rule();
            databaseRule.setCreationTime(new java.util.Date());
        }

        databaseRule.setClassFilename(classFilename);
        databaseRule.setPriority(rule.getPriority());
        databaseRule.setAction(rule.getAction());
        databaseRule.setAuthor(rule.getAuthor());
        databaseRule.setCitations(rule.getCitations());
        databaseRule.setData(rule.getData());
        databaseRule.setExplanation(rule.getExplanation());
        databaseRule.setInstitution(rule.getInstitution());
        databaseRule.setKeywords(rule.getKeywords());
        databaseRule.setLinks(rule.getLinks());
        databaseRule.setLogic(rule.getLogic());
        databaseRule.setPurpose(rule.getPurpose());
        databaseRule.setRuleCreationDate(rule.getDate());
        databaseRule.setSpecialist(rule.getSpecialist());
        databaseRule.setTitle(rule.getTitle());
        databaseRule.setVersion(rule.getVersion());
        if (rule.getType() != null) {
            databaseRule.setRuleType(rule.getType());
        }
        databaseRule.setTokenName(tokenName);
        databaseRule.setAgeMax(rule.getAgeMax());
        databaseRule.setAgeMin(rule.getAgeMin());
        databaseRule.setAgeMinUnits(rule.getAgeMinUnits());
        databaseRule.setAgeMaxUnits(rule.getAgeMaxUnits());

        return getDssDAO().addOrUpdateRule(databaseRule);

    }

    @Override
    public boolean addMapping(Rule rule, Concept concept) throws APIException {
        if (concept == null) {
            return false;
        }
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        concepts.add(concept);
        return addMapping(rule, concepts);
    }

    @Override
    public boolean addMapping(Rule rule, ArrayList<Concept> concepts) throws APIException {
        if (concepts == null) {
            return false;
        }

        if (concepts.isEmpty()) {
            return false;
        }

        getDssDAO().addMapping(rule, concepts);

        return true;
    }

    @Override
    public List<Concept> getMappings(Rule rule) {
        return getDssDAO().getMappings(rule);
    }

    @Override
    public List<Rule> getMappings(Concept concept) {
        return getDssDAO().getMappings(concept);
    }
    
    @Override
    public Map<Concept,List<Concept>> getDrugRecommendationForEncounter(Encounter encounter){
        try {
            OrderService orderService = Context.getOrderService();
            
            // get obs from encounter
            Set<Obs> obs = encounter.getObs();
            System.out.println("encounter id: " + encounter.getEncounterId());
            if(obs.isEmpty()){
                System.out.println("obs is empty. drug recommendation.");
                return Collections.<Concept,List<Concept>>emptyMap();
            }         
            // get all drug recommendations to the obs of this encounter
            HashMap<Concept, List<Concept>> recommendationList = new HashMap<Concept, List<Concept>>();

            recommendationList.putAll(getDssDAO().getDrugRecommendationByObs(obs));


            if(recommendationList.isEmpty()) {
                return Collections.<Concept,List<Concept>>emptyMap();
            }
            
            // get drug orders of this patient;
            List<DrugOrder> drugOrders = orderService.getDrugOrdersByPatient(encounter.getPatient());
            if(drugOrders.isEmpty()){
                return recommendationList;
            }
            
            // get drugs in use from drug orders
            Set<Drug> drugs = getDssDAO().getActiveMedicationsByDrugOrders(drugOrders);
            
            if(drugs.isEmpty()){
                return Collections.<Concept,List<Concept>>emptyMap();
            }
            
            // extract concept from drugs, using Set to eliminate duplicates
            Set<Concept> drugConcepts = new HashSet<Concept>();
            for(Drug drug: drugs){
                drugConcepts.add(drug.getConcept());
            }

            HashMap<Concept, List<Concept>> finalList = new HashMap<Concept, List<Concept>>();
            Iterator it = recommendationList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Concept,List<Concept>> pairs = (Map.Entry<Concept,List<Concept>>)it.next();
                boolean shouldAdd = true;
                for(Concept drugConcept: drugConcepts){
                    if(pairs.getValue().contains(drugConcept)){
                        shouldAdd = false;
                        break;
                    }
                } 
                if(shouldAdd){
                    finalList.put(pairs.getKey(),pairs.getValue());
                }                
            }

            if(finalList.isEmpty()){
                return Collections.<Concept,List<Concept>>emptyMap();
            }
            return finalList;

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Concept,List<Concept>>emptyMap();
    }

    @Override
    public Map<Drug,Set<Drug>> getPoorDrugInteractionsForEncounter(Encounter encounter, Integer patientId) {
       try {
           OrderService orderService = Context.getOrderService();

            // get orders from encounter
            HashSet<Concept> orderConcepts = new HashSet<Concept>();
            // get drug orders from encounter
            List<Encounter> encounters = new ArrayList<Encounter>();
            encounters.add(encounter);
            List<DrugOrder> drugOrders = orderService.getOrders(DrugOrder.class, null, null, OrderService.ORDER_STATUS.ANY, null, encounters, null);
            if(drugOrders.isEmpty()){
                System.out.println("drug orders empty in drug interaction.");
                return Collections.<Drug,Set<Drug>>emptyMap();
            }

            Map<Drug,Set<Drug>> resultList = this.getDrugInteractionsByDrugOrders(drugOrders,patientId);
            
            if(resultList.isEmpty()){
                return Collections.<Drug,Set<Drug>>emptyMap();
            }
            return resultList;


        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Drug,Set<Drug>>emptyMap();
    }

    
    public Map<Drug,Set<Drug>> getDrugInteractionsByDrugOrders(List<DrugOrder> drugOrders, Integer patientId) {
       try {
         
           if(drugOrders.isEmpty()){
               return Collections.<Drug,Set<Drug>>emptyMap();
           }
           
           // use HashSet to eliminate duplicates
           Set<Drug> drugs = new HashSet<Drug>();
           
           for(DrugOrder drugOrder: drugOrders){
               Drug drug = drugOrder.getDrug();
               if(drug != null){
                    drugs.add(drug);
               }
           }
           
           if(drugs.isEmpty()){
               return Collections.<Drug,Set<Drug>>emptyMap();
           }
           
           Map<Drug,Set<Drug>> resultSet = new HashMap<Drug,Set<Drug>>();
           
           for(Drug drug: drugs){
               
               List<Concept> interactionListByDrug = getDssDAO().getInteractionListByDrug(drug);
               if(!interactionListByDrug.isEmpty()){
                   Set<Drug> drugsInInteractionList = getDssDAO().getDrugsInInteractionList(interactionListByDrug, patientId);
                   if(!drugsInInteractionList.isEmpty()){
                       resultSet.put(drug, drugsInInteractionList);
                   }
               }
               
           }
            
            if(resultSet.isEmpty()){
                return Collections.<Drug,Set<Drug>>emptyMap();
            }
            
            return resultSet;

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Drug,Set<Drug>>emptyMap(); 
    }

    
    public Map<Drug,Set<Drug>> getDrugInteractionsByDrugOrder(DrugOrder drugOrder, Integer patientId) {
        if (drugOrder == null) {
            return Collections.<Drug,Set<Drug>>emptyMap();
        }
        List<DrugOrder> drugOrders = new ArrayList<DrugOrder>();
        drugOrders.add(drugOrder);
        return this.getDrugInteractionsByDrugOrders(drugOrders, patientId);
    }

    @Override
    public Set<Concept> getAllergyConceptsToDrugOrdersInEncounter(Encounter encounter) {
        try{
            System.out.println("encounter id: " + encounter.getEncounterId());
            OrderService orderService = Context.getOrderService();
            List<Encounter> encounters = new ArrayList<Encounter>();
            encounters.add(encounter);
            List<DrugOrder> drugOrders = orderService.getOrders(DrugOrder.class, null, null, OrderService.ORDER_STATUS.ANY, null, encounters, null);
            if(drugOrders.isEmpty()){
                System.out.println("drug orders empty.");
                return Collections.<Concept>emptySet();
            }
            System.out.println("drug orders: " + drugOrders.get(0).getDrug().getConcept().getName().getName());
            Set<Drug> drugs = new HashSet<Drug>();
            for(DrugOrder drugOrder: drugOrders){
                Drug drug = drugOrder.getDrug();
                if(drug != null){
                    drugs.add(drug);
                }
            }
            if(drugs.isEmpty()){
                return Collections.<Concept>emptySet();
            }
            PersonService personService = Context.getPersonService();
            Person person = personService.getPerson(encounter.getPatientId());
            Set<Concept> allergiesFromActiveList = getDssDAO().getAllergiesFromActiveListByDrugs(drugs, person);
            Set<Concept> allergiesFromObs = this.getAllergiesFromObsByDrugs(drugs,person);
            Set<Concept> finalAllergyList = new HashSet<Concept>();
            if(!allergiesFromActiveList.isEmpty()){
                finalAllergyList.addAll(allergiesFromActiveList);
            }
            if(!allergiesFromObs.isEmpty()){
                finalAllergyList.addAll(allergiesFromObs);
            }
            if(finalAllergyList.isEmpty()){
                return Collections.<Concept>emptySet();
            }
            return finalAllergyList;
        }catch(Exception e){
            this.log.error(e);
        }
        return Collections.<Concept>emptySet();
    }
    
    public Set<Concept> getAllergiesFromObsByDrugs(Set<Drug> drugs, Person person) {
        try{
            if(drugs.isEmpty()){
                return Collections.<Concept>emptySet();
            }
            ObsService obsService = Context.getObsService();
            Set<Concept> drugConcepts = new HashSet<Concept>();
            for(Drug drug: drugs){
                drugConcepts.add(drug.getConcept());
            }
            
            ConceptService conceptService = Context.getConceptService();
            Set<Concept> alllergyConcepts = new HashSet<Concept>();
            
            // 1. Class: diagnosis, Data Type: N/A, e.g. "ALLERGY TO PENICILLIN"
            // 2. Class: diagnosis, Data Type: Boolean, e.g. "ALLERGY TO FULSA"
            for(Concept concept:drugConcepts){                
                List<Concept> concepts = conceptService.getConceptsByName(concept.getName().getName());
                for(Concept cncpt: concepts){
                    if(cncpt.getName().getName().contains("ALLERG")){
                        List<Obs> obs = obsService.getObservationsByPersonAndConcept(person, cncpt);
                        System.out.println("size of obs: " + obs.size());
                        if(!obs.isEmpty()){
                            if(cncpt.getDatatype().isBoolean()){
                                if(obs.get(0).getValueBoolean()){
                                    alllergyConcepts.add(cncpt);
                                }
                            } else {
                                alllergyConcepts.add(cncpt);
                            }
                        }
                    }
                }
            }
            
            // 3. Class: Findings, Data Type: Coded, e.g. "ALLERGY MEDICATION LIST"
            Concept allergyMedicationList = conceptService.getConceptByName("ALLERGY MEDICATION LIST");
            System.out.println(allergyMedicationList.getName().getName());
            if(allergyMedicationList != null && allergyMedicationList.getDatatype().isCoded()){
                List<Obs> obs = obsService.getObservationsByPersonAndConcept(person, allergyMedicationList);
                for(Obs ob:obs){
                    Concept codedValue = ob.getValueCoded();
                    if(drugConcepts.contains(codedValue)){
                        alllergyConcepts.add(codedValue);
                    }
                }
            }
            
            if(alllergyConcepts.isEmpty()){
                return Collections.<Concept>emptySet();
            }
            
            return alllergyConcepts;
            
        }catch(Exception e){
            this.log.equals(e);
        }
        return Collections.<Concept>emptySet();
    }
     
    public Set<Concept> getAllergiesFromObsByDrug(Drug drug, Person person) {
        
        Set<Drug> drugs = new HashSet<Drug>();
        drugs.add(drug);
        return getAllergiesFromObsByDrugs(drugs, person);
        
    }    
        

    @Override
    public List<Rule> getGeneralizedRules() {

        Rule rule1 = dao.getRule("GENERALIZED1");
        Rule rule2 = dao.getRule("GENERALIZED2");
        Rule rule3 = dao.getRule("GENERALIZED3");

        List<Rule> rules = new ArrayList<Rule>();
        
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        
        return rules;
    }

    @Override
    public List<Rule> getGeneralizedJavaRules() {
        LogicService logicService = Context.getLogicService();
        Rule rule1 = Util.convertRule(logicService.getRule("allergiesToDrugOrders"), "allergiesToDrugOrders");
        Rule rule2 = Util.convertRule(logicService.getRule("drugRecommendation"), "drugRecommendation");
        Rule rule3 = Util.convertRule(logicService.getRule("drugInteraction"), "drugInteraction");
        List<Rule> rules = new ArrayList<Rule>();
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        return rules;
    }


}