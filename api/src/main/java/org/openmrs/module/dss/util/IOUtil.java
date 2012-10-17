package org.openmrs.module.dss.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

/**
 * This class contains utility methods to aide in IO processing.
 *
 * @author Tammy Dugan, Andrew Martin
 */
public class IOUtil {

    protected static final Log log = LogFactory
            .getLog(IOUtil.class);
    private final static int BUFFER_SIZE = 4096;

    /**
     * Provides an easy way to write from a Reader to a Writer with a buffer
     *
     * @param input Reader content to be written
     * @param output Writer place to write content
     * @param bufferSize int size of data buffer
     * @param closeOutput whether the output Writer should be closed when
     * finished
     * @throws IOException
     */
    public static void bufferedReadWrite(Reader input, Writer output,
            int bufferSize, boolean closeOutput) throws IOException {
        int bytesRead = 0;
        char[] buff = new char[bufferSize];

        while (-1 != (bytesRead = input.read(buff, 0, buff.length))) {
            output.write(buff, 0, bytesRead);
            output.flush();
        }

        input.close();
        if (closeOutput) {
            output.close();
        }
    }

    /**
     * Provides an easy way to write from a Reader to a Writer with a buffer
     *
     * @param input Reader content to be written
     * @param output Writer place to write content
     * @param closeOutput whether the output Writer should be closed when
     * finished
     * @throws IOException
     */
    public void bufferedReadWrite(Reader input, Writer output,
            boolean closeOutput) throws IOException {
        bufferedReadWrite(input, output, BUFFER_SIZE, closeOutput);
    }

    /**
     * Provides an easy way to write from a Reader to a Writer with a buffer
     *
     * @param input Reader content to be written
     * @param output Writer place to write content
     * @throws IOException
     */
    public static void bufferedReadWrite(Reader input, Writer output)
            throws IOException {
        bufferedReadWrite(input, output, BUFFER_SIZE, true);
    }

    /**
     * Provides an easy way to write from a Reader to a Writer with a buffer
     *
     * @param input Reader content to be written
     * @param output Writer place to write content
     * @param bufferSize int size of data buffer
     * @throws IOException
     */
    public static void bufferedReadWrite(Reader input, Writer output,
            int bufferSize) throws IOException {
        bufferedReadWrite(input, output, bufferSize, true);
    }

    /**
     * Provides an easy way to write from an InputStream to an OutputStream with
     * a buffer
     *
     * @param input InputStream content to be written
     * @param output OutputStream place to write content
     * @param closeOutput whether the OutputStream should be closed
     * @throws IOException
     */
    public static void bufferedReadWrite(InputStream input,
            OutputStream output, boolean closeOutput) throws IOException {
        bufferedReadWrite(input, output, BUFFER_SIZE, closeOutput);
    }

    /**
     * Provides an easy way to write from an InputStream to an OutputStream with
     * a buffer
     *
     * @param input InputStream content to be written
     * @param output OutputStream place to write content
     * @throws IOException
     */
    public static void bufferedReadWrite(InputStream input, OutputStream output)
            throws IOException {
        bufferedReadWrite(input, output, BUFFER_SIZE, true);
    }

    /**
     * Provides an easy way to write from an InputStream to an OutputStream with
     * a buffer
     *
     * @param input InputStream content to be written
     * @param output OutputStream place to write content
     * @param bufferSize int size of data buffer
     * @throws IOException
     */
    public static void bufferedReadWrite(InputStream input,
            OutputStream output, int bufferSize) throws IOException {
        bufferedReadWrite(input, output, bufferSize, true);
    }

    /**
     * Provides an easy way to write from an input stream to an output stream
     * with a buffer
     *
     * @param input InputStream content to be written
     * @param output OutputStream place to write content
     * @param bufferSize int size of data buffer
     * @param closeOutput boolean whether the OutputStream should be closed
     * @throws IOException
     */
    public static void bufferedReadWrite(InputStream input,
            OutputStream output, int bufferSize, boolean closeOutput)
            throws IOException {
        int bytesRead = 0;
        byte[] buff = new byte[bufferSize];
        BufferedInputStream inputStream = new BufferedInputStream(input);
        BufferedOutputStream outputStream = new BufferedOutputStream(output);

        try {
            while (-1 != (bytesRead = inputStream.read(buff, 0, buff.length))) {
                outputStream.write(buff, 0, bytesRead);
            }

            inputStream.close();

            if (closeOutput) {
                outputStream.close();
            } else {
                outputStream.flush();
            }

        } catch (IOException ex) {
            log.error(ex.getMessage());
            log.error(Util.getStackTrace(ex));
            outputStream.close();
            throw new IOException(
                    "An error occurred while trying to write to the output stream");
        }
    }

    /**
     * Copies the source file to the target file location
     *
     * @param sourceFilename name of the source file location
     * @param targetFilename name of the target file location
     * @throws Exception
     */
    public static void copyFile(String sourceFilename, String targetFilename) throws Exception {
        copyFile(sourceFilename, targetFilename, false);
    }

    /**
     * Copies the source file to the target file location
     *
     * @param sourceFilename name of the source file location
     * @param targetFilename name of the target file location
     * @throws Exception
     *
     * Please use this method with caution, the file size is type casted to int
     * as opposed to long which is what the length() returns.
     */
    public static void copyFile(String sourceFilename, String targetFilename, boolean useBufferSize) throws Exception {
        try {
            File srcFile = new File(sourceFilename);
            FileInputStream sourceFile = new FileInputStream(srcFile);
            FileOutputStream targetFile = new FileOutputStream(new File(
                    targetFilename));
            if (useBufferSize) {
                IOUtil.bufferedReadWrite(sourceFile, targetFile, (int) srcFile.length());
            } else {
                IOUtil.bufferedReadWrite(sourceFile, targetFile);
            }
            targetFile.close();
            sourceFile.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new Exception("copyFile using Buffer size: Error copying " + sourceFilename + " to "
                    + targetFilename);
        }
    }

    /**
     * Deletes the given file
     *
     * @param filename file to delete
     */
    public static void deleteFile(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            log.error("Delete failed. File " + filename + " does not exist.");
            return;
        }

        if (!file.canWrite()) {
            log.error("Delete failed. File " + filename + " is not writable.");
            return;
        }

        // If it is a directory, make sure it is empty
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files.length > 0) {
                log.error("Delete failed. Directory " + filename + " is not empty.");
                return;
            }
        }

        // Attempt to delete it
        boolean success = file.delete();

        if (!success) {
            //try garbage collection to release locks on file
            System.gc();
            // Attempt to delete again
            success = file.delete();

            if (!success) {
                log.error("Delete failed. Could not delete file " + filename + ".");
            }
        }
    }

    /**
     * Renames old file to new filename
     *
     * @param oldname old file name
     * @param newname new file name
     */
    public static void renameFile(String oldname, String newname) {
        File file = new File(oldname);

        if (!file.exists()) {
            log.error("Rename failed. File " + oldname + " does not exist.");
            return;
        }

        if (!file.canWrite()) {
            log.error("Rename failed. File " + oldname + " is not writable.");
            return;
        }

        // If it is a directory, make sure it is empty
        if (file.isDirectory()) {
            String[] files = file.list();
            if (files.length > 0) {
                log.error("Rename failed. Directory " + oldname + " is not empty.");
                return;
            }
        }

        // Attempt to rename it
        boolean success = file.renameTo(new File(newname));

        if (!success) {
            //try garbage collection to release locks on file
            System.gc();
            // Attempt to rename again
            success = file.renameTo(new File(newname));

            if (!success) {
                log.error("Rename failed. Could not rename file "
                        + oldname + " to " + newname + ".");
            }
        }
    }

    /**
     * Returns a list of file names within a specific directory
     *
     * @param xmlDirectory specific directory
     * @return String[] list of file names
     */
    public static String[] getFilesInDirectory(String xmlDirectory) {
        File dir = new File(xmlDirectory);

        return dir.list();
    }

    public static File[] getFilesInDirectory(String directoryName, final String[] fileExtensions) {
        File directory = new File(directoryName);

        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                for (String currFileExtension : fileExtensions) {
                    if (name.endsWith(currFileExtension)) {
                        return true;
                    }
                }
                return false;
            }
        });
        return files;
    }

    /**
     * Returns the file name without an extension or directory path
     *
     * @param filepath path to the file
     * @return String file name without an extension or directory path
     */
    public static String getFilenameWithoutExtension(String filepath) {
        String filename = filepath;
        int index = filename.lastIndexOf("/");
        int index2 = filename.lastIndexOf("\\");

        if (index2 > index) {
            index = index2;
        }

        if (index > -1) {
            filename = filename.substring(index + 1);
        }

        index = filename.lastIndexOf(".");

        if (index > -1) {
            filename = filename.substring(0, index);
        }

        return filename;
    }

    public static String getDirectoryName(String filepath) {
        //if there is no dot, then the filepath 
        //is already a directory
        if (filepath.lastIndexOf(".") < 0) {
            return filepath;
        }
        String filename = filepath;
        int index = filename.lastIndexOf("/");
        int index2 = filename.lastIndexOf("\\");

        if (index2 > index) {
            index = index2;
        }

        if (index > -1) {
            filename = filename.substring(0, index);
        }

        return filename;
    }

    /**
     * Adds slashes if needed to a file directory
     *
     * @param fileDirectory file directory path
     * @return String formatted file directory path
     */
    public static String formatDirectoryName(String fileDirectory) {
        if (fileDirectory == null
                || fileDirectory.length() == 0) {
            fileDirectory = OpenmrsUtil.getApplicationDataDirectory();
        }

        if (!(fileDirectory.endsWith("/") || fileDirectory.endsWith("\\"))) {
            fileDirectory += "/";
        }
        return fileDirectory;
    }

    /**
     *
     * Converts a TIF file into a PDF file using itext library
     *
     * @param tiff
     * @param pdf
     */
    public static void convertTifToPDF(String tiff, OutputStream pdf) {

        try {
            Document document = new Document(PageSize.LETTER, 0, 0, 0, 0);
            Rectangle rect = document.getPageSize();
            Float pageWidth = rect.getWidth();
            Float pageHeight = rect.getHeight();

            PdfWriter.getInstance(document, pdf);
            document.open();

            RandomAccessFileOrArray ra = new RandomAccessFileOrArray(tiff);
            int pages = TiffImage.getNumberOfPages(ra);
            for (int i = 1; i <= pages; i++) {
                Image image = TiffImage.getTiffImage(ra, i);

                Float heightPercent = (pageHeight / image.getScaledHeight()) * 100;
                Float widthPercent = (pageWidth / image.getScaledWidth()) * 100;

                image.scalePercent(heightPercent, widthPercent);

                document.add(image);
                if (i < pages) {
                    document.newPage();
                }
            }

            document.close();
        } catch (Exception e) {
            log.error("", e);
        }

    }

    /**
     * Finds an image file based on location id, form id, and form instance id
     * in the provided directory.
     *
     * @param imageFilename String containing the location id + form id + form
     * instance id.
     * @param imageDir The directory to search for the file.
     *
     * @return File matching the search criteria.
     */
    public static File searchForImageFile(String imageFilename, String imageDir) {
        //This FilenameFilter will get ALL tifs starting with the filename
        //including of rescan versions nnn_1.tif, nnn_2.tif, etc
        FilenameFilter filtered = new FileListFilter(imageFilename, "tif");
        File dir = new File(imageDir);
        File[] files = dir.listFiles(filtered);
        if (!(files == null || files.length == 0)) {
            //This FileDateComparator will list in order
            //with newest file first.
            Arrays.sort(files, new FileDateComparator());
            imageFilename = files[0].getPath();
        }

        File imagefile = new File(imageFilename);

        return imagefile;
    }

    public static Properties getProps(String filename) {
        try {

            Properties prop = new Properties();
            InputStream propInputStream = new FileInputStream(filename);
            prop.loadFromXML(propInputStream);
            return prop;

        } catch (FileNotFoundException e) {
        } catch (InvalidPropertiesFormatException e) {
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return null;
    }
}