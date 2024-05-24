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
 
import com.ifx.core.services.common.myifx.ResourceOwnerTokenService;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ifx.core.pojos.design.DesignFamily;
import com.ifx.core.services.api.ApiGatewayService;
import com.ifx.core.services.design.DesignService;
import com.ifx.core.utils.Constants;
import com.ifx.core.utils.PageUtil;
 
@Component(service = DesignService.class, immediate = true)
public class DesignServiceImpl implements DesignService {
 
      private static final Logger LOGGER = LoggerFactory.getLogger(DesignServiceImpl.class);
 
      @Reference
      private ResourceResolverFactory resolverFactory;
      @Reference
      private ResourceOwnerTokenService resourceOwnerTokenService;
 
 
      @Reference
      private ApiGatewayService apiGatewayService;
 
      /**
       * Get array of ProductFamily. It will fetch data through IPMS API In case of
       * any error, it will return null value
       *
       * @param language
       * @return
       */
      @Override
      public DesignFamily[] getDesignFamilies(Optional<String> language) {
 
            Map<String, String> queryParameters = new HashMap<>();
            LOGGER.debug("getDesignFamilies(language): start");
            queryParameters.put(Constants.LANGUAGE, getLanguage(language));
            return getDesignDetails(queryParameters);
      }
 
      // Overloading the method to get product subfamilies for product ID
      public DesignFamily[] getDesignFamilies(Optional<String> language, String productFamilyId) {
 
            Map<String, String> queryParameters = new HashMap<>();
            queryParameters.put(Constants.LANGUAGE, getLanguage(language));
            queryParameters.put(Constants.FAMILY_ID, String.valueOf(productFamilyId));
            return getDesignDetails(queryParameters);
      }
 
      private DesignFamily[] getDesignDetails(Map<String, String> queryParameter) {
            DesignFamily[] designFamilies = new DesignFamily[0];
            String url;
            String langCode = queryParameter.get(Constants.LANGUAGE);
            if(langCode.equalsIgnoreCase(Constants.JA) || langCode.equalsIgnoreCase(Constants.ZH)) {
             url = PageUtil.replaceLanguageCode(apiGatewayService.getDesignFamilyApi(queryParameter));
            }else {
                  url = apiGatewayService.getDesignFamilyApi(queryParameter);
            }
            try {
                  HttpRequest request = resourceOwnerTokenService.getHttpRequest().uri(new URI(url)).GET().build();
                  HttpResponse<String> response = HttpClient.newBuilder()
                              .connectTimeout(Duration.ofMillis(apiGatewayService.getTimeout())).build()
                              .send(request, HttpResponse.BodyHandlers.ofString());
                  String responseJSONStr = response.body();
                  if (response.statusCode() == 200) {
                        return getDesignFamilies(responseJSONStr);
                  }
            }  catch (URISyntaxException | InterruptedException | IOException e) {
                  LOGGER.error("getProductDetails(): IO exception occured", e);
            }
            return designFamilies;
      }
 
      private DesignFamily[] getDesignFamilies(String responseJSONStr) {
            DesignFamily[] designFamilies = new DesignFamily[0];
            Gson gson = new Gson();
            try {
                  designFamilies = gson.fromJson(responseJSONStr, DesignFamily[].class);
            } catch (JsonSyntaxException e) {
                  LOGGER.error("getDesignDetails(): JSON syntax exception occured", e);
            }
            return designFamilies;
      }
 
      /**
       * To get Language if present else will use default language provided in Product
       * Service configuration
       *
       * @param language
       * @return
       */
      private String getLanguage(Optional<String> language) {
            if (language.isPresent()) {
                  return language.get();
            } else {
                  return Constants.DEFAULT_LANGUAGE_EN;
            }
      }
 
}
