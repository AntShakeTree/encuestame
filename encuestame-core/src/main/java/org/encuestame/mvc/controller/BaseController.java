/*
 ************************************************************************************
 * Copyright (C) 2001-2010 encuestame: system online surveys Copyright (C) 2010
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.encuestame.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.tanesha.recaptcha.ReCaptcha;

import org.apache.log4j.Logger;
import org.aspectj.apache.bcel.verifier.statics.Pass1Verifier;
import org.encuestame.core.persistence.pojo.SecUserSecondary;
import org.encuestame.core.service.IServiceManager;
import org.encuestame.core.service.ISurveyService;
import org.encuestame.core.service.ITweetPollService;
import org.encuestame.core.service.ServiceManager;
import org.encuestame.core.service.AbstractSurveyService;
import org.encuestame.core.service.TweetPollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationProcessingFilter;
import org.springframework.security.web.context.HttpSessionContextIntegrationFilter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base Controller.
 * @author Picado, Juan juan@encuestame.org
 * @since Mar 13, 2010 10:41:38 PM
 * @version $Id: $
 */
public class BaseController {

     protected Logger log = Logger.getLogger(this.getClass());

     /**
      * {@link ReCaptcha}.
      */
     private ReCaptcha reCaptcha;

    /**
     * {@link ServiceManager}.
     */
    @Autowired
    private IServiceManager serviceManager;

    /**
     * {@link AuthenticationManager}.
     */
    @Autowired
    private AuthenticationManager authenticationManager;


    /**
     * @return the serviceManager
     */
    public IServiceManager getServiceManager() {
        return serviceManager;
    }

    /**
     * Get Current Request Attributes.
     * @return {@link RequestAttributes}
     */
    public RequestAttributes getContexHolder(){
         return RequestContextHolder.currentRequestAttributes();
    }

    /**
     * Get {@link ServletRequestAttributes}.
     * @return {@link ServletRequestAttributes}
     */
    public HttpServletRequest getServletRequestAttributes(){
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * Build Domain.
     * @param request {@link HttpServletRequest}.
     * @return
     */
    public String buildDomainWithRequest(final HttpServletRequest request){
            final StringBuffer stringBuffer = new StringBuffer(this.isSecure(request) ? "https://" : "http://");
            stringBuffer.append(request.getServerName());
            stringBuffer.append(request.getContextPath());
            return stringBuffer.toString();
    }

    /**
     *
     * @param request
     * @return
     */
    public Boolean isSecure(final HttpServletRequest request){
        Boolean secure = false;
        final String protocol = request.getProtocol();
        if (protocol.indexOf("HTTPS") > -1) {
            secure = true;
        } else {
            secure = false;
            }
        return secure;
    }

    /**
     * Get By Username.
     * @param username username
     * @return
     */
    public SecUserSecondary getByUsername(final String username){
        return getServiceManager().getApplicationServices().getSecurityService().findUserByUserName(username);
    }

    /**
     * Get Ip Client.
     * @return ip
     */
    public String getIpClient(){
        //FIXME if your server use ProxyPass you need get IP from x-forwarder-for, we need create
        // a switch change for ProxyPass to normal get client Id.
               //getServletRequestAttributes().getRemoteAddr();
        return getServletRequestAttributes().getHeader("X-FORWARDED-FOR");
    }

    /**
     * Authenticate.
     * @param request {@link HttpServletRequest}
     * @param username username
     * @param password password
     */
    protected void authenticate(HttpServletRequest request, final String username, final String password) {
        try{
            final UsernamePasswordAuthenticationToken usernameAndPassword = new UsernamePasswordAuthenticationToken(username, password);
            final HttpSession session = request.getSession();
            session.setAttribute(
                    AuthenticationProcessingFilter.SPRING_SECURITY_LAST_USERNAME_KEY,
                    username);

            final Authentication auth = getAuthenticationManager().authenticate(usernameAndPassword);

            final SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(auth);
            session.setAttribute( HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        }
        catch (AuthenticationException e) {
            SecurityContextHolder.getContext().setAuthentication(null);
            log.error("Authenticate", e);
        }
    }

    /**
     * @param serviceManager
     *            the serviceManager to set
     */
    public void setServiceManager(IServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /**
     * Get {@link AbstractSurveyService}.
     * @return survey service
     */
    public ISurveyService getSurveyService(){
        return getServiceManager().getApplicationServices().getSurveyService();
    }

    /**
     * Get {@link TweetPollService}.
     * @return
     */
    public ITweetPollService getTweetPollService(){
        return getServiceManager().getApplicationServices().getTweetPollService();
    }

    /**
     * @return the authenticationManager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @param authenticationManager the authenticationManager to set
     */
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    /**
     * @return the reCaptcha
     */
    public ReCaptcha getReCaptcha() {
        return reCaptcha;
    }

    /**
     * @param reCaptcha
     *            the reCaptcha to set
     */
    @Autowired
    public void setReCaptcha(final ReCaptcha reCaptcha) {
        this.reCaptcha = reCaptcha;
    }
}
