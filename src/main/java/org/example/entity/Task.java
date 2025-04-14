package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.TypeTask;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "employee")
@Builder
@Entity
@ToString(exclude = "employee")
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private LocalDate deadline;
    private String description;
    @Enumerated(EnumType.STRING)
    private TypeTask type;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public void setEmployeeWithLinks(Employee employee) {
        this.employee = employee;
        employee.setTask(this);
    }

    public void setEmployeeWithLinks(List<Employee> employees) {
        employees.forEach(this::setEmployeeWithLinks);
    }
}
