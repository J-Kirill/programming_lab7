package common.stored;

import common.InvalidData;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Location implements Serializable {
    private double x;
    private Float y;
    private Double z;
    private String name;
    private String owner;
    public Location(){}
    public static Location getFromSQL(ResultSet rs, String name) throws SQLException, InvalidData {
        Location location = new Location();
        location.setX(rs.getDouble(name + "_x"));
        location.setY(rs.getFloat(name + "_y"));
        location.setZ(rs.getDouble(name + "_z"));
        location.setName(rs.getString(name + "_name"));
        location.setOwner(rs.getString(name + "_owner"));
        return location;
    }

    public double getX() {
        return x;
    }
    public Float getY() {
        return y;
    }
    public Double getZ() {
        return z;
    }
    public String getName() {
        return name;
    }
    public String getOwner() {
        return owner;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(Float y) throws InvalidData {
        if (y==null){
            throw new InvalidData("y is null");
        }
        this.y = y;
    }
    public void setZ(Double z) throws InvalidData {
        if (z==null){
            throw new InvalidData("z is null");
        }
        this.z = z;
    }
    public void setName(String name) throws InvalidData {
        if (name==null || name.isEmpty()){
            throw new InvalidData("name is empty");
        }
        this.name = name;
    }
    public void setOwner(String owner) throws InvalidData {
        if (owner==null || owner.isEmpty()){
            throw new InvalidData("owner is empty");
        }
        this.owner = owner;
    }
    @Override
    public String toString() {
        return "{" +
                "\"x\": " + x +
                ", \"y\": " + y +
                ", \"z\": " + z +
                ", \"name\": " + "\""+ name + "\"}";
    }
    public String toString(int tabs) {
        return "{\n" +
                "\t".repeat(tabs+1) + "\"x\": " + x +
                ",\n" + "\t".repeat(tabs+1) + "\"y\": " + y +
                ",\n" + "\t".repeat(tabs+1) + "\"z\": " + z +
                ",\n" + "\t".repeat(tabs+1) + "\"name\": " + "\""+ name + "\"\n" +
                "\t".repeat(tabs) + "}";
    }
    public static Location getFromDescription(Scanner scanner) throws InvalidData {
        Location location = new Location();
        System.out.print("Введите x точки: ");
        location.setX(Double.parseDouble(scanner.nextLine()));
        System.out.print("Введите y точки: ");
        location.setY(Float.parseFloat(scanner.nextLine()));
        System.out.print("Введите z точки: ");
        location.setZ(Double.parseDouble(scanner.nextLine()));
        System.out.print("Введите имя точки: ");
        location.setName(scanner.nextLine());
        return location;
    }
}
