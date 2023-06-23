package net.queencoder.springboot.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "receives_procedures")
public class Drug {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "drug_id")
	private Long id;
	
	@Column(name = "drug_name")
	private String drugName;
	
	@Column(name = "drug_class")
	private String drugClass;
	
	private String quantity;
	
	private String rate;
		
	@Column(name = "match_name")
	private String matchName;
	
	@Column(name = "match_code")
	private String matchCode;
	
	@Column(name = "edit_distance")
	private int editDistance;
	
	private boolean flag = false;
	
	private boolean status = false;
	
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "drug")
//	    private List < ProcedureMatch > matches  = new ArrayList<>(); 
	
	
}
