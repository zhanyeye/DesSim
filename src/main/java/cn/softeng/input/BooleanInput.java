package cn.softeng.input;

/**
 * @date: 12/16/2020 3:38 PM
 */
public class BooleanInput extends Input<Boolean> {

    public BooleanInput(String keyword, Boolean defValue) {
        super(keyword, defValue);
    }

    @Override
    public void updateValue(Object newValue) {
        value = (Boolean) newValue;
    }
}
