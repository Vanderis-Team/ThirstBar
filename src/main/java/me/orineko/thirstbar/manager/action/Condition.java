package me.orineko.thirstbar.manager.action;

public class Condition {

    private final String condition;
    private final double multiply;
    public Condition(String condition, double multiply) {
        this.condition = condition;
        this.multiply = multiply;
    }

    public String getCondition() {
        return condition;
    }

    public double getMultiply() {
        return multiply;
    }

}
