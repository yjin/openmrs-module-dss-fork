package org.openmrs.module.dss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.arden.MlmRule;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.dss.util.IOUtil;
import org.openmrs.module.dss.util.Util;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.arden.MlmRule;

/**
 * A CompilingClassLoader compiles your Java source on-the-fly. It checks for nonexistent .class files, or .class files
 * that are older than their corresponding source code.
 *
 * @Author - Vibha Anand - Adapted from ibm.com/developerWorks
 */
public class CompilingClassLoader extends OpenmrsClassLoader {

    private Log log = LogFactory.getLog(this.getClass());
    private String rulePackagePrefix = null;
    private String javaRuleDirectory = null;
    private String classRulesDirectory = null;
    private String archivedMLMRuleDirectory = null;
    private String archivedJavaRuleDirectory = null;
    private String mlmRuleDirectory = null;
    private boolean clearMLMFiles = false;
    private boolean clearJavaFiles = false;
    private Map<String, Class<?>> classMap = Collections.synchronizedMap(new HashMap<String, Class<?>>());

    /**
     * Private class to hold the one classloader used throughout openmrs. This is an alternative to storing the instance
     * object on {@link CompilingClassLoader} itself so that garbage collection can happen correctly.
     */
    private static class CompilingClassLoaderHolder {

        private static CompilingClassLoader INSTANCE = null;
    }

    /**
     * Get the static/singular instance of the module class loader
     *
     * @return CompilingClassLoader
     */
    public static CompilingClassLoader getInstance() {
        if (CompilingClassLoaderHolder.INSTANCE == null) {
            CompilingClassLoaderHolder.INSTANCE = new CompilingClassLoader();
        }

        return CompilingClassLoaderHolder.INSTANCE;
    }

    private CompilingClassLoader(ClassLoader parent) {
        super(parent);
        setupClassLoader();
    }

    private CompilingClassLoader() {
        this(CompilingClassLoader.class.getClassLoader());
    }

    private void setupClassLoader() {
        AdministrationService adminService = Context.getAdministrationService();

        String property = adminService
                .getGlobalProperty("dss.rulePackagePrefix");
        this.rulePackagePrefix = Util.formatPackagePrefix(property);

        property = adminService.getGlobalProperty("dss.javaRuleDirectory");
        this.javaRuleDirectory = IOUtil.formatDirectoryName(property);

        property = adminService.getGlobalProperty("dss.classRuleDirectory");
        this.classRulesDirectory = IOUtil.formatDirectoryName(property);

        property = adminService
                .getGlobalProperty("dss.archivedMLMRuleDirectory");
        this.archivedMLMRuleDirectory = IOUtil.formatDirectoryName(property);

        property = adminService
                .getGlobalProperty("dss.archivedJavaRuleDirectory");
        this.archivedJavaRuleDirectory = IOUtil.formatDirectoryName(property);

        property = adminService.getGlobalProperty("dss.mlmRuleDirectory");
        this.mlmRuleDirectory = IOUtil.formatDirectoryName(property);

        try {
            this.clearMLMFiles = Boolean.parseBoolean(adminService
                    .getGlobalProperty("dss.clearMLMFiles"));
        } catch (Exception e1) {
        }

        try {
            this.clearJavaFiles = Boolean.parseBoolean(adminService
                    .getGlobalProperty("dss.clearJavaFiles"));
        } catch (Exception e) {
        }
    }

    // Given a filename, read the entirety of that file from disk
    // and return it as a byte array.
    private byte[] getBytes(String filename) throws IOException {
        FileInputStream fileInput = new FileInputStream(filename);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        IOUtil.bufferedReadWrite(fileInput, output);

        return output.toByteArray();
    }

    // Compile the java source code file
    // specified in the 'javaFile' parameter. Return a true if
    // the compilation worked, false otherwise.
    private boolean compile(String javaFile) {
        log.info("CCL: Compiling " + javaFile + "...");

        String classpath = getClasspath();

        log.debug("CLASSPATH is " + classpath);

        try {
            if (this.classRulesDirectory == null) {
                log.error("Global property dss.classRuleDirectory must be set.");
                throw new Exception("Global property dss.classRuleDirectory must be set.");
            }
            String[] options = new String[]{"-classpath", classpath, "-d", this.classRulesDirectory};
            log.debug("Options are {" + options[0] + "," + options[1] + "," + options[2] + "," + options[3] + "}");

            log.debug("Going to load file: " + javaFile);
            File file = new File(javaFile);

            if (file == null) {
                log.error("File is NULL!");
            } else {
                log.info("File loaded.");
            }

            log.debug("Going to get the SystemJavaCompiler...");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            log.debug("Going to get the StandardFileManager...");
            StandardJavaFileManager fileManager = compiler
                    .getStandardFileManager(null, null, null);

            log.debug("Going to get the JavaFileObjects...");
            Iterable<? extends JavaFileObject> fileObjects = fileManager
                    .getJavaFileObjects(file);
            StringWriter writer = new StringWriter();

            log.info("Going to compile...");
            boolean success = compiler.getTask(writer, fileManager, null, Arrays.asList(options), null, fileObjects).call();

            if (success) {
                log.info("Success!");
            } else {
                log.info("Error :(");
                log.error(writer.toString());
            }

            fileManager.close();
            return success;
        } catch (Exception e) {
            log.error("Error compiling java rule file: " + javaFile);
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
        }
        return false;
    }

    /**
     * Builds a classpath string by looking at different class loaders
     *
     * @return String classpath
     */
    public String getClasspath() {
        log.debug("Going to build CLASSPATH...");
        HashSet<String> classpathFiles = new HashSet<String>();

        Collection<ModuleClassLoader> moduleClassLoaders =
                ModuleFactory.getModuleClassLoaders();

        // check module dependencies
        for (ModuleClassLoader classLoader : moduleClassLoaders) {
            URL[] urls = classLoader.getURLs();
            for (URL url : urls) {
                classpathFiles.add(url.getPath());
            }
        }

        // check current class loader and all its parents
        ClassLoader currClassLoader = Thread.currentThread().getContextClassLoader();
        while (currClassLoader != null) {
            if (currClassLoader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) currClassLoader).getURLs();

                for (URL url : urls) {
                    classpathFiles.add(url.getPath());
                }
            }
            currClassLoader = currClassLoader.getParent();
        }

        // build CLASSPATH
        Iterator<String> classpathEntries = classpathFiles.iterator();
        StringBuilder classpathBuilder = new StringBuilder();
        while (classpathEntries.hasNext()) {
            if (classpathBuilder.length() > 0) {
                classpathBuilder.append(java.io.File.pathSeparator);
            }
            classpathBuilder.append(classpathEntries.next());
        }

        String classpath = classpathBuilder.toString().replaceAll("%20", " ");
        log.debug("CLASSPATH is " + classpath);

        // return CLASSPATH
        return classpath;
    }

    /**
     * Parse the mlm file to java source code file specified in the 'mlmFile' parameter. Return a true if the parsing
     * worked, false otherwise.
     *
     * @param mlmFile path to mlm file to convert to java file
     * @return boolean true if the mlm file was successfully converted to a java file
     */
    public boolean parse(String mlmFile) {
        // Let the user know what's going on
        log.info("CCL: Parsing " + mlmFile + "...");

        int errorCode = -1;
        try {
            if (this.javaRuleDirectory == null) {
                throw new Exception("Global property dss.javaRuleDirectory must be set.");
            }
            Context.getArdenService().compileFile(mlmFile, this.javaRuleDirectory); // converts from mlm to java
            // For now
            errorCode = 0;

        } catch (Exception e) {
            log.error("Error compiling mlm rule file: " + mlmFile);
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
        }

        return errorCode == 0;
    }

    /**
     * @see org.openmrs.util.OpenmrsClassLoader#loadClass(java.lang.String, boolean)
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        Class<?> clas = loadClass(name);
        if (resolve) {
            resolveClass(clas);
        }
        return clas;
    }

    /**
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        log.info("Trying to load class " + name + "...");
        Class classObject;
        try {
            classObject = getParent().loadClass(name);
            if (classObject instanceof Class) {
                log.info("Class found!");
            }
            return classObject;
        } catch (Exception e) {
            log.info("Class not found: " + e.getMessage());
        }

        Collection<ModuleClassLoader> classLoaders = ModuleFactory.getModuleClassLoaders();

        for (ModuleClassLoader classLoader : classLoaders) {
            try {
                classObject = classLoader.loadClass(name);
                if (classObject instanceof Class) {
                    log.info("Class found!");
                }
                return classObject;
            } catch (Exception e) {
                log.info("Class not found: " + e.getMessage());
            }
        }
        classObject = findClass(name);
        if (classObject instanceof Class) {
            log.info("Class found!");
        } else {
            log.info("Class not found.");
        }
        return classObject;
    }

    private Class defineDynamicClass(String name, byte raw[]) {
        try {
            return defineClass(name, raw, 0, raw.length);
        } catch (Exception e) {
        }
        return null;
    }

    // The heart of the ClassLoader -- automatically compile
    // source as necessary when looking for class files
    @Override
    public Class findClass(String name) throws ClassNotFoundException {

        boolean compiledNewClassfile = false;
        boolean updateRuleTable = false;
        Class classObject = null;

        // Our goal is to get a Class object
        // only check super classloader for non-dynamic rules
        if (this.rulePackagePrefix == null
                || (this.rulePackagePrefix != null && !name.startsWith(this.rulePackagePrefix))) {
            try {
                log.info("Trying with URLClassLoader...");
                return super.findClass(name);
            } catch (Exception e) {
                log.info("Class not found: " + e.getMessage());
            }
        }
        // if the class is not found, look in rule directory
        if (classObject == null) {
            // Create a pathname from the class name
            // E.g. java.lang.Object => java/lang/Object
            String fileStub = name;
            if (fileStub.lastIndexOf('.') > -1) {
                fileStub = fileStub.substring(fileStub.lastIndexOf('.') + 1,
                        fileStub.length());
            }

            // Build objects pointing to the source code (.java) and object
            // code (.class)
            String mlmFilename = "";
            if (this.mlmRuleDirectory != null) {
                mlmFilename = this.mlmRuleDirectory;
            }
            mlmFilename += fileStub + ".mlm";
            String javaFilename = "";
            if (this.javaRuleDirectory != null) {
                javaFilename = this.javaRuleDirectory;
            }
            javaFilename += this.rulePackagePrefix.replace('.', '/');
            javaFilename += fileStub + ".java";
            String classFilename = "";
            if (this.classRulesDirectory != null) {
                classFilename = this.classRulesDirectory;
            }
            if (this.rulePackagePrefix != null) {
                classFilename += this.rulePackagePrefix.replace('.', '/');
            }
            classFilename += fileStub + ".class";

            File mlmFile = new File(mlmFilename);
            File javaFile = new File(javaFilename);
            File classFile = new File(classFilename);

            // If no javafile exists, run thru the arden parser
            if (mlmFile.exists() && (!javaFile.exists() || mlmFile.lastModified() > javaFile.lastModified())) {
                if (!parse(mlmFilename) || !javaFile.exists()) {
                    throw new ClassNotFoundException("{Parse from mlm to java failed: "
                            + mlmFilename);
                }

                if (this.archivedMLMRuleDirectory != null) {
                    //copy mlm file to archive directory
                    String archivedMlmFile = this.archivedMLMRuleDirectory
                            + fileStub + "_" + Util.archiveStamp() + ".mlm";
                    try {
                        IOUtil.copyFile(mlmFilename, archivedMlmFile);
                    } catch (Exception e1) {
                        log.error("Could not copy mlm file: " + mlmFilename + " to " + archivedMlmFile);
                        log.error(e1.getMessage());
                    }
                }

                //delete mlm file from the processing directory
                if (this.clearMLMFiles) {
                    try {
                        IOUtil.deleteFile(mlmFilename);
                    } catch (Exception e) {
                        log.error("Could not delete mlm file: " + mlmFilename);
                        log.error(e.getMessage());
                    }
                }
            }

            // First, see if we want to try compiling. We do if (a) there
            // is source code, and either (b0) there is no object code,
            // or (b1) there is object code, but it's older than the source
            if (javaFile.exists()
                    && (!classFile.exists()
                    || javaFile.lastModified() > classFile
                    .lastModified())) {
                // Try to compile it. If this doesn't work, then
                // we must declare failure. (It's not good enough to use
                // an already-existing, but out-of-date, classfile)
                if (!compile(javaFilename) || !classFile.exists()) {
                    throw new ClassNotFoundException("Compile failed: "
                            + javaFilename);
                }

                compiledNewClassfile = true;

                updateRuleTable = true;

                if (this.archivedJavaRuleDirectory != null) {
                    // copy java file to archive directory
                    String archivedJavaFile = this.archivedJavaRuleDirectory
                            + fileStub + "_" + Util.archiveStamp() + ".java";
                    try {
                        IOUtil.copyFile(javaFilename, archivedJavaFile);
                    } catch (Exception e1) {
                        log.error("Could not copy java file: " + javaFilename
                                + " to " + archivedJavaFile);
                        log.error(e1.getMessage());
                    }
                }

                // delete java file from the processing directory
                if (this.clearJavaFiles) {
                    try {
                        IOUtil.deleteFile(javaFilename);
                    } catch (Exception e) {
                        log.error("Could not delete: " + javaFilename);
                        log.error(e.getMessage());
                    }
                }
            }
            // Let's try to load up the raw bytes, assuming they were
            // properly compiled, or didn't need to be compiled
            try {
                //if a new class file was created, re-define the class in the classloader
                //otherwise find the loaded class
                if (!compiledNewClassfile) {
                    try {
                        classObject = findLoadedClass(name);
                        if (classObject == null) {
                            classObject = getInstance().classMap.get(name);
                        }
                    } catch (Exception e) {
                    }
                }
                if (compiledNewClassfile || classObject == null) {
                    // read the bytes
                    byte raw[] = getBytes(classFilename);
                    // try to turn them into a class
                    CompilingClassLoader compilingClassLoader = new CompilingClassLoader(this);
                    classObject = compilingClassLoader.defineDynamicClass(name, raw);
                    getInstance().classMap.put(name, classObject);
                }

                if (updateRuleTable) {
                    // load class filename into rule table
                    try {
                        Object obj = classObject.newInstance();
                        if (obj instanceof MlmRule) {
                            DssService dssService = Context
                                    .getService(DssService.class);
                            dssService.addRule(classFilename, (MlmRule) obj);
                        }
                    } catch (Exception e) {
                        log.error("Error saving rule class file: "
                                + classFilename + " to dss_rule table");
                        log.error(e.getMessage());
                        log.error(Util.getStackTrace(e));
                    }
                }

            } catch (Exception ie) {
                //ignore this error message
                //the message gets written when we try to
                //load precompiled rules from the dynamic directory
            }
        }
        return classObject;
    }
}
