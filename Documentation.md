# Introduction #

This document explains how to set up CXF to integrate with Spring Security (formerly known as Acegi Security). From the outset it should be noted that there are numerous ways how things can be set up and that there is no unique approach. This is simply due to the fact that there are multiple protocols that can be used to pass credentials (WS-Security, HTTP authentication, custom protocols, etc.) and that requirements related to authentication and authorization vary from one use case to the other. For example, in some use cases all requests must be authenticated, but there is no role based authorization. In other use cases, one would like to use declarative security to implement fine grained authorizations per method, potentially even with methods that don't need authentication and that may be invoked anonymously.

Although CXF doesn't know anything about Spring Security, some scenarios are actually supported out-of-the-box by CXF, because Spring Security and CXF are able to exchange information through standard APIs that both frameworks understand. Other scenarios require tighter integration between Spring Security and CXF.

For these scenarios, cxf-spring-security provides a set of ready-to-use components that can be combined in different ways, depending on the use case. cxf-spring-security has a Spring namespace handler so that these components can easily be configured using schema based configurations. In the following sections, we will first describe the different available components, and at the end of the chapter, describe how to use these components in some specific scenarios. We assume that the reader is familiar with the concepts underlying the Spring Security framework and with the relevant parts of CXF.

**Note:** The reader should be aware that the concept of _security context_ used in this document may have different meanings depending on the context. In fact, there are three different Java interfaces called `SecurityContext`:

  * CXF defines a `org.apache.cxf.security.SecurityContext` interface exposing information about the principal and its roles if the request is authenticated by the container.
  * Spring Security defines a `org.springframework.security.context.SecurityContext` which is basically a holder for an authentication token (i.e. an `org.springframework.security.Authentication` object).
  * JAX-RS defines a `javax.ws.rs.core.SecurityContext` interface exposing information about the principal, the roles assumed by the requestor, whether the request arrived over a secure channel and the authentication scheme used.

# Scenarios supported out-of-the-box by CXF #

The scenarios that don't require any particular integration between Spring Security and CXF are those where the authentication and authorization process is driven by Spring Security. This is the case for HTTP based authentication, provided that authentication is handled by Spring Security's servlet filters. Method level authorization works in this setup because the servlet filters bind the authentication token to the current thread. The only requirement is that the appropriate interceptors are set up in the Spring configuration.

Note however that this works correctly only if requests are processed by a single thread. More precisely, the thread executing the service must be the same as the one on which the servlet filters are executed. This is the case in most setups, but in some scenarios involving asynchronous execution or one-way operations, this may not be true. There is a more detailed discussion about these scenarios in one of the following sections.

Another feature that is supported out-of-the-box is retrieving the user credentials provided by Spring Security in a JAX-RS resource through injection of a `javax.ws.rs.core.SecurityContext` object. Indeed, the default filter configuration in Spring Security includes `SecurityContextHolderAwareRequestFilter` which replaces the `ServletRequest` with a request wrapper that overrides the `getUserPrincipal` and `isUserInRole` methods so that they return information from Spring Security. CXF's HTTP transport uses that information to build a `org.apache.cxf.security.SecurityContext` and the JAX-RS front-end will use that to inject `javax.ws.rs.core.SecurityContext` instances.

# Using schema based Spring configuration #

To enable schema based configuration of the different cxf-spring-security components, it is sufficient to add the relevant namespace declaration and schema location. Since Spring Security also supports schema based configuration, it is in general a good idea to enable that too. The following example shows the required declarations:

```
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:security="http://www.springframework.org/schema/security"
       xmlns:ssec="http://cxf.apache.org/spring-security"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation=
          "http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
           http://www.springframework.org/schema/security
           http://www.springframework.org/schema/security/spring-security-2.0.4.xsd
           http://cxf.apache.org/spring-security
           http://cxf-spring-security.googlecode.com/svn/trunk/cxf-spring-security/src/main/resources/schemas/spring-security.xsd">
```

Before configuring any of the cxf-spring-security components, you should first set up the authentication provider you would like to use. Please refer to the Spring Security documentation for more information about the available providers and their configuration. For testing purposes you can use the following bean declaration, which uses an in-memory user database with two users in different roles:

```
<security:authentication-provider>
  <security:user-service>
    <security:user name="joe" password="password" authorities="ROLE_USER,ROLE_ADMIN"/>
    <security:user name="bob" password="password" authorities="ROLE_USER"/>
  </security:user-service>
</security:authentication-provider>
```

# The server side callback handler for WS-Security #

This component provides a password callback handler that authenticates against the configured Spring authentication provider. The authentication token passed to the provider will be of type `org.springframework.security.providers.UsernamePasswordAuthenticationToken`. Additionally, if the authentication is successful, the callback handler will also store the authentication token in the current `Exchange` for later use. Note that this is not the original `UsernamePasswordAuthenticationToken` instance, but the authentication token returned by the authentication manager. The following snippet shows a sample `WSS4JInInterceptor` configuration that uses the callback handler:

```
<bean class="org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor">
  <constructor-arg>
    <map>
      <entry key="action" value="UsernameToken"/>
      <entry key="passwordType" value="PasswordText"/>
      <entry key="passwordCallbackRef">
        <ssec:server-password-callback-handler logExceptions="true" nestExceptions="false"/>
      </entry>
    </map>
  </constructor-arg>
</bean>
```

The `nestExceptions` and `logExceptions` attributes determine the error reporting behavior in case of an authentication failure. If `nestExceptions` is set to `true`, then the exception from the authentication provider is chained to the exception thrown by the callback handler. As a consequence, the SOAP fault returned to the client will contain the error message from the authentication provider. This should be used with care since it may disclose too much information to the client. If `logExceptions` is set to `true`, then the exception thrown by the authentication provider will be logged separately by the callback handler. This is especially useful when `nestExceptions` is `false`, because in this case, CXF will not see the original exception.

The above configuration assumes that the WSS4J interceptors are configured explicitly. If instead the service is configured using policies, then the `ws-security.callback-handler` property can be used to set the callback handler. For example, when using JAX-WS:

```
<jaxws:endpoint implementor="..." wsdlLocation="..." serviceName="..." address="...">
  <jaxws:properties>
    <entry key="ws-security.callback-handler">
      <ssec:server-password-callback-handler logExceptions="true" nestExceptions="false"/>
    </entry>
  </jaxws:properties>
</jaxws:endpoint>
```

# Building a `UsernamePasswordAuthenticationToken` from protocol headers: 

&lt;ssec:generic-username-password-interceptor&gt;

 #

In some use cases, WS-Security is not an option, e.g. because it would add too much overhead or simply because the service doesn't use SOAP. In these cases, an alternative is to define a custom protocol that uses protocol headers to pass the credentials. E.g. when using JMS, credentials could be passed using JMS message properties. cxf-spring-security provides an interceptor that can be used in these cases, provided that the user name and password are stored in two distinct protocol headers and that the password is sent as plain text. The names of the two protocol headers are configurable, as shown in the following example:

```
<ssec:generic-username-password-interceptor usernameHeader="user" passwordHeader="password"/>
```

The interceptor does NOT perform authentication. It simply builds an unauthenticated `UsernamePasswordAuthenticationToken` and stores it in the current `Exchange`. Also, it does NOT trigger any error if the protocol headers are not present in the message. Therefore, to be effective, this interceptor must be combined with one of the authentication interceptors described below, unless it is used on a service that has method security enabled (in which case Spring Security will perform authentication as required).

# Perform authentication for SOAP services: 

&lt;ssec:soap-authentication-interceptor&gt;

 #

This interceptor looks up the `Authentication` from the current `Exchange`, invokes the configured authentication manager and triggers an appropriate SOAP fault if authentication fails. By default the interceptor also triggers a fault if no authentication token is found. This behavior is configurable; authentication can be made optional as follows:

```
<ssec:soap-authentication-interceptor authenticationRequired="false"/>
```

Note that using this interceptor is not meaningful when WS-Security is enabled because the callback handler provided by cxf-spring-security already forces authentication.

# Setting up CXF's security context: 

&lt;ssec:cxf-security-context-provider-interceptor&gt;

 #

If the request is authenticated by the container or by the transport (see e.g. the `JMSXUserID` support in the JMS transport), CXF adds a `org.apache.cxf.security.SecurityContext` object to the request message object to expose information about the principal and the roles assumed by the requestor. This information is used by other CXF components. E.g. the JAX-RS front-end uses this information to build and inject `javax.ws.rs.core.SecurityContext` instances. If Spring Security is used for authentication, then CXF's `SecurityContext` is not initialized automatically. cxf-spring-security provides an interceptor that can be used if this is required. This interceptor adapts an authenticated `Authentication` object found in the current `Exchange` to the `org.apache.cxf.security.SecurityContext` interface and adds it to the current message. Authorities in the `Authentication` object are mapped one-to-one to roles in the `SecurityContext`.

Using `<ssec:cxf-security-context-provider-interceptor>` is meaningful together with `<ssec:server-password-callback-handler>` or any of the authentication interceptors described in the previous sections.

# Setting up Spring's security context: 

&lt;ssec:spring-security-context-feature&gt;

 #

Attentive readers familiar with the internals of Spring Security may have noticed that the components described so far only add the authentication token to the current `Exchange`, but don't set up Spring's `SecurityContext`, which is required to enable method security. There is a good reason for that: the security context is bound to a particular thread. On the other hand, a given security context instance is only valid for a single request. This has two important implications:

  * Authorization only works properly if there is a one-to-one relation between requests and threads. For the vast majority of configurations this is the case, but in some configurations, CXF may invoke the service implementation asynchronously. In that case, the service method is executed on a different thread than the interceptors. This is the case for one-way operations implemented with JAX-WS: once CXF finishes parsing the input and determines the operation is a one-way, it passes the rest of the chain to an executor and returns immediately so that the client can continue (the client has to wait for the 200 OK response). Note that this is not the case for JMS because one-way operations implemented using JMS are truly asynchronous and the client isn't waiting.
  * The security context must be removed from the thread before the processing of the request completes. Otherwise there is a risk that the security context accidentally gets picked up by another request, and this may cause a security breach.

This makes it clear that an interceptor would not be the right place to manage Spring's security context. cxf-spring-security solves this issue with the help of a `org.apache.cxf.service.invoker.Invoker` proxy that will be installed in front of the real invoker (whose responsibility is to dispatch to the right method of the service implementation). This proxy sets up the security context before delegating to the real invoker and removes it after completion.

To simplify configuration, cxf-spring-security defines a feature (see `org.apache.cxf.feature.AbstractFeature` for a description of the concept of 'feature') that installs this proxy in the right place. This feature is enabled as shown in the following example:

```
<security:global-method-security secured-annotations="enabled"/>

<simple:server serviceClass="..." address="...">
  <simple:serviceBean>
    ...
  </simple:serviceBean>
  ...
  <simple:features>
    <ssec:spring-security-context-feature/>
  </simple:features>
</simple:server>
```

The example uses the simple front-end, but the feature works in the same way for the JAX-WS and JAX-RS front-ends. Also shown in the example is the declaration that enables annotation based method security. As noted above, binding the security context is a prerequisite for using method security.

# Exception mapper for JAX-RS #

When method security on a JAX-RS service is enabled, invocation of a method of that service may result in a `SpringSecurityException` if the request is unauthenticated or the user doesn't have the required role. cxf-spring-security provides an `javax.ws.rs.ext.ExceptionMapper` implementation that maps these exceptions to the appropriate HTTP response statuses (401, 403 or 500). To use this exception mapper, simply register it as a provider:

```
<jaxrs:server address="...">
  <jaxrs:serviceBeans>
    ...
  </jaxrs:serviceBeans>
  <jaxrs:providers>
    <bean class="org.apache.cxf.security.spring.SpringSecurityExceptionMapper"/>
  </jaxrs:providers>
  ...
</jaxrs:server>
```

# Scenario 1: JAX-WS + SOAP/JMS + custom authentication protocol + no method security #

Requirements:
  * The service is implemented using JAX-WS with SOAP/JMS as transport protocol.
  * Credentials are passed as JMS message properties: `user` for the user name and `password` for the (unencrypted) password.
  * Method security is not used and all requests must be authenticated.

This use case can easily be implemented by using two of the interceptors provided by cxf-spring-security. The bean configuration for the JAX-WS endpoint would look as follows:

```
<jaxws:endpoint implementor="..."
                address="jms:..."
                transportId="http://www.w3.org/2010/soapjms/">
  <jaxws:inInterceptors>
    <ssec:generic-username-password-interceptor usernameHeader="user" passwordHeader="password"/>
    <ssec:soap-authentication-interceptor/>
  </jaxws:inInterceptors>
</jaxws:endpoint>
```