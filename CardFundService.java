package com.ifx.core.services.design;

import java.util.Optional;
import com.ifx.core.pojos.design.CardFundFamily;

public interface CardFundService {

    public CardFundFamily[] getCardFundFamilies(double apirThreshold, Optional<String> language);
}
