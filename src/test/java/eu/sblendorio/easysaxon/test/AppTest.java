package eu.sblendorio.easysaxon.test;

import eu.sblendorio.easysaxon.SaxonFacade;
import eu.sblendorio.easysaxon.XsltBundle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;

public class AppTest
        extends TestCase {

    public AppTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testSimpleApp() throws FileNotFoundException, SaxonApiException, IOException {

        SaxonFacade api = new SaxonFacade();

        XdmNode input = api.getXdm(Paths.get("src/test/resources/testSource.xml"));
        XsltExecutable transform = api.getXsltExecutable(Paths.get("src/test/resources/testTransformation.xsl"));
        
        Map<String, Object> params = new TreeMap<>();
        params.put("newtag", "John Doe");
        XdmNode result = api.executeTransformation(transform, input, params);

        System.out.println("RESULT ----------------------------------------------------");
        System.out.println(result.toString());

    }

    public void testIncludingApp() throws Exception {
        SaxonFacade api = new SaxonFacade();

        XdmNode input = api.getXdm(Paths.get("src/test/resources/testSource.xml"));
        XsltBundle bundle = new XsltBundle(Paths.get("src/test/resources/test-main.xsl"));
        bundle.addImport("uri:getData-1.xsl", Paths.get("src/test/resources/test-included-1.xsl"));
        bundle.addImport("uri:getData-2.xsl", Paths.get("src/test/resources/test-included-2.xsl"));
        XsltExecutable transform = api.getXsltExecutable(bundle);

        XdmNode output = api.executeTransformation(transform, input, Collections.EMPTY_MAP);
        System.out.println("RESULT testIncludingApp ----------------------------------------------------");
        System.out.println(output.toString());

    }

}
