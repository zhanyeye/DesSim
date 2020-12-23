package cn.softeng.input;

/**
 * @date: 12/17/2020 10:15 AM
 */
public class ValueInput extends Input<Long> {
    public ValueInput(String keyword, Long defValue) {
        super(keyword, defValue);
    }

    @Override
    public void updateValue(Object newValue) {
        value = Long.valueOf((Long) newValue);
    }

}
