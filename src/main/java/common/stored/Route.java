package common.stored;

import common.InvalidData;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class Route implements Comparable<Route>, Serializable {
    private int id;
    private String name;
    private Coordinates coordinates;
    private ZonedDateTime creationDate;
    private Location from;
    private Location to;
    private float distance;
    private String owner;
    public Route() {}
    public static Route getFromSQL(ResultSet rs) throws SQLException, InvalidData {
        Route route = new Route();
        route.setId(rs.getInt("id"));
        route.setName(rs.getString("name"));
        route.setCoordinates(Coordinates.getFromSQL(rs));
        route.setCreationDate(rs.getObject("creation_date", OffsetDateTime.class).toZonedDateTime());
        route.setFrom(Location.getFromSQL(rs, "from"));
        rs.getInt("to_id");
        if (!rs.wasNull()) {
            route.setTo(Location.getFromSQL(rs, "to"));
        }
        route.setDistance(rs.getFloat("distance"));
        route.setOwner(rs.getString("owner"));
        return route;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) throws InvalidData {
        if (id < 1) {
            throw new InvalidData("id is out of range. Something went really wrong");
        }
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) throws InvalidData {
        if (name == null || name.isEmpty()) {
            throw new InvalidData("name is empty");
        }
        this.name = name;
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(Coordinates coordinates) throws InvalidData {
        if (coordinates == null) {
            throw new InvalidData("no coordinates");
        }
        this.coordinates = coordinates;
    }
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(ZonedDateTime creationDate) throws InvalidData {
        if (creationDate == null) {
            throw new InvalidData("no date");
        }
        this.creationDate = creationDate;
    }
    public Location getFrom() {
        return from;
    }
    public void setFrom(Location from) throws InvalidData {
        if (from == null) {
            throw new InvalidData("no start location");
        }
        this.from = from;
    }
    public Location getTo() {
        return to;
    }
    public void setTo(Location to) throws InvalidData {
        this.to = to;
    }
    public float getDistance() {
        return distance;
    }
    public void setDistance(float distance) throws InvalidData {
        if (distance <= 1) {
            throw new InvalidData("distance is too small or negative");
        }
        this.distance = distance;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) throws InvalidData {
        if (owner==null || owner.isEmpty()){
            throw new InvalidData("owner is empty");
        }
        this.owner = owner;
    }
    @Override
    public int compareTo(Route o) {
        return (int) (this.getDistance() - o.getDistance());
    }
    public float compareDistance(float distance) {
        return this.distance - distance;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Route)) return false;
        Route other = (Route) obj;
        return this.id == other.id;
    }
    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }
    @Override
    public String toString() {
        return "{\"id\": " +
                this.id +
                ", \"name\": " +
                "\""+ this.name + "\"" +
                ", \"coordinates\": " +
                this.coordinates.toString() +
                ", \"creationDate\": \"" +
                this.creationDate.toString() +
                "\", \"from\": " +
                this.from.toString() +
                ", \"to\": " +
                this.to.toString() +
                ", \"distance\": " +
                this.distance +
                "}";
    }
    public String toString(int tabs){
        return "\t".repeat(tabs) + "{\n" + "\t".repeat(tabs+1) + "\"id\": " +
                this.id +
                ",\n" + "\t".repeat(tabs+1) + "\"name\": " +
                "\""+ this.name + "\"" +
                ",\n" + "\t".repeat(tabs+1) + "\"coordinates\": " +
                this.coordinates.toString(tabs+1) +
                ",\n" + "\t".repeat(tabs+1) + "\"creationDate\": \"" +
                this.creationDate.toString() +
                "\",\n" + "\t".repeat(tabs+1) + "\"from\": " +
                this.from.toString(tabs+1) +
                ",\n" + "\t".repeat(tabs+1) + "\"to\": " +
                this.to.toString(tabs+1) +
                ",\n" + "\t".repeat(tabs+1) + "\"distance\": " +
                this.distance +
                "\n" + "\t".repeat(tabs) + "}";
    }
    public static Route getFromDescription(Scanner scanner) throws InvalidData {
        Route route = new Route();
        System.out.print("Введите название пути: ");
        route.setName(scanner.nextLine());
        route.setCoordinates(Coordinates.getFromDescription(scanner));
        route.setCreationDate(ZonedDateTime.now());
        route.setFrom(Location.getFromDescription(scanner));
        route.setTo(Location.getFromDescription(scanner));
        System.out.print("Введите расстояние между точками: ");
        route.setDistance(Float.parseFloat(scanner.nextLine()));
        return route;
    }
}
