/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util;

import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TemplateParserTestCase extends AbstractMuleTestCase
{

    @Test
    public void testStringParserSquareBraces()
    {
        TemplateParser tp = TemplateParser.createSquareBracesStyleParser();
        assertNotNull(tp.getStyle());
        assertEquals("[", tp.getStyle().getPrefix());
        assertEquals("]", tp.getStyle().getSuffix());

        Map props = new HashMap();
        props.put("fromAddress", "ross.mason@symphonysoft.com");
        String string = "smtp://[fromAddress]";

        String result = tp.parse(props, string);
        assertEquals("smtp://ross.mason@symphonysoft.com", result);
        string = "smtp://[toAddress]";
        result = tp.parse(props, string);
        assertEquals("smtp://[toAddress]", result);
    }

    @Test
    public void testParserValidationSquareBraces()
    {
        TemplateParser tp = TemplateParser.createSquareBracesStyleParser();
        assertTrue(tp.isValid("[][]"));
        assertTrue(tp.isValid("[[]]"));
        assertFalse(tp.isValid("[[][]"));
    }

    @Test
    public void testParserValidationAntStyle()
    {
        TemplateParser tp = TemplateParser.createAntStyleParser();
        assertTrue(tp.isValid("${}"));
        assertTrue(tp.isValid("${}${}"));
        assertFalse(tp.isValid("${}&{}"));
        assertFalse(tp.isValid("{}${}"));
        assertTrue(tp.isValid("${$}${}"));
        assertFalse(tp.isValid("${${}}${}"));
        assertFalse(tp.isValid("$ {}"));

    }

    @Test
    public void testParserValidationMuleStyle()
    {
        TemplateParser tp = TemplateParser.createMuleStyleParser();
        assertTrue(tp.isValid("#[]"));
        assertTrue(tp.isValid("#[]   #[]"));
        assertFalse(tp.isValid("#[]&[]"));
        assertFalse(tp.isValid("[]$[]#"));
        assertTrue(tp.isValid("#[#]#[]"));
        assertFalse(tp.isValid("#[#[]]#[]"));
        assertFalse(tp.isValid("# []"));

        assertTrue(tp.isValid("#[foo:blah[4] = 'foo']"));
        assertTrue(tp.isValid("#[foo:blah[4] = '#foo']"));
        assertFalse(tp.isValid("#[foo:blah4] = '#foo']"));
        //Can't have embedded
        assertFalse(tp.isValid("#[foo:blah = '#[foo]']"));


    }

    @Test
    public void testStringParserAntBraces()
    {
        TemplateParser tp = TemplateParser.createAntStyleParser();
        assertNotNull(tp.getStyle());
        assertEquals("${", tp.getStyle().getPrefix());
        assertEquals("}", tp.getStyle().getSuffix());

        Map props = new HashMap();
        props.put("prop1", "value1");
        props.put("prop2", "value2");

        String string = "Some String with ${prop1} and ${prop2} in it";
        String result = tp.parse(props, string);
        assertEquals("Some String with value1 and value2 in it", result);

        string = "${prop1}${prop1}${prop2}";
        result = tp.parse(props, string);
        assertEquals("value1value1value2", result);

        // MULE-978: a property with backslashes (on Windows)
        String homeDir = System.getProperty("user.home");
        props.put("homeDir", homeDir);
        string = "${homeDir}/foo";
        result = tp.parse(props, string);
        assertEquals(homeDir + "/foo", result);

        // whitespace is really popular too
        String whitespaceValue = "C:\\Documents and Settings\\";
        props.put("whitespaceValue", whitespaceValue);
        string = "start${whitespaceValue}end";
        result = tp.parse(props, string);
        assertEquals("start" + whitespaceValue + "end", result);
    }

    @Test
    public void testListParserAntBraces()
    {
        TemplateParser tp = TemplateParser.createAntStyleParser();

        Map props = new HashMap();
        props.put("prop1", "value1");
        props.put("prop2", "value2");
        List list = new ArrayList();
        list.add("Some String with ${prop1} and ${prop2} in it");
        list.add("Some String with ${prop1} in it");

        List result = tp.parse(props, list);
        assertEquals("Some String with value1 and value2 in it", result.get(0));
        assertEquals("Some String with value1 in it", result.get(1));

        result = tp.parse(props, (List)null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testMapParserAntBraces()
    {
        TemplateParser tp = TemplateParser.createAntStyleParser();
        Map props = new HashMap();
        props.put("prop1", "value1");
        props.put("prop2", "value2");
        Map map = new HashMap();
        map.put("value1", "Some String with ${prop1} and ${prop2} in it");
        map.put("value2", "Some String with ${prop1} in it");

        Map result = tp.parse(props, map);
        assertEquals("Some String with value1 and value2 in it", result.get("value1"));
        assertEquals("Some String with value1 in it", result.get("value2"));

        result = tp.parse(props, (Map)null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testStringParserAntBracesWithSimilarNames()
    {
        TemplateParser tp = TemplateParser.createAntStyleParser();
        Map props = new HashMap();
        props.put("prop1", "value1");
        props.put("prop1-2", "value2");
        String string = "Some String with ${prop1} and ${prop1-2} in it";

        String result = tp.parse(props, string);
        assertEquals("Some String with value1 and value2 in it", result);
        string = "A${prop1-2}B${prop1}C${prop2}";
        result = tp.parse(props, string);
        assertEquals("Avalue2Bvalue1C${prop2}", result);
    }

}
