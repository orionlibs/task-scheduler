package io.github.orionlibs.task_scheduler.config;

/**
 * provides access to the plugin's config
 */
public class ConfigurationService
{
    private OrionConfiguration configurationRegistry;


    /**
     * stores a config object
     * @param configuration
     */
    public void registerConfiguration(OrionConfiguration configuration)
    {
        configurationRegistry = configuration;
    }


    /**
     * retrieves the value associated with the provided key
     * @param key
     * @return
     */
    public String getProp(String key)
    {
        return configurationRegistry.getProperty(key);
    }


    /**
     * retrieves the value associated with the provided key casted to a boolean
     * @param key
     * @return
     */
    public Boolean getBooleanProp(String key)
    {
        return Boolean.parseBoolean(configurationRegistry.getProperty(key));
    }


    /**
     * remaps the given key to the given value
     * @param key
     * @param value
     */
    public void updateProp(String key, String value)
    {
        configurationRegistry.updateProp(key, value);
    }
}
