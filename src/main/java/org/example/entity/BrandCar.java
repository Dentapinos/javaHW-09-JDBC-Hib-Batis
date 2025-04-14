package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "models")
@Builder
@Entity
@ToString(exclude = "models")
@Table(name = "brands_car")
public class BrandCar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(name = "date")
    private LocalDate dateFoundation;

    @OneToMany(mappedBy = "brand", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ModelCar> models = new ArrayList<>();

    public void addModel(ModelCar model) {
        models.add(model);
        model.setBrand(this);
    }

    public void setModel(ModelCar model) {
        models.add(model);
    }

    public void setModelsWithLinks(List<ModelCar> models) {
        models.forEach(this::addModel);
    }
}
