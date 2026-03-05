package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class TokenAuctionSimulation {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;
    private static List<Host> hostList;

    private static final int NUM_HOSTS = 2;
    private static final int NUM_VMS = 5;
    private static final int NUM_CLOUDLETS = 10;

    // ADDED: Simple Power Model Constants
    private static final double HOST_POWER_IDLE = 100;
    private static final double HOST_POWER_FULL = 200;

    public static void main(String[] args) {
        Log.printLine("Starting TokenAuctionSimulation with Custom Energy Metric...");

        try {
            CloudSim.init(1, Calendar.getInstance(), false);
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            
            TokenAuctionBroker broker = new TokenAuctionBroker("TokenAuctionBroker");
            int brokerId = broker.getId();

            vmlist = createVmList(brokerId);
            broker.submitGuestList(vmlist);
            cloudletList = createCloudletList(brokerId);
            broker.submitCloudletList(cloudletList);

            broker.placeVmsUsingTokenAuction(hostList);

            double lastClock = CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);
           
            calculateAndPrintEnergy(hostList, lastClock);

            CloudSim.stopSimulation();
            Log.printLine("TokenAuctionSimulation finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) {
        hostList = new ArrayList<>();
        for (int i = 0; i < NUM_HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            peList.add(new Pe(0, new PeProvisionerSimple(2000)));
            peList.add(new Pe(1, new PeProvisionerSimple(2000)));
            
            hostList.add(
                new TokenBiddingHost(
                    i,
                    new RamProvisionerSimple(8192),
                    new BwProvisionerSimple(10000),
                    1000000,
                    peList,
                    new VmSchedulerTimeShared(peList),
                    1000.0
                )
            );
        }

        Datacenter datacenter = null;
        try {
            DatacenterCharacteristics characteristics = new DatacenterCharacteristics("x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0);
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }
    
    
    private static void calculateAndPrintEnergy(List<Host> hostList, double simulationTime) {
        double totalEnergyKWh = 0;
        for (Host host : hostList) {
            double utilizationPercent = (host.getTotalMips() - host.getVmScheduler().getAvailableMips()) / host.getTotalMips();
            double powerDrawnWatts = HOST_POWER_IDLE + (HOST_POWER_FULL - HOST_POWER_IDLE) * utilizationPercent;
            double energyConsumedWs = powerDrawnWatts * simulationTime;
            double energyConsumedKWh = energyConsumedWs / 3600000;
            totalEnergyKWh += energyConsumedKWh;

            Log.printLine(String.format("Host #%d final utilization is %.2f%%, consuming %.4f kWh", host.getId(), utilizationPercent * 100, energyConsumedKWh));
        }
        Log.printLine(String.format("Total Energy Consumption for all hosts: %.4f kWh", totalEnergyKWh));
    }

    private static List<Vm> createVmList(int brokerId) {
        List<Vm> list = new ArrayList<>();
        for (int i = 0; i < NUM_VMS; i++) {
            Vm vm = new Vm(i, brokerId, 1000, 1, 512, 1000, 10000, "Xen", new CloudletSchedulerTimeShared());
            list.add(vm);
        }
        return list;
    }

    private static List<Cloudlet> createCloudletList(int brokerId) {
        List<Cloudlet> list = new ArrayList<>();
        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            Cloudlet cloudlet = new Cloudlet(i, 400000, 1, 300, 300, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(brokerId);
            list.add(cloudlet);
        }
        return list;
    }
    
    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

}
