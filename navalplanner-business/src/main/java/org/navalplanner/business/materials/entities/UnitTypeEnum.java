package org.navalplanner.business.materials.entities;

public enum UnitTypeEnum {
    KILOGRAMS("kg"),
    KILOMETERS("km"),
    LITER("l"),
    METER("m"),
    SQUARE_METER("m2"),
    CUBIC_METER("m3"),
    TONS("tn");

    private String measure;

    private UnitTypeEnum(String measure) {
        this.measure = measure;
    }

    public String toString() {
        return measure;
    }
}
