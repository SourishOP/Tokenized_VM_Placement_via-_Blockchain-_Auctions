package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import java.util.List;

public class TokenAuctionBroker extends DatacenterBroker {

    public TokenAuctionBroker(String name) throws Exception {
        super(name);
    }

    // This is the main auction logic with the economic tie-breaker
    public void placeVmsUsingTokenAuction(List<Host> hostList) {
        for (Object vmObject : this.vmList) {
            Vm vm = (Vm) vmObject;
            Log.printLine("--------------------------------------------------");
            Log.printLine("Starting Token Auction for VM #" + vm.getId());

            Host cpuWinner = runAuctionForResource("CPU", vm, hostList);
            Host ramWinner = runAuctionForResource("RAM", vm, hostList);

            if (cpuWinner != null && cpuWinner.getId() == ramWinner.getId()) {
                TokenBiddingHost winningHost = (TokenBiddingHost) cpuWinner;
                Log.printLine(String.format(">>> Host #%d won both CPU and RAM auctions for VM #%d", winningHost.getId(), vm.getId()));
                vm.setHost(winningHost);
                double totalCost = 25.0;
                winningHost.deductTokens(totalCost);
                Log.printLine(String.format(">>> Deducted %.2f tokens from Host #%d. New balance: %.2f", totalCost, winningHost.getId(), winningHost.getTokenBalance()));
            } else {
                Log.printLine(">>> AUCTION FAILED for VM #" + vm.getId() + ": No single host won both resource auctions.");
                vm.setHost(null);
            }
        }
    }

    // This is the helper method for running individual resource auctions
    private Host runAuctionForResource(String resourceType, Vm vm, List<Host> hostList) {
        Log.printLine(String.format("--- Starting auction for %s tokens...", resourceType));
        Host winningHost = null;
        double lowestBid = Double.MAX_VALUE;

        for (Host host : hostList) {
            TokenBiddingHost biddingHost = (TokenBiddingHost) host;
            if (biddingHost.isSuitableForVm(vm)) {
                double currentBid = biddingHost.generateBid();
                if (currentBid != Double.MAX_VALUE) {
                    Log.printLine(String.format("VM #%d, %s: Host #%d bids %.2f", vm.getId(), resourceType, host.getId(), currentBid));
                }

                if (currentBid < lowestBid) {
                    lowestBid = currentBid;
                    winningHost = host;
                } else if (currentBid == lowestBid) {
                    TokenBiddingHost currentWinner = (TokenBiddingHost) winningHost;
                    TokenBiddingHost newChallenger = (TokenBiddingHost) biddingHost;
                    if (newChallenger.getTokenBalance() > currentWinner.getTokenBalance()) {
                        Log.printLine(String.format("--- Tie-break: Host #%d wins with higher token balance.", newChallenger.getId()));
                        winningHost = newChallenger;
                    }
                }
            }
        }
        if (winningHost != null) {
            Log.printLine(String.format("--- %s auction for VM #%d won by Host #%d with bid %.2f", resourceType, vm.getId(), winningHost.getId(), lowestBid));
        }
        return winningHost;
    }

    @Override
    protected void clearDatacenters() {
        // Intentionally left empty
    }

}
