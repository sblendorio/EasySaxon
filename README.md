Overview
========
**Saxon** is a great XSLT processor, but for common needs it is quite complex to use. **EasySaxon** is a facade over Saxon that makes its use easier.

Main features
============
* Use of parametric XSLT sources with automatic type detection
* Modular transformations through the use of `<xsl:import />` tag

Install
=======
The package is built through MAVEN2, so you just have to run **clean**, **compile** and **package** tasks:
```
mvn clean
mvn compile
mvn package
```
...then you will find **easysaxon-1.0-SNAPSHOT.jar** in newly created **target** folder.

Examples of use
==============

Simple transformation
---------------------
* One XML input file
* One XSL transformation file
* A string parameter (newtag="John Doe")

```
SaxonFacade api = new SaxonFacade();

XdmNode input = api.getXdm(Paths.get("src/test/resources/testSource.xml"));
XsltExecutable transform = api.getXsltExecutable(Paths.get("src/test/resources/testTransformation.xsl"));

Map<String, Object> params = new TreeMap<>();
params.put("newtag", "John Doe");

XdmNode output = api.executeTransformation(transform, input, params);
System.out.println(output.toString());
```

Modular transformation
----------------------
* One XML input file
* Three XSL transformation files (Entry point + 2):
  * [**test-main.xsl**](https://github.com/sblendorio/EasySaxon/blob/master/src/test/resources/test-main.xsl): the entry point, using `<xsl:import href="...">`
  * [**test-included-1.xsl**](https://github.com/sblendorio/EasySaxon/blob/master/src/test/resources/test-included-1.xsl): referenced as **uri:getData-1.xsl**
  * [**test-included-1.xsl**](https://github.com/sblendorio/EasySaxon/blob/master/src/test/resources/test-included-2.xsl): referenced as **uri:getData-2.xsl**
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

Live examples and demo
----------------------
Look at [**eu.sblendorio.easysaxon.test.AppTest**](https://github.com/sblendorio/EasySaxon/blob/master/src/test/java/eu/sblendorio/easysaxon/test/AppTest.java) class for live examples
