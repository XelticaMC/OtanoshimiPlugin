package work.xeltica.craft.core.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class SignData implements Cloneable, ConfigurationSerializable {
    public SignData(Location location, String command) {
        this(location, command, null);
    }

    public SignData(Location location, String command, String arg1) {
        this(location, command, arg1, null);
    }

    public SignData(Location location, String command, String arg1, String arg2) {
        this.location = location;
        this.command = command;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public Location getLocation() { return location; }
    public String getCommand() { return command; }
    public String getArg1() { return arg1; }
    public String getArg2() { return arg2; }

    public Map<String, Object> serialize() {
        var result = new LinkedHashMap<String, Object>();
        result.put("location", location.serialize());
        result.put("command", command);
        result.put("arg1", arg1);
        result.put("arg2", arg2);
        return result;
    }

    public static SignData deserialize(Map<String, Object> args) {
        Location location;
        String command;
        String arg1;
        String arg2;
        if (!args.containsKey("location"))  throw new IllegalArgumentException("location is null");
        if(!args.containsKey("command")) throw new IllegalArgumentException("command is null");
        if (!args.containsKey("arg1")) throw new IllegalArgumentException("arg1 is null");
        if (!args.containsKey("arg2")) throw new IllegalArgumentException("arg2 is null");

        location = Location.deserialize((Map<String, Object>) args.get("location"));
        command = ((String) args.get("command"));
        arg1 = ((String) args.get("arg1"));
        arg2 = ((String) args.get("arg2"));

        return new SignData(location, command, arg1, arg2);
    }

    private Location location;
    private String command;
    private String arg1;
    private String arg2;
}