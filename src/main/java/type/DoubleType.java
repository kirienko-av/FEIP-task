package type;

import java.util.regex.Pattern;

public class DoubleType extends BaseType<Double> {
    private Pattern pattern = Pattern.compile("^\\d+((,|\\.)\\d+)?$");

    @Override
    protected Boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

    @Override
    public Double parseValue(String value) {
        return Double.parseDouble(value.replaceFirst(",", "."));
    }
}
