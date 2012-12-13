package org.openmrs.module.dss.util;

import java.io.File;
import java.util.Comparator;

/**
 * @author msheley Sorts on lastModified datetime desc (newest first)
 */
public class FileDateComparator implements Comparator<File> {

    // Sorted on lastModified datetime desc
    // with newest file first.
    // ex. Arrays.sort(files, new FileDateComparator());
    @Override
    public int compare(File file1, File file2) {
        Long file1Date = file1.lastModified();
        Long file2Date = file2.lastModified();
        return file2Date.compareTo(file1Date);
    }
}