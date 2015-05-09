package eu.sblendorio.easysaxon;

import eu.sblendorio.xsltresourceschema.UriInfo;
import eu.sblendorio.xsltresourceschema.XsltResource;
import java.io.IOException;
import java.nio.file.Path;

public class XsltBundle extends XsltResource {

    public XsltBundle() {
    }

    public XsltBundle(Path mainPath) throws IOException {
        this.setMain(mainPath);
    }

    public final void setMain(Path path) throws IOException {
        UriInfo uri = new UriInfo();
        uri.setUri("uri:main-" + System.currentTimeMillis());
        uri.setContent(SaxonFacade.readStringFromPath(path));
        this.getUriInfoList().clear();
        this.getUriInfoList().add(uri);
        this.setPrimaryUri(uri.getUri());
    }

    public void addImport(String uriString, Path path) throws IOException {
        UriInfo uri = new UriInfo();
        uri.setUri(uriString);
        uri.setContent(SaxonFacade.readStringFromPath(path));
        this.getUriInfoList().add(uri);
    }
}
