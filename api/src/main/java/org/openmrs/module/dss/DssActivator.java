package org.openmrs.module.dss;

import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.dss.util.Util;
import org.openmrs.notification.Alert;

/**
 * Purpose: Checks that module specific global properties have been set
 *
 * @author Tammy Dugan
 */
public class DssActivator extends BaseModuleActivator {

    private Log log = LogFactory.getLog(this.getClass());

    /**
     * @see org.openmrs.module.BaseModuleActivator#started()
     */
    @Override
    public void started() {
        log.info("Starting Dss Module");
        // check that all the required global properties are set
        checkGlobalProperties();
    }

    private void checkGlobalProperties() {
        boolean thereWereErrors = false;

        try {
            log.info("Checking if records in global_property are set up correctly...");
            AdministrationService adminService = Context.getAdministrationService();
            Context.authenticate(adminService
                    .getGlobalProperty("scheduler.username"), adminService
                    .getGlobalProperty("scheduler.password"));
            Iterator<GlobalProperty> properties = adminService.getAllGlobalProperties().iterator();
            GlobalProperty currProperty;
            String currValue;
            String currName;
            while (properties.hasNext()) {
                currProperty = properties.next();
                currName = currProperty.getProperty();
                if (currName.startsWith("dss")) {
                    currValue = currProperty.getPropertyValue();
                    if (currValue == null || currValue.length() == 0) {
                        this.log.error("You must set a value for global property: " + currName);
                        thereWereErrors = true;
                    }
                }
            }

        } catch (Exception e) {
            thereWereErrors = true;
            this.log.error("Error checking global properties for dss module");
            this.log.error(e.getMessage());
            this.log.error(Util.getStackTrace(e));

        } finally {
            if (thereWereErrors) {
                Alert alert = new Alert();
                alert.setText("There were erros while checking the global properties required by the DSS module. Please revise the logs for more details.");
                alert.addRecipient(Context.getAuthenticatedUser());
                Context.getAlertService().saveAlert(alert);
            }
        }
    }

    /**
     * @see org.openmrs.module.BaseModuleActivator#stopped()
     */
    @Override
    public void stopped() {
        this.log.info("Shutting down Dss Module");
    }
}
