package com.ifx.core.services.design.impl;

import java.util.Optional;
import com.ifx.core.services.design.CardFundService;
import com.ifx.core.pojos.design.CardFundFamily;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CardFundServiceImpl implements CardFundService {

    // Define API parameters
    private static final String BASE_API_URL = "https://secure.colonialfirststate.com.au/fp/pricenperformance/products/funds/performance";
    private static final Map<String, String[]> API_PARAMETERS;

    static {
        // Initialize API_PARAMETERS
        API_PARAMETERS = new LinkedHashMap<>(); // Use LinkedHashMap to preserve insertion order
        API_PARAMETERS.put("companyCode", new String[]{"001"});
        API_PARAMETERS.put("mainGroup", new String[]{"SF"});
        API_PARAMETERS.put("productId", new String[]{"11"});
        API_PARAMETERS.put("category", new String[]{"Conservative", "Defensive", "Geared", "Growth", "High Growth", "Moderate", "Single sector option"});
        API_PARAMETERS.put("asset", new String[]{"Alternatives", "Australian Property Securities", "Australian Share", "Cash and other income", "Fixed Interest", "Global Property Securities", "Global Share", "Infrastructure securities", "Multi-Sector"});
        API_PARAMETERS.put("risk", new String[]{"1", "3", "4", "5", "6", "7"});
        API_PARAMETERS.put("mintimeframe", new String[]{"At least 10 years", "At Least 3 years", "At least 5 years", "At least 7 years", "No minimum"});
    }
	
   	public DesignFamily[] getDesignFamilies(String baseUrl, Map<String, String[]> apiParameters) {
 
		String apiUrl = constructApiUrl(baseUrl, apiParameters)
		return getDesignDetails(apiUrl);
	}

  private CardFundFamily[] getCardFundDetails(Map<String, String> queryParameter) {
    CardFundFamily[] cardFundFamilies = new CardFundFamily[0];
    String url = apiGatewayService.getCardFundApi(queryParameter);
    try {
        HttpRequest request = resourceOwnerTokenService.getHttpRequest().uri(new URI(url)).GET().build();
        HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(apiGatewayService.getTimeout())).build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        String responseJSONStr = response.body();
        if (response.statusCode() == 200) {
            Gson gson = new Gson();
            cardFundFamilies = gson.fromJson(responseJSONStr, CardFundFamily[].class);
        }
    } catch (URISyntaxException | InterruptedException | IOException e) {
        LOGGER.error("getCardFundDetails(): Error occurred while fetching data from API", e);
    }
    return cardFundFamilies;
}
private CardFundFamily[] getCardFundFamilies(String responseJSONStr) {
    CardFundFamily[] cardFundFamilies = new CardFundFamily[0];
    Gson gson = new Gson();
    try {
        cardFundFamilies = gson.fromJson(responseJSONStr, CardFundFamily[].class);
    } catch (JsonSyntaxException e) {
        LOGGER.error("getCardFundFamilies(): JSON syntax exception occurred", e);
    }
    return cardFundFamilies;
}
private String getLanguage(Optional<String> language) {
		if (language.isPresent()) {
			return language.get();
		} else {
			return Constants.DEFAULT_LANGUAGE_EN;
		}
	}

}
