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

import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

public class GenericUsernamePasswordInterceptor extends AbstractPhaseInterceptor<Message>
        implements InitializingBean {
    
    private String usernameHeader;
    private String passwordHeader;

    public GenericUsernamePasswordInterceptor() {
        super(Phase.RECEIVE);
    }
    
    public void setUsernameHeader(String usernameHeader) {
        this.usernameHeader = usernameHeader;
    }

    public void setPasswordHeader(String passwordHeader) {
        this.passwordHeader = passwordHeader;
    }

    public void afterPropertiesSet() throws Exception {
        if (usernameHeader == null) {
            throw new IllegalStateException("usernameHeader not set");
        }
        if (passwordHeader == null) {
            throw new IllegalStateException("passwordHeader not set");
        }
    }

    public void handleMessage(Message message) throws Fault {
        Map<String, List<String>> headers = CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
        List<String> username = headers.get(usernameHeader);
        List<String> password = headers.get(passwordHeader);
        if (username != null && username.size() == 1 && password != null && password.size() == 1) {
            message.getExchange().put(Authentication.class, new UsernamePasswordAuthenticationToken(
                    username.get(0), password.get(0)));
        }
    }
}
