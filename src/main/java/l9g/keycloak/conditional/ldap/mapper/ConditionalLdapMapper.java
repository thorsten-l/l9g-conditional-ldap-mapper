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
package l9g.keycloak.conditional.ldap.mapper;

import java.util.Collections;
import java.util.List;
import l9g.keycloak.conditional.ldap.mapper.config.Config;
import l9g.keycloak.conditional.ldap.mapper.config.ConfigContition;
import l9g.keycloak.conditional.ldap.mapper.config.ConfigFactory;
import l9g.keycloak.conditional.ldap.mapper.config.ConfigMapper;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class ConditionalLdapMapper extends AbstractLDAPStorageMapper
{
  private final static Logger LOGGER = Logger.getLogger(ConditionalLdapMapper.class);

  public ConditionalLdapMapper(
    ComponentModel mapperModel, LDAPStorageProvider ldapProvider)
  {
    super(mapperModel, ldapProvider);
    this.config = ConfigFactory.getConfig();
  }

  @Override
  public void onImportUserFromLDAP(
    LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate)
  {
    String dn = ldapUser.getDn().toString().toLowerCase();
    LOGGER.debug("onImportUserFromLDAP: " + user.getUsername() + " " + dn);

    if (config != null)
    {
      handleConditions(config.getConfigConditions(), ldapUser, user, realm);
      handleMappers(config.getConfigMappers(), user, realm);
    }
    else
    {
      LOGGER.debug("config not available");
    }
  }

  @Override
  public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel user,
    RealmModel realm)
  {
  }

  @Override
  public UserModel proxy(LDAPObject ldapUser, UserModel user, RealmModel realm)
  {
    return user;
  }

  @Override
  public void beforeLDAPQuery(LDAPQuery ldapq)
  {
  }

  private void handleConditions(List<ConfigContition> conditions,
    LDAPObject ldapUser, UserModel user, RealmModel realm)
  {
    if (conditions != null && !conditions.isEmpty())
    {
      for (ConfigContition condition : conditions)
      {
        String ldapAttributeName = condition.getLdapAttribute();
        String ldapAttributeValue;

        if ("dn".equalsIgnoreCase(ldapAttributeName))
        {
          ldapAttributeValue = ldapUser.getDn().toString().toLowerCase();
        }
        else
        {
          ldapAttributeValue = ldapUser.getAttributeAsString(ldapAttributeName);
        }

        LOGGER.debug(ldapAttributeName + " : " + ldapAttributeValue);
        boolean conditionIsValid = false;

        if (ldapAttributeValue != null && ldapAttributeValue.length() > 0)
        {
          if (condition.getStartsWith() != null
            && condition.getStartsWith().length() > 0
            && ldapAttributeValue.startsWith(condition.getStartsWith()))
          {
            conditionIsValid = true;
          }
          else if (condition.getEndsWith() != null
            && condition.getEndsWith().length() > 0
            && ldapAttributeValue.endsWith(condition.getEndsWith()))
          {
            conditionIsValid = true;
          }
          else if (condition.getEquals() != null
            && condition.getEquals().length() > 0
            && ldapAttributeValue.equals(condition.getEquals()))
          {
            conditionIsValid = true;
          }

          if (conditionIsValid)
          {
            handleConditions(
              condition.getConfigConditions(), ldapUser, user, realm);
            handleMappers(condition.getConfigMappers(), user, realm);
          }
        }
      }
    }
  }

  private void handleMappers(
    List<ConfigMapper> mappers, UserModel user, RealmModel realm)
  {
    if (mappers != null && !mappers.isEmpty())
    {
      for (ConfigMapper mapper : mappers)
      {
        String roleName = mapper.getUserRole();
        String attributeName = mapper.getUserAttribute();
        String attributeValue = mapper.getValue();

        if (roleName != null && roleName.length() > 0)
        {
          RoleModel role = realm.getRole(roleName);
          if (role == null)
          {
            realm.addRole(roleName, roleName);
            role = realm.getRole(roleName);
            role.setDescription(config.getDescription());
          }
          user.grantRole(role);
        }
        else if (attributeName != null && attributeName.length() > 0)
        {
          user.setAttribute(attributeName,
            Collections.singletonList(attributeValue));
        }
      }
    }
  }

  private final Config config;
}
