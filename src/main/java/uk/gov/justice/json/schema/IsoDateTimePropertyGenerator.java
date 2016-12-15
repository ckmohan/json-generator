package uk.gov.justice.json.schema;

import uk.gov.justice.json.PropertyGenerator;

import com.google.common.annotations.VisibleForTesting;

public class IsoDateTimePropertyGenerator implements PropertyGenerator {

    private final String name;
    private final RandomDateTimeGenerator randomDateTimeGenerator;
    private final QuotedJsonPropertyFormatter quotedJsonPropertyFormatter = new QuotedJsonPropertyFormatter();

    public IsoDateTimePropertyGenerator(final String name) {
        this(name, new RandomDateTimeGenerator());
    }

    @VisibleForTesting
    IsoDateTimePropertyGenerator(
            final String name,
            final RandomDateTimeGenerator randomDateTimeGenerator) {
        this.name = name;
        this.randomDateTimeGenerator = randomDateTimeGenerator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String next() {
        return quotedJsonPropertyFormatter.toJson(name, randomDateTimeGenerator.randomDateTime());
    }
}