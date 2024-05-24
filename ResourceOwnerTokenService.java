import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.io.IOException;

public class ResourceOwnerTokenService {

    private HttpClient httpClient;

    public ResourceOwnerTokenService() {
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    }

    public HttpRequest.Builder getHttpRequest() {
        return httpClient.newRequestBuilder();
    }

}