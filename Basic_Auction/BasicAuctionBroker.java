package org.cloudbus.cloudsim.examples;

import java.util.List;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

public class BasicAuctionBroker extends DatacenterBroker {

    public BasicAuctionBroker(String name) throws Exception {
        super(name);
    }

    public void placeVmsUsingAuction(List<Host> hostList) {
         for (Object vmObject : this.vmList) {
            if (vmObject instanceof Vm) {
                Vm vm = (Vm) vmObject;
                Host winningHost = null;
                double lowestBid = Double.MAX_VALUE;

                for (Host host : hostList) {
                    BasicBiddingHost biddingHost = (BasicBiddingHost) host;
                    if (biddingHost.isSuitableForVm(vm)) {
                        double currentBid = biddingHost.generateBid();
                        Log.printLine(String.format("VM #%d: Host #%d bids %.2f", vm.getId(), host.getId(), currentBid));
                        if (currentBid < lowestBid) {
                            lowestBid = currentBid;
                            winningHost = host;
                        }
                    }
                }
                if (winningHost != null) {
                    Log.printLine(String.format("VM #%d auction won by Host #%d with bid %.2f", vm.getId(), winningHost.getId(), lowestBid));
                    vm.setHost(winningHost);
                } else {
                    Log.printLine(String.format("VM #%d: No suitable host found in auction.", vm.getId()));
                }
            }
        }
    }

    /*
     This overridden method prevents the broker from destroying VMs, so we can read the final host utilization for our energy calculation.
     */
    @Override
    protected void clearDatacenters() {
        // Intentionally left empty
    }

}
