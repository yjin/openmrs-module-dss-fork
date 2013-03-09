/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.dss.db.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
//import org.openmrs.Order;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.LogicTransform;
import org.openmrs.logic.op.Operand;
import org.openmrs.logic.op.OperandConcept;
import org.openmrs.logic.op.OperandDate;
import org.openmrs.logic.op.OperandNumeric;
import org.openmrs.logic.op.OperandText;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.util.LogicExpressionToCriterion;
import org.openmrs.module.dss.db.DssOrderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author kimye_000
 */

@Repository
public class HibernateDssOrderDAO extends LogicExpressionToCriterion implements DssOrderDAO {
	
	private static final String COMPONENT_ENCOUNTER_ID = "encounter";
	
	private static final String COMPONENT_ORDER_DATETIME = "dateCreated";
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	@Autowired
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public Criterion getCriterion(LogicExpression logicExpression, Date indexDate, Criteria criteria) {
		Operator operator = logicExpression.getOperator();
		Operand rightOperand = logicExpression.getRightOperand();
		Operand leftOperand = null;
		if (logicExpression instanceof LogicExpressionBinary) {
			leftOperand = ((LogicExpressionBinary) logicExpression).getLeftOperand();
		}
		List<Criterion> criterion = new ArrayList<Criterion>();
		
		//the root token can be a concept name for the obs datasource
		String rootToken = logicExpression.getRootToken();
		
		Concept concept = getConceptForToken(rootToken);
		if (concept != null) {
			criterion.add(Restrictions.eq("concept", concept));
		} else {
			if (rootToken != null
			        && (rootToken.equalsIgnoreCase(COMPONENT_ENCOUNTER_ID) || rootToken
			                .equalsIgnoreCase(COMPONENT_ORDER_DATETIME))) {
				//this is a component not a concept so it is fine
			} else {
				throw new LogicException("Concept: " + rootToken + " does not exist");
			}
		}
		
		if (operator == Operator.BEFORE) {
			criterion.add(Restrictions.lt("obsDatetime", rightOperand));
			
		} else if (operator == Operator.AFTER) {
			criterion.add(Restrictions.gt("obsDatetime", rightOperand));
			
		} else if (operator == Operator.AND || operator == Operator.OR) {
			
			Criterion leftCriteria = null;
			Criterion rightCriteria = null;
			
			if (leftOperand instanceof LogicExpression) {
				leftCriteria = this.getCriterion((LogicExpression) leftOperand, indexDate, criteria);
			}
			if (rightOperand instanceof LogicExpression) {
				rightCriteria = this.getCriterion((LogicExpression) rightOperand, indexDate, criteria);
			}
			
			if (leftCriteria != null && rightCriteria != null) {
				if (operator == Operator.AND) {
					criterion.add(Restrictions.and(leftCriteria, rightCriteria));
				}
				if (operator == Operator.OR) {
					criterion.add(Restrictions.or(leftCriteria, rightCriteria));
				}
			}
		} else if (operator == Operator.NOT) {
			
			Criterion rightCriteria = null;
			
			if (rightOperand instanceof LogicExpression) {
				rightCriteria = this.getCriterion((LogicExpression) rightOperand, indexDate, criteria);
			}
			
			if (rightCriteria != null) {
				criterion.add(Restrictions.not(rightCriteria));
			}
			
		} else if (operator == Operator.CONTAINS) {
			// used with PROBLEM ADDED concept, to retrieve the "ANSWERED
			// BY" concept, stashed inside the concept's valueCoded member
			// variable. for example:
			// new LogicCriteria("PROBLEM ADDED").contains("HIV INFECTED");
			if (rightOperand instanceof OperandNumeric) {
				concept = Context.getConceptService().getConcept(((OperandNumeric) rightOperand).asInteger());
				criterion.add(Restrictions.eq("valueCoded", concept));
			} else if (rightOperand instanceof OperandText) {
				concept = Context.getConceptService().getConcept((String) ((OperandText) rightOperand).asString());
				criterion.add(Restrictions.eq("valueCoded", concept));
				
			} else if (rightOperand instanceof OperandConcept) {
				criterion.add(Restrictions.eq("valueCoded", ((OperandConcept) rightOperand).asConcept()));
				
			} else
				log.error("Invalid operand value for CONTAINS operation");
		} else if (operator == Operator.IN) {
			log.error("Invalid operand value for IN operation");
		} else if (operator == Operator.EQUALS) {
			if (rightOperand instanceof OperandNumeric) {
				if (rootToken.equalsIgnoreCase(COMPONENT_ENCOUNTER_ID)) {
					EncounterService encounterService = Context.getEncounterService();
					Encounter encounter = encounterService.getEncounter(((OperandNumeric) rightOperand).asInteger());
					criterion.add(Restrictions.eq("encounter", encounter));
				} else
					criterion.add(Restrictions.eq("valueNumeric", ((OperandNumeric) rightOperand).asDouble()));
			} else if (rightOperand instanceof OperandText)
				criterion.add(Restrictions.eq("valueText", ((OperandText) rightOperand).asString()));
			else if (rightOperand instanceof OperandDate)
				if (leftOperand instanceof OperandText
				        && ((OperandText) leftOperand).asString().equals(COMPONENT_ORDER_DATETIME)) {
					criterion.add(Restrictions.eq(COMPONENT_ORDER_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.eq("valueDatetime", rightOperand));
				}
			else if (rightOperand instanceof OperandConcept)
				criterion.add(Restrictions.eq("valueCoded", ((OperandConcept) rightOperand).asConcept()));
			else
				log.error("Invalid operand value for EQUALS operation");
			
		} else if (operator == Operator.LTE) {
			if (rightOperand instanceof OperandNumeric)
				criterion.add(Restrictions.le("valueNumeric", ((OperandNumeric) rightOperand).asDouble()));
			else if (rightOperand instanceof OperandDate)
				if (leftOperand instanceof OperandText
				        && ((OperandText) leftOperand).asString().equals(COMPONENT_ORDER_DATETIME)) {
					criterion.add(Restrictions.le(COMPONENT_ORDER_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.le("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for LESS THAN EQUAL operation");
			
		} else if (operator == Operator.GTE) {
			if (rightOperand instanceof OperandNumeric)
				criterion.add(Restrictions.ge("valueNumeric", ((OperandNumeric) rightOperand).asDouble()));
			else if (rightOperand instanceof OperandDate)
				if (leftOperand instanceof OperandText
				        && ((OperandText) leftOperand).asString().equals(COMPONENT_ORDER_DATETIME)) {
					criterion.add(Restrictions.ge(COMPONENT_ORDER_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.ge("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for GREATER THAN EQUAL operation");
			
		} else if (operator == Operator.LT) {
			if (rightOperand instanceof OperandNumeric)
				criterion.add(Restrictions.lt("valueNumeric", ((OperandNumeric) rightOperand).asDouble()));
			else if (rightOperand instanceof OperandDate)
				if (leftOperand instanceof OperandText
				        && ((OperandText) leftOperand).asString().equals(COMPONENT_ORDER_DATETIME)) {
					criterion.add(Restrictions.lt(COMPONENT_ORDER_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.lt("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for LESS THAN operation");
			
		} else if (operator == Operator.GT) {
			if (rightOperand instanceof OperandNumeric)
				criterion.add(Restrictions.gt("valueNumeric", ((OperandNumeric) rightOperand).asDouble()));
			else if (rightOperand instanceof OperandDate)
				if (leftOperand instanceof OperandText
				        && ((OperandText) leftOperand).asString().equals(COMPONENT_ORDER_DATETIME)) {
					criterion.add(Restrictions.gt(COMPONENT_ORDER_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.gt("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for GREATER THAN operation");
			
		} else if (operator == Operator.EXISTS) {
			// EXISTS can be handled on the higher level (above
			// LogicService, even) by coercing the Result into a Boolean for
			// each patient
		} else if (operator == Operator.ASOF && rightOperand instanceof OperandDate) {
			indexDate = (Date) rightOperand;
			criterion.add(Restrictions.le("dateCreated", indexDate));
			
		} else if (operator == Operator.WITHIN && rightOperand instanceof Duration) {
			
			Duration duration = (Duration) rightOperand;
			Calendar within = Calendar.getInstance();
			within.setTime(indexDate);
			
			if (duration.getUnits() == Duration.Units.YEARS) {
				within.add(Calendar.YEAR, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.MONTHS) {
				within.add(Calendar.MONTH, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.WEEKS) {
				within.add(Calendar.WEEK_OF_YEAR, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.DAYS) {
				within.add(Calendar.DAY_OF_YEAR, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.HOURS) {
				within.add(Calendar.HOUR_OF_DAY, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.MINUTES) {
				within.add(Calendar.MINUTE, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.SECONDS) {
				within.add(Calendar.SECOND, duration.getDuration().intValue());
			}
			
			if (indexDate.compareTo(within.getTime()) > 0) {
				criterion.add(Restrictions.between("dateCreated", within.getTime(), indexDate));
			} else {
				criterion.add(Restrictions.between("dateCreated", indexDate, within.getTime()));
			}
		}
		
		Criterion c = null;
		
		for (Criterion crit : criterion) {
			if (c == null) {
				c = crit;
			} else {
				c = Restrictions.and(c, crit);
			}
		}
		return c;
	}
	
	/**
	 * Retrieves the Concept given the passed root token name.
	 * @param token the token to lookup
	 * @return the Concept that matches the passed token
	 */
	protected Concept getConceptForToken(String token) {
		return Context.getConceptService().getConceptByName(token);
	}
	
	// Helper function, converts logic service's criteria into Hibernate's
	// criteria
	@SuppressWarnings("unchecked")
	private List<org.openmrs.Order> logicToHibernate(LogicExpression expression, Cohort who, LogicContext logicContext) throws LogicException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(org.openmrs.Order.class);
		
		Date indexDate = logicContext.getIndexDate();
		Operator transformOperator = null;
		LogicTransform transform = expression.getTransform();
		Integer numResults = null;
		
		if (transform != null) {
			transformOperator = transform.getTransformOperator();
			numResults = transform.getNumResults();
		}
		
		if (numResults == null) {
			numResults = 1;
		}
		
		// set the transform and evaluate the right criteria
		// if there is any
		if (transformOperator == Operator.LAST) {
			criteria.addOrder(Order.desc("dateCreated")).addOrder(Order.desc("startDate")).addOrder(Order.desc("orderId"));
		} else if (transformOperator == Operator.FIRST) {
			criteria.addOrder(Order.asc("dateCreated")).addOrder(Order.asc("startDate")).addOrder(Order.asc("orderId"));
		} else if (transformOperator == Operator.DISTINCT) {
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		} else {
			criteria.addOrder(Order.desc("dateCreated"));
		}
		
		Criterion c = this.getCriterion(expression, indexDate, criteria);
		if (c != null) {
			criteria.add(c);
		}
		
		List<org.openmrs.Order> results = new ArrayList<org.openmrs.Order>();
		
		criteria.add(Restrictions.eq("voided", false));
                criteria.add(Restrictions.in("patient.personId", who.getMemberIds()));
		results.addAll(criteria.list());
		
		//return a single result per patient for these operators
		//I don't see an easy way to do this in hibernate so I am
		//doing some postprocessing
		if (transformOperator == Operator.FIRST || transformOperator == Operator.LAST) {
			HashMap<Integer, ArrayList<org.openmrs.Order>> nResultMap = new HashMap<Integer, ArrayList<org.openmrs.Order>>();
			
			for (org.openmrs.Order currResult : results) {
				Integer currPersonId = currResult.getPatient().getPersonId();
				ArrayList<org.openmrs.Order> prevResults = nResultMap.get(currPersonId);
				if (prevResults == null) {
					prevResults = new ArrayList<org.openmrs.Order>();
					nResultMap.put(currPersonId, prevResults);
				}
				
				if (prevResults.size() < numResults) {
					prevResults.add(currResult);
				}
			}
			
			if (nResultMap.values().size() > 0) {
				results.clear();
				
				for (ArrayList<org.openmrs.Order> currPatientObs : nResultMap.values()) {
					results.addAll(currPatientObs);
				}
			}
		}
		return results;
	}
	
	/**
	 * @throws LogicException
	 * @see org.openmrs.api.db.OrderDAO#getOrders(java.lang.Class, List, List, org.openmrs.api.OrderService.ORDER_STATUS, List, List, List)
	 */
	public List<org.openmrs.Order> getOrders(Cohort who, LogicCriteria logicCriteria, LogicContext logicContext) throws LogicException {
		log.debug("*** Reading observations ***");
		return logicToHibernate(logicCriteria.getExpression(), who, logicContext);
	}

	/**
	 * @see org.openmrs.module.dss.db.DssOrderDAO#getAllQuestionConceptIds()
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getAllQuestionConceptIds() {
		Query query = sessionFactory.getCurrentSession().createQuery("select conceptId from Concept where datatype.uuid != :naUuid");
		query.setString("naUuid", ConceptDatatype.N_A_UUID);
	    return query.list();
	}

}
