/*
 * Copyright 2023 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.keycloak.conditional.ldap.mapper.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import l9g.keycloak.conditional.ldap.mapper.Util;
import static l9g.keycloak.conditional.ldap.mapper.Util.isNullOrEmpty;
import org.jboss.logging.Logger;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class ConfigFactory
{
  private final static Logger LOGGER = Logger.getLogger(ConfigFactory.class);

  private final static String CONFIG_FILENAME
    = "l9g-conditional-ldap-mapper-config.xml";

  private final static String[] CONFIG_DIRS =
  {
    ".", "providers", "/opt/keycloak/providers"
  };

  private final static ConfigFactory SINGLETON = new ConfigFactory();

  public final static String ATTRIBUTE_UPDATED_AT = "updatedAt";

  private ConfigFactory()
  {
    LOGGER.debug("Config Factory created");
    this.xmlMapper = new XmlMapper();

    for (int i = 0; i < CONFIG_DIRS.length && !fileExists; i++)
    {
      configFile = new File(CONFIG_DIRS[i], CONFIG_FILENAME);
      fileExists = configFile.exists() && configFile.canRead();
    }

    if (!fileExists)
    {
      LOGGER.error("Config file NOT found!");
    }
  }

  private synchronized Config _getConfig()
  {
    if (fileExists && configFileTimestamp < configFile.lastModified())
    {
      configFileTimestamp = configFile.lastModified();
      LOGGER.info("Loading config file : "
        + configFile.getAbsolutePath());

      try
      {
        config = xmlMapper.readValue(configFile, Config.class);

        if (Util.isNullOrEmpty(config.getDescription()))
        {
          config.setDescription("l9g conditional ldap mapper");
        }

        attributes.clear();

        if (config.getUpdatedAt() != null
          && !Util.isNullOrEmpty(config.getUpdatedAt().getValue()))
        {
          attributes.add(ATTRIBUTE_UPDATED_AT);
        }

        addAttributeMapperNames(config.getConfigMappers());
        scanConditionsForAttributeMappers(config.getConfigConditions());

        attributes.forEach(attributeName
          -> LOGGER.info("L9G conditional LDAP mapper, additional attribute: "
            + attributeName));
      }
      catch (Throwable th)
      {
        LOGGER.error("Error loading config file: " + th.getMessage(), th);
      }
    }

    return config;
  }

  private void scanConditionsForAttributeMappers(
    List<ConfigContition> conditions)
  {
    if (conditions != null)
    {
      conditions.forEach(condition ->
      {
        scanConditionsForAttributeMappers(condition.getConfigConditions());
        addAttributeMapperNames(condition.getConfigMappers());
      });
    }
  }

  private void addAttributeMapperNames(List<ConfigMapper> mappers)
  {
    if (mappers != null)
    {
      mappers.forEach(mapper ->
      {
        if (!isNullOrEmpty(mapper.getUserAttribute()))
        {
          attributes.add(mapper.getUserAttribute());
        }
      });
    }
  }

  public static Config getConfig()
  {
    return SINGLETON._getConfig();
  }

  public static Set<String> getAttributes()
  {
    return SINGLETON.attributes;
  }

  private final XmlMapper xmlMapper;

  private File configFile;

  private boolean fileExists;

  private long configFileTimestamp;

  private Config config;

  private final HashSet<String> attributes = new HashSet<>();
}
