package me.orineko.thirstbar.manager.player;

public interface PlayerThirstValue {

    void addThirst(double value);
    void setThirst(double value);
    double getThirst();
    void setThirstMax(double value);
    double getThirstMax();
    void setThirstReduce(double value);
    double getThirstReduce();
    void setThirstTime(long value);
    long getThirstTime();
    void setThirstDamage(double value);
    double getThirstDamage();

}
