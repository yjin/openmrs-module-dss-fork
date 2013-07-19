package org.openmrs.module.dss.db.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.activelist.ActiveListType;
import org.openmrs.activelist.Allergy;
import org.openmrs.api.ActiveListService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
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
    public List<Concept> getDrugRecommendationByOb(Obs ob) {
        try {

            if (ob == null) {
                return Collections.<Concept>emptyList();
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT d.drug_concept_id");
            query.append(" FROM diagnosis_to_drug d");
            query.append(" WHERE d.diagnosis_concept_id=");
            query.append(ob.getConcept().getConceptId());

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
    public Map<Concept,List<Concept>> getDrugRecommendationByObs(Set<Obs> obs) {
        if (obs == null || obs.isEmpty()) {
            return Collections.<Concept,List<Concept>>emptyMap();
        }
        
        HashMap<Concept,List<Concept>> hm = new HashMap<Concept,List<Concept>>();
        for(Obs ob:obs){
            List<Concept> list = this.getDrugRecommendationByOb(ob);
            if(!list.isEmpty()){
                hm.put(ob.getConcept(), list);
            }
        }

        return hm;
    }

    @Override
    public Set<Drug> getActiveMedicationsByDrugOrders(List<DrugOrder> drugOrders){
        try{
            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT o.order_id");
            query.append(" FROM orders o");
            query.append(" WHERE o.discontinued = 0");
            query.append(" AND o.order_type_id = 2");
            query.append(" AND o.auto_expire_date > DATE(NOW())");
            query.append(" OR o.auto_expire_date is null");
            query.append(" AND o.order_id IN (");
            int remainingElements = drugOrders.size();
            for(DrugOrder drugOrder: drugOrders){
                query.append(drugOrder.getOrderId());
                remainingElements--;
                if(remainingElements > 0){
                    query.append(",");
                }
            }
            query.append(")");
            // scalar query
            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(query.toString());

            List<Integer> orderIds = qry.list();
            
            if(orderIds.isEmpty()){
                return Collections.<Drug>emptySet();
            }
            
            OrderService orderService = Context.getOrderService();
            Set<Drug> drugs = new HashSet<Drug>();
            for(Integer id: orderIds){
                Drug drug = orderService.getDrugOrder(id).getDrug();
                if(drug != null){
                    drugs.add(drug);
                }
            }
            
            if(drugs.isEmpty()){
                return Collections.<Drug>emptySet();
            }
            
            return drugs;
        }catch(Exception e){
            this.log.equals(e);
        }
        
        return Collections.<Drug>emptySet();
    }

  
    @Override
    public List<Concept> getInteractionListByDrugConcepts(Set<Concept> concepts){
        
        try {
            if (concepts == null || concepts.isEmpty()) {
                return Collections.<Concept>emptyList();  
            }
            
            List<Integer> conceptsIdList = new ArrayList<Integer>();
            StringBuilder query1 = new StringBuilder();
            query1.append("SELECT DISTINCT p.drug_concept_B_id");
            query1.append(" from poor_drug_interactions p");
            query1.append(" WHERE p.drug_concept_A_id IN (");
            int remainingElements1 = concepts.size();
            for(Concept concept: concepts){
                query1.append(concept.getConceptId());
                remainingElements1--;
                if(remainingElements1 > 0){
                    query1.append(",");
                }
            }            
            query1.append(")");          

            // scalar query
            SQLQuery qry1 = this.sessionFactory.getCurrentSession().createSQLQuery(query1.toString());


            List<Integer> drugConceptBIds = qry1.list();
            
            if(!drugConceptBIds.isEmpty()){
                conceptsIdList.addAll(drugConceptBIds); 
            }
            
            StringBuilder query2 = new StringBuilder();
            query2.append("SELECT DISTINCT p.drug_concept_A_id");
            query2.append(" from poor_drug_interactions p");
            query2.append(" WHERE p.drug_concept_B_id IN (");
            int remainingElements2 = concepts.size();
            for(Concept concept: concepts){
                query2.append(concept.getConceptId());
                remainingElements2--;
                if(remainingElements2 > 0){
                    query2.append(",");
                }
            }            
            query2.append(")");          

            // scalar query
            SQLQuery qry2 = this.sessionFactory.getCurrentSession().createSQLQuery(query2.toString());


            List<Integer> drugConceptAIds = qry2.list();            
            
            if(!drugConceptAIds.isEmpty()){
                conceptsIdList.addAll(drugConceptAIds);
            }
            
            if(conceptsIdList.isEmpty()){
                return Collections.<Concept>emptyList();
            }
            
            List<Concept> resultList = new ArrayList<Concept>();
            ConceptService conceptService = Context.getConceptService();
            for(Integer id: conceptsIdList){
                Concept concept = conceptService.getConcept(id);
                resultList.add(concept);
            }
            
            return resultList;
            
        } catch (Exception e){
            this.log.error(Util.getStackTrace(e));
        }
        return Collections.<Concept>emptyList(); 
    }
    
    @Override
    public List<Concept> getInteractionListByDrugConcept(Concept concept) {
        Set<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);
        return this.getInteractionListByDrugConcepts(concepts);
    }

    @Override
    public Set<Drug> getDrugsInInteractionList(List<Concept> interactionConcepts, Integer patientId){
        try{
            if(interactionConcepts == null || interactionConcepts.isEmpty()){
                return Collections.<Drug>emptySet(); 
            }
            OrderService orderService = Context.getOrderService();
            // get active drug orders
            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT d.order_id");
            query.append(" FROM drug_order d");
            query.append(" JOIN orders o ON (d.order_id = o.order_id)");
            query.append(" WHERE o.discontinued = 0");
            query.append(" AND o.order_type_id = 2");
            query.append(" AND o.auto_expire_date > DATE(NOW())");
            query.append(" OR o.auto_expire_date is null");
            // scalar query
            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(query.toString());


            List<Integer> drugOrderIds = qry.list();
            
            if(drugOrderIds.isEmpty()){
                return Collections.<Drug>emptySet(); 
            }
            
            Set<Drug> drugSet = new HashSet<Drug>();
            for(Integer drugOrderId: drugOrderIds) {
                Drug drug = orderService.getDrugOrder(drugOrderId).getDrug();
                if(drug != null && interactionConcepts.contains(drug.getConcept())){
                    drugSet.add(drug);
                }            
            }
            
            if(drugSet.isEmpty()){
                return Collections.<Drug>emptySet(); 
            }          
            
            return drugSet;
            
        } catch (Exception e){
            this.log.error(Util.getStackTrace(e));
        }
        return Collections.<Drug>emptySet(); 
    }

    @Override
    public Set<Concept> getAllergiesFromActiveListByDrugs(Set<Drug> drugs, Person person) {
        try{
            if(drugs.isEmpty()){
                return Collections.<Concept>emptySet();
            }
            Set<Concept> drugConcepts = new HashSet<Concept>();
            for(Drug drug: drugs){
                drugConcepts.add(drug.getConcept());
            }
            Set<Concept> resultSet = new HashSet<Concept>();
            
            // the following code does not work with raxa encounter controller, but work with Rule Tester.
            /* 
            ActiveListService activeListService = Context.getActiveListService();
            List<Allergy> allergens = activeListService.getActiveListItems(Allergy.class, person, new ActiveListType(1));
            ConceptService conceptService = Context.getConceptService();
            Concept positiveReaction = conceptService.getConceptByName("YES");
            for(Concept concept: drugConcepts){
                for(Allergy allergy: allergens){
                    System.out.println("allergen: " + allergy.getAllergen().getName().getName() + " " + allergy.getReaction().getName().getName());               
                    if(allergy.getReaction().equals(positiveReaction) && allergy.getAllergen().equals(concept)){
                            resultSet.add(concept);
                    }
                }
                
            }
            */
            
            // we use mysql query to address the above problem
            StringBuilder query = new StringBuilder();
            query.append("SELECT DISTINCT a.concept_id");
            query.append(" FROM active_list a");
            query.append(" JOIN active_list_allergy aa ON (a.active_list_id = aa.active_list_id)");
            query.append(" WHERE  a.voided = 0");
            query.append(" AND aa.reaction_concept_id = 1065");
            query.append(" AND a.end_date > DATE(NOW())");
            query.append(" OR a.end_date is null");
            // scalar query
            SQLQuery qry = this.sessionFactory.getCurrentSession().createSQLQuery(query.toString());


            List<Integer> conceptIds = qry.list();
            
            ConceptService conceptService = Context.getConceptService();
            
            for(Integer conceptId: conceptIds){
                Concept concept = conceptService.getConcept(conceptId);
                if(drugConcepts.contains(concept)){
                    resultSet.add(concept);
                }                
            }
            
            if(resultSet.isEmpty()){
                System.out.println("active list is empty");
                return Collections.<Concept>emptySet();
            }
            return resultSet;
        }catch(Exception e){
            this.log.equals(e);
        }
        return Collections.<Concept>emptySet();
    }




}
