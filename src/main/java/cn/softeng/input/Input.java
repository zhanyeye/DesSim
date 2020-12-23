package cn.softeng.input;

/**
 * 模型输入的抽象类，所有输入继承自Input，以方便管理
 * @date: 12/16/2020 3:06 PM
 */
public abstract class Input<T> {
    private String keyword;
    protected T value;
    protected T defValue;

    public Input(String keyword, T defValue) {
        this.keyword = keyword;
        this.defValue = defValue;
    }

    @Override
    public String toString() {
        return String.format("%s", value);
    }

    public final String getKeyword() {
        return keyword;
    }

    public T getValue() {
        return value;
    }

}
