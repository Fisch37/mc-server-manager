package de.maria_writes_code.mcsm.backend.features.components.configuration;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@NullMarked
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @Type(value = ConfigurationDescriptor.Select.class, name="select"),
    @Type(value = ConfigurationDescriptor.Text.class, name="text"),
    @Type(value = ConfigurationDescriptor.Number.class, name="number")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract sealed class ConfigurationDescriptor<T>
    permits ConfigurationDescriptor.Select, ConfigurationDescriptor.ArbitraryValue
{
    private final String id, name;
    private final @Nullable String placeholder, description;
    private final @Nullable T defaultValue;
    private final boolean required;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getPlaceholder() {
        return placeholder;
    }

    public @Nullable String getDescription() {
        return description;
    }

    @JsonProperty("default_value")
    public @Nullable T getDefault() {
        return defaultValue;
    }

    @JsonProperty("required")
    public boolean isRequired() {
        return required;
    }

    public abstract T validate(String input) throws IllegalArgumentException;

    protected ConfigurationDescriptor(
        String id, String name,
        @Nullable String placeholder, @Nullable String description,
        @Nullable T defaultValue,
        boolean required
    ) {
        this.id = id;
        this.name = name;
        this.placeholder = placeholder;
        this.description = description;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private abstract static sealed class ArbitraryValue<T> extends ConfigurationDescriptor<T>
        permits ConfigurationDescriptor.Text, ConfigurationDescriptor.Number
    {
        protected final @Nullable Pattern valueRegex;

        public ArbitraryValue(
            String id, String name,
            @Nullable String placeholder, @Nullable String description,
            @Nullable T defaultValue,
            boolean required,
            @Nullable Pattern valueRegex
        ) {
            super(id, name, placeholder, description, defaultValue, required);
            this.valueRegex = valueRegex;
        }

        @JsonProperty("value_filter")
        public String getValueFilter() {
            return valueRegex != null ? valueRegex.toString() : null;
        }

        @Override
        public T validate(String input) throws IllegalArgumentException {
            if (valueRegex != null && valueRegex.asMatchPredicate().test(input)) {
                throw new IllegalArgumentException("Input for config option does not match given pattern");
            }
            return convert(input);
        }

        protected abstract T convert(String input) throws IllegalArgumentException;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Text extends ConfigurationDescriptor.ArbitraryValue<String> {
        public Text(
            String id, String name,
            @Nullable String placeholder, @Nullable String description,
            @Nullable String defaultValue,
            boolean required,
            @Nullable Pattern valueRegex
        ) {
            super(id, name, placeholder, description, defaultValue, required, valueRegex);
        }

        @Override
        protected String convert(String input) {
            return input;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Number extends ConfigurationDescriptor.ArbitraryValue<Double> {
        public Number(
            String id, String name,
            @Nullable String placeholder, @Nullable String description,
            @Nullable Double defaultValue,
            boolean required,
            @Nullable Pattern valueRegex
        ) {
            super(id, name, placeholder, description, defaultValue, required, valueRegex);
        }

        @Override
        protected Double convert(String input) throws IllegalArgumentException {
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Input for number configuration cannot be interpreted as a number", e);
            }
        }
    }

    public static final class Select extends ConfigurationDescriptor<String> {
        private final List<Option> options;

        public Select(
            String id, String name,
            @Nullable String placeholder, @Nullable String description,
            @Nullable String defaultValue,
            boolean required,
            List<Option> options
        ) {
            super(id, name, placeholder, description, defaultValue, required);
            this.options = options;
        }

        public List<Option> getOptions() {
            return Collections.unmodifiableList(options);
        }

        @Override
        public String validate(String input) throws IllegalArgumentException {
            if (
                options.stream()
                    .filter(s -> s.id.equals(input))
                    .findAny()
                    .isPresent()
            ) {
                throw new IllegalArgumentException("Input for select configuration is not one of the allowed options");
            }
            return input;
        }

        public record Option(String id, String name, String description) { }
    }
}
