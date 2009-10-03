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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationException;
import org.springframework.security.SpringSecurityException;

public class SpringSecurityExceptionMapper implements ExceptionMapper<SpringSecurityException> {
    public Response toResponse(SpringSecurityException exception) {
        Response.Status status;
        if (exception instanceof AccessDeniedException) {
            // This means that the principal doesn't have the required authority and we should return 403.
            status = Response.Status.FORBIDDEN;
        } else if (exception instanceof AuthenticationException) {
            // This means that the client could not be authenticated. In this case the client may want to
            // send (new) credentials and we should return 401.
            status = Response.Status.UNAUTHORIZED;
        } else {
            // Everything else is a server problem.
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        return Response.status(status).build();
    }
}
