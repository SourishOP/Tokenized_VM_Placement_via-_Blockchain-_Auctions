package org.cloudbus.cloudsim.examples;

import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;


public class BasicBiddingHost extends Host {

    public BasicBiddingHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
    }

    
    public double generateBid() {
        double baseCost = 100.0; 
        // Calculate current utilization as a percentage 
        double utilizationPercent = (getTotalMips() - getVmScheduler().getAvailableMips()) / getTotalMips();
        return baseCost + (utilizationPercent * 100);
    }

}
