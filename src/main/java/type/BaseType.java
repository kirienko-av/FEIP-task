package type;

import javax.xml.bind.ValidationException;

import static java.util.Optional.ofNullable;

public abstract class BaseType<T> {

    public T transform(String value) throws ValidationException {
        if (!ofNullable(value).map(this::validate).orElse(true)) {
            throw new ValidationException(String.format("Invalid value \"%s\" for type \"%s\"", value, this.getClass().getSimpleName()));
        }
        return ofNullable(value).map(this::parseValue).orElse(null);
    }

    protected abstract Boolean validate(String value);

    protected abstract T parseValue(String value);
}
