/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.security.spring;

import java.security.Principal;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

public class CXFSecurityContextProviderInterceptor extends AbstractPhaseInterceptor<Message> {
    public CXFSecurityContextProviderInterceptor() {
        super(Phase.RECEIVE);
    }

    public void handleMessage(Message message) throws Fault {
        final Authentication authentication = message.getExchange().get(Authentication.class);
        if (authentication != null && authentication.isAuthenticated()) {
            message.put(SecurityContext.class, new SecurityContext() {
                public Principal getUserPrincipal() {
                    return authentication;
                }
                
                public boolean isUserInRole(String role) {
                    GrantedAuthority[] authorities = authentication.getAuthorities();
                    if (authorities != null) {
                        for (GrantedAuthority authority : authorities) {
                            if (role.equals(authority.getAuthority())) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }
}
