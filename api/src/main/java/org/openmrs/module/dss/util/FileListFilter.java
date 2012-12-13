package org.openmrs.module.dss.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author msheley Filters files in a directory based on the extension and file
 * name must start with the provided text string.
 *
 */
public class FileListFilter implements FilenameFilter {

    private String name;
    private String extension;

    public FileListFilter(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    @Override
    public boolean accept(File directory, String filename) {
        boolean ok = true;

        if (name != null) {
            ok &= (filename.startsWith(name + ".") || filename.startsWith(name + "_")
                    || filename.startsWith("_" + name + "_"));
        }

        if (extension != null) {
            ok &= filename.endsWith('.' + extension);
        }

        return ok;
    }
}