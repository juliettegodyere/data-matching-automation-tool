package net.queencoder.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class ProcedureMatch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "client_procedure_name")
	private String clientProcedureName;
	
	@Column(name = "match_procedure_name")
	private String matchProcedureName;
	
	@Column(name = "client_procedure_id")
	private String clientProcedureId;
	
	@Column(name = "match_procedure_code")
	private String matchProcedureCode;
	
}
