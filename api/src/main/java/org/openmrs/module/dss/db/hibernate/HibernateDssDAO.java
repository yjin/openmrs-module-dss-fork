package org.openmrs.module.dss.db.hibernate;

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
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.dss.db.DssDAO;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.util.Util;

/**
 * Hibernate implementations of Dss related database functions.
 *
 * @author Tammy Dugan
 */
public class HibernateDssDAO implements DssDAO {

    protected final Log log = LogFactory.getLog(getClass());
    /**
     * Hibernate session factory
     */
    private SessionFactory sessionFactory;

    /**
     * Empty constructor
     */
    public HibernateDssDAO() {
    }

    /**
     * Set session factory
     *
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Rule getRule(int ruleId) {
        try {
            String sql = "select * from dss_rule where rule_id=?";
            SQLQuery qry = this.sessionFactory.getCurrentSession()
                    .createSQLQuery(sql);
            qry.setInteger(0, ruleId);
            qry.addEntity(Rule.class);
            return (Rule) qry.uniqueResult();
        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }
        return null;
    }

    @Override
    public Rule getRule(String tokenName) {
        try {
            String sql = "select * from dss_rule where token_name=?";
            SQLQuery qry = this.sessionFactory.getCurrentSession()
                    .createSQLQuery(sql);
            qry.setString(0, tokenName);
            qry.addEntity(Rule.class);
            return (Rule) qry.uniqueResult();
        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }
        return null;
    }

    @Override
    public void deleteRule(int ruleId) {
        try {
            Rule rule = this.getRule(ruleId);
            this.sessionFactory.getCurrentSession().delete(rule);
        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }
    }

    @Override
    public Rule addOrUpdateRule(Rule rule) {
        try {
            // If the rule type for the rule to save is still null,
            // then set the rule type to the token_name
            if (rule.getRuleType() == null) {
                rule.setRuleType(rule.getTokenName());
                this.log.error("Rule " + rule.getTokenName() + " does not have a rule type set. "
                        + "It will not be available for prioritization until the rule type is set to a form name.");
            }
            return (Rule) this.sessionFactory.getCurrentSession().merge(rule);
        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }
        return null;
    }

    @Override
    public boolean addMapping(Rule rule, List<Concept> concepts) {
        try {

            // build the SQL query
            StringBuilder sb = new StringBuilder();

            sb.append("INSERT IGNORE INTO dss_rule_by_concept (rule_id, concept_id) VALUES ");

            int remaining = concepts.size();
            for (Concept concept : concepts) {
                remaining--;
                sb.append('(');
                sb.append(rule.getRuleId());
                sb.append(',');
                sb.append(concept.getConceptId());
                sb.append(')');
                if (remaining > 0) {
                    sb.append(',');
                }
            }

            SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
            int rowsCreated = query.executeUpdate();

            return rowsCreated == concepts.size();

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
            return false;
        }
    }

    @Override
    public List<Concept> getMappings(Rule rule) {

        ArrayList<Concept> concepts = new ArrayList<Concept>();

        try {
            String sql = "select concept_id from dss_rule_by_concept where rule_id=?";
            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
            qry.setInteger(0, rule.getRuleId());
            qry.addScalar("concept_id", Hibernate.INTEGER);
            List<Integer> conceptIds = qry.list();
            ConceptService conceptService = Context.getConceptService();
            for (Integer id : conceptIds) {
                Concept c = conceptService.getConcept(id);
                if (c != null) {
                    concepts.add(c);
                }
            }

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));

        }
        return concepts;
    }

    @Override
    public List<Rule> getMappings(Concept concept) {
        try {
            String sql = "SELECT * FROM dss_rule"
                    + " WHERE priority>=0 AND priority<1000"
                    + "   AND rule_id IN (SELECT DISTINCT rule_id FROM dss_rule_by_concept WHERE concept_id=?)";
            SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
            query.setInteger(0, concept.getConceptId());
            query.addEntity(Rule.class);
            return query.list();

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
            return new ArrayList<Rule>();
        }
    }

    @Override
    public List<Rule> getPrioritizedRules() throws DAOException {
        try {
            AdministrationService adminService = Context.getAdministrationService();
            String sortOrder = adminService.getGlobalProperty("dss.ruleSortOrder");

            if (sortOrder == null) {
                sortOrder = "DESC";
            }

            String sql = "SELECT *"
                    + " FROM dss_rule"
                    + " WHERE priority>=0 AND priority<1000"
                    + " AND version='1.0'"
                    + " ORDER BY priority " + sortOrder;

            SQLQuery query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
            query.addEntity(Rule.class);
            return query.list();

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }
        return null;
    }

    @Override
    public List<Rule> getPrioritizedRules(String type) throws DAOException {
        try {
            AdministrationService adminService = Context
                    .getAdministrationService();
            String sortOrder = adminService
                    .getGlobalProperty("dss.ruleSortOrder");
            if (sortOrder == null) {
                sortOrder = "DESC";
            }

            String sql = "select * from dss_rule where rule_type=? and "
                    + "priority >=0 and priority<1000 and "
                    + "version='1.0' order by priority " + sortOrder;
            SQLQuery qry = this.sessionFactory.getCurrentSession()
                    .createSQLQuery(sql);
            qry.setString(0, type);
            qry.addEntity(Rule.class);
            return qry.list();
        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }
        return null;
    }

    @Override
    public List<Rule> getPrioritizedRulesByConcepts(Set<Concept> concepts) throws DAOException {
        try {

            if (concepts == null || concepts.isEmpty()) {
                return Collections.<Rule>emptyList();
            }

            AdministrationService adminService = Context.getAdministrationService();

            String sortOrder = adminService.getGlobalProperty("dss.ruleSortOrder");
            if (sortOrder == null) {
                sortOrder = "DESC";
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT r.*");
            query.append(" FROM dss_rule_by_concept c");
            query.append(" LEFT OUTER JOIN dss_rule r ON (c.rule_id=r.rule_id)");
            query.append(" WHERE r.priority >=0 and r.priority<1000");
            query.append("   AND r.version='1.0'");
            query.append("   AND c.concept_id IN (");
            int remainingElements = concepts.size();
            for (Concept concept : concepts) {
                query.append(concept.getConceptId());
                remainingElements--;
                if (remainingElements > 0) {
                    query.append(',');
                }
            }
            query.append(')');
            query.append(" ORDER BY r.priority ").append(sortOrder);

            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(query.toString());
            qry.addEntity(Rule.class);

            return qry.list();

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Rule>emptyList();
    }

    @Override
    public List<Rule> getPrioritizedRulesByConcept(Concept concept) throws DAOException {
        if (concept == null) {
            return Collections.<Rule>emptyList();
        }
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);
        return this.getPrioritizedRulesByConcepts(concepts);
    }

    @Override
    public List<Rule> getPrioritizedRulesByConceptsInEncounter(Encounter encounter) throws DAOException {
        try {

            HashSet<Concept> concepts = new HashSet<Concept>();
            for (Obs obs : encounter.getObs()) {
                concepts.add(obs.getConcept());
            }

            if (concepts.isEmpty()) {
                return Collections.<Rule>emptyList();
            }

            ArrayList<Rule> list = new ArrayList<Rule>();
            for (Concept concept : concepts) {
                list.addAll(this.getPrioritizedRulesByConcept(concept));
            }

            return list;

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Rule>emptyList();
    }

    @Override
    public List<Rule> getNonPrioritizedRules(String type) throws DAOException {
        try {
            AdministrationService adminService = Context
                    .getAdministrationService();
            String sortOrder = adminService
                    .getGlobalProperty("dss.ruleSortOrder");
            if (sortOrder == null) {
                sortOrder = "DESC";
            }

            String sql = "select * from dss_rule where rule_type = ? and "
                    + "priority is null and version='1.0' order by priority "
                    + sortOrder;
            SQLQuery qry = this.sessionFactory.getCurrentSession()
                    .createSQLQuery(sql);
            qry.setString(0, type);
            qry.addEntity(Rule.class);
            return qry.list();
        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return null;
    }

    @Override
    public List<Rule> getRules(Rule rule, boolean ignoreCase,
            boolean enableLike, String sortColumn) throws DAOException {
        try {
            Example example = Example.create(rule);

            if (ignoreCase) {
                example = example.ignoreCase();
            }

            if (enableLike) {
                example = example.enableLike(MatchMode.ANYWHERE);
            }

            AdministrationService adminService = Context
                    .getAdministrationService();
            String sortOrder = adminService
                    .getGlobalProperty("dss.ruleSortOrder");

            if (sortColumn == null) {
                sortColumn = "priority";
            }

            Order order;
            if (sortOrder == null || sortOrder.equalsIgnoreCase("ASC")) {
                order = Order.asc(sortColumn);
            } else {
                order = Order.desc(sortColumn);
            }

            List<Rule> results = this.sessionFactory.getCurrentSession()
                    .createCriteria(Rule.class).add(example).addOrder(order)
                    .list();

            return results;

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));

        }

        return null;
    }

    @Override
    public Map<Concept,List<Concept>> getDrugRecommendationByDiagnosisConceptsInEncounter(Encounter encounter) {
        try {
            OrderService orderService = Context.getOrderService();
            // get obs from encounter
            Set<Obs> obs = encounter.getObs();
            if(obs.isEmpty()){
                return Collections.<Concept,List<Concept>>emptyMap();
            }
            HashSet<Concept> obsConcepts = new HashSet<Concept>();
            for (Obs ob : obs) {
                obsConcepts.add(ob.getConcept());
            }

            if (obsConcepts.isEmpty()) {
                return Collections.<Concept,List<Concept>>emptyMap();
            }           

            // get all drug recommendations to the obs of this encounter
            HashMap<Concept, List<Concept>> recommendationList = new HashMap<Concept, List<Concept>>();

            recommendationList.putAll(this.getDrugRecommendationByDiagnosisConcepts(obsConcepts));


            if(recommendationList.isEmpty()) {
                return Collections.<Concept,List<Concept>>emptyMap();
            }
            

            // get orders from encounter
            List<org.openmrs.Order> orders = orderService.getOrdersByEncounter(encounter);
            HashSet<Concept> orderConcepts = new HashSet<Concept>();
            for (org.openmrs.Order order : orders) {
                orderConcepts.add(order.getConcept());
            }

            if (orderConcepts.isEmpty()) {
                return recommendationList;
            }

            HashMap<Concept, List<Concept>> finalList = new HashMap<Concept, List<Concept>>();
            Iterator it = recommendationList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Concept,List<Concept>> pairs = (Map.Entry<Concept,List<Concept>>)it.next();
                boolean shouldAdd = true;
                for(Concept orderConcept: orderConcepts){
                    if(pairs.getValue().contains(orderConcept)){
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
    public List<Concept> getDrugRecommendationByDiagnosisConcept(Concept concept) {
        try {

            if (concept == null) {
                return Collections.<Concept>emptyList();
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT d.drug_concept_id");
            query.append(" FROM diagnosis_to_drug d");
            query.append(" WHERE d.diagnosis_concept_id=");
            query.append(concept.getConceptId());

            // scalar query
            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(query.toString());
  //          qry.addEntity(Integer.class);

            List<Integer> drugConceptIds = qry.list();
            
            if(drugConceptIds.isEmpty()){
                return Collections.<Concept>emptyList();
            }
            
            List<Concept> drugConcepts = new ArrayList<Concept>();
            
            ConceptService conceptService = Context.getConceptService();
            for(Integer id: drugConceptIds){
                drugConcepts.add(conceptService.getConcept(id));
            }
            
            return drugConcepts;

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Concept>emptyList();
    }

    @Override
    public Map<Concept,List<Concept>> getDrugRecommendationByDiagnosisConcepts(Set<Concept> concepts) {
        if (concepts == null || concepts.isEmpty()) {
            return Collections.<Concept,List<Concept>>emptyMap();
        }
        
        HashMap<Concept,List<Concept>> hm = new HashMap<Concept,List<Concept>>();
        for(Concept concept:concepts){
            List<Concept> list = this.getDrugRecommendationByDiagnosisConcept(concept);
            if(!list.isEmpty()){
                hm.put(concept, list);
            }
        }

        return hm;
    }

    @Override
    public List<Concept> getDrugInteractionsForEncounter(Encounter encounter, Integer patientId) {
       try {
           OrderService orderService = Context.getOrderService();

            // get orders from encounter
            HashSet<Concept> orderConcepts = new HashSet<Concept>();
            List<org.openmrs.Order> orders = orderService.getOrdersByEncounter(encounter);
            for (org.openmrs.Order order : orders) {
                orderConcepts.add(order.getConcept());
            }

            if (orderConcepts.isEmpty()) {
                return Collections.<Concept>emptyList(); 
            }           
            System.out.print("the number of orders: ");
            System.out.println(orderConcepts.size());
            
            List<Concept> resultList = this.getDrugInteractionsByConcepts(orderConcepts,patientId);
            
            if(resultList.isEmpty()){
                return Collections.<Concept>emptyList(); 
            }
            System.out.println("the result is not empty");
            System.out.println(resultList.get(0).getName().getName());
            return resultList;


        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Concept>emptyList();         
    }

    @Override
    public List<Concept> getDrugInteractionsByConcepts(Set<Concept> concepts, Integer patientId) {
       try {
         
            
            List<Concept> interactionBetweenDrugOrders = this.getInteractionBetweenDrugOrders(concepts, patientId);

            
           
            if(interactionBetweenDrugOrders.isEmpty()){
                return Collections.<Concept>emptyList();
            }
            
            System.out.println("the concept from query: " + interactionBetweenDrugOrders.get(0).getName().getName());
            
            List<Concept> interactionResult = new ArrayList<Concept>();
            
            interactionResult.addAll(interactionBetweenDrugOrders);
            
            if(interactionResult.isEmpty()){
                return Collections.<Concept>emptyList();
            }
            
            return interactionResult;

        } catch (Exception e) {
            this.log.error(Util.getStackTrace(e));
        }

        return Collections.<Concept>emptyList();  
    }

    @Override
    public List<Concept> getDrugInteractionsByConcept(Concept concept, Integer patientId) {
        if (concept == null) {
            return Collections.<Concept>emptyList();
        }
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);
        return this.getDrugInteractionsByConcepts(concepts, patientId);
    }
    
    
    List<Concept> getInteractionBetweenDrugOrders(Set<Concept> concepts, Integer patientId){
        
        try {
            if (concepts == null || concepts.isEmpty()) {
                return Collections.<Concept>emptyList();  
            }
           System.out.println("preparing for query.");
            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT o.concept_id");
            query.append(" from drug_to_drug d");
            query.append(" RIGHT OUTER JOIN orders o ON (d.drug_concept_right_id=o.concept_id)");
            query.append(" WHERE o.auto_expire_date>DATE(NOW())");
            query.append("   AND o.patient_id=");
            query.append(patientId);
            query.append("   AND d.drug_concept_left_id IN (");

            int remainingElements = concepts.size();
            for(Concept concept: concepts){
                query.append(concept.getConceptId());
                remainingElements--;
                if(remainingElements > 0){
                    query.append(",");
                }
            }            
            query.append(")");          

            // scalar query
            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(query.toString());


            List<Integer> ordersConceptIds = qry.list();
            
            if(ordersConceptIds.isEmpty()){
                return Collections.<Concept>emptyList(); 
            }
            
            System.out.println("the result from query: ");
            System.out.println(ordersConceptIds.get(0));
            
            List<Concept> resultList = new ArrayList<Concept>();
            ConceptService conceptService = Context.getConceptService();
            for(Integer id: ordersConceptIds){
                Concept concept = conceptService.getConcept(id);
                resultList.add(concept);
            }
            
            return resultList;
            
        } catch (Exception e){
            this.log.error(Util.getStackTrace(e));
        }
        return Collections.<Concept>emptyList(); 
    }
}
