package org.openmrs.module.dss;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.dss.util.IOUtil;
import org.openmrs.module.dss.util.Util;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Checks periodically for classes to load dynamically
 *
 * @author Tammy Dugan
 */
public class ClassLoaderTask extends AbstractTask {

    private Log log = LogFactory.getLog(this.getClass());
    private String javaRuleDirectory = null;
    private String mlmRuleDirectory = null;
    private String classRulesDirectory = null;
    private String rulePackagePrefix = null;
    private boolean initialLoad = true;

    /**
     * Assigns locations of directories to look for files to compile/transform
     */
    public ClassLoaderTask() {
        log.info(this.getClass().getCanonicalName() + " is starting...");

        AdministrationService adminService = Context.getAdministrationService();

        // directory where the service can find the Java files
        String property = adminService.getGlobalProperty("dss.javaRuleDirectory");
        if (property == null) {
            log.error("You must set the global property dss.javaRuleDirectory");
        }
        this.javaRuleDirectory = IOUtil.formatDirectoryName(property);

        // directory where the service can find the MLM files
        property = adminService.getGlobalProperty("dss.mlmRuleDirectory");
        if (property == null) {
            log.error("You must set the global property dss.mlmRuleDirectory");
        }
        this.mlmRuleDirectory = IOUtil.formatDirectoryName(property);

        // directory where the classes will be located after compilation
        property = adminService.getGlobalProperty("dss.classRuleDirectory");
        if (property == null) {
            log.error("You must set the global property dss.classRuleDirectory");
        }
        this.classRulesDirectory = IOUtil.formatDirectoryName(property);

        // prefix for the DSS rule package
        property = adminService.getGlobalProperty("dss.rulePackagePrefix");
        if (property == null) {
            log.error("You must set the global property dss.rulePackagePrefix");
        }
        this.rulePackagePrefix = Util.formatPackagePrefix(property);

        log.info(ClassLoaderTask.class.getName() + " parameters: "
                + " javaRuleDirectory=" + javaRuleDirectory
                + " mlmRuleDirectory=" + mlmRuleDirectory
                + " classRulesDirectory=" + classRulesDirectory
                + " rulePackagePrefix=" + rulePackagePrefix);
    }

    @Override
    public void initialize(TaskDefinition config) {
        this.log.info("Initializing class loader task...");
        super.initialize(config);
        initialLoad = true;
        this.log.info("Finished initializing class loader task.");
    }

    @Override
    public void execute() {
        Context.openSession();
        try {
            lookForNewClasses();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(org.openmrs.module.dss.util.Util.getStackTrace(e));
        } finally {
            Context.closeSession();
        }
    }

    /**
     * Looks for mlm and java files to load as rule tokens
     */
    public void lookForNewClasses() {
        log.info("Looking for new classess...");

        HashSet<String> rules = new HashSet<String>();
        DssService dssService = Context.getService(DssService.class);

        // look for mlm file rule tokens
        HashMap<String, File> mlmFileMap = this.lookForRules(this.mlmRuleDirectory, ".mlm");

        // look for java file rule tokens
        HashMap<String, File> javaFileMap = this.lookForRules(this.javaRuleDirectory, ".java");

        String classDirectory = "";
        if (this.classRulesDirectory != null) {
            classDirectory = this.classRulesDirectory;
        }
        if (this.rulePackagePrefix != null) {
            classDirectory += this.rulePackagePrefix.replace('.', '/');
        }

        // look for java class file rule tokens
        HashMap<String, File> javaClassFileMap = this.lookForRules(classDirectory, ".class");

        // Check java rules against java class first
        Set<Entry<String, File>> entrySet = javaFileMap.entrySet();
        Iterator<Entry<String, File>> iter = entrySet.iterator();
        while (iter.hasNext()) {
            Entry<String, File> entry = iter.next();
            String filename = entry.getKey();
            File javaFile = entry.getValue();
            File classFile = javaClassFileMap.get(filename);
            if (initialLoad || classFile == null || (javaFile.lastModified() > classFile.lastModified())) {
                rules.add(filename);
            }
        }

        // Check mlm rules against java rules next
        entrySet = mlmFileMap.entrySet();
        iter = entrySet.iterator();
        while (iter.hasNext()) {
            Entry<String, File> entry = iter.next();
            String filename = entry.getKey();
            File mlmFile = entry.getValue();
            File javaFile = javaFileMap.get(filename);
            if (initialLoad || javaFile == null || (mlmFile.lastModified() > javaFile.lastModified())) {
                rules.add(filename);
            }
        }

        // load rule tokens into LogicService
        for (String ruleName : rules) {
            try {
                dssService.loadRule(ruleName, true);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error(Util.getStackTrace(e));
            }
        }

        initialLoad = false;
    }

    private HashMap<String, File> lookForRules(String directoryName, String ext) {
        log.info("Looking for rules with extension " + ext + " in directory " + directoryName);
        HashMap<String, File> files = new HashMap<String, File>();
        String[] fileExtensions = new String[]{ext};

        File[] filesInDirectory = IOUtil.getFilesInDirectory(directoryName, fileExtensions);
        if (filesInDirectory == null) {
            log.info("No files found.");
            return files;
        }

        int length = filesInDirectory.length;
        String currFile;

        log.info("Found " + length + " files with extension " + ext + ". Going to collect them...");
        for (int i = 0; i < length; i++) {
            File file = filesInDirectory[i];
            currFile = IOUtil.getFilenameWithoutExtension(file.getPath());
            if (currFile != null && currFile.length() > 0) {
                files.put(currFile, file);
            }
        }

        log.info("Collected " + files.size() + " files with extension " + ext);
        return files;
    }
}
