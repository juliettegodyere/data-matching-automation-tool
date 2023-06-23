package net.queencoder.springboot.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.queencoder.springboot.model.CbaProcedure;
import net.queencoder.springboot.service.CbaProcedureService;

@RestController
public class CbaProcedureController {
	
	@Autowired
	private CbaProcedureService cbaProcedureService;
	
	public CbaProcedureController(CbaProcedureService service) {
		super();
		this.cbaProcedureService = service;
	}

	@PostMapping("/procedures")
	public List<CbaProcedure> createProcedures(@RequestParam("file") MultipartFile file) throws IOException {
		return cbaProcedureService.createProcedure(file);
	}
	
	@GetMapping("/procedures")
	public List<CbaProcedure> getAllProcedures() {
		return cbaProcedureService.getAllProcedures();
	}
	
	@GetMapping("/procedures/{id}")
	public CbaProcedure getProceduresById(@PathVariable(value = "id") long id) {
		return cbaProcedureService.getProcedureById(id);
	}
}
