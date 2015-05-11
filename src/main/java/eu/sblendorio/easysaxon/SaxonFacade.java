package eu.sblendorio.easysaxon;

//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.config.ClientConfig;
//import com.sun.jersey.api.client.config.DefaultClientConfig;
//import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.sblendorio.xsltresourceschema.XsltResource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.ItemTypeFactory;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.xqj.SaxonXQConnection;
import net.sf.saxon.xqj.SaxonXQDataSource;
import net.sf.saxon.xqj.SaxonXQItem;

public class SaxonFacade {

    final private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SaxonFacade.class);
    private Processor processor = null;
    private Configuration configuration = null;
    final private XsltCompiler xsltCompiler; // XsltCompiler can be cached and reused because we do not configure its static context

    public SaxonFacade() {
        configuration = Configuration.newConfiguration();
        processor = new Processor(configuration);
        xsltCompiler = processor.newXsltCompiler();
    }

    public SaxonFacade(Configuration configuration) {
        this.configuration = configuration;
        processor = new Processor(configuration);
        xsltCompiler = processor.newXsltCompiler();
    }

    public SaxonFacade(Processor oldProcessor) {
        assert (oldProcessor != null);
        processor = oldProcessor;
        xsltCompiler = processor.newXsltCompiler();
    }

    public Processor getProcessor() {
        return processor;
    }

    public XsltCompiler getXsltCompiler() {
        return xsltCompiler;
    }

//    public XdmNode getXdmByURL(String URL) throws Exception {
//        return getXdmByURL(URL, null, null);
//    }
//    
//    @Deprecated
//    public XdmNode getXdmByURL(String URL, String username, String password) throws Exception {
//        ClientConfig cfg = new DefaultClientConfig();
//        cfg.getClasses().add(JacksonJsonProvider.class);
//        Client client = Client.create(cfg);
//
//        if (username != null) {
//            final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(username, password);
//            client.addFilter(authFilter);
//        }
//        WebResource tenantDomWebResource = client.resource(URL);
//        InputStream clientResponse = tenantDomWebResource.get(InputStream.class);
//        XdmNode xdmNode =getXdm(clientResponse, TreeModel.TINY_TREE);
//        return xdmNode;
//    }
//    // avoid using
//    @Deprecated
//    public XdmNode getXdm(String classpathAsString) throws SaxonApiException {
//        InputStream is = this.getClass().getResourceAsStream(classpathAsString);
//        Assert.notNull(is, "expected not null this.getClass().getResourceAsStream(classpathAsString). for classpathAsString=" + classpathAsString);
//        StreamSource source = new StreamSource(is);
//        return processor.newDocumentBuilder().build(source);
//    }
    // default treeModel is TreeModel.TINY_TREE
    
    
    public XdmNode getXdm(Path path) throws FileNotFoundException, SaxonApiException, IOException {
        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            return getXdm(inputStream);
        }
    }
    
    public XdmNode getXdm(InputStream inputStream) throws SaxonApiException {
        return getXdm(inputStream, TreeModel.TINY_TREE);
    }

    public XdmNode getXdmFromString(String xmlAsString) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(xmlAsString.getBytes(Charset.forName("utf-8")));) {
            return getXdm(inputStream, TreeModel.TINY_TREE);
        }
    }

    public XdmNode getXdm(InputStream inputStream, TreeModel treeModel) throws SaxonApiException {
        processor.newDocumentBuilder().setTreeModel(treeModel);
        StreamSource source = new StreamSource(inputStream);
        return processor.newDocumentBuilder().build(source);
    }

// TODO: support for linked tree, compact tree and tiny tree (default)
//    public XdmNode getXdm(InputStream inputStream, boolean isLinkedTree) throws SaxonApiException {
//        StreamSource source = new StreamSource(inputStream);
//        return processor.newDocumentBuilder().build(source);
//    }
    
    public <T> T getJaxbObjectFromXdm(XdmNode xdmNodeInput, Unmarshaller unmarshaller) throws Exception {
        SaxonXQItem xqItem = null;
        XMLStreamReader xmlStreamReader = null;
        try {
            xqItem = new SaxonXQItem(xdmNodeInput.getUnderlyingNode(), (SaxonXQConnection) new SaxonXQDataSource(processor.getUnderlyingConfiguration()).getConnection());
            xmlStreamReader = xqItem.getItemAsStream();
            T jaxbObjectOutput = (T) unmarshaller.unmarshal(xmlStreamReader);
            return jaxbObjectOutput;
        } finally {
            if (xmlStreamReader != null) {
                xmlStreamReader.close();
            }
            if (xqItem != null) {
                xqItem.close();
            }
        }
    }

    public <T> XdmNode getXdmNodeFromJaxbObject(T jaxbObjectInput, Marshaller marshaller) throws Exception {
        XdmNode xdmNodeOutput = processor.newDocumentBuilder().build(new JAXBSource(marshaller, jaxbObjectInput));
        return xdmNodeOutput;
    }

    public XsltExecutable getXsltExecutable(String xsltAsString) throws SaxonApiException {
        try (StringReader stringReader = new StringReader(xsltAsString);) {
            return this.getXsltExecutable(new StreamSource(stringReader));
        }
    }

    public XsltExecutable getXsltExecutable(Source source) throws SaxonApiException {
        return xsltCompiler.compile(source);
    }

    public XsltExecutable getXsltExecutable(Path path) throws SaxonApiException, FileNotFoundException, IOException {
        try (InputStream inputStream = new FileInputStream(path.toFile())) {
            return getXsltExecutable(new StreamSource(inputStream));
        }
    }

    public XsltExecutable getXsltExecutable(InputStream inputStream) throws SaxonApiException {
        return xsltCompiler.compile(new StreamSource(inputStream));
    }

    public XsltExecutable getXsltExecutable(XsltResource xsltResource) throws Exception {
        try (CloseableUriResolver closeableUriResolverCompileTime = new CloseableUriResolver(xsltResource);) {
            return this.getXsltExecutableFromUri(closeableUriResolverCompileTime, xsltResource.getPrimaryUri());
        }
    }

    public XsltExecutable getXsltExecutableFromUri(CloseableUriResolver closeableUriResolverCompileTime, String uriString) throws SaxonApiException, TransformerException {
        xsltCompiler.setURIResolver(closeableUriResolverCompileTime);
        Source primaryUriSource = closeableUriResolverCompileTime.resolve(uriString, "java:getXsltExecutableFromUri()");
        return xsltCompiler.compile(primaryUriSource);
    }

    public XdmNode executeTransformation(XsltExecutable xsltExecutable, XdmNode xmlSource, Object... args) throws IOException, SaxonApiException {
        Map<String, Object> parMap = null;
        if (args != null && args.length != 0) {
            parMap = new TreeMap<>();
            for (int i=0; i<args.length/2; ++i) {
                String key = args[i*2].toString();
                Object value = null;
                if (i*2+1 <= args.length-1)
                    value = args[i*2+1];
                
                parMap.put(key, value);
            }
        }
        return executeTransformation(xsltExecutable, xmlSource, parMap, null);
    }

    public XdmNode executeTransformation(XsltExecutable xsltExecutable, XdmNode xmlSource, Map<String, ?> parameterMap) throws IOException, SaxonApiException {
        return executeTransformation(xsltExecutable, xmlSource, parameterMap, null);
    }

    private ErrorListener getLoggingErrorListener() {
        return new ErrorListener() {
            @Override
            public void warning(TransformerException exception) throws TransformerException {
                logger.info("compiler warning", exception);
            }

            @Override
            public void error(TransformerException exception) throws TransformerException {
                logger.info("compiler error", exception);
            }

            @Override
            public void fatalError(TransformerException exception) throws TransformerException {
                logger.info("compiler fatalError", exception);
            }
        };
    }

    public XdmNode executeTransformation(XsltExecutable xsltExecutable, XdmNode xmlSource, Map<String, ?> parameterMap, CloseableUriResolver optionalTransformTimeCloseableUriResolver) throws IOException, SaxonApiException {
        if (parameterMap == null) {
            parameterMap = Collections.EMPTY_MAP;
        }
        XsltTransformer transformation = xsltExecutable.load();
        Map<String, Object> parameterMapToPrint = new HashMap<>();
        for (Map.Entry<String, ?> parameterMapEntry0 : parameterMap.entrySet()) {
            String key = parameterMapEntry0.getKey();
            Object value = parameterMapEntry0.getValue();
            if (!key.equals("@input")) {
                String valueStr;
                if (value == null) {
                    valueStr = "<null>";
                } else if (value instanceof XdmNode) {
                    valueStr = "[XdmNode]";
                } else {
                    valueStr = value.toString();
                }
                parameterMapToPrint.put(key, valueStr);
            }
        }

        for (String pName : parameterMap.keySet()) {
            if (!pName.equals("@input")) {
                Object value0 = parameterMap.get(pName);

                if (value0 != null) {
                    if (value0 instanceof XdmValue) {
                        transformation.setParameter(new QName(pName), (XdmValue) value0);
                    } else if (value0 instanceof String) {
                        transformation.setParameter(new QName(pName), new XdmAtomicValue((String) value0));
                    } else if (value0 instanceof java.util.Date) {
                        ItemTypeFactory itf = new ItemTypeFactory(processor);
                        ItemType dateType = itf.getAtomicType(new QName("http://www.w3.org/2001/XMLSchema", "date"));
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String dateStr = formatter.format((Date) value0);
                        transformation.setParameter(new QName(pName), new XdmAtomicValue(dateStr, dateType));
                    } else if (value0 instanceof Long) {
                        transformation.setParameter(new QName(pName), new XdmAtomicValue((Long) value0));
                    } else if (value0 instanceof Boolean) {
                        transformation.setParameter(new QName(pName), new XdmAtomicValue((Boolean) value0));
                    } else if (value0 instanceof Collection) {
                        Collection inputCollection = (Collection) value0;
                        List<XdmItem> xdmItemList = new ArrayList<>(inputCollection.size());
                        for (Object inputCollectionItem0 : inputCollection) {
                            XdmAtomicValue xdmAtomicValue0;

                            if (inputCollectionItem0 instanceof String) {
                                xdmAtomicValue0 = new XdmAtomicValue((String) inputCollectionItem0);
                            } else if (inputCollectionItem0 instanceof Long) {
                                xdmAtomicValue0 = new XdmAtomicValue((Long) inputCollectionItem0);
                            } else if (inputCollectionItem0 instanceof Boolean) {
                                xdmAtomicValue0 = new XdmAtomicValue((Boolean) inputCollectionItem0);
                            } else {
                                throw new UnsupportedOperationException();
                            }

                            xdmItemList.add(xdmAtomicValue0);
                        }
                        transformation.setParameter(new QName(pName), new XdmValue(xdmItemList));
                    }
                } else {
                    transformation.setParameter(new QName(pName), null);
                    //throw new NullPointerException("null value given for parameterName="+pName);
                }
                //transformation.setParameter(new QName(pName), new XdmAtomicValue(parameterMap.get(pName)));
            }
        }

        transformation.setInitialContextNode(xmlSource);
        XdmDestination resultTree = new XdmDestination();
        transformation.setDestination(resultTree);
        if (optionalTransformTimeCloseableUriResolver != null) {
            transformation.setURIResolver(optionalTransformTimeCloseableUriResolver);
        }
        transformation.transform();
        XdmNode xdmNode = resultTree.getXdmNode();
        transformation.close();
        return xdmNode;
    }

    public XdmValue doXPathXdmValue(String query, XdmNode xmlSource, Map<String, Object> parameterMap) throws SaxonApiException {
        XPathCompiler compiler = processor.newXPathCompiler(); // XPathCompiler cannot be directly cached because we configure its static context

        if (parameterMap != null) {
            for (String pName : parameterMap.keySet()) {
                compiler.declareVariable(new QName(pName));
            }
        }

        compiler.setSchemaAware(false);
        XPathExecutable executable = compiler.compile(query);
        XPathSelector selector = executable.load();

        if (parameterMap != null) {
            for (String pName : parameterMap.keySet()) {
                Object value0 = parameterMap.get(pName);

                if (value0 != null) {
                    if (value0 instanceof String) {
                        selector.setVariable(new QName(pName), new XdmAtomicValue((String) value0));
                    } else if (value0 instanceof Long) {
                        selector.setVariable(new QName(pName), new XdmAtomicValue((Long) value0));
                    } else if (value0 instanceof Boolean) {
                        selector.setVariable(new QName(pName), new XdmAtomicValue((Boolean) value0));
                    }
                } else {
                    throw new NullPointerException();
                }
                //selector.setVariable(new QName(pName), new XdmAtomicValue(value0));
            }
        }

        selector.setContextItem(xmlSource);
        return selector.evaluate();
    }

    public List<String> doXPath(String query, XdmNode xmlSource, Map<String, Object> parameterMap) throws SaxonApiException {

        XdmValue xdmResult = doXPathXdmValue(query, xmlSource, parameterMap);
        List<String> result = new LinkedList<>();

        for (XdmItem item : xdmResult) {
            result.add(item.getStringValue());
        }
        return result;
    }

    public String doXPathOneRow(String query, XdmNode xmlSource, Map<String, Object> parameterMap) throws SaxonApiException {
        List<String> listResult = doXPath(query, xmlSource, parameterMap);
        String result = "";
        boolean first = true;
        for (String s : listResult) {
            result += (!first ? "," : "") + s;
            first = false;
        }
        return result;
    }

}
