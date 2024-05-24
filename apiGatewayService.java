import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Card Fund apiGatewayService")
public @interface apiGatewayService {

    @AttributeDefinition(name = "Base API URL")
    String baseApiUrl() default "https://secure.colonialfirststate.com.au/fp/pricenperformance/products/funds/performance";

    @AttributeDefinition(name = "Company Code")
    String[] companyCode() default {"001"};

    @AttributeDefinition(name = "Main Group")
    String[] mainGroup() default {"SF"};

    @AttributeDefinition(name = "Product ID")
    String[] productId() default {"11"};

    @AttributeDefinition(name = "Category")
    String[] category() default {"Conservative", "Defensive", "Geared", "Growth", "High Growth", "Moderate", "Single sector option"};

    @AttributeDefinition(name = "Asset")
    String[] asset() default {"", "Alternatives", "Australian Property Securities", "Australian Share", "Cash and other income", "Fixed Interest", "Global Property Securities", "Global Share", "Infrastructure securities", "Multi-Sector"};

    @AttributeDefinition(name = "Risk")
    String[] risk() default {"1", "3", "4", "5", "6", "7"};

    @AttributeDefinition(name = "Minimum Timeframe")
    String[] mintimeframe() default {"At least 10 years", "At Least 3 years", "At least 5 years", "At least 7 years", "No minimum"};
}
