package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "task")
@EqualsAndHashCode(exclude = "task")
@Builder
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToOne(mappedBy = "employee", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Task task;

    public void setTaskWithLink(Task task) {
        this.task = task;
        task.setEmployee(this);
    }

}
