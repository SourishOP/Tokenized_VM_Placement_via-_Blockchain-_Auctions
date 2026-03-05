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

public class CloudSimExample1 {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;
    private static List<Host> hostList; 

    // --- Simulation Parameters ---
    private static final int NUM_HOSTS = 2;
    private static final int NUM_VMS = 5;
    private static final int NUM_CLOUDLETS = 10;

    //  Simple Power Model Constants (in Watts)
    private static final double HOST_POWER_IDLE = 100; // Power consumed when idle
    private static final double HOST_POWER_FULL = 200; // Power consumed at 100% CPU utilization

    public static void main(String[] args) {
        Log.printLine("Starting Baseline Simulation with Custom Energy Metric...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            createDatacenter("Datacenter_0");

            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            vmlist = createVmList(brokerId);
            broker.submitGuestList(vmlist);
            cloudletList = createCloudletList(brokerId);
            broker.submitCloudletList(cloudletList);

            double lastClock = CloudSim.startSimulation();
            
            
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Simulation is over!");
            
            printCloudletList(newList);
            calculateAndPrintEnergy(hostList, lastClock); 

            CloudSim.stopSimulation();

            Log.printLine("Baseline Simulation finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {            
            broker = new NonDestroyingBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }

    private static Datacenter createDatacenter(String name) {
        hostList = new ArrayList<>();

        // Host #0 will be the POWERFUL host
        List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(4000))); // 4000 MIPS
        peList1.add(new Pe(1, new PeProvisionerSimple(4000)));
        peList1.add(new Pe(2, new PeProvisionerSimple(4000)));
        peList1.add(new Pe(3, new PeProvisionerSimple(4000)));
        hostList.add(
            new Host(0, new RamProvisionerSimple(16384), new BwProvisionerSimple(20000), 2000000, peList1, new VmSchedulerTimeShared(peList1))
        );

        // Host #1 will be the WEAKER host
        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(1000))); // 1000 MIPS
        hostList.add(
            new Host(1, new RamProvisionerSimple(4096), new BwProvisionerSimple(5000), 500000, peList2, new VmSchedulerTimeShared(peList2))
        );

        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }

    private static void calculateAndPrintEnergy(List<Host> hostList, double simulationTime) {
        double totalEnergyKWh = 0;
        for (Host host : hostList) {
            // We use the host's final utilization as an approximation for its average utilization
        	double utilizationPercent = (host.getTotalMips() - host.getVmScheduler().getAvailableMips()) / host.getTotalMips(); 

            // Simple linear power model
            double powerDrawnWatts = HOST_POWER_IDLE + (HOST_POWER_FULL - HOST_POWER_IDLE) * utilizationPercent;
            double energyConsumedWs = powerDrawnWatts * simulationTime; // Energy in Watt-seconds

            // Convert Watt-seconds to kWh (1 kWh = 3,600,000 Ws)
            double energyConsumedKWh = energyConsumedWs / 3600000;
            totalEnergyKWh += energyConsumedKWh;

            Log.printLine(String.format("Host #%d final utilization is %.2f%%, consuming %.4f kWh", host.getId(), utilizationPercent * 100, energyConsumedKWh));
        }
        Log.printLine(String.format("Total Energy Consumption for all hosts: %.4f kWh", totalEnergyKWh));
    }

   
    private static List<Vm> createVmList(int brokerId) {
         vmlist = new ArrayList<Vm>();
         for(int i=0;i<NUM_VMS;i++){
            Vm vm = new Vm(i, brokerId, 1000, 1, 512, 1000, 10000, "Xen", new CloudletSchedulerTimeShared());
            vmlist.add(vm);
         }
         return vmlist;
    }

    private static List<Cloudlet> createCloudletList(int brokerId) {
        cloudletList = new ArrayList<Cloudlet>();
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for(int i=0;i<NUM_CLOUDLETS;i++){
            Cloudlet cloudlet = new Cloudlet(i, 400000, 1, 300, 300, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }

    private static void printCloudletList(List<Cloudlet> list) {
       
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");
        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}
