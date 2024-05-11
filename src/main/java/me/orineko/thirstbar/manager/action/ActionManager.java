package me.orineko.thirstbar.manager.action;

import me.orineko.thirstbar.ThirstBar;
import me.orineko.thirstbar.manager.action.data.*;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ActionManager {

    private final List<ActionRegister> actionRegisterList;
    private boolean enable;

    public ActionManager(){
        actionRegisterList = new ArrayList<>();
        registerDataFile();
    }

    public void removeRegister(){
        actionRegisterList.forEach(ActionRegister::removeScheduleRepeat);
    }

    public void registerDataFile(){
        FileConfiguration file = ThirstBar.getInstance().getActionsFile();
        enable = file.getBoolean("enable", false);
        if(!enable) return;
        register(new ActionStanding().loadFileData(file));
        register(new ActionSprinting().loadFileData(file));
        register(new ActionUnderWater().loadFileData(file));
        register(new ActionSneaking().loadFileData(file));
        register(new ActionFighting().loadFileData(file));
    }

    public void register(ActionRegister actionRegister){
        actionRegisterList.add(actionRegister);
    }

    @Nullable
    public ActionRegister getActionRegister(@Nonnull String name){
        return actionRegisterList.stream().filter(v -> v.getName().equals(name.toUpperCase())).findAny().orElse(null);
    }

    @Nullable
    public ActionRegister getActionRegister(@Nonnull ActionType actionType){
        return getActionRegister(actionType.name());
    }

    public List<ActionRegister> getActionRegisterList() {
        return actionRegisterList;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
