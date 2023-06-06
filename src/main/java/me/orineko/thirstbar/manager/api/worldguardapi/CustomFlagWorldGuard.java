package me.orineko.thirstbar.manager.api.worldguardapi;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;

public class CustomFlagWorldGuard extends FlagValueChangeHandler<State> {
    protected CustomFlagWorldGuard(Session session, Flag<State> flag) {
        super(session, flag);
    }

    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, State state) {

    }

    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, com.sk89q.worldedit.util.Location location, com.sk89q.worldedit.util.Location location1, ApplicableRegionSet applicableRegionSet, State state, State t1, MoveType moveType) {
        return false;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, com.sk89q.worldedit.util.Location location, com.sk89q.worldedit.util.Location location1, ApplicableRegionSet applicableRegionSet, State state, MoveType moveType) {
        return false;
    }

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<CustomFlagWorldGuard> {
        @Override
        public CustomFlagWorldGuard create(Session session) {
            // create an instance of a handler for the particular session
            // if you need to pass certain variables based on, for example, the player
            // whose session this is, do it here
            return new CustomFlagWorldGuard(session);
        }
    }
    // construct with your desired flag to track changes
    public CustomFlagWorldGuard(Session session) {
        super(session, WorldGuardApi.getStateFlag());
    }
}
