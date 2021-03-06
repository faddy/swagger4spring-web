package com.knappsack.swagger4springweb.controller;

import com.knappsack.swagger4springweb.parser.ApiParser;
import com.knappsack.swagger4springweb.parser.ApiParserImpl;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ResourceListing;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/api")
public class ApiDocumentationController {

    private String baseControllerPackage = "";
    private List<String> additionalControllerPackages = new ArrayList<String>();
    private String baseModelPackage = "";
    private List<String> additionalModelPackages = new ArrayList<String>();
    private String basePath = "";
    private String apiVersion = "v1";
    private Map<String, ApiListing> documentation;
    private List<String> ignorableAnnotations = new ArrayList<String>();
    private boolean ignoreUnusedPathVariables = true;
    private boolean basePathFromReferer = false;
    private ResourceListing resourceList;
    private ApiInfo apiInfo;

    @RequestMapping(value = "/resourceList", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    ResourceListing getResources(HttpServletRequest request) {
        return getResourceList(request);
    }

    @RequestMapping(value = "/resourceList/doc/**", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    ApiListing getDocumentation(HttpServletRequest request) {
        String handlerMappingPath = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        //trim the operation request mapping from the desired value
        handlerMappingPath = handlerMappingPath.substring(handlerMappingPath.lastIndexOf("/doc") + 4, handlerMappingPath.length());

        Map<String, ApiListing> docs = getDocs(request);
        if (docs == null) {
            //TODO throw exception
            return null;
        }

        return docs.get(handlerMappingPath);
    }

    @SuppressWarnings("unused")
    public String getBaseControllerPackage() {
        return baseControllerPackage;
    }

    @SuppressWarnings("unused")
    public void setBaseControllerPackage(String baseControllerPackage) {
        this.baseControllerPackage = baseControllerPackage;
    }

    @SuppressWarnings("unused")
    public List<String> getAdditionalControllerPackages() {
        return additionalControllerPackages;
    }

    @SuppressWarnings("unused")
    public void setAdditionalControllerPackages(List<String> additionalControllerPackages) {
        this.additionalControllerPackages = additionalControllerPackages;
    }

    @SuppressWarnings("unused")
    public String getBaseModelPackage() {
        return baseModelPackage;
    }

    @SuppressWarnings("unused")
    public void setBaseModelPackage(String baseModelPackage) {
        this.baseModelPackage = baseModelPackage;
    }

    @SuppressWarnings("unused")
    public List<String> getAdditionalModelPackages() {
        return additionalModelPackages;
    }

    @SuppressWarnings("unused")
    public void setAdditionalModelPackages(List<String> additionalModelPackages) {
        this.additionalModelPackages = additionalModelPackages;
    }

    @SuppressWarnings("unused")
    public Map<String, ApiListing> getDocumentation() {
        return documentation;
    }

    @SuppressWarnings("unused")
    public void setDocumentation(Map<String, ApiListing> documentation) {
        this.documentation = documentation;
    }

    @SuppressWarnings("unused")
    public String getBasePath() {
        if (basePath == null || basePath.isEmpty()) {
            //If no base path was specified, attempt to get the base path from the request URL
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            if (request != null) {
                // requested from
                String referer = request.getHeader("Referer");

                if (basePathFromReferer && referer != null) {
                    basePath = referer.substring(0, referer.lastIndexOf("/"));
                } else {
                    String mapping = request.getServletPath();
                    basePath = request.getRequestURL().toString();
                    basePath = basePath.substring(0, basePath.indexOf(mapping));
                }
            }
        }
        return basePath;
    }

    @SuppressWarnings("unused")
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @SuppressWarnings("unused")
    public String getApiVersion() {
        return apiVersion;
    }

    @SuppressWarnings("unused")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    private Map<String, ApiListing> getDocs(HttpServletRequest request) {
        if (this.documentation == null) {
            String servletPath = null;
            if(request != null) {
                servletPath = request.getServletPath();
            }
            ApiParser apiParser = new ApiParserImpl(apiInfo, getControllerPackages(), getModelPackages(), getBasePath(),
                    servletPath, apiVersion, ignorableAnnotations, ignoreUnusedPathVariables);
            documentation = apiParser.createApiListings();
        }
        return documentation;
    }

    private ResourceListing getResourceList(HttpServletRequest request) {
        if (this.resourceList == null) {
            String servletPath = null;
            if(request != null) {
                servletPath = request.getServletPath();
                servletPath = servletPath.replace("/resourceList", "");
            }
            ApiParser apiParser = new ApiParserImpl(apiInfo, getControllerPackages(), getModelPackages(), getBasePath(),
                    servletPath, apiVersion, ignorableAnnotations, ignoreUnusedPathVariables);
            resourceList = apiParser.getResourceListing(getDocs(request));
        }
        return resourceList;
    }

    @SuppressWarnings("unused")
    public void setResourceList(ResourceListing resourceList) {
        this.resourceList = resourceList;
    }

    private List<String> getControllerPackages() {
        List<String> controllerPackages = new ArrayList<String>();
        if(baseControllerPackage != null && !baseControllerPackage.isEmpty()) {
            controllerPackages.add(baseControllerPackage);
        }

        if(additionalControllerPackages != null && !additionalControllerPackages.isEmpty()) {
            controllerPackages.addAll(additionalControllerPackages);
        }

        return controllerPackages;
    }

    private List<String> getModelPackages() {
        List<String> modelPackages = new ArrayList<String>();
        if(baseModelPackage != null && !baseModelPackage.isEmpty()) {
            modelPackages.add(baseModelPackage);
        }

        if(additionalModelPackages != null && !additionalModelPackages.isEmpty()) {
            modelPackages.addAll(additionalModelPackages);
        }

        return modelPackages;
    }

    @SuppressWarnings("unused")
    public List<String> getIgnorableAnnotations() {
        return ignorableAnnotations;
    }

    @SuppressWarnings("unused")
    public void setIgnorableAnnotations(List<String> ignorableAnnotations) {
        this.ignorableAnnotations = ignorableAnnotations;
    }

    @SuppressWarnings("unused")
    public boolean isIgnoreUnusedPathVariables() {
        return ignoreUnusedPathVariables;
    }

    @SuppressWarnings("unused")
    public void setIgnoreUnusedPathVariables(final boolean ignoreUnusedPathVariables) {
        this.ignoreUnusedPathVariables = ignoreUnusedPathVariables;
    }

    @SuppressWarnings("unused")
    public void setBasePathFromReferer(final boolean basePathFromReferer) {
        this.basePathFromReferer = basePathFromReferer;
    }

    @SuppressWarnings("unused")
    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    @SuppressWarnings("unused")
    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo = apiInfo;
    }
}
