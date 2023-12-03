package net.queencoder.springboot.model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table
@Entity
public class Claim extends AuditModel{
    
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    private String hospitalName;
    private String narration;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Procedure> procedures;

    // @Transient
    // private Set<Procedure> distinctProcedures;

    // public void calculateDistinctProcedures() {
    //     this.distinctProcedures = new HashSet<>(this.procedures.stream()
    //             .collect(Collectors.toMap(Procedure::getName, Function.identity(), (existing, replacement) -> existing))
    //             .values());
    // }

    // private List<Procedure> procedures;
}
