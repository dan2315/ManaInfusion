package com.example.mana_infusion.ModBlocks.Crystal;

import net.minecraft.util.StringRepresentable;

public enum CrystalModelState implements StringRepresentable {

    MODEL1("unobtained"),
    MODEL2("obtained");

    private final String name;

    CrystalModelState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
