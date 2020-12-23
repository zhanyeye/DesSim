package cn.softeng.input;

/**
 * @date: 12/16/2020 3:30 PM
 */
public class StringInput extends Input<String> {
    public StringInput(String keyword, String defValue) {
        super(keyword, defValue);
    }

    @Override
    public void updateValue(Object newValue) {
        value = String.valueOf(newValue);
    }

}
