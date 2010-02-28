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
package org.apache.cxf.systest.security;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.testutil.common.AbstractClientServerTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpringHttpAuthJaxwsOnewayTest extends AbstractClientServerTestBase {
    public static class SpringServer extends AbstractSpringServer {
        public SpringServer() {
            super("/security_spring_http_auth_jaxws_oneway");
        }
        
        public static void main(String args[]) {
            try {
                SpringServer s = new SpringServer();
                s.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(-1);
            } finally {
                System.out.println("done!");
            }
        }
    }
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        assertTrue(launchServer(SpringServer.class));
    }
    
    @Test
    public void test() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(OnewayService.class);
        factory.setAddress("http://localhost:9080/oneway");
        OnewayService service = (OnewayService)factory.create();
        service.testOneway("test");
    }
}
