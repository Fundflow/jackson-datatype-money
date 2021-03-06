package org.zalando.jackson.datatype.money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import javax.annotation.Nullable;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import java.io.IOException;
import java.util.Locale;

final class MonetaryAmountSerializer extends JsonSerializer<MonetaryAmount> {

    private final FieldNames names;
    private final AmountWriter writer;
    private final MonetaryAmountFormatFactory factory;

    MonetaryAmountSerializer(final FieldNames names, final AmountWriter writer,
            final MonetaryAmountFormatFactory factory) {
        this.writer = writer;
        this.factory = factory;
        this.names = names;
    }

    @Override
    public void serializeWithType(final MonetaryAmount value, final JsonGenerator generator,
            final SerializerProvider provider, final TypeSerializer serializer) throws IOException {

        // effectively assuming no type information at all
        serialize(value, generator, provider);
    }

    @Override
    public void serialize(final MonetaryAmount value, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException {

        final CurrencyUnit currency = value.getCurrency();
        @Nullable final String formatted = format(value, provider);

        generator.writeStartObject();
        {
            generator.writeObjectField(names.getAmount(), writer.write(value));
            generator.writeStringField(names.getCurrency(), currency.getCurrencyCode());

            if (formatted != null) {
                generator.writeStringField(names.getFormatted(), formatted);
            }
        }
        generator.writeEndObject();
    }

    @Nullable
    private String format(final MonetaryAmount value, final SerializerProvider provider) {
        final Locale locale = provider.getConfig().getLocale();
        final MonetaryAmountFormat format = factory.create(locale);
        return format == null ? null : format.format(value);
    }

}
