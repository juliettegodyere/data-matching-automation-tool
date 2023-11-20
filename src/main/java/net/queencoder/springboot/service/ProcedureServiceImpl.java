package net.queencoder.springboot.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.extern.slf4j.Slf4j;

import net.queencoder.springboot.exception.CustomNotFoundException;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.ProcedureLookUp;
import net.queencoder.springboot.model.Status;
import net.queencoder.springboot.repository.ProcedureLookUpRepository;
import net.queencoder.springboot.repository.ProcedureRepository;

@Service
@Slf4j
public class ProcedureServiceImpl implements ProcedureService {

	@Autowired
	private ProcedureRepository procedureRepository;

	@Autowired
	private ProcedureLookUpRepository procedureLookUpRepository;

	@Autowired
	private Environment environment;

	@Override
	public List<ProcedureLookUp> getAllExistingPrecedures() {
		return procedureLookUpRepository.findAll();
	}

	@Override
	public List<Procedure> getAllMatches() {
		return procedureRepository.findAll();
	}

	public Page<Procedure> getProceduresByStatuses(List<Status> statuses, Pageable pageable) {
		return procedureRepository.findByStatusIn(statuses, pageable);
	}

	@Override
	public Page<Procedure> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection, String query,
			List<Status> statuses) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		Page<Procedure> proceduresPage = getProceduresByStatuses(statuses, pageable);

		if (query != null && query != "") {

			proceduresPage = procedureRepository.search(query, pageable);
		}

		return proceduresPage;
	}

	@Override
	public List<Procedure> uploadData(MultipartFile file) throws IOException {
		// Delete all existing entries from the database
		procedureRepository.deleteAll();

		try (InputStream inputStream = file.getInputStream()) {
			List<Record> parsedRecords = parseCsvRecords(inputStream);

			List<Procedure> matchedProcedures = matchAndSaveRecords(parsedRecords);
			return matchedProcedures;
		} catch (IOException e) {
			throw new IOException("Error reading the Schedule. Check the that file format is correct and upload again",
					e);
		}
	}

	private List<Record> parseCsvRecords(InputStream inputStream) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		CsvParser parser = new CsvParser(settings);
		return parser.parseAllRecords(inputStream);
	}

	private List<Procedure> matchAndSaveRecords(List<Record> uploadedRecords) {
		List<Procedure> matchedProcedures = new ArrayList<>();

		List<ProcedureLookUp> existingProcedures = getAllExistingPrecedures(); 

		for (Record uploadedRecord : uploadedRecords) {
			Procedure bestMatch = findBestMatchingProcedure(uploadedRecord, existingProcedures);

			if (bestMatch != null) {

				matchedProcedures.add(bestMatch);
			}
		}

		// Save the matched drugs to the database or perform other necessary actions
		saveMatches(matchedProcedures);
		
		//Make a post
		return matchedProcedures;
	}

	private Procedure findBestMatchingProcedure(Record uploadedRecord, List<ProcedureLookUp> existingProcedures) {
		int threshold = 3; // Adjust this value according to your preference

		int bestEditDistance = Integer.MAX_VALUE;
		Procedure bestMatch = null;

		// Create a set to store matched lookup codes
		// Set<String> matchedCodes = new HashSet<>();

		for (ProcedureLookUp existingProcedure : existingProcedures) {
			// Check if the code has already been matched
			// if (matchedCodes.contains(existingProcedure.getCode())) {
			// continue; // Skip this code, as it has already been matched
			// }

			int editDistance = eD2(
					uploadedRecord.getString("DRUG NAME"),
					existingProcedure.getDescription(),
					uploadedRecord.getString("DRUG NAME").length(),
					existingProcedure.getDescription().length());

			if (editDistance < bestEditDistance) {
				bestEditDistance = editDistance;
				bestMatch = Procedure.builder()
						.procedureClass(uploadedRecord.getString("DRUG CLASS"))
						.name(uploadedRecord.getString("DRUG NAME"))
						.lookUpName(existingProcedure.getDescription())
						.lookUpCode(existingProcedure.getCode())
						.quantity(uploadedRecord.getString("QUANTITY"))
						.rate(uploadedRecord.getString("RATE"))
						.editDistance(bestEditDistance)
						.status(bestEditDistance == 0 ? Status.ACCEPTED : Status.UNACCEPTED)
						.build();

			}
		}

		return bestMatch;
	}

	@Override
	public List<Procedure> saveMatches(List<Procedure> procedure) {
		return procedureRepository.saveAll(procedure);
	}


	// This method is the edit distance dynamic programming solution
	static int eD2(String s1, String s2, int m, int n) {
		int dp[][] = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= n; j++) {
			dp[0][j] = j;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
				dp[i][j] = Math.min(dp[i - 1][j] + 1,
						Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost));
			}
		}

		return dp[m][n];
	}

	@Override
	public List<ProcedureLookUp> uploadLookUpData(MultipartFile file) throws IOException {
		
		List<ProcedureLookUp> procedureList = new ArrayList<>();
		try (InputStream inputStream = file.getInputStream()) {
			List<Record> parsedRecords = parseCsvRecords(inputStream);

			for (Record s : parsedRecords) {
				String code = s.getString("Procedure Code");
				String description = s.getString("Procedure Description");

				// Check if an entry with the same code already exists
				ProcedureLookUp existingProcedure = procedureLookUpRepository.findByCode(code);
				if (existingProcedure != null) {
					existingProcedure.setDescription(description);
					procedureList.add(existingProcedure);
				} else {
					//Create a new entry
					ProcedureLookUp newProcedure = ProcedureLookUp.builder()
							.code(code)
							.description(description)
							.build();
					procedureList.add(newProcedure);
					
				}
			}
			procedureLookUpRepository.saveAll(procedureList);
			return procedureList;
		} catch (IOException e) {
			throw new IOException(
					"Error reading the look up Schedule. Check the that file format is correct and upload again", e);
		}
	}
	
	@Override
	public Procedure findById(Long id) throws CustomNotFoundException {
		Procedure procedure = procedureRepository.findById(id)
				.orElseThrow(() -> new CustomNotFoundException("Procedure not found with id: " + id));

		return procedure;
	}

	@Override
	public List<Procedure> getAllById(List<Long> ids) throws CustomNotFoundException {
		List<Procedure> procedures = procedureRepository.findAllById(ids);
		
		// Check if all requested IDs were found
		for (Long id : ids) {
			boolean found = procedures.stream().anyMatch(procedure -> procedure.getId().equals(id));
			if (!found) {
				throw new CustomNotFoundException("Procedure not found with id: " + id);
			}
		}

		return procedures;
	}



	@Override
	public void downloadRecordsByStatus(String status) {
		List<Procedure> procedures = procedureRepository.findByStatus(Status.valueOf(status));
		// Generate a CSV content
		String csvContent = generateCSVContent(procedures);

		// Define the download folder path
		String downloadFolderPath = environment.getProperty("download.folder.path");

		// Define the file name
		String fileName = status.toLowerCase().trim()+"Records.csv";

		try {
			// Create the download folder if it doesn't exist
			File downloadFolder = new File(downloadFolderPath);
			if (!downloadFolder.exists()) {
				downloadFolder.mkdirs();
			}

			// Create the CSV file
			File csvFile = new File(downloadFolder, fileName);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
				writer.write(csvContent);
			}

			// Read the saved file's content as bytes
			byte[] bytes = Files.readAllBytes(csvFile.toPath());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", fileName);

			// return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
			//log.info("Bytes: {}; headers {}, HttpStatus {}", bytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			// Handle the exception
			e.printStackTrace();
			// Return an error response here
			// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			//log.info(" HttpStatus {}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String generateCSVContent(List<Procedure> procedures) {
		// Generate CSV content based on the list of procedures
		// You can use libraries like OpenCSV to help with CSV generation
		// Example: using OpenCSV - https://www.baeldung.com/java-csv
		// StringJoiner and StringBuilder can also be used for simple CSV generation

		// Sample CSV generation using StringJoiner
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add("ID,Procedure Class,Name,Look Up Name,Look Up Code,Quantity,Rate,Edit Distance,Status");

		for (Procedure procedure : procedures) {
			String row = String.format("%d,%s,%s,%s,%s,%s,%s,%d,%s",
					procedure.getId(),
					procedure.getProcedureClass(),
					procedure.getName(),
					procedure.getLookUpName(),
					procedure.getLookUpCode(),
					procedure.getQuantity(),
					procedure.getRate(),
					procedure.getEditDistance(),
					procedure.getStatus());
			joiner.add(row);
		}

		return joiner.toString();
	}

	@Override
	public List<Procedure> findByLookUpCode(Procedure existingLookUpCode) {
		return procedureRepository.findByLookUpCode(existingLookUpCode.getLookUpCode());
	}

	@Override
	public Procedure updateStatus(Procedure existingProcedure, Status status) {
		if(status != null){
			existingProcedure.setStatus(status);
		}
		return procedureRepository.save(existingProcedure);
	}

	@Override
	public List<Procedure> updateStatusInBatches(List<Procedure> proceduresToUpdate, Status status) {
    if (!proceduresToUpdate.isEmpty() && status != null) {
        // Use the map operation to update the status of each record, but only if status is not null
        proceduresToUpdate.forEach(record -> {
            if (status != null) {
                record.setStatus(status);
            }
        });

        // Save all updated records and return the list
        return procedureRepository.saveAll(proceduresToUpdate);
    }
    	return Collections.emptyList();
	}



	@Override
	public List<Procedure> getRecordsByIds(List<Long> recordIds) {
        // Implement logic to fetch records from the database using recordIds
        return procedureRepository.findAllById(recordIds);
    }

	@Override
	public void createDownloadableResource(List<Procedure> procedures) {
        String csvContent = generateCSVContent(procedures);

		// Define the download folder path
		String downloadFolderPath = environment.getProperty("download.folder.path");

		// Define the file name
		//String fileName = status.toLowerCase().trim()+"Records.csv";
		String fileName = "Records_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".csv";

		try {
			// Create the download folder if it doesn't exist
			File downloadFolder = new File(downloadFolderPath);
			if (!downloadFolder.exists()) {
				downloadFolder.mkdirs();
			}

			// Create the CSV file
			File csvFile = new File(downloadFolder, fileName);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
				writer.write(csvContent);
			}

			// Read the saved file's content as bytes
			byte[] bytes = Files.readAllBytes(csvFile.toPath());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", fileName);

			// return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
			//log.info("Bytes: {}; headers {}, HttpStatus {}", bytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			// Handle the exception
			e.printStackTrace();
			// Return an error response here
			// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			//log.info(" HttpStatus {}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
    }

	@Override
	public void markProcedureAsRejected(List<Procedure> procedures) {
		for (Procedure procedure : procedures) {
			// Find all procedures with the same lookup code and status ACCEPTED
			List<Procedure> acceptedRecords = procedureRepository.findByLookUpCodeAndStatus(procedure, Status.ACCEPTED);
			
			// Mark all found records as REJECTED
			acceptedRecords.forEach(record -> {
				record.setStatus(Status.REJECTED);
			});
		}
	}

	@Override
	public void clearDB(){
		procedureLookUpRepository.deleteAll();
	}
}
