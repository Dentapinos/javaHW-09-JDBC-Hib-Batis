package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "masters")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "name", "birthday"})
@Builder
@Data
@ToString(exclude = "kitties")
public class Master {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private LocalDate birthday;

    @ManyToMany(mappedBy = "masters", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @Builder.Default
    List<Kitty> kitties = new ArrayList<>();

    public void addKitty(Kitty kitty) {
        kitties.add(kitty);
    }

    public void setKittyWithLinks(Kitty kitty) {
        if (!kitties.contains(kitty)) {
            kitty.addMaster(this);
            kitties.add(kitty);
        }
    }

    public void setKittyWithLinks(List<Kitty> kitties) {
        for (Kitty k : kitties) setKittyWithLinks(k);
    }
}
