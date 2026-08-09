package com.novamens.wicket.converter;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.novamens.scheduler.CronExpressionJ8;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class CronConverter implements IConverter {

    public Object convertToObject(String val, Locale locale)
    {
        CronDefinition cronDefinitionUnix = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronDefinition cronDefinitionQuartz = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        final Cron parse = new CronParser(cronDefinitionUnix).parse(val);
        Cron quartsCron = CronMapper.fromUnixToQuartz().map(parse);
        String expr = quartsCron.asString();
        expr = expr.substring(0, expr.lastIndexOf(" "));
        return new CronExpressionJ8(expr);

    }


    public String convertToString(Object value, Locale locale)
    {
        if (value == null)
        {
            return null;
        }
        CronExpressionJ8 cron = (CronExpressionJ8)value;

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final Cron parse = new CronParser(cronDefinition).parse(cron.getExpression());
        return CronMapper.fromQuartzToUnix().map(parse).asString();

        //return cron.getExpression();
    }
}
