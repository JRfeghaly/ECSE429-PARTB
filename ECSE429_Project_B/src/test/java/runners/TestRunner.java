// src/test/java/runners/TestRunner.java
package runners;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME,
        value = "stepDefinitions")
@ConfigurationParameter(key = io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-report.html, json:target/cucumber.json")
public class TestRunner { }
