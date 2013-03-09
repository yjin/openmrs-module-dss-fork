/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.dss.db;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;


public interface DssOrderDAO {

	/**
	 * @see org.openmrs.module.dss.db.hibernate.HibernateOrderDAO#getOrders(List, List, List, List,
	 *      List, List, List, Integer, Integer, java.util.Date, java.util.Date, boolean)
	 */
	public List<Order> getOrders(Cohort who, LogicCriteria logicCriteria, LogicContext logicContext) throws LogicException;
	
	/**
	 * @return ids of all concepts which may be used as questions (i.e. their datatype is not N/A)
	 */
	public List<Integer> getAllQuestionConceptIds();

}

