package org.cloudbus.cloudsim.examples;
import org.cloudbus.cloudsim.DatacenterBroker;
public class NonDestroyingBroker extends DatacenterBroker {

    public NonDestroyingBroker(String name) throws Exception {
        super(name);
    }

    /**
     * This is the method that the default broker calls to shut down VMs.
     * We override it and leave it empty to prevent that from happening.
     */
    @Override
    protected void clearDatacenters() {
        // This method is intentionally left empty.
    }
}
