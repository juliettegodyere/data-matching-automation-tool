package net.queencoder.springboot.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import net.queencoder.springboot.model.CbaProcedure;
import net.queencoder.springboot.model.Drug;
import net.queencoder.springboot.model.ProcedureMatch;
import net.queencoder.springboot.repository.CbaProcedureRepository;

@Service
public class CbaProcedureServiceImpl implements CbaProcedureService{
	
	@Autowired
	private CbaProcedureRepository repository;

	@Override
	public List<CbaProcedure> createProcedure(MultipartFile file) throws IOException {
		List<CbaProcedure> cba_procedure_list = new ArrayList<>();
//		https://www.youtube.com/watch?v=WiLCuMeBp3c
		
		InputStream inputStream;
		inputStream = file.getInputStream();
		CsvParserSettings setting = new CsvParserSettings();
		setting.setHeaderExtractionEnabled(true);
		CsvParser parser = new CsvParser(setting);
		List<Record> parseAllRecords = parser.parseAllRecords(inputStream);

		parseAllRecords.forEach(record -> {
			CbaProcedure cba_procedure = new CbaProcedure();

			cba_procedure.setProcedureName(record.getString("Procedure Description"));
			cba_procedure.setProcedureCode(record.getString("Procedure Code"));
			cba_procedure_list.add(cba_procedure);
		});
				
		
		return repository.saveAll(cba_procedure_list);
	}

	@Override
	public List<CbaProcedure> getAllProcedures() {
		return repository.findAll();
	}

	@Override
	public CbaProcedure getProcedureById(long id) {
		return repository.findById(id).orElseThrow(() -> new RuntimeException("Could not find the id " + id));
	}

}
