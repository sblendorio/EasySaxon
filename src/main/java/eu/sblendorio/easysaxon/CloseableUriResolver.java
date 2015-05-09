package eu.sblendorio.easysaxon;

import eu.sblendorio.xsltresourceschema.XsltResource;
import eu.sblendorio.xsltresourceschema.UriInfo;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.springframework.util.Assert;

public class CloseableUriResolver implements Closeable, URIResolver {

    Map<String, Path> pathMapBySystemId = new TreeMap<>();
    Map<String, String> classpathMapBySystemId = new TreeMap<>();
    Map<String, String> contentAsStringMapBySystemId = new TreeMap<>();
    List<Closeable> closableResourceList = new ArrayList<>();
    
    

    @Override
    public String toString() {
        return "CloseableUriResolver{" + "pathMapBySystemId.keySet()=" + pathMapBySystemId.keySet() + ", classpathMapBySystemId.keySet()=" + classpathMapBySystemId.keySet() + ", contentAsStringMapBySystemId.keySet()=" + contentAsStringMapBySystemId.keySet() + '}';
    }

    public CloseableUriResolver() {
    }

    public CloseableUriResolver(XsltResource xsltResource) {

        Set<String> uriNameSet = new TreeSet<>();
        for (UriInfo uriInfo0 : xsltResource.getUriInfoList()) {
            this.addContentAsStringMapping(uriInfo0.getUri(), uriInfo0.getContent());
            uriNameSet.add(uriInfo0.getUri());
        }
        if (xsltResource.getPrimaryUri() != null) {
            Assert.isTrue(uriNameSet.contains(xsltResource.getPrimaryUri()), "expected isTrue uriNameSet.contains(xsltResource.getPrimaryUri()). for uriNameSet=" + uriNameSet + ", xsltResource.getPrimaryUri()=" + xsltResource.getPrimaryUri());
        }
    }

    public void addFilePathMapping(String systemId, Path path) {
        Assert.isTrue(Files.exists(path));
        pathMapBySystemId.put(systemId, path);
    }

    public void addClassPathMapping(String systemId, String classpath) {
        classpathMapBySystemId.put(systemId, classpath);
    }

    public final void addContentAsStringMapping(String systemId, String contentAsString) {
        contentAsStringMapBySystemId.put(systemId, contentAsString);
    }

    @Override
    public void close() throws IOException {
        for (Closeable closeable0 : closableResourceList) {
            closeable0.close();
        }
        closableResourceList = null;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Assert.notNull(closableResourceList, "already closed");
        if (pathMapBySystemId.containsKey(href)) {
            Path path = pathMapBySystemId.get(href);
            try {
                InputStream inputStream = Files.newInputStream(path);
                Assert.notNull(inputStream);
                closableResourceList.add(inputStream);
                Source source = new StreamSource(inputStream);
                return source;
            } catch (Exception ex) {
                throw new TransformerException("exception encountered for href=" + href + ", base=" + base, ex);
            }
        } else if (classpathMapBySystemId.containsKey(href)) {
            String classpathString = classpathMapBySystemId.get(href);
            try {
                InputStream inputStream = this.getClass().getResourceAsStream(classpathString);
                Assert.notNull(inputStream, "expected notNull inputStream. for classpathString=" + classpathString);
                closableResourceList.add(inputStream);
                Source source = new StreamSource(inputStream);
                return source;
            } catch (Exception ex) {
                throw new TransformerException("exception encountered for href=" + href + ", base=" + base, ex);
            }
        } else if (contentAsStringMapBySystemId.containsKey(href)) {
            String contentAsString = contentAsStringMapBySystemId.get(href);
            try {
                //InputStream inputStream = this.getClass().getResourceAsStream(classpathString);
                Assert.notNull(contentAsString, "expected notNull contentAsString");
                //closableResourceList.add(inputStream);
                Source source = new StreamSource(new StringReader(contentAsString));
                return source;
            } catch (Exception ex) {
                throw new TransformerException("exception encountered for href=" + href + ", base=" + base, ex);
            }
        } else {
            throw new TransformerException("no mapping found for href=" + href + ", base=" + base+", this.toString()="+this.toString());
        }
    }
}
