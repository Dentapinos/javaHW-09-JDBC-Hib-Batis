package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.TypeBuilding;

import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "street")
@Builder
@ToString(exclude = "street")
@Table(name = "houses")
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(name = "date_building")
    private LocalDate dateBuilding;
    @Column(name = "floors")
    private int numberStoreys;
    @Enumerated(EnumType.STRING)
    private TypeBuilding type;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "street_id")
    private Street street;

    public void setStreetWithLink(Street street) {
        this.street = street;
        street.setHouseWithLinks(this);
    }

}
