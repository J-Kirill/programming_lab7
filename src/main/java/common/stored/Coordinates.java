package common.stored;

import common.InvalidData;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Coordinates implements Serializable {
    private Double x;
    private float y;
    private String owner;
    public Coordinates() {}
    public static Coordinates getFromSQL(ResultSet rs) throws SQLException, InvalidData {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(rs.getDouble("coordinate_x"));
        coordinates.setY(rs.getFloat("coordinate_y"));
        coordinates.setOwner(rs.getString("coordinate_owner"));
        return coordinates;
    }

    public Double getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public String getOwner() {
        return owner;
    }
    public void setX(Double x) throws InvalidData {
        if (x>128){
            throw new InvalidData("x is out of limit");
        }
        this.x = x;
    }
    public void setY(Float y) {
        this.y = y;
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
                '}';
    }
    public String toString(int tabs) {
        return "{\n" +
                "\t".repeat(tabs+1) + "\"x\": " + x +
                ",\n" + "\t".repeat(tabs+1) + "\"y\": " + y +
                "\n" + "\t".repeat(tabs) + "}";
    }
    public static Coordinates getFromDescription(Scanner scanner) throws InvalidData {
        Coordinates coordinates = new Coordinates();
        System.out.print("Введите x координат: ");
        coordinates.setX(Double.parseDouble(scanner.nextLine()));
        System.out.print("Введите y координат: ");
        coordinates.setY(Float.parseFloat(scanner.nextLine()));
        return coordinates;
    }
}
