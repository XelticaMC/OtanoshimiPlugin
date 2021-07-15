package work.xeltica.craft.core.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("VehicleModel")
public class VehicleModel implements Cloneable, ConfigurationSerializable {
    public Map<String, Object> serialize() {
        var result = new LinkedHashMap<String, Object>();
        return result;
    }

    public static VehicleModel deserialize(Map<String, Object> args) {
        return new VehicleModel();
    }
}