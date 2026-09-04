package com.youyimc.createmassenergy.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 可调容量的能量存储实现（基于 NeoForge IEnergyStorage）
 */
public class SimpleEnergyStorage implements IEnergyStorage {

    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public SimpleEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    /** capacity, maxReceive, maxExtract */
    public SimpleEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public SimpleEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            onEnergyChanged();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            onEnergyChanged();
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    /** 能量变化回调（用于标记方块实体脏/同步） */
    protected void onEnergyChanged() {}

    public void setMaxExtract(int maxExtract) {
        this.maxExtract = maxExtract;
    }

    public void setMaxReceive(int maxReceive) {
        this.maxReceive = maxReceive;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
        onEnergyChanged();
    }

    /** 添加能量，返回实际添加量 */
    public int addEnergy(int amount) {
        int toAdd = Math.min(amount, capacity - energy);
        energy += toAdd;
        if (toAdd > 0) {
            onEnergyChanged();
        }
        return toAdd;
    }

    /** 消耗能量，返回实际消耗量 */
    public int consumeEnergy(int amount) {
        int toConsume = Math.min(energy, amount);
        energy -= toConsume;
        if (toConsume > 0) {
            onEnergyChanged();
        }
        return toConsume;
    }
}
