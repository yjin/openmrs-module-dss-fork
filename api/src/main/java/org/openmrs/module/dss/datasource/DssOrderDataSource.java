/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.dss.datasource;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.db.LogicObsDAO;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.rule.provider.RegisterAtStartupDataSourceRuleProvider;
import org.openmrs.logic.rule.provider.RuleProvider;
import org.openmrs.logic.util.LogicUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.openmrs.Order;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.module.dss.db.DssOrderDAO;
import org.openmrs.module.dss.result.DssResult;

/**
 * Provides access to clinical drug orders. The keys for this data source are the primary names of
 * all tests within the concept dictionary. Results have a result date equal to the observation
 * datetime and a value based on the observed value.
 * TODO either make {@link RegisterAtStartupDataSourceRuleProvider} more efficient, or else write a custom {@link #afterStartup()} method.
 */
@Repository
public class DssOrderDataSource extends RegisterAtStartupDataSourceRuleProvider implements RuleProvider, LogicDataSource {

	public static final String NAME = "order";

	private static final Collection<String> keys = new ArrayList<String>();

	@Autowired
	private DssOrderDAO dssOrderDAO;

	public void setDssOrderDAO(DssOrderDAO dssOrderDAO) {
		this.dssOrderDAO = dssOrderDAO;
	}

	public DssOrderDAO getDssOrderDAO() {
		return dssOrderDAO;
	}

	/**
	 * @throws LogicException
	 * @should get all orders
	 * @should get first orders
	 * @should get last orders
	 * @should get orders gt value
	 * @should get orders ge value
	 * @should get orders lt value
	 * @should get orders le value
	 * @should get orders eq value
	 * @should get orders obs if it is lt value
	 * @should get orders obs of those lt value
	 * @should get orders before date
	 * @should get orders after date
	 * @should get orders gt value after date
	 * @should get last orders if it is before date
	 * @should get last orders of those before date
	 * @should get first n orders
	 * @should get last n orders
	 * @should get first n orders of those lt value
	 * @should get first n orders if they are lt value
	 * @should get not of a clause
	 * @should get and of two clauses
	 * @should get or of two clauses
	 * @should get average of orders
	 * @should get average of null when no orders
	 * @should get count of orders when orders
	 * @should get count of zero when no orders
	 * @should get return orders ordered by datetime
	 * @see org.openmrs.logic.datasource.LogicDataSource#read(org.openmrs.logic.LogicContext,
	 *      org.openmrs.Cohort, org.openmrs.logic.LogicCriteria)
	 */
	public Map<Integer, Result> read(LogicContext context, Cohort patients, LogicCriteria criteria) throws LogicException {

		Map<Integer, Result> finalResult = new HashMap<Integer, Result>();
		// TODO: make the order service method more efficient (so we don't have to re-organize
		// into groupings by patient...or it can be done most expeditiously
		List<Order> orders = getDssOrderDAO().getOrders(patients, criteria, context);

		// group the received observations by patient and convert them to
		// Results
		for (Order order : orders) {
			int personId = order.getPatient().getPersonId();
			Result result = finalResult.get(personId);
			if (result == null) {
				result = new Result();
				finalResult.put(personId, result);
			}

			result.add(new DssResult(order));
		}

		LogicUtil.applyAggregators(finalResult, criteria, patients);

		return finalResult;
	}

	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#getDefaultTTL()
	 */
	public int getDefaultTTL() {
		return 60 * 30; // 30 minutes
	}

	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#getKeys()
	 */
	public Collection<String> getKeys() {
		return keys;
	}

	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#hasKey(java.lang.String)
	 */
	public boolean hasKey(String key) {
		Concept concept = Context.getConceptService().getConcept(key);
		if (concept == null)
			return false;
		else
			return true;
	}

	public void addKey(String key) {
		getKeys().add(key);
	}

	/**
	 * @see org.openmrs.logic.rule.provider.RegisterAtStartupDataSourceRuleProvider#getAllKeysAndTokens()
	 */
	@Override
	public Map<String, String> getAllKeysAndTokens() {
		Map<String, String> ret = new HashMap<String, String>();

		// determine which locale use for names from
		Locale conceptNameLocale = Locale.US;
		String localeProp = Context.getAdministrationService().getGlobalProperty("logic.defaultTokens.conceptNameLocale");
		if (localeProp != null)
			conceptNameLocale = new Locale(localeProp);

		// get all Concepts in specified classes
		List<ConceptClass> conceptClasses = new ArrayList<ConceptClass>();
		String classProp = Context.getAdministrationService().getGlobalProperty("logic.defaultTokens.conceptClasses");
		if (StringUtils.isNotEmpty(classProp))
			for (String className : classProp.split(","))
				conceptClasses.add(Context.getConceptService().getConceptClassByName(className));
		else
			conceptClasses = Context.getConceptService().getAllConceptClasses();

		for (ConceptClass currClass : conceptClasses) {
			for (Concept c : Context.getConceptService().getConceptsByClass(currClass)) {
				if (!c.getDatatype().isAnswerOnly()) {
					ConceptName conceptName = c.getPreferredName(conceptNameLocale);
					if (conceptName != null)
						ret.put(c.getConceptId().toString(), conceptName.getName());
				}
			}
		}

		return ret;
	}

}

