# l9g-conditional-ldap-mapper for Keycloak

Setting realm roles and user attributes depending on LDAP user attributes.

## build
- run `mvn package` (Maven)

## keycloak server
- copy the `l9g-conditional-ldap-mapper.jar` and the sample `l9g-conditional-ldap-mapper-config.xml` into `keycloak/providers` directory.
- restart keycloak
- *edit* `l9g-conditional-ldap-mapper-config.xml`
- add `l9g-conditional-ldap-mapper` to user federated (your LDAP) Mappers.
- [Action] -> Sync All Users
