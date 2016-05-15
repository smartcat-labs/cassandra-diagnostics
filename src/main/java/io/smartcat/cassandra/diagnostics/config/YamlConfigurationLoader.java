package io.smartcat.cassandra.diagnostics.config;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is a YAML based implementation of {@link ConfigurationLoader}.
 */
public class YamlConfigurationLoader implements ConfigurationLoader {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(YamlConfigurationLoader.class);

    /**
     * Default external configuration file name.
     */
    private static final String DEFAULT_CONFIGURATION_URL = "cassandra-diagnostics.yml";

    /**
     * Determines and returns the external configuration URL.
     *
     * @return {@link URL} configuration file URL
     * @throws ConfigurationException in case of a bogus URL
     */
    private URL getStorageConfigUrl() throws ConfigurationException {
        String configUrl = System.getProperty("cassandra.diagnostics.config");
        if (configUrl == null) {
            configUrl = DEFAULT_CONFIGURATION_URL;
        }

        URL url;
        try {
            url = new URL(configUrl);
            url.openStream().close(); // catches well-formed but bogus URLs
        } catch (Exception err) {
            ClassLoader loader = YamlConfigurationLoader.class.getClassLoader();
            url = loader.getResource(configUrl);
            if (url == null) {
                String required = "file:" + File.separator + File.separator;
                if (!configUrl.startsWith(required)) {
                    throw new ConfigurationException(
                            "Expecting URI in variable [cassandra.diagnostics.config]. "
                                    + "Please prefix the file with " + required + File.separator
                                    + " for local files or " + required + "<server>"
                                    + File.separator + " for remote files. Aborting.");
                }
                throw new ConfigurationException(
                        "Cannot locate " + configUrl
                                + ".  If this is a local file, please confirm you've provided "
                                + required + File.separator + " as a URI prefix.");
            }
        }

        return url;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * io.smartcat.cassandra.diagnostics.config.ConfigurationLoader#loadConfig()
     */
    @Override
    public Configuration loadConfig() throws ConfigurationException {
        return loadConfig(getStorageConfigUrl());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * io.smartcat.cassandra.diagnostics.config.ConfigurationLoader#loadConfig(
     * java.net.URL)
     */
    @Override
    public Configuration loadConfig(URL url) throws ConfigurationException {
        try {
            logger.info("Loading settings from {}", url);
            byte[] configBytes;
            try (InputStream is = url.openStream()) {
                configBytes = ByteStreams.toByteArray(is);
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            logConfig(configBytes);

            Constructor constructor = new Constructor(Configuration.class);
            Yaml yaml = new Yaml(constructor);
            Configuration result = yaml.loadAs(new ByteArrayInputStream(configBytes), Configuration.class);

            return result;
        } catch (YAMLException e) {
            throw new ConfigurationException("Invalid yaml", e);
        }
    }

    /**
     * Prints the given configuration using the class logger.
     *
     * @param configBytes YAML configuration in raw format
     */
    private void logConfig(byte[] configBytes) {
        Map<Object, Object> configMap = new TreeMap<>((Map<?, ?>) new Yaml().load(new ByteArrayInputStream(configBytes)));
        logger.info("Node configuration:[" + Joiner.on("; ").join(configMap.entrySet()) + "]");
    }

}
