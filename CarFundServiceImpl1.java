import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ifx.core.pojos.design.DesignFamily;
import com.ifx.core.services.api.ApiGatewayService;
import com.ifx.core.services.common.myifx.ResourceOwnerTokenService;
import com.ifx.core.services.design.DesignService;
import com.ifx.core.utils.Constants;
import com.ifx.core.utils.PageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = CarFundService.class, immediate = true)
public class CarFundServiceImpl implements CarFundService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarFundServiceImpl.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private ResourceOwnerTokenService resourceOwnerTokenService;

    @Reference
    private ApiGatewayService apiGatewayService;

    @Override
    public CarFund[] getCarFunds(Optional<String> productId) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(Constants.COMPANY_CODE, APIConstants.COMPANY_CODE);
        queryParameters.put(Constants.MAIN_GROUP, APIConstants.MAIN_GROUP);
        queryParameters.put(Constants.PRODUCT_ID, productId.orElse(APIConstants.PRODUCT_ID));
        queryParameters.put(Constants.CATEGORY, "Conservative");
        queryParameters.put(Constants.CATEGORY, "Defensive");
        queryParameters.put(Constants.CATEGORY, "Geared");
        queryParameters.put(Constants.CATEGORY, "Growth");
        queryParameters.put(Constants.CATEGORY, "High Growth");
        queryParameters.put(Constants.CATEGORY, "Moderate");
        queryParameters.put(Constants.CATEGORY, "Single sector option");
        queryParameters.put(Constants.ASSET, "");
        queryParameters.put(Constants.ASSET, "Alternatives");
        queryParameters.put(Constants.ASSET, "Australian Property Securities");
        queryParameters.put(Constants.ASSET, "Australian Share");
        queryParameters.put(Constants.ASSET, "Cash and other income");
        queryParameters.put(Constants.ASSET, "Fixed Interest");
        queryParameters.put(Constants.ASSET, "Global Property Securities");
        queryParameters.put(Constants.ASSET, "Global Share");
        queryParameters.put(Constants.ASSET, "Infrastructure securities");
        queryParameters.put(Constants.ASSET, "Multi-Sector");
        queryParameters.put(Constants.RISK, "1");
        queryParameters.put(Constants.RISK, "3");
        queryParameters.put(Constants.RISK, "4");
        queryParameters.put(Constants.RISK, "5");
        queryParameters.put(Constants.RISK, "6");
        queryParameters.put(Constants.RISK, "7");
        queryParameters.put(Constants.MIN_TIME_FRAME, "At least 10 years");
        queryParameters.put(Constants.MIN_TIME_FRAME, "At Least 3 years");
        queryParameters.put(Constants.MIN_TIME_FRAME, "At least 5 years");
        queryParameters.put(Constants.MIN_TIME_FRAME, "At least 7 years");
        queryParameters.put(Constants.MIN_TIME_FRAME, "No minimum");

        return getCarFundDetails(queryParameters);
    }

    private CarFund[] getCarFundDetails(Map<String, String> queryParameter) {
        CarFund[] carFunds = new CarFund[0];
        String url = constructCarFundApiUrl(queryParameter);

        try {
            HttpRequest request = resourceOwnerTokenService.getHttpRequest().uri(new URI(url)).GET().build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(apiGatewayService.getTimeout())).build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            String responseJSONStr = response.body();
            if (response.statusCode() == 200) {
                return parseCarFunds(responseJSONStr);
            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            LOGGER.error("getCarFundDetails(): Exception occurred", e);
        }
        return carFunds;
    }

    private String constructCarFundApiUrl(Map<String, String> queryParameters) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(APIConstants.BASE_URL);

        boolean isFirstParam = true;
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (!isFirstParam) {
                urlBuilder.append("&");
            } else {
                isFirstParam = false;
            }
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return urlBuilder.toString();
    }

    private CarFund[] parseCarFunds(String responseJSONStr) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(responseJSONStr, CarFund[].class);
        } catch (JsonSyntaxException e) {
            LOGGER.error("parseCarFunds(): JSON syntax exception occurred", e);
        }
        return new CarFund[0];
    }
}
