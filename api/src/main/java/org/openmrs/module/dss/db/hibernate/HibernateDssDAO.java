package org.openmrs.module.dss.db.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
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
}
