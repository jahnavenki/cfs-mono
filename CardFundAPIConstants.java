public class CardFundAPIConstants {
    // Base URL
    public static final String BASE_URL = "https://secure.colonialfirststate.com.au/fp/pricenperformance/products/funds/performance?";
    
    // Parameters
    public static final String COMPANY_CODE = "companyCode=001";
    public static final String MAIN_GROUP = "mainGroup=SF";
    public static final String PRODUCT_ID = "productId=11";
    public static final String CATEGORY = "category=";
    public static final String ASSET = "asset=";
    public static final String RISK = "risk=";
    public static final String MIN_TIME_FRAME = "mintimeframe=";

    // Constants for specific categories and assets
    public static final String[] CATEGORIES = {
        "Conservative", "Defensive", "Geared", "Growth", "High Growth", "Moderate", "Single sector option"
    };

    public static final String[] ASSETS = {
        "", "Alternatives", "Australian Property Securities", "Australian Share", 
        "Cash and other income", "Fixed Interest", "Global Property Securities", 
        "Global Share", "Infrastructure securities", "Multi-Sector"
    };

    // Constants for risk levels
    public static final String[] RISK_LEVELS = {"1", "3", "4", "5", "6", "7"};

    // Constants for minimum time frame
    public static final String[] MIN_TIME_FRAMES = {
        "At least 10 years", "At Least 3 years", "At least 5 years", "At least 7 years", "No minimum"
    };
}
