/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.newsreader.mvc.portlet.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.newsreader.NewsConfiguration;
import org.jasig.portlet.newsreader.NewsSet;
import org.jasig.portlet.newsreader.PredefinedNewsConfiguration;
import org.jasig.portlet.newsreader.PredefinedNewsDefinition;
import org.jasig.portlet.newsreader.UserDefinedNewsConfiguration;
import org.jasig.portlet.newsreader.dao.NewsStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;


/**
 * EditNewsPreferencesController provides the main edit page for the News Reader
 * portlet.  The page allows users to view, add, delete and edit all available
 * feeds.
 *
 * @author Anthony Colebourne
 * @author Jen Bourey
 */
@Controller
@RequestMapping("EDIT")
public class EditNewsPreferencesController {

    protected final Log log = LogFactory.getLog(getClass());

    @RequestMapping
    public ModelAndView handleRenderRequestInternal(RenderRequest request,
                                                    RenderResponse response) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();

        PortletSession session = request.getPortletSession();
        Long setId = (Long) session.getAttribute("setId", PortletSession.PORTLET_SCOPE);
        NewsSet set = newsStore.getNewsSet(setId);
        Set<NewsConfiguration> configurations = set.getNewsConfigurations();
        
        // divide the configurations into user-defined and pre-defined
        // configurations for display
        List<UserDefinedNewsConfiguration> myNewsConfigurations = new ArrayList<UserDefinedNewsConfiguration>();
        List<PredefinedNewsConfiguration> predefinedNewsConfigurations = new ArrayList<PredefinedNewsConfiguration>();
        for (NewsConfiguration configuration : configurations) {
        	if (configuration instanceof UserDefinedNewsConfiguration) {
        		myNewsConfigurations.add((UserDefinedNewsConfiguration) configuration);
        	} else if (configuration instanceof PredefinedNewsConfiguration) {
        		predefinedNewsConfigurations.add((PredefinedNewsConfiguration) configuration);
        	}
        }
        
        model.put("myNewsConfigurations", myNewsConfigurations);
        model.put("predefinedNewsConfigurations", predefinedNewsConfigurations);

        // get the user's role listings
        Set<String> userRoles = (Set<String>) session.getAttribute("userRoles", PortletSession.PORTLET_SCOPE);

        // get a list of predefined feeds the user doesn't
        // currently have configured
        List<PredefinedNewsDefinition> definitions = newsStore.getHiddenPredefinedNewsDefinitions(setId, userRoles);
        model.put("hiddenFeeds", definitions);

        model.put("predefinedEditActions", predefinedEditActions);

        // return the edit view
        return new ModelAndView("/editNews", "model", model);
    }

    public void handleActionRequestInternal(ActionRequest request,
                                               ActionResponse response) throws Exception {
        Long id = Long.parseLong(request.getParameter("id"));
        String actionCode = request.getParameter("actionCode");
        PortletSession session = request.getPortletSession();
        Long setId = (Long) session.getAttribute("setId", PortletSession.PORTLET_SCOPE);
        NewsSet set = newsStore.getNewsSet(setId);

        if (actionCode.equals("delete")) {
            NewsConfiguration config = newsStore.getNewsConfiguration(id);
            newsStore.deleteNewsConfiguration(config);
            //Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenNewss");
            //hidden.remove(config.getId());
        } else if (actionCode.equals("show")) {
            NewsConfiguration config = newsStore.getNewsConfiguration(id);
            config.setDisplayed(true);
            newsStore.storeNewsConfiguration(config);
            //Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenNewss");
            //hidden.remove(config.getId());
        } else if (actionCode.equals("hide")) {
            NewsConfiguration config = newsStore.getNewsConfiguration(id);
            config.setDisplayed(false);
            newsStore.storeNewsConfiguration(config);
            //Map<Long, String> hidden = (Map<Long, String>) session.getAttribute("hiddenNewss");
            //hidden.remove(config.getId());
        } else if (actionCode.equals("showNew")) {
            // get user information
            PredefinedNewsDefinition definition = (PredefinedNewsDefinition) newsStore.getNewsDefinition(id);
            log.debug("definition to save " + definition.toString());
            PredefinedNewsConfiguration config = new PredefinedNewsConfiguration();
            config.setNewsDefinition(definition);
            config.setNewsSet(set);
            newsStore.storeNewsConfiguration(config);
        }
    }


    private Map predefinedEditActions;

    public void setPredefinedEditActions(Map predefinedEditActions) {
        this.predefinedEditActions = predefinedEditActions;
    }

    private NewsStore newsStore;

    @Autowired(required = true)
    public void setNewsStore(NewsStore newsStore) {
        this.newsStore = newsStore;
    }
}