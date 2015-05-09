Overview
========
**Saxon** is a great XSLT preprocessor, but for common needs it is quite complex to use. **EasySaxon** is a facade over Saxon that make easier the use of this great preprocessor.

Main features
============
* Use of parametric XSLT sources with automatic type detection
* Modular transformations through the use of **xsl:import** tag

Install
=======
The package is built through MAVEN2, so you just have to run **clean** and **build** tasks.

Examples of use
==============

Simple transformation
---------------------
* One XML input file
* One XSL transformation file
* A string parameter (newtag="Foo")

```
    SaxonFacade api = new SaxonFacade();

    XdmNode input = api.getXdm(Paths.get("src/test/resources/testSource.xml"));
    XsltExecutable transform = api.getXsltExecutable(Paths.get("src/test/resources/testTransformation.xsl"));

    Map<String, Object> params = new TreeMap<>();
    params.put("newtag", "Ciccio Pasticcio");
    XdmNode result = api.executeTransformation(transform, input, params);
    System.out.println(result.toString());
```

Modular transformation
----------------------
* One XML input file
* Two XSL transformation file
* Empty parameter list

```
   SaxonFacade api = new SaxonFacade();

    XdmNode input = api.getXdm(Paths.get("src/test/resources/testSource.xml"));
    XsltBundle bundle = new XsltBundle(Paths.get("src/test/resources/test-main.xsl"));
    bundle.addImport("uri:getData-1.xsl", Paths.get("src/test/resources/test-included-1.xsl"));
    bundle.addImport("uri:getData-2.xsl", Paths.get("src/test/resources/test-included-2.xsl"));
    XsltExecutable transform = api.getXsltExecutable(bundle);

    XdmNode output = api.executeTransformation(transform, input, Collections.EMPTY_MAP);
    System.out.println(output.toString());
```
