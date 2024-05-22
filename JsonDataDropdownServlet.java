import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component(service = Servlet.class,
        property = {Constants.SERVICE_DESCRIPTION + "=Card Fund Servlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=" + "/bin/jsonDataDropdown"})
public class JsonDataDropdownServlet extends SlingSafeMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDataDropdownServlet.class);
    private final Gson gson = new Gson();

    // Constants for API parameters
    private static final String BASE_API_URL = "https://secure.colonialfirststate.com.au/fp/pricenperformance/products/funds/performance";
    private static final Map<String, String[]> API_PARAMETERS;

    static {
        // Add more parameters as needed...
        API_PARAMETERS = new LinkedHashMap<>(); // Use LinkedHashMap to preserve insertion order
        API_PARAMETERS.put("companyCode", new String[]{"001"});
        API_PARAMETERS.put("mainGroup", new String[]{"SF"});
        API_PARAMETERS.put("productId", new String[]{"11"});
        API_PARAMETERS.put("category", new String[]{"Conservative", "Defensive", "Geared", "Growth", "High Growth", "Moderate", "Single sector option"});
        API_PARAMETERS.put("asset", new String[]{"Alternatives", "Australian Property Securities", "Australian Share", "Cash and other income", "Fixed Interest", "Global Property Securities", "Global Share", "Infrastructure securities", "Multi-Sector"});
        API_PARAMETERS.put("risk", new String[]{"1", "3", "4", "5", "6", "7"});
        API_PARAMETERS.put("mintimeframe", new String[]{"At least 10 years", "At Least 3 years", "At least 5 years", "At least 7 years", "No minimum"});
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        LOGGER.error("Venkat.{}", out.toString());
        String jsonDataPath = getJsonDataPath(request);
        if (jsonDataPath == null) {
            LOGGER.error("JSON Data path is not provided.");
            return;
        }

        try {
            JsonObject apiResponse = callExternalAPI(BASE_API_URL, API_PARAMETERS);
            if (apiResponse != null) {
                writeToJSONFile(request.getResourceResolver(), jsonDataPath, apiResponse);
            }
        } catch (IOException | PersistenceException e) {
            LOGGER.error("Error: {}", e.getMessage());
        }
    }

    private String getJsonDataPath(SlingHttpServletRequest request) {
        Resource pathResource = request.getResource();
        if (pathResource != null) {
            return pathResource.getPath() + "/apir";
        }
        return null;
    }

    private JsonObject callExternalAPI(String baseUrl, Map<String, String[]> parameters) throws IOException {
        StringBuilder apiURL = new StringBuilder(baseUrl + "?");
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            for (String value : values) {
                try {
                    String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
                    apiURL.append(key).append("=").append(encodedValue).append("&");
                } catch (IOException e) {
                    LOGGER.error("Error encoding parameter value: {}", e.getMessage());
                }
            }
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(apiURL.toString());
            try (CloseableHttpResponse apiResponse = httpClient.execute(httpGet)) {
                if (apiResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = apiResponse.getEntity();
                    if (entity != null) {
                        String apiResponseString = EntityUtils.toString(entity);
                        return JsonParser.parseString(apiResponseString).getAsJsonObject();
                    }
                }
            }
        }
        return null;
    }

    private void writeToJSONFile(ResourceResolver resourceResolver, String jsonDataPath, JsonObject apiResponse) throws PersistenceException {
        Resource resource = resourceResolver.getResource(jsonDataPath);
        if (resource != null) {
            ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
            if (valueMap != null) {
                String jsonString = gson.toJson(apiResponse);
                valueMap.put("apir", jsonString);
                resourceResolver.commit();
            }
        } else {
            LOGGER.error("Resource at path {} not found", jsonDataPath);
        }
    }
}
