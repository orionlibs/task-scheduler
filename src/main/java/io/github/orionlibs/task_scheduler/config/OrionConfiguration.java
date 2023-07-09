package io.github.orionlibs.task_scheduler.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Properties-based class that holds configuration.
 */
public class OrionConfiguration extends Properties
{
    /**
     * The location of the configuration file that has configuration for the features of this plugin.
     */
    public static final String FEATURE_CONFIGURATION_FILE = "/io/github/orionlibs/task-scheduler/configuration/orion-feature-configuration.prop";


    public static OrionConfiguration loadFeatureConfiguration() throws IOException
    {
        OrionConfiguration featureConfiguration = new OrionConfiguration();
        InputStream defaultConfigStream = OrionConfiguration.class.getResourceAsStream(FEATURE_CONFIGURATION_FILE);
        try
        {
            featureConfiguration.loadDefaultAndCustomConfiguration(defaultConfigStream, null);
            return featureConfiguration;
        }
        catch(IOException e)
        {
            throw new IOException("Could not setup feature configuration for Orion task-scheduler: ", e);
        }
    }


    /**
     * It takes default configuration and nullable custom configuration.
     * For each default configuration property, it registers that one if there is no custom
     * configuration for that property or if customConfig is null. Otherwise it registers the custom one.
     * @param defaultConfiguration
     * @param customConfig
     * @throws IOException if an error occurred when reading from the input stream
     */
    public void loadDefaultAndCustomConfiguration(InputStream defaultConfiguration, Properties customConfig) throws IOException
    {
        Properties tempProperties = new Properties();
        tempProperties.load(defaultConfiguration);
        Map<String, String> allProperties = new HashMap<>();
        boolean doesCustomConfigExist = customConfig != null;
        for(Map.Entry<Object, Object> prop : tempProperties.entrySet())
        {
            String key = (String)prop.getKey();
            String value = null;
            if(doesCustomConfigExist)
            {
                value = customConfig.getProperty(key);
                if(value == null)
                {
                    value = (String)prop.getValue();
                }
            }
            else
            {
                value = (String)prop.getValue();
            }
            allProperties.put(key, value);
        }
        putAll(allProperties);
    }


    /**
     * It converts the configuration this object holds into an InputStream
     * @return an InputStream of the configuration this object holds
     * @throws IOException if writing this property list to the specified output stream throws an IOException
     */
    public InputStream getAsInputStream() throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        store(output, null);
        return new ByteArrayInputStream(output.toByteArray());
    }


    /**
     * remaps the given key to the given value
     * @param key
     * @param value
     */
    public void updateProp(String key, String value)
    {
        put(key, value);
    }
}
