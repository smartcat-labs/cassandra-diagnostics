package io.smartcat.cassandra_diagnostics.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;

public class YamlConfigurationLoader implements ConfigurationLoader {

    private final static Logger logger = LoggerFactory.getLogger(YamlConfigurationLoader.class);
    private final static String DEFAULT_CONFIGURATION_URL = "cassandra-diagnostics.yml";
    
    private URL getStorageConfigURL() throws ConfigurationException
    {
        String configUrl = System.getProperty("cassandra.diagnostics.config");
        if (configUrl == null) {
            configUrl = DEFAULT_CONFIGURATION_URL;
        }

        URL url;
        try
        {
            url = new URL(configUrl);
            url.openStream().close(); // catches well-formed but bogus URLs
        }
        catch (Exception e)
        {
            ClassLoader loader = YamlConfigurationLoader.class.getClassLoader();
            url = loader.getResource(configUrl);
            if (url == null)
            {
                String required = "file:" + File.separator + File.separator;
                if (!configUrl.startsWith(required))
                    throw new ConfigurationException("Expecting URI in variable: [cassandra.diagnostics.config].  Please prefix the file with " + required + File.separator +
                            " for local files or " + required + "<server>" + File.separator + " for remote files. Aborting.");
                throw new ConfigurationException("Cannot locate " + configUrl + ".  If this is a local file, please confirm you've provided " + required + File.separator + " as a URI prefix.");
            }
        }

        return url;
    }

    /* (non-Javadoc)
	 * @see io.smartcat.cassandra_diagnostics.config.ConfigurationLoader#loadConfig()
	 */
    @Override
	public Configuration loadConfig() throws ConfigurationException
    {
        return loadConfig(getStorageConfigURL());
    }

    /* (non-Javadoc)
	 * @see io.smartcat.cassandra_diagnostics.config.ConfigurationLoader#loadConfig(java.net.URL)
	 */
    @Override
	public Configuration loadConfig(URL url) throws ConfigurationException
    {
        try
        {
            logger.info("Loading settings from {}", url);
            byte[] configBytes;
            try (InputStream is = url.openStream())
            {
                configBytes = ByteStreams.toByteArray(is);
            }
            catch (IOException e)
            {
                throw new AssertionError(e);
            }

            logConfig(configBytes);
            
            org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor(Configuration.class);
            Yaml yaml = new Yaml(constructor);
            Configuration result = yaml.loadAs(new ByteArrayInputStream(configBytes), Configuration.class);

            return result;
        }
        catch (YAMLException e)
        {
            throw new ConfigurationException("Invalid yaml", e);
        }
    }

    private void logConfig(byte[] configBytes)
    {
        Map<Object, Object> configMap = new TreeMap<>((Map<?, ?>) new Yaml().load(new ByteArrayInputStream(configBytes)));
        logger.info("Node configuration:[" + Joiner.on("; ").join(configMap.entrySet()) + "]");
    }

}
