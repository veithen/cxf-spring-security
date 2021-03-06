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

import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.Invoker;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

public class SpringSecurityInvokerProxy implements Invoker {
    private final Invoker target;
    
    public SpringSecurityInvokerProxy(Invoker target) {
        this.target = target;
    }

    public Object invoke(Exchange exchange, Object o) {
        Authentication authentication = exchange.get(Authentication.class);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        try {
            return target.invoke(exchange, o);
        } finally {
            securityContext.setAuthentication(null);
        }
    }
}
