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

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapperFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class ConditionalLdapMapperFactory
  extends AbstractLDAPStorageMapperFactory
  implements LDAPStorageMapperFactory<LDAPStorageMapper>
{
  private final static Logger LOGGER = Logger.getLogger(
    ConditionalLdapMapperFactory.class);

  public static final String PROVIDER_ID = "l9g-conditional-ldap-mapper";

  @Override
  protected AbstractLDAPStorageMapper createMapper(ComponentModel cm,
    LDAPStorageProvider ldapsp)
  {
    LOGGER.debug("createMapper");
    return new ConditionalLdapMapper(cm, ldapsp);
  }

  @Override
  public String getId()
  {
    return PROVIDER_ID;
  }
}
