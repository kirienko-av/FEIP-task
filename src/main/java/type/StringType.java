package type;

public class StringType extends BaseType<String> {

    @Override
    protected Boolean validate(String value) {
        return true;
    }

    @Override
    public String parseValue(String value) {
        return value;
    }
}
