package kbee.util;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CurrencyHelper {
    private static Map<Integer, Currency> currencies = new HashMap<>();

    static {
        Set<Currency> set = Currency.getAvailableCurrencies();
        for (Currency currency : set) {
            currencies.put(currency.getNumericCode(), currency);
        }
    }

    public static Currency getInstance(Integer code) {
        return currencies.get(code);
    }
}
