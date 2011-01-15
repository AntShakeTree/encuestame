/*
 ************************************************************************************
 * Copyright (C) 2001-2010 encuestame: system online surveys Copyright (C) 2009
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.encuestame.mvc.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.encuestame.core.security.EnMeUserDetails;
import org.encuestame.persistence.dao.IAccountDao;
import org.encuestame.persistence.domain.security.UserAccount;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Default Interceptor for all Controllers.
 * @author Picado, Juan juanATencuestame.org
 * @since Dec 26, 2010 3:34:59 PM
 * @version $Id:$
 */
public class DefaultEnMeInterceptor implements HandlerInterceptor {

    private static Logger log = Logger.getLogger(DefaultEnMeInterceptor.class);

    private final String COOKIE_NAME = "en_me_cookie";

    private final String COOKIE_LANGUAGE = "en_me-language";

    private final String COOKIE_TIMEZONE = "en_me-timezone";

    private final String COOKIE_CONTEXT = "en_me-context";

    /**
     * Account Dao.
     */
    @Autowired
    private IAccountDao accountDao;

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
     */
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        //filter account
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EnMeUserDetails) {
            final EnMeUserDetails details = (EnMeUserDetails) auth.getPrincipal();
            final UserAccount account = this.accountDao.getUserByUsername(details.getUsername());
            if(account != null){
                request.setAttribute("userAccount", account);
                log.debug("Added account to request "+account.getUid());
            } else {
                log.warn("This account not should be null: Username ["+details.getUsername()+"]");
            }
            //cookies
            Cookie cookieName = WebUtils.getCookie(request, this.COOKIE_NAME);
            if(cookieName != null){
                log.debug("Cookie "+cookieName.getName());
                cookieName.setValue(RandomStringUtils.random(4)); //TODO: testing cookies.
            }
        }
        return true;
    }

    /**
     *
     * @param cookieName
     * @param response
     */
    private void createAddCookie(
            final String cookieName,
            final HttpServletResponse response,
            final String value){
        Cookie cookie = new Cookie(cookieName, value);
        //cookie.setMaxAge(expiry)
        response.addCookie(cookie);
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
     */
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
            Cookie cookieName = WebUtils.getCookie(request, this.COOKIE_NAME);
            if(cookieName == null){
                this.createAddCookie(COOKIE_NAME, response, RandomStringUtils.random(4));
            }
            Cookie cookieLanguage = WebUtils.getCookie(request, this.COOKIE_LANGUAGE);
            if(cookieLanguage == null){
                this.createAddCookie(COOKIE_LANGUAGE, response, request.getLocale().toString());
            }
            Cookie cookieTimeZone = WebUtils.getCookie(request, this.COOKIE_TIMEZONE);
            if(cookieTimeZone == null){
                this.createAddCookie(COOKIE_TIMEZONE, response, DateTimeZone.getDefault().toTimeZone().toString());
            }
            Cookie cookieContext = WebUtils.getCookie(request, this.COOKIE_CONTEXT);
            if(cookieContext == null){
                this.createAddCookie(COOKIE_CONTEXT, response, request.getContextPath());
            }
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
     */
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }

    /**
     * @param accountDao the accountDao to set
     */
    public void setAccountDao(final IAccountDao accountDao) {
        this.accountDao = accountDao;
    }
}