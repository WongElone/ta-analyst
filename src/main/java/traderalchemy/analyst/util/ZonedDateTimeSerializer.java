package traderalchemy.analyst.util;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Customized ZonedDateTime serializer to serialize ZonedDateTime to python datetime iso format
 * e.g. 2025-09-07T11:31:56.114514+00:00
 * ref: https://stackoverflow.com/questions/60451939/jackson-offsetdatetime-serialization-z-instead-of-0000-timezone
 */
public class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime>
{
    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSxxx")
        .withZone(ZoneId.systemDefault()); // control by jvm argument: -Duser.timezone

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException
    {
        if (value == null) {
            throw new IOException("ZonedDateTime argument is null.");
        }

        jsonGenerator.writeString(ISO_8601_FORMATTER.format(value));
    }
}
