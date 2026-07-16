package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class OilPotVisualState {
    public static final EnumProperty<OilType> OIL_TYPE = EnumProperty.create("grilling_oil", OilType.class);

    public static OilType fromId(String id) {
        return switch (id) {
            case "canola" -> OilType.CANOLA;
            case "secret_chili" -> OilType.SECRET_CHILI;
            case "premium_chili" -> OilType.PREMIUM_CHILI;
            default -> OilType.DEFAULT;
        };
    }

    public enum OilType implements StringRepresentable {
        DEFAULT("default"), CANOLA("canola"), SECRET_CHILI("secret_chili"), PREMIUM_CHILI("premium_chili");

        private final String name;

        OilType(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    private OilPotVisualState() {}
}
