package runners;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-report.html"
)
public class TestRunner {}
