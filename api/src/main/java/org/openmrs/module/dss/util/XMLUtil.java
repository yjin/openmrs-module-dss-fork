package org.openmrs.module.dss.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Contains several utility methods to aide in XML processing.
 *
 * @author Tammy Dugan
 *
 */
public class XMLUtil {

    private static Log log = LogFactory.getLog(XMLUtil.class);

    /**
     * Serializes a dom tree to an xml file
     *
     * @param rootElement root element to serialize
     * @param fileName name of file to write xml to
     * @param doc xml document
     * @throws IOException
     */
    public synchronized static void xmlToFile(Element rootElement, String fileName,
            Document doc) throws IOException {
        try {
            FileOutputStream outputFile = new FileOutputStream(fileName);
            xmlToOutputStream(outputFile, doc, rootElement);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("The xml output file: " + fileName + " could not be found.");
        }
    }

    /**
     * Writes an xml dom to an output stream
     *
     * @param output output stream for xml
     * @param doc xml document
     * @param rootElement root element to serialize
     * @throws IOException
     */
    public synchronized static void xmlToOutputStream(OutputStream output, Document doc,
            Element rootElement) throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setLineSeparator(LineSeparator.Windows);
        format.setIndenting(true);
        format.setLineWidth(0);
        format.setPreserveSpace(true);
        format.setEncoding("ISO-8859-1");
        try {
            XMLSerializer serializer = new XMLSerializer(output, format);
            serializer.asDOMSerializer();
            serializer.serialize(rootElement);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("Error writing xml to output stream.");
        }
    }

    /**
     * Creates an empty dom object
     *
     * @return Document newly created dom object
     */
    public synchronized static Document createDOM() {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            return doc;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
        }
        return null;
    }

    /**
     * Takes in an input file name and parses the xml file into a dom
     *
     * @param inputFilename name of the input file
     * @return Document dom of the xml from the file
     * @throws IOException
     */
    public synchronized static Document parseXMLFromFile(String inputFilename) throws IOException {
        InputStream input = null;

        try {
            input = new FileInputStream(inputFilename);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("The xml input file name is: " + inputFilename + ". You must provide a valid input file name.");
        }
        return parseXMLFromInputStream(input);
    }

    /**
     * Parses xml from an input stream into a dom
     *
     * @param input input stream
     * @return Document dom tree for input xml
     * @throws IOException
     */
    public synchronized static Document parseXMLFromInputStream(InputStream input) throws IOException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(input);

            return doc;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("Error parsing xml input stream.");
        }
    }

    /**
     * Transforms an input stream to an output stream using an xslt transform
     *
     * @param transformInput data to be transformed
     * @param transformOutput output of transform
     * @param xslt transform xslt
     * @param parameters parameters to the xslt transform
     * @throws IOException
     */
    public synchronized static void transformXML(InputStream transformInput,
            OutputStream transformOutput, InputStream xslt,
            HashMap<String, Object> parameters) throws IOException {
        Source xmlSource = new StreamSource(transformInput);
        Source xsltSource = new StreamSource(xslt);

        // create the xslt tranformer
        TransformerFactory transFact = TransformerFactory.newInstance();

        try {
            Transformer trans = transFact.newTransformer(xsltSource);
            if (parameters != null) {
                Iterator<String> keys = parameters.keySet().iterator();
                while (keys.hasNext()) {
                    String name = keys.next();
                    Object value = parameters.get(name);
                    trans.setParameter(name, value);
                }
            }

            trans.transform(xmlSource, new StreamResult(transformOutput));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("Error transforming xml.");
        }
    }

    /**
     * Serializes an object to xml using JiBx
     *
     * @param objectToSerialize object to serialize to xml
     * @param output output stream to write xml to
     * @throws IOException
     */
    public synchronized static void serializeXML(Object objectToSerialize, OutputStream output) throws IOException {
        try {
            IBindingFactory bfact = BindingDirectory.getFactory(objectToSerialize.getClass());
            IMarshallingContext mctx = bfact.createMarshallingContext();
            mctx.marshalDocument(objectToSerialize, "ISO-8859-1", null, output);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("Error writing xml to output stream.");
        }
    }

    /**
     * Deserializes an input stream of xml into an object using JiBx
     *
     * @param objectClass type of object to create
     * @param input xml input
     * @return Object resulting object
     * @throws IOException
     */
    public synchronized static Object deserializeXML(Class objectClass, InputStream input) throws IOException {
        try {
            IBindingFactory bfact = BindingDirectory.getFactory(objectClass);
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            return uctx.unmarshalDocument(input, null);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Util.getStackTrace(e));
            throw new IOException("Error parsing xml input stream.");
        }
    }
}