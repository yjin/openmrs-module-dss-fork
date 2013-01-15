package org.openmrs.module.dss.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;

/**
 * This class contains several utility methods and other modules that depend on
 * it.
 *
 * @author Tammy Dugan
 */
public class Util {

    protected static final Log log = LogFactory.getLog(Util.class);
    public static final String MEASUREMENT_LB = "lb";
    public static final String MEASUREMENT_IN = "in";
    public static final String MEASUREMENT_CM = "cm";
    public static final String MEASUREMENT_KG = "kg";
    public static final String YEAR_ABBR = "yo";
    public static final String MONTH_ABBR = "mo";
    public static final String WEEK_ABBR = "wk";
    public static final String DAY_ABBR = "do";

    /**
     * Converts specific measurements in English units to metric
     *
     * @param measurement measurement to be converted
     * @param measurementUnits units of the measurement
     * @return double metric value of the measurement
     */
    public static double convertUnitsToMetric(double measurement,
            String measurementUnits) {
        if (measurementUnits == null) {
            return measurement;
        }

        if (measurementUnits.equalsIgnoreCase(MEASUREMENT_IN)) {
            measurement = measurement * 2.54; // convert inches to centimeters
        }

        if (measurementUnits.equalsIgnoreCase(MEASUREMENT_LB)) {
            measurement = measurement * 0.45359237; // convert pounds to kilograms
        }
        return measurement; // truncate to one decimal
        // place
    }

    /**
     * Converts specific measurements in metric units to English
     *
     * @param measurement measurement to be converted
     * @param measurementUnits units of the measurement
     * @return double English value of the measurement
     */
    public static double convertUnitsToEnglish(double measurement,
            String measurementUnits) {
        if (measurementUnits == null) {
            return measurement;
        }

        if (measurementUnits.equalsIgnoreCase(MEASUREMENT_CM)) {
            measurement = measurement * 0.393700787; // convert centimeters to inches
        }

        if (measurementUnits.equalsIgnoreCase(MEASUREMENT_KG)) {
            measurement = measurement * 2.20462262; // convert kilograms to pounds
        }
        return measurement; // truncate to one decimal
        // place
    }

    /**
     * Returns the numeric part of a string input as a string
     *
     * @param input alphanumeric input
     * @return String all numeric
     */
    public static String extractIntFromString(String input) {
        if (input == null) {
            return null;
        }
        String[] tokens = Pattern.compile("\\D").split(input);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            result.append(tokens[i]);
        }
        return result.toString();
    }

    /**
     * Adds period if necessary to a package prefix
     *
     * @param packagePrefix a java package prefix
     * @return String formatted package prefix
     */
    public static String formatPackagePrefix(String packagePrefix) {
        if (packagePrefix != null && !packagePrefix.endsWith(".")) {
            packagePrefix += ".";
        }
        return packagePrefix;
    }

    /**
     * Parses a giving string into a list of package prefixes based on the
     * delimiter provided. This will also add a period (if necessary) to each of
     * the package prefixes. This will not return null.
     *
     * @param packagePrefixes one or more java package prefix
     * @param delimiter the delimiter that separates the package prefixes in the
     * packagePrefixes parameter.
     * @return List of Strings containing formatted package prefixes
     */
    public static List<String> formatPackagePrefixes(String packagePrefixes, String delimiter) {
        List<String> packagePrefixList = new ArrayList<String>();
        if (packagePrefixes == null) {
            return packagePrefixList;
        }

        StringTokenizer tokenizer = new StringTokenizer(packagePrefixes, delimiter, false);
        while (tokenizer.hasMoreTokens()) {
            String packagePrefix = tokenizer.nextToken().trim();
            if (packagePrefix.length() == 0) {
                continue;
            }

            packagePrefix = formatPackagePrefix(packagePrefix);
            packagePrefixList.add(packagePrefix);
        }

        return packagePrefixList;
    }

    public static String toProperCase(String str) {
        if (str == null || str.length() < 1) {
            return str;
        }

        StringBuffer resultString = new StringBuffer();
        String delimiter = " ";

        StringTokenizer tokenizer = new StringTokenizer(str, delimiter, true);

        String currToken = null;

        while (tokenizer.hasMoreTokens()) {
            currToken = tokenizer.nextToken();

            if (!currToken.equals(delimiter)) {
                if (currToken.length() > 0) {
                    currToken = currToken.substring(0, 1).toUpperCase()
                            + currToken.substring(1).toLowerCase();
                }
            }

            resultString.append(currToken);
        }

        return resultString.toString();
    }

    public static double getFractionalAgeInUnits(Date birthdate, Date today, String unit) {
        int ageInUnits = getAgeInUnits(birthdate, today, unit);
        Calendar birthdateCalendar = Calendar.getInstance();
        birthdateCalendar.setTime(birthdate);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);

        if (unit.equalsIgnoreCase(MONTH_ABBR)) {
            int todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH);
            int birthdateDayOfMonth = birthdateCalendar.get(Calendar.DAY_OF_MONTH);

            double dayDiff = todayDayOfMonth - birthdateDayOfMonth;

            if (dayDiff == 0) {
                return ageInUnits;
            }

            double daysInMonth = 0;

            if (dayDiff > 0) {
                daysInMonth = todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            } else {
                todayCalendar.add(Calendar.MONTH, -1);
                daysInMonth = todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                dayDiff = daysInMonth + dayDiff;
            }
            return ageInUnits + (dayDiff / daysInMonth);
        }
        if (unit.equalsIgnoreCase(YEAR_ABBR)) {
            int todayDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR);
            int birthdateDayOfYear = birthdateCalendar.get(Calendar.DAY_OF_YEAR);

            double dayDiff = todayDayOfYear - birthdateDayOfYear;

            if (dayDiff == 0) {
                return ageInUnits;
            }

            //code to handle leap years
            Integer daysInYear = 365;
            if (birthdateCalendar.getActualMaximum(Calendar.DAY_OF_YEAR) > 365
                    || todayCalendar.getActualMaximum(Calendar.DAY_OF_YEAR) > 365) {
                dayDiff--;
            }

            if (dayDiff < 0) {
                todayCalendar.add(Calendar.YEAR, -1);
                dayDiff = daysInYear + dayDiff;
            }
            return ageInUnits + (dayDiff / daysInYear);
        }
        if (unit.equalsIgnoreCase(WEEK_ABBR)) {
            int todayDayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK);
            int birthdateDayOfWeek = birthdateCalendar.get(Calendar.DAY_OF_WEEK);

            int dayDiff = todayDayOfWeek - birthdateDayOfWeek;

            if (dayDiff == 0) {
                return ageInUnits;
            }

            int daysInWeek = 0;

            if (dayDiff > 0) {
                daysInWeek = todayCalendar.getActualMaximum(Calendar.DAY_OF_WEEK);
            } else {
                todayCalendar.add(Calendar.WEEK_OF_YEAR, -1);
                daysInWeek = todayCalendar.getActualMaximum(Calendar.DAY_OF_WEEK);
                dayDiff = daysInWeek + dayDiff;
            }
            return ageInUnits + (dayDiff / daysInWeek);
        }
        return ageInUnits;
    }

    /**
     * Returns a person's age in the specified units (days, weeks, months,
     * years)
     *
     * @param birthdate person's date of birth
     * @param today date to calculate age from
     * @param unit unit to calculate age in (days, weeks, months, years)
     * @return int age in the given units
     */
    public static int getAgeInUnits(Date birthdate, Date today, String unit) {
        if (birthdate == null) {
            return 0;
        }

        if (today == null) {
            today = new Date();
        }

        int diffMonths = 0;
        int diffDayOfMonth = 0;
        int diffDayOfYear = 0;
        int years = 0;
        int months = 0;
        int days = 0;

        Calendar birthdateCalendar = Calendar.getInstance();
        birthdateCalendar.setTime(birthdate);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(today);

        // return 0 if the birthdate is after today
        if (birthdate.compareTo(today) > 0) {
            return 0;
        }

        years = todayCalendar.get(Calendar.YEAR)
                - birthdateCalendar.get(Calendar.YEAR);

        diffMonths = todayCalendar.get(Calendar.MONTH)
                - birthdateCalendar.get(Calendar.MONTH);
        diffDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR)
                - birthdateCalendar.get(Calendar.DAY_OF_YEAR);

        diffDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
                - birthdateCalendar.get(Calendar.DAY_OF_MONTH);

        months = years * 12;
        months += diffMonths;

        days = years * 365;
        days += diffDayOfYear;

        if (unit.equalsIgnoreCase(YEAR_ABBR)) {
            if (diffMonths < 0) {
                years--;
            } else if (diffMonths == 0 && diffDayOfYear < 0) {
                years--;
            }
            return years;
        }

        if (unit.equalsIgnoreCase(MONTH_ABBR)) {
            if (diffDayOfMonth < 0) {
                months--;
            }
            return months;
        }

        if (unit.equalsIgnoreCase(WEEK_ABBR)) {
            return days / 7;
        }

        if (days < 0) {
            days = 0;
        }
        return days;
    }

    public static Double round(Double value, int decimalPlaces) {
        if (decimalPlaces < 0 || value == null) {
            return value;
        }

        double intermVal = value * Math.pow(10, decimalPlaces);
        intermVal = Math.round(intermVal);
        return intermVal / (Math.pow(10, decimalPlaces));
    }

    public static String getStackTrace(Exception x) {
        OutputStream buf = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(buf);
        x.printStackTrace(p);
        return buf.toString();
    }

    public static String archiveStamp() {
        Date currDate = new java.util.Date();
        String dateFormat = "yyyyMMdd-kkmmss-SSS";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String formattedDate = formatter.format(currDate);
        return formattedDate;
    }

    public static boolean isToday(Date date) {
        if (date != null) {
            Calendar today = Calendar.getInstance();
            Calendar dateToCompare = Calendar.getInstance();
            dateToCompare.setTime(date);
            return (today.get(Calendar.ERA) == dateToCompare.get(Calendar.ERA)
                    && today.get(Calendar.YEAR) == dateToCompare.get(Calendar.YEAR) && today
                    .get(Calendar.DAY_OF_YEAR) == dateToCompare
                    .get(Calendar.DAY_OF_YEAR));

        }
        return false;
    }

    public static String removeTrailingZeros(String str1) {

        char[] chars = str1.toCharArray();
        int index = chars.length - 1;
        for (; index >= 0; index--) {
            if (chars[index] != '0') {
                break;
            }
        }
        if (index > -1) {
            return str1.substring(0, index + 1);
        }
        return str1;
    }

    public static String removeLeadingZeros(String mrn) {

        char[] chars = mrn.toCharArray();
        int index = 0;
        for (; index < chars.length; index++) {
            if (chars[index] != '0') {
                break;
            }
        }
        if (index > -1) {
            return mrn.substring(index);
        }
        return mrn;
    }

    public static Obs saveObs(Patient patient, Concept currConcept, int encounterId, String value,
            Date obsDatetime) {
        if (value == null || value.length() == 0) {
            return null;
        }

        ObsService obsService = Context.getObsService();
        Obs obs = new Obs();
        String datatypeName = currConcept.getDatatype().getName();

        if (datatypeName.equalsIgnoreCase("Numeric")) {
            try {
                obs.setValueNumeric(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                log.error("Could not save value: " + value + " to the database for concept "
                        + currConcept.getName().getName());
            }
        } else if (datatypeName.equalsIgnoreCase("Coded")) {
            ConceptService conceptService = Context.getConceptService();
            Concept answer = conceptService.getConceptByName(value);
            if (answer == null) {
                log.error(value + " is not a valid concept name. " + value + " will be stored as text.");
                obs.setValueText(value);
            } else {
                obs.setValueCoded(answer);
            }
        } else if (datatypeName.equalsIgnoreCase("Date")) {
            Date valueDatetime = new Date(Long.valueOf(value));
            obs.setValueDatetime(valueDatetime);
        } else {
            obs.setValueText(value);
        }

        EncounterService encounterService = Context.getService(EncounterService.class);
        Encounter encounter = encounterService.getEncounter(encounterId);

        Location location = encounter.getLocation();

        obs.setPerson(patient);
        obs.setConcept(currConcept);
        obs.setLocation(location);
        obs.setEncounter(encounter);
        obs.setObsDatetime(obsDatetime);
        obsService.saveObs(obs, null);
        return obs;
    }

    /**
     * Calculates age to a precision of days, weeks, months, or years based on a
     * set of rules
     *
     * @param birthdate patient's birth date
     * @param cutoff date to calculate age from
     * @return String age with units
     */
    public static String adjustAgeUnits(Date birthdate, Date cutoff) {
        int years = getAgeInUnits(birthdate, cutoff, YEAR_ABBR);
        int months = getAgeInUnits(birthdate, cutoff, MONTH_ABBR);
        int weeks = getAgeInUnits(birthdate, cutoff, WEEK_ABBR);
        int days = getAgeInUnits(birthdate, cutoff, DAY_ABBR);

        if (years >= 2) {
            return years + " " + YEAR_ABBR;
        }

        if (months >= 2) {
            return months + " " + MONTH_ABBR;
        }

        if (days > 30) {
            return weeks + " " + WEEK_ABBR;
        }

        return days + " " + DAY_ABBR;
    }

    public static String computeMD5(String strToMD5) throws DigestException {
        try {
            // get md5 of input string
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(strToMD5.getBytes());
            byte[] bytes = md.digest();

            // convert md5 bytes to a hex string
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                hexString.append(Integer.toHexString(0xFF & bytes[i]));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new DigestException("couldn't make digest of partial content");
        }
    }
}