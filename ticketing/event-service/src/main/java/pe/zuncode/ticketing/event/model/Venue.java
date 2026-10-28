package pe.zuncode.ticketing.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Recinto donde se realizan los eventos (estadio, teatro, arena...).
 * <p>
 * Sesión 2: entidad JPA mapeada a la tabla {@code venues}.
 * Ya no se expone en la API: los controladores trabajan con DTOs.
 */
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(length = 200)
    private String address;

    @Column(nullable = false)
    private int capacity;

    // JPA necesita un constructor sin argumentos; protected para que nadie más lo use
    protected Venue() {
    }

    public Venue(String name, String city, String address, int capacity) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
