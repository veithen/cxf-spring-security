This page lists some use cases that are currently not covered by cxf-spring-security and some ideas of what needs to be implemented to satisfy them.

# WS-Security with authorization only #

For WS-Security, cxf-spring-security only covers use cases where authentication is delegated from the callback handler to Spring Security. There are use cases where authentication should be handled entirely by WSS4JInInterceptor (e.g. X509 certificates?) and where Spring Security would only be used for authorization. This requires an interceptor that retrieves the principal authenticated by WSS4J and that build an appropriate (pre-authenticated) authentication token for Spring Security. Note that an authentication manager would still be required in order to determine the authorities for the principal (otherwise using authorization would be pointless).

Links:

  * Use case: http://www.mail-archive.com/users@cxf.apache.org/msg09851.html
  * Extracting the WSS4J output from the message context: http://www.mail-archive.com/users@cxf.apache.org/msg10460.html

# Scenario: `<jaxws:endpoint>` with HTTP Auth #

When a JAX-WS service is published using `<jaxws:endpoint>`, it is not possible to use the Spring Security servlet filters and HTTP Auth must be implemented using interceptors.

# Scenario: URL based authentication with JAX-RS #

Systest [exists](http://svn.apache.org/repos/asf/cxf/sandbox/veithen/cxf-spring-security/cxf-systests-spring-security/src/test/resources/security_spring_url_based_auth_jaxrs/WEB-INF/beans.xml), but needs to be documented.