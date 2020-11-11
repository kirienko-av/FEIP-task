package type;

import java.util.regex.Pattern;

public class PhoneType extends BaseType<String> {
    private Pattern pattern = Pattern.compile("^(\\+7|7|8)?[\\s\\-]?\\(?[489][0-9]{2}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$");

    @Override
    protected Boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

    @Override
    public String parseValue(String value) {
        String normalize = value.replaceAll("[^\\d.]", "");
        if (normalize.length() == 11 && normalize.charAt(0) == '8') {
            normalize = "7" + normalize.substring(1);
        } else if (normalize.length() == 10) {
            normalize = "7" + normalize;
        }

        return normalize;
    }
}
