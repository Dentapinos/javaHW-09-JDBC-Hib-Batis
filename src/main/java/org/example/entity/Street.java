package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(exclude = "houses")
@ToString(exclude = "houses")
@Table(name = "streets")
public class Street {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(nullable = false)
    private int postcode;

    @OneToMany(mappedBy = "street", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @Builder.Default
    private List<House> houses = new ArrayList<>();

    public void setHouseWithLinks(House house) {
        if (!houses.contains(house)) {
            houses.add(house);
            house.setStreet(this);
        }
    }

    public void setHousesWithLinks(List<House> houses) {
        for (House house : houses) {
            setHouseWithLinks(house);
        }
    }


}
