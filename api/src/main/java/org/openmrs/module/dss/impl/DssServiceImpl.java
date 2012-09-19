package org.openmrs.module.dss.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.result.Result;
import org.openmrs.module.dss.DssRule;
import org.openmrs.module.dss.CompilingClassLoader;
import org.openmrs.module.dss.db.DssDAO;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.dss.util.IOUtil;
import org.openmrs.module.dss.util.Util;

/**
 * Defines implementations of services used by this module
 *
 * @author Tammy Dugan
 *
 */
public class DssServiceImpl implements DssService {

    private Log log = LogFactory.getLog(this.getClass());
    private DssDAO dao;

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
     * Sets the DAO for this service. The dao allows interaction with the
     * database.
     *
     * @param dao
     */
    public void setDssDAO(DssDAO dao) {
        this.dao = dao;
    }

    public String runRulesAsString(Patient p,
            ArrayList<Rule> ruleList,
            String defaultPackagePrefix, String rulePackagePrefix) {
        ArrayList<Result> results = this.runRules(p, ruleList,
                defaultPackagePrefix, rulePackagePrefix);
        String reply = "";

        if (results == null || results.size() == 0) {
            return "No rules run!!!!";
        }

        for (Result result : results) {
            if (result != null) {
                reply += result + "\n";
            }
        }
        return reply;
    }

    public Result runRule(Patient p, Rule rule,
            String defaultPackagePrefix, String rulePackagePrefix) {
        ArrayList<Rule> ruleList = new ArrayList<Rule>();
        ruleList.add(rule);

        ArrayList<Result> results =
                this.runRules(p, ruleList, defaultPackagePrefix, rulePackagePrefix);

        //Since we ran only one rule, we will only have
        //one result object in the list of returned results
        //the single result object could have multiple results
        //depending on how the single rule was evaluated
        if (results != null && results.size() > 0) {
            return results.get(0);
        }

        return Result.emptyResult();
    }

    public ArrayList<Result> runRules(Patient p,
            ArrayList<Rule> ruleList,
            String defaultPackagePrefix, String rulePackagePrefix) {
        ArrayList<Result> results = new ArrayList<Result>();
        Map<String, Object> parameters = null;
        String ruleName = null;
        LogicService logicSvc = Context.getLogicService();

        try {
            for (Rule rule : ruleList) {
                ruleName = rule.getTokenName();
                parameters = rule.getParameters();

                try {
                    this.loadRule(ruleName, defaultPackagePrefix, rulePackagePrefix, false);
                } catch (APIAuthenticationException e) {
                    //ignore a privilege exception
                } catch (Exception e1) {
                    log.error("Error loading rule: " + ruleName);
                    log.error(e1.getMessage());
                    log.error(Util.getStackTrace(e1));
                    results.add(null);
                    continue;
                }

                Result result;
                try {
                    result = logicSvc.eval(p, ruleName, parameters);
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

            }

            return results;

        } catch (Exception e) {
            log.error("Error running rules.");
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            return null;
        }
    }

    public void loadRule(String rule, boolean updateRule) throws Exception {
        this.loadRule(rule, null, null, updateRule);
    }

    public void loadRule(String rule, String defaultPackagePrefix,
            String rulePackagePrefix, boolean updateRule) throws Exception {
        LogicService logicService = Context.getLogicService();

        //if we don't want to update the rule and the rule token
        //already exists in the logic service, don't reload the class
        if (!updateRule && logicService.getTokens().contains(rule)) {
            return;
        }

        // Create a CompilingClassLoader
        CompilingClassLoader ccl = new CompilingClassLoader();

        AdministrationService adminService = Context.getAdministrationService();
        if (rulePackagePrefix == null) {
            rulePackagePrefix = Util.formatPackagePrefix(adminService
                    .getGlobalProperty("dss.rulePackagePrefix"));
        }

        Class clas = null;

        // try to load the class dynamically
        if (!rule.contains(rulePackagePrefix)) {
            try {
                clas = ccl.loadClass(rulePackagePrefix + rule);
            } catch (Exception e) {
                //ignore this exception
            }
        } else {
            try {
                clas = ccl.loadClass(rule);
            } catch (Exception e) {
                //ignore this exception
            }
        }

        // try to load the class from the class library
        if (clas == null && defaultPackagePrefix != null) {
            if (!rule.contains(defaultPackagePrefix)) {
                try {
                    clas = ccl.loadClass(defaultPackagePrefix + rule);
                } catch (Exception e) {
                    //ignore this exception
                }
            } else {
                try {
                    clas = ccl.loadClass(rule);
                } catch (Exception e) {
                    //ignore this exception
                }
            }
        }

        // try to load the class as it is
        if (clas == null) {
            try {
                clas = ccl.loadClass(rule);
            } catch (Exception e) {
                //ignore this exception
            }
        }

        if (clas == null) {
            throw new Exception("Could not load class for rule: " + rule);
        }

        Object obj = null;
        try {
            obj = clas.newInstance();
        } catch (Exception e) {
            log.error("", e);
        }

        if (!(obj instanceof org.openmrs.logic.Rule)) {
            throw new Exception("Could not load class for rule: " + rule
                    + ". The rule must implement the Rule interface.");
        }

        try {
            logicService.updateRule(rule, (org.openmrs.logic.Rule) obj);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public Rule getRule(int ruleId) throws APIException {
        return getDssDAO().getRule(ruleId);
    }

    @Override
    public List<Rule> getPrioritizedRules(String type) throws DAOException {
        return getDssDAO().getPrioritizedRules(type);
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
    public Rule addRule(String classFilename, DssRule rule) throws APIException {
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
}