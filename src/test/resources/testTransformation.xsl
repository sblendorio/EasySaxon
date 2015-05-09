<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jr="http://jobrapido.com/commons"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0">
<xsl:param name="newtag" as="xs:string" select="''" />
    
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:variable name="tagname" select="name()" as="xs:string"/>
            <xsl:if test="$tagname=('record') and not( exists(./newfield) )">
                <xsl:element name="newfield">
                    <xsl:value-of select="name" />
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="surname" />
                    <xsl:if test="exists($newtag)">
                        <xsl:text> (</xsl:text>
                        <xsl:value-of select="$newtag" />
                        <xsl:text>)</xsl:text>
                    </xsl:if>
                </xsl:element>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>