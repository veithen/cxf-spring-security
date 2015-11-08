This page tracks the issues that need to be addressed before the code can be contributed to CXF.

# Dependencies #

Since this project integrates CXF with Spring Security, it obviously has dependencies on both frameworks. Since CXF is highly modular and some of the components in cxf-spring-security only apply to some protocols or front-ends, there are dependencies not only on the CXF API, but on specific CXF modules:

  * WS-Security: actually the dependency here is on `wss4j`, not on `cxf-rt-ws-security`.
  * JAX-RS (for the exception mapper): dependency on `jsr311-api`.
  * SOAP (for the `SoapAuthenticationInterceptor` which needs to produce well defined SOAP faults): `cxf-rt-bindings-soap`.

This is an issue because someone who wants to use `cxf-spring-security` only with JAX-RS doesn't want to introduce a dependency on WSS4J (and vice-versa).

There are three possible approaches to handle this (from a Maven perspective):

  * Contribute the individual components in `cxf-spring-security` to the relevant modules in CXF: `cxf-rt-ws-security`, `cxf-rt-frontend-jaxrs`, `cxf-rt-bindings-soap` and some common module (for those components that are protocol/front-end independent). This would introduce a (provided or optional) dependency on `spring-security-core` into these modules.
  * Keep all the Spring Security related code in a single module (as in the current situation) and declare the dependencies on specific CXF modules as optional or provided.
  * Move the protocol/front-end specific components of `cxf-spring-security` into several new modules, each one depending on Spring Security and the relevant CXF module: `cxf-spring-security-ws-security`, `cxf-spring-security-jaxrs`, `cxf-spring-security-soap`, etc. There is then no need for optional/provided dependencies.

While the last solution would be the cleanest, it would also be somewhat of an overkill, because each of the modules would only contain a very small number of classes. For the moment, most of the arguments are in favor of the second solution:

  * The code in `cxf-spring-security` only depends on very stable CXF APIs. This makes this component a good candidate for mixing versions. Somebody who wants to integrate CXF with Spring Security may find it acceptable to use a snapshot version of a `cxf-spring-security` component, but may be reluctant to use a snapshot version (or upgrade to a newer release) of the core libraries. This is not possible with first approach described above.
  * `cxf-spring-security` provides bean definition parsers to make Spring based configuration easy. Keeping all this code in a single module (and use a schema that is separate from the existing front-end specific schemas) is more manageable and less intrusive. It also allows the user to get a quick overview of the available features.
  * Some users may be required to use Acegi Security (e.g. because they have existing company specific authentication providers) and thus want to backport the code to Acegi Security. This is easier if the code lives in a separate module.
  * Last but not least, it's always a good idea not to add too much stuff into the core libraries of a framework, but instead to keep things separated.

Optional/provided dependencies also work best in the second approach: e.g. if somebody wants to use the WS-Security specific components in his project, he will have to add the relevant dependencies to the project anyway (to use the WSS4J interceptors).

While the second approach seems the most appropriate from a Maven perspective, it is not yet clear how this would work out in OSGi.

# OSGi #

TODO

# Tests #

TODO

# Documentation #

TODO