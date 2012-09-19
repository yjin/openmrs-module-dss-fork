package org.openmrs.module.dss.service;

import java.util.Date;
import java.util.List;

import org.openmrs.module.dss.hibernateBeans.EventLog;
import org.openmrs.module.dss.hibernateBeans.LocationAttributeValue;
import org.openmrs.module.dss.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.dss.hibernateBeans.LocationTagAttributeValue;
import org.springframework.transaction.annotation.Transactional;

/**
 * Defines services used by this module
 *
 * @author Tammy Dugan
 */
@Transactional
public interface ChirdlUtilService {

    public LocationTagAttributeValue getLocationTagAttributeValue(Integer locationTagId, String locationTagAttributeName,
            Integer locationId);

    public LocationAttributeValue getLocationAttributeValue(Integer locationId, String locationAttributeName);

    public LocationTagAttributeValue getLocationTagAttributeValueById(Integer location_tag_attribute_value_id);

    public LocationTagAttribute getLocationTagAttribute(Integer locationTagAttributeId);

    public LocationTagAttribute getLocationTagAttribute(String locationTagAttributeName);

    public LocationTagAttribute saveLocationTagAttribute(LocationTagAttribute value);

    public LocationTagAttributeValue saveLocationTagAttributeValue(LocationTagAttributeValue value);

    public LocationAttributeValue saveLocationAttributeValue(LocationAttributeValue value);

    public void deleteLocationTagAttribute(LocationTagAttribute value);

    public void deleteLocationTagAttributeValue(LocationTagAttributeValue value);

    /**
     * Logs an event to the database.
     *
     * @param eventLog The EventLog data to store in the database.
     *
     * @return The EventLog stored in the database.
     */
    public EventLog logEvent(EventLog eventLog);

    /**
     * Method to find event logs based on search criteria. Specify data for any
     * of the parameters to find matches in the log. All parameters can be
     * specified as null.
     *
     * @param eventId The event ID.
     * @param locationId The location ID.
     * @param formId The form ID.
     * @param studyId The study ID.
     * @param event The event. Constants for event can be found in the
     * org.openmrs.module.chirdlutil.log.LoggingConstants class.
     * @param startDate The starting date of the event to search.
     * @param endDate The ending date of the event to search.
     * @param userId The ID of the user committing the event.
     * @param description The description of the event.
     *
     * @return List of EventLog object matching the search criteria. This will
     * not return null.
     */
    public List<EventLog> getEventLogs(Integer eventId, Integer locationId, Integer formId, Integer studyId, String event,
            Date startDate, Date endDate, Integer userId, String description);
}