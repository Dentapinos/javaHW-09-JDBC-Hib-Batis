package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.ColorKitty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kitties")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "masters")
@EqualsAndHashCode(exclude = "masters")
@Builder
@Data
public class Kitty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private LocalDate birthday;
    private String breed;
    @Enumerated(EnumType.STRING)
    private ColorKitty color;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "master_kitty",
            joinColumns = @JoinColumn(name = "kitty_id"),
            inverseJoinColumns = @JoinColumn(name = "master_id")
    )
    List<Master> masters = new ArrayList<>();

    public void addMaster(Master master) {
        masters.add(master);
    }

    public void setMasterWithLink(Master master) {
        if (!masters.contains(master)) {
            master.addKitty(this);
            masters.add(master);
        }
    }

    public void setMasterWithLink(List<Master> masters) {
        for (Master m : masters) setMasterWithLink(m);
    }
}
