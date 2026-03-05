package org.cloudbus.cloudsim.examples;

import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class TokenBiddingHost extends Host {

    private double tokenBalance;

    public TokenBiddingHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, double initialTokenBalance) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        this.tokenBalance = initialTokenBalance;
    }

    public double getTokenBalance() {
        return tokenBalance;
    }

    public void deductTokens(double amount) {
        this.tokenBalance -= amount;
    }

    
    public double generateBid() {
        double baseCost = 10.0; // Base cost 
        double utilizationPercent = (getTotalMips() - getVmScheduler().getAvailableMips()) / getTotalMips();
        double bidPrice = baseCost + (utilizationPercent * 20); // More load = higher price

        // The host won't bid if it's running low on tokens 
        if (this.tokenBalance < bidPrice) {
            return Double.MAX_VALUE; 
        }
        return bidPrice;
    }
}