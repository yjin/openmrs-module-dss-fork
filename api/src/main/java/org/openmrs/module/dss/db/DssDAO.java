package org.openmrs.module.dss.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dss related database functions
 *
 * @author Tammy Dugan
 */
@Transactional
public interface DssDAO {

    /**
     * Looks up a rule from the dss_rule table by rule_id
     *
     * @param ruleId unique identifier for rule in the dss_rule table
     * @return Rule from the dss_rule table
     */
    public Rule getRule(int ruleId);

    /**
     * Looks up a rule from the dss_rule table by token name
     *
     * @param tokenName name that is used to register a rule with the openmrs LogicService
     * @return Rule from the dss_rule table
     */
    public Rule getRule(String tokenName);

    /**
     *
     * @return
     */
    public List<Rule> getPrioritizedRules();

    /**
     *
     * @param type
     * @return
     */
    public List<Rule> getPrioritizedRules(String type);

    /**
     *
     * @param concept
     * @return
     */
    public List<Rule> getPrioritizedRulesByConcept(Concept concept);

    /**
     *
     * @param concepts
     * @return
     */
    public List<Rule> getPrioritizedRulesByConcepts(Set<Concept> concepts);

    /**
     *
     * @param encounter
     * @return
     */
    public List<Rule> getPrioritizedRulesByConceptsInEncounter(Encounter encounter);

    /**
     *
     * @param type
     * @return
     */
    public List<Rule> getNonPrioritizedRules(String type);

    /**
     * Returns a list of rules from the dss_rule table that match the criteria assigned to the rule parameter
     *
     * @param rule Rule whose assigned attributes indicate the restrictions of the dss_rule table query
     * @param ignoreCase String attributes assigned in the Rule parameter should be matched in the dss_rule query
     * regardless of case
     * @param enableLike String attributes assigned in the Rule parameter should be matched in the dss_rule query using
     * LIKE instead of exact matching
     * @return List<Rule>
     */
    public List<Rule> getRules(Rule rule, boolean ignoreCase,
            boolean enableLike, String sortColumn);

    /**
     * Adds a new rule to the dss_rule table
     *
     * @param rule new rule to add to the dss_rule table
     * @return Rule added to the dss_rule table
     */
    public Rule addOrUpdateRule(Rule rule);

    /**
     * Deletes an existing rule in the dss_rule table
     *
     * @param ruleId unique id of the rule to delete
     */
    public void deleteRule(int ruleId);

    /**
     *
     * @param rule
     * @param concepts
     */
    public boolean addMapping(Rule rule, List<Concept> concepts);

    /**
     *
     * @param rule
     * @return
     */
    public List<Concept> getMappings(Rule rule);

    /**
     *
     * @param rule
     * @return
     */
    public List<Rule> getMappings(Concept concept);

    /**
     * 
     * @param encounter
     * @return 
     */
    public Map<Concept,List<Concept>> getDrugRecommendationByDiagnosisConceptsInEncounter(Encounter encounter);

    /**
     * 
     * @param concepts
     * @return 
     */
    public Map<Concept,List<Concept>> getDrugRecommendationByDiagnosisConcepts(Set<Concept> concepts);

    /**
     * 
     * @param concept
     * @return 
     */
    public List<Concept> getDrugRecommendationByDiagnosisConcept(Concept concept);

    /**
     * 
     * @param encounter
     * @return 
     */
    public List<Concept> getDrugInteractionsForEncounter(Encounter encounter);

    public List<Concept> getDrugInteractionsByConcepts(Set<Concept> concepts, Integer patientId);

    public List<Concept> getDrugInteractionsByConcept(Concept concept, Integer patientId);
}
