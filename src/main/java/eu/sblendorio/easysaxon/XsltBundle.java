package eu.sblendorio.easysaxon;

import eu.sblendorio.xsltresourceschema.UriInfo;
import eu.sblendorio.xsltresourceschema.XsltResource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.util.Assert;

public class XsltBundle extends XsltResource {

    public XsltBundle() {
    }

    public XsltBundle(Path mainPath) throws IOException {
        this.setMain(mainPath);
    }

    public XsltBundle(String mainXslContent) throws IOException {
        this.setMain(mainXslContent);
    }

    private void setMain(Path path) throws IOException {
        UriInfo uri = new UriInfo();
        uri.setUri("uri:main-" + System.currentTimeMillis());
        uri.setContent(XsltBundle.readStringFromPath(path));
        this.getUriInfoList().clear();
        this.getUriInfoList().add(uri);
        this.setPrimaryUri(uri.getUri());
    }

    private void setMain(String mainXslContent) throws IOException {
        UriInfo uri = new UriInfo();
        uri.setUri("uri:main-" + System.currentTimeMillis());
        uri.setContent(mainXslContent);
        this.getUriInfoList().clear();
        this.getUriInfoList().add(uri);
        this.setPrimaryUri(uri.getUri());
    }

    public void addImport(String uriString, Path path) throws IOException {
        UriInfo uri = new UriInfo();
        uri.setUri(uriString);
        uri.setContent(XsltBundle.readStringFromPath(path));
        this.getUriInfoList().add(uri);
    }

    public void addImport(String uriString, String xslContent) throws IOException {
        UriInfo uri = new UriInfo();
        uri.setUri(uriString);
        uri.setContent(xslContent);
        this.getUriInfoList().add(uri);
    }
    
    public static String readStringFromPath(Path path) throws IOException {
        //build a Stream Reader, it can read char by char
        //build a buffered Reader, so that i can read whole line at once
        Assert.isTrue(Files.exists(path), "expected isTrue Files.exists(path). for path=" + path);
        try (BufferedReader bReader = Files.newBufferedReader(path, Charset.forName("utf-8"));) {

            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bReader.readLine()) != null) {  //Read till end
                builder.append(line);
            }
            //iStream.close();
            return builder.toString();
        }
    }

    public static void writeStringToPath(Object s, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter bWriter = Files.newBufferedWriter(path, Charset.forName("utf-8"));) {
            bWriter.write(s.toString());
        }
    }
}
