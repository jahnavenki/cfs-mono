import java.util.Map;
import java.util.Optional;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.time.Duration;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = CardFundService.class)
@Designate(ocd = ApiGatewayService.class)
public class CardFundServiceImpl implements CardFundService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardFundServiceImpl.class);

    private ApiGatewayService apiGatewayService;

    @Activate
    protected void activate(Map<String, Object> properties) {
        this.apiGatewayService = Configurable.createConfigurable(ApiGatewayService.class, properties);
    }

    public DesignFamily[] getDesignFamilies() {
        String apiUrl = constructApiUrl(apiGatewayService.baseApiUrl(), getApiParameters());
        return getCardFundDetails(apiUrl);
    }

    private String constructApiUrl(String baseUrl, Map<String, String[]> apiParameters) {
        StringBuilder apiUrlBuilder = new StringBuilder(baseUrl);

        // Check if base URL already contains parameters
        boolean hasParams = baseUrl.contains("?");

        for (Map.Entry<String, String[]> entry : apiParameters.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();

            for (String value : values) {
                if (!hasParams) {
                    apiUrlBuilder.append("?");
                    hasParams = true;
                } else {
                    apiUrlBuilder.append("&");
                }

                apiUrlBuilder.append(key).append("=").append(value);
            }
        }

        return apiUrlBuilder.toString();
    }

    private DesignFamily[] getCardFundDetails(String apiUrl) {
        DesignFamily[] cardFundFamilies = new DesignFamily[0];
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(apiGatewayService.timeout()))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            String responseJSONStr = response.body();
            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                cardFundFamilies = gson.fromJson(responseJSONStr, DesignFamily[].class);
            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            LOGGER.error("getCardFundDetails(): Error occurred while fetching data from API", e);
        }
        return cardFundFamilies;
    }

    private Map<String, String[]> getApiParameters() {
        Map<String, String[]> apiParameters = new HashMap<>();
        // Populate API parameters from the OSGi configuration
        apiParameters.put("companyCode", apiGatewayService.companyCode());
        apiParameters.put("mainGroup", apiGatewayService.mainGroup());
        apiParameters.put("productId", apiGatewayService.productId());
        apiParameters.put("category", apiGatewayService.category());
        apiParameters.put("asset", apiGatewayService.asset());
        apiParameters.put("risk", apiGatewayService.risk());
        apiParameters.put("mintimeframe", apiGatewayService.mintimeframe());
        return apiParameters;
    }

    private String getLanguage(Optional<String> language) {
        if (language.isPresent()) {
            return language.get();
        } else {
            return Constants.DEFAULT_LANGUAGE_EN;
        }
    }
}
