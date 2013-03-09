/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.dss.result;

import java.util.Date;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.logic.result.Result;

/**
 *
 * @author kimye_000
 */
public class DssResult extends Result {
    
    public DssResult(Order order) {
		super(new Date(), null, null, null, null, null, null, order);
		
		Concept concept = order.getConcept();
		ConceptDatatype conceptDatatype = null;
		
		if (concept != null) {
			conceptDatatype = concept.getDatatype();
			
			if (conceptDatatype == null) {
				return;
			}
			if (conceptDatatype.isCoded())
				setDatatype(Datatype.CODED);
			else if (conceptDatatype.isNumeric())
				setDatatype(Datatype.NUMERIC);
			else if (conceptDatatype.isDate())
				setDatatype(Datatype.DATETIME);
			else if (conceptDatatype.isText())
				setDatatype(Datatype.TEXT);
			else if (conceptDatatype.isBoolean())
				setDatatype(Datatype.BOOLEAN);
                      
		} 
    }

}
