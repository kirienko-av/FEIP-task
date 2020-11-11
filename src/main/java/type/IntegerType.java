package type;

import java.util.regex.Pattern;

public class IntegerType extends BaseType<Integer> {
    private Pattern pattern = Pattern.compile("^\\d+$");

    @Override
    protected Boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

    @Override
    public Integer parseValue(String value) {
        return Integer.parseInt(value);
    }
}
