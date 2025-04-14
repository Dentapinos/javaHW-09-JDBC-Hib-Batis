package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.TypeBody;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "brand")
@Builder
@Entity
@ToString(exclude = "brand")
@Table(name = "models_car")
public class ModelCar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    private int length;
    private int width;
    @Enumerated(EnumType.STRING)
    private TypeBody body;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private BrandCar brand;

    public void setBrandWithLinks(BrandCar brand) {
        this.brand = brand;
        brand.setModel(this);
    }
}
