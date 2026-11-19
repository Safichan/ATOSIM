package org.palladiosimulator.blockchainsystems.core.crashes.events;

import java.util.Set;

public class CrashPlan {
	
	private final Set<String> crashingNodeIds;
    private final long crashTime;
    private final long crashDuration;

    public CrashPlan(Set<String> crashingNodeIds, long crashTime, long crashDuration) {
        this.crashingNodeIds = Set.copyOf(crashingNodeIds);
        this.crashTime = crashTime;
        this.crashDuration = crashDuration;
    }

    public Set<String> getCrashingNodeIds() {
        return crashingNodeIds;
    }

    public long getCrashTime() {
        return crashTime;
    }

    public long getCrashDuration() {
        return crashDuration;
    }

}
