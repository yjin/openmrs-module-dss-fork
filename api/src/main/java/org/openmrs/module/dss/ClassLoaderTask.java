package org.openmrs.module.dss;

import java.io.File;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.dss.util.IOUtil;
import org.openmrs.module.dss.util.Util;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Checks periodically for classes to load dynamically
 *
 * @author Tammy Dugan
 */
public class ClassLoaderTask extends AbstractTask {

    private Log log = LogFactory.getLog(this.getClass());
    private TaskDefinition taskConfig;
    private String javaRuleDirectory = null;
    private String mlmRuleDirectory = null;

    /**
     * Assigns locations of directories to look for files to compile/transform
     */
    public ClassLoaderTask() {
        AdministrationService adminService = Context.getAdministrationService();
        String property = adminService.getGlobalProperty("dss.javaRuleDirectory");
        if (property == null) {
            log.error("You must set the global property dss.javaRuleDirectory");
        }
        this.javaRuleDirectory = IOUtil.formatDirectoryName(property);

        property = adminService.getGlobalProperty("dss.mlmRuleDirectory");
        if (property == null) {
            log.error("You must set the global property dss.mlmRuleDirectory");
        }
        this.mlmRuleDirectory = IOUtil.formatDirectoryName(property);
    }

    @Override
    public void initialize(TaskDefinition config) {
        this.log.info("Initializing class loader task...");
        this.taskConfig = config;
        this.log.info("Finished initializing class loader task.");
    }

    @Override
    public void execute() {
        Context.openSession();
        try {
            if (Context.isAuthenticated() == false) {
                authenticate();
            }
            lookForNewClasses();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
        } finally {
            Context.closeSession();
        }
    }

    /**
     * Looks for mlm and java files to load as rule tokens
     */
    public void lookForNewClasses() {
        HashSet<String> rules = new HashSet<String>();
        DssService dssService = Context.getService(DssService.class);

        //look for mlm file rule tokens
        this.lookForRules(this.mlmRuleDirectory, rules, ".mlm");

        //look for java file rule tokens
        this.lookForRules(this.javaRuleDirectory, rules, ".java");

        //load rule tokens into LogicService
        for (String ruleName : rules) {
            try {
                dssService.loadRule(ruleName, true);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error(Util.getStackTrace(e));
            }
        }
    }

    private void lookForRules(String directoryName, HashSet<String> rules, String ext) {
        String[] fileExtensions = new String[]{ext};

        File[] filesInDirectory = IOUtil.getFilesInDirectory(directoryName, fileExtensions);
        if (filesInDirectory == null) {
            return;
        }
        int length = filesInDirectory.length;
        String currFile = null;

        for (int i = 0; i < length; i++) {
            currFile = IOUtil.getFilenameWithoutExtension(filesInDirectory[i].getPath());
            if (currFile != null && currFile.length() > 0) {
                rules.add(currFile);
            }
        }
    }
}
