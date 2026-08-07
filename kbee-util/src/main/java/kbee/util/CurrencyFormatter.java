package kbee.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyFormatter {

    public static String formatAmount(BigDecimal amount, Currency currency,Locale userLocale){
        Locale locale= (userLocale !=null) ? userLocale: Locale.getDefault();
        final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(locale);
        currencyInstance.setCurrency(currency);

        return currencyInstance.format(amount.floatValue());
    }


}
