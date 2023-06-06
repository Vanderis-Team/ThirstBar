package me.orineko.thirstbar.manager.player;

public interface PlayerThirstScheduler {

    long getCooldownRefresh();
    void setCooldownRefresh(long value);
    void disableExecuteReduce();
    void executeReduce();
    void disableExecuteRefresh();
    void executeRefresh();
    long delayRefresh();

}
