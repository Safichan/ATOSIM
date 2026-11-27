package org.palladiosimulator.blockchainsystems.core.crashes.events;

import java.util.Set;

public class CrashPlan {
	
	private final Set<String> crashingNodeIds;
    private final long crashStart;
    private final long crashDuration;

    public CrashPlan(Set<String> crashingNodeIds, long crashTime, long crashDuration) {
        this.crashingNodeIds = Set.copyOf(crashingNodeIds);
        this.crashStart = crashTime;
        this.crashDuration = crashDuration;
    }

    public Set<String> getCrashingNodeIds() {
        return crashingNodeIds;
    }

    public long getCrashStart() {
        return crashStart;
    }

    public long getCrashDuration() {
        return crashDuration;
    }

}
