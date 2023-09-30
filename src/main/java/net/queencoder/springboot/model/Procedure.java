package net.queencoder.springboot.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "procedures")
public class Procedure extends AuditModel{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "procedure_class")
	private String procedureClass;

	private String name;

	@Column(name = "look_up_name")
	private String lookUpName;
	
	@Column(name = "look_up_code")
	private String lookUpCode;
	
	private String quantity;
	
	private String rate;
	
	@Column(name = "edit_distance")
	private int editDistance;
	
	//private boolean flag = false;
	
	@Enumerated(EnumType.STRING)
    private Status status;
	
}
