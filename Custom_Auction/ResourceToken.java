package org.cloudbus.cloudsim.examples;


public class ResourceToken {
    private String resourceType; // e.g.CPU,RAM
    private double amount;

    public ResourceToken(String resourceType, double amount) {
        this.resourceType = resourceType;
        this.amount = amount;
    }

    public String getResourceType() {
        return resourceType;
    }

    public double getAmount() {
        return amount;
    }
}
