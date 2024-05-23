package com.ifx.core.servlets;

import java.io.IOException;
import java.util.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.ifx.core.pojos.design.DesignFamily;
import com.ifx.core.services.design.DesignService;
import com.ifx.core.utils.PageUtil;

@Component(service = { Servlet.class }, immediate = true, name = "Card Fund List Dropdown Servlet")
@SlingServletResourceTypes(resourceTypes="ifx/list/cardfund", methods=HttpConstants.METHOD_GET)
@ServiceDescription("Card Fund List Dropdown Servlet")
public class CardFundDropDownServlet extends SlingSafeMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardFundDropDownServlet.class);

    private static final long serialVersionUID = -4458186320139222562L;
    private static final String DATASOURCE = "datasource";
    private static final String APRI_THRESHOLD_PARAM = "apir";

    @Reference
    private transient DesignService designService;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {

        CardFundFamily[] cardFundFamilies;
        ResourceResolver resourceResolver = request.getResourceResolver();
        String pagePath = PageUtil.getPagePathFromRequest(request);
        PageManager pageMgr = resourceResolver.adaptTo(PageManager.class);
        Page currentPage = pageMgr.getPage(pagePath);
        String currentPageDesignId="";
        int index=0;
        if(currentPage.getProperties().containsKey(APRI_THRESHOLD_PARAM)) {
            currentPageDesignId = currentPage.getProperties().get(APRI_THRESHOLD_PARAM, String.class);
        }
        Locale locale = currentPage.getLanguage(false);
        Resource resource = request.getResource();
        Resource res = resource.getChild(DATASOURCE);
        boolean lookupParentFamily = false;
        Optional<String> langauge = Optional.of(locale.getLanguage());
        Page parentPage = PageUtil.getParentPage(request,currentPage);
        String apir= parentPage.getProperties().get(APRI_THRESHOLD_PARAM, String.class);
        if (lookupParentFamily && StringUtils.isNotBlank(apir)) {
            cardFundFamilies = designService.getCardFundFamilies(langauge,apir);
        } else {
            cardFundFamilies = designService.getCardFundFamilies(langauge);
        }

        Gson gson = new Gson();
        List<Resource> resourceList = new ArrayList<>();
        if (null != cardFundFamilies) {
            for (int i=0;i<cardFundFamilies.length;i++) {
                CardFundFamily cardFundFamily=cardFundFamilies[i];
                ValueMap vm = new ValueMapDecorator(new HashMap<>());
                if(currentPageDesignId.equals(cardFundFamily.getApir())){
                    index=i;
                }
                vm.put("value", Base64.getEncoder()
                        .encodeToString(gson.toJson(cardFundFamily).getBytes()));
                vm.put("text", cardFundFamily.getMarketingName()));
                resourceList.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED , vm));
            }
            if(!Objects.equals(parentPage,currentPage)) {
                Collections.swap(resourceList, 0, index);
            }
        }
        DataSource dataSource = new SimpleDataSource(resourceList.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }
}
