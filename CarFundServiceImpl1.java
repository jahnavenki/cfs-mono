package au.com.cfs.winged.core.impl;

import au.com.cfs.winged.core.config.ApiGatewayService;
import au.com.cfs.winged.core.models.common.CardFundAPIConstants;
import au.com.cfs.winged.core.models.pojo.CardFundFamily;
import au.com.cfs.winged.core.services.CardFundService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import au.com.cfs.winged.core.models.common.ResourceOwnerTokenService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@Component(service = CardFundService.class, immediate = true)
public class CarFundServiceImpl implements CardFundService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarFundServiceImpl.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private ResourceOwnerTokenService resourceOwnerTokenService;

    @Reference
    private ApiGatewayService apiGatewayService;

    @Override
    public CardFundFamily[] getCardFundFamilies() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("companyCode", CardFundAPIConstants.COMPANY_CODE);
        queryParameters.put("mainGroup", CardFundAPIConstants.MAIN_GROUP);
        queryParameters.put("category", "Conservative");
        queryParameters.put("category", "Defensive");
        queryParameters.put("category", "Geared");
        queryParameters.put("category", "Growth");
        queryParameters.put("category", "High Growth");
        queryParameters.put("category", "Moderate");
        queryParameters.put("category", "Single sector option");
        queryParameters.put("asset", "");
        queryParameters.put("asset", "Alternatives");
        queryParameters.put("asset", "Australian Property Securities");
        queryParameters.put("asset", "Australian Share");
        queryParameters.put("asset", "Cash and other income");
        queryParameters.put("asset", "Fixed Interest");
        queryParameters.put("asset", "Global Property Securities");
        queryParameters.put("asset", "Global Share");
        queryParameters.put("asset", "Infrastructure securities");
        queryParameters.put("asset", "Multi-Sector");
        queryParameters.put("risk", "1");
        queryParameters.put("risk", "3");
        queryParameters.put("risk", "4");
        queryParameters.put("risk", "5");
        queryParameters.put("risk", "6");
        queryParameters.put("risk", "7");
        queryParameters.put("minTimeFrame", "At least 10 years");
        queryParameters.put("minTimeFrame", "At Least 3 years");
        queryParameters.put("minTimeFrame", "At least 5 years");
        queryParameters.put("minTimeFrame", "At least 7 years");
        queryParameters.put("minTimeFrame", "No minimum");

        return getCarFundDetails(queryParameters);
    }

    private CardFundFamily[] getCarFundDetails(Map<String, String> queryParameter) {
        CardFundFamily[] cardFundFamily = new CardFundFamily[0];
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
        return cardFundFamily;
    }

    private String constructCarFundApiUrl(Map<String, String> queryParameters) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(CardFundAPIConstants.BASE_URL);

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

    private CardFundFamily[] parseCarFunds(String responseJSONStr) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(responseJSONStr, CardFundFamily[].class);
        } catch (JsonSyntaxException e) {
            LOGGER.error("parseCarFunds(): JSON syntax exception occurred", e);
        }
        return new CardFundFamily[0];
    }
}
