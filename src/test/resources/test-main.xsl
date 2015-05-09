<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:my="http://sblendorio.eu/commons"
                version="2.0">
    <xsl:import href="uri:getData-1.xsl" />
    <xsl:import href="uri:getData-2.xsl" />

    <xsl:template match="/">
        <xsl:element name="rootResult">
            <xsl:element name="value1">
                <xsl:value-of select="my:getData()" />
            </xsl:element>
            <xsl:element name="value2">
                <xsl:value-of select="my:getDataTwo()" />
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>