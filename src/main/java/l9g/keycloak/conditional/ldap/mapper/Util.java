/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public class Util
{

  private final static SimpleDateFormat LDAP_DATE_FORMAT
    = new SimpleDateFormat("yyyyMMddHHmmss'Z'");

  static
  {
    LDAP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private Util()
  {
  }

  public static long asn1ToSeconds(String ldapTime)
  {
    long seconds = 0;
    try
    {
      Date date = LDAP_DATE_FORMAT.parse(ldapTime);
      seconds = date.getTime() / 1000;
    }
    catch (java.text.ParseException ex)
    {
      java.util.logging.Logger.getLogger(
        ConditionalLdapMapper.class.getName()).log(Level.SEVERE, null, ex);
    }
    return seconds;
  }

  public static boolean isNullOrEmpty(String value)
  {
    return value == null || value.trim().length() == 0;
  }
}
