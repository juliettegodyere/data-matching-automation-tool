package net.queencoder.springboot.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.extern.slf4j.Slf4j;
import net.queencoder.springboot.dto.CbaApiResponse;
import net.queencoder.springboot.dto.ProcedureFilterRequest;
import net.queencoder.springboot.dto.ProcedureSpecifications;
import net.queencoder.springboot.exception.ClaimExistsException;
import net.queencoder.springboot.exception.CustomBadRequestException;
import net.queencoder.springboot.exception.CustomNotFoundException;
import net.queencoder.springboot.model.Claim;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.ProcedureLookUp;
import net.queencoder.springboot.model.Status;
import net.queencoder.springboot.repository.ClaimRepository;
import net.queencoder.springboot.repository.ProcedureLookUpRepository;
import net.queencoder.springboot.repository.ProcedureRepository;

import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.WebClient;


@Service
@Slf4j
public class ProcedureServiceImpl implements ProcedureService {

	@Value("${download.folder.path}")
    private String downloadFolderPath;

	@Autowired
	private ProcedureRepository procedureRepository;

	@Autowired
	private ProcedureLookUpRepository procedureLookUpRepository;

	@Autowired
	private ClaimRepository claimRepository;

	@Autowired
	private Environment environment;

	@Autowired
	private WebClient.Builder webClientBuilder;


	@Override
	public List<ProcedureLookUp> getAllExistingPrecedures() {
		return procedureLookUpRepository.findAll();
	}

	@Override
	public List<Procedure> getAllMatches() {
		return procedureRepository.findAll();
		
	}

	private String getBase64Credentials(String username, String password) {
		String credentials = username + ":" + password;
		return Base64.getEncoder().encodeToString(credentials.getBytes());
	}

	public List<ProcedureLookUp> fetchCbaDataFromFormelo() {
		String username = System.getenv("FORMELO_USERNAME");
		String password = System.getenv("FORMELO_PASSWORD");
		String baseUrl = System.getenv("FORMELO_URL");
		CbaApiResponse[] result = webClientBuilder.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + getBase64Credentials(username, password))
			.build()
			.get()
			.uri("/api/documents/?collection.slug=cba_lookup")
			.retrieve()
			.onStatus(
                    HttpStatus.BAD_REQUEST::equals,
                    response -> response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new CustomBadRequestException("Bad request: " + errorBody))))
			.bodyToMono(CbaApiResponse[].class)
			.block();
	
			if (result != null) {
				return Arrays.stream(result)
						.map(response -> {
							ProcedureLookUp procedureLookUp = new ProcedureLookUp();
							String numericPart = response.getId().replaceAll("\\D", "");
                
                			procedureLookUp.setId(Long.valueOf(numericPart));
							// procedureLookUp.setId(Long.valueOf(response.getId()));
							procedureLookUp.setCode(response.getData().getCode());
							procedureLookUp.setDescription(response.getData().getDescription());
							return procedureLookUp;
						})
						.collect(Collectors.toList());
			} else {
				return Collections.emptyList();
			}
	}	

	@Override
	public Page<Procedure> findPaginated(int pageNo, int pageSize, String sortField, String sortDir,ProcedureFilterRequest filterRequest) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();		

		Pageable pageable = PageRequest.of(pageNo - 1, Integer.parseInt(filterRequest.getPageSize()), sort);
        return procedureRepository.findAll(ProcedureSpecifications.filterByProcedureRequest(filterRequest), pageable);
	}

	@Override
	public List<Procedure> uploadData(MultipartFile file) throws IOException {
		//procedureRepository.deleteAll();
		try (InputStream inputStream = file.getInputStream()) {

			String filename = file.getOriginalFilename();

			if (!isValidFilenameFormat(filename)) {
				throw new IllegalArgumentException("Invalid filename format. Please use the format: hospital-name_month-year_procedure.csv");
			}
	
			String[] parts = extractHospitalName(filename);
			String hospitalName = parts[0];
			String narration = parts[1];

			List<Record> parsedRecords = parseCsvRecords(inputStream);

			List<Procedure> matchedProcedures = matchAndSaveRecords(parsedRecords, hospitalName, narration);
			return matchedProcedures;
		} catch (IOException e) {
			throw new IOException("Error reading the Schedule. Check the that file format is correct and upload again",
					e);
		}
	}

	public boolean isValidFilenameFormat(String filename) {
		filename = filename.trim();
	
		// Define a regular expression for the expected filename format
		String regex = "^[a-zA-Z0-9-]+_[a-zA-Z0-9-]+_[a-zA-Z0-9-]+\\.csv$";
	
		// Use Pattern and Matcher classes to validate the filename
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(filename);
	
		if (matcher.matches()) {
			System.out.println("The input matches the pattern!");
		} else {
			System.out.println("The input does not match the pattern.");
		}
	
		return matcher.matches();
	}
	

	private String[] extractHospitalName(String filePath) {
		// Assuming the hospital name is the first part of the file path
		String[] pathParts = filePath.split("_");
		String [] file_extracts = new String[pathParts.length];
		if (pathParts.length > 0) {
			file_extracts[0] = pathParts[0].trim();
			file_extracts[1] = pathParts[1].trim();
		}
		return file_extracts; 
	}

	private List<Record> parseCsvRecords(InputStream inputStream) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setHeaderExtractionEnabled(true);
		CsvParser parser = new CsvParser(settings);
		return parser.parseAllRecords(inputStream);
	}

	private List<Procedure> matchAndSaveRecords(List<Record> uploadedRecords, String hospitalName, String narration) {
		List<Procedure> matchedProcedures = new ArrayList<>();

		Optional<Claim> existingClaim = claimRepository.findByHospitalNameAndNarration(
                hospitalName,
                narration
        );

		if (existingClaim.isPresent()) {
			 // If a matching Claim already exists, throw an exception with an error message
			 throw new ClaimExistsException("Hospital with the same name and narration already exists.");
		}

		Claim claim = Claim.builder()
		.hospitalName(hospitalName)
		.narration(narration)
		.build();

		claimRepository.save(claim);

		List<ProcedureLookUp> existingProcedures = getAllExistingPrecedures();
		// List<ProcedureLookUp> existingProcedures = fetchCbaDataFromFormelo();

		for (Record uploadedRecord : uploadedRecords) {
			Procedure bestMatch = findBestMatchingProcedure(uploadedRecord, existingProcedures, claim);

			if (bestMatch != null) {

				matchedProcedures.add(bestMatch);
			}
		}
		

		saveMatches(matchedProcedures);

		return matchedProcedures;
	}

	private Procedure findBestMatchingProcedure(Record uploadedRecord, List<ProcedureLookUp> existingProcedures, Claim claim) {
		int threshold = 3; // Adjust this value according to your preference

		int bestEditDistance = Integer.MAX_VALUE;
		Procedure bestMatch = null;
		Optional<Claim> existingClaim = claimRepository.findByHospitalNameAndNarration(
                claim.getHospitalName(),
                claim.getNarration()
        );

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
						.claim(existingClaim.get())
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

				ProcedureLookUp newProcedure = ProcedureLookUp.builder()
							.code(code)
							.description(description)
							.build();
					procedureList.add(newProcedure);

				// Check if an entry with the same code already exists
				// ProcedureLookUp existingProcedure = procedureLookUpRepository.findByCode(code);
				// if (existingProcedure != null) {
				// 	existingProcedure.setDescription(description);
				// 	procedureList.add(existingProcedure);
				// } else {
				// 	// Create a new entry
				// 	ProcedureLookUp newProcedure = ProcedureLookUp.builder()
				// 			.code(code)
				// 			.description(description)
				// 			.build();
				// 	procedureList.add(newProcedure);

				// }
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
		log.info("The processed ID {}", procedures);
		// Check if all requested IDs were found
		for (Long id : ids) {
			boolean found = procedures.stream().anyMatch(procedure -> procedure.getId().equals(id));
			log.info("The found ID {}", found);
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
		String fileName = status.toLowerCase().trim() + "Records.csv";

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
			// log.info("Bytes: {}; headers {}, HttpStatus {}", bytes, headers,
			// HttpStatus.OK);
		} catch (IOException e) {
			// Handle the exception
			e.printStackTrace();
			// Return an error response here
			// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			// log.info(" HttpStatus {}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String generateCSVContent(List<Procedure> procedures) {
		// Generate CSV content based on the list of procedures
		// You can use libraries like OpenCSV to help with CSV generation
		// Example: using OpenCSV - https://www.baeldung.com/java-csv
		// StringJoiner and StringBuilder can also be used for simple CSV generation
		// Sample CSV generation using StringJoiner
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add("ID,Name,Look Up Name,Look Up Code,Edit Distance,Status");

		for (Procedure procedure : procedures) {
			String row = String.format("%d,%s,%s,%s,%d,%s",
					procedure.getId(),
					procedure.getName(),
					procedure.getLookUpName(),
					procedure.getLookUpCode(),
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
		if (status != null) {
			existingProcedure.setStatus(status);
		}
		return procedureRepository.save(existingProcedure);
	}

	@Override
	public List<Procedure> updateStatusInBatches(List<Procedure> proceduresToUpdate, Status status) {
		if (!proceduresToUpdate.isEmpty() && status != null) {
			// Use the map operation to update the status of each record, but only if status
			// is not null
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
	public void createDownloadableResource(ProcedureFilterRequest filterRequest) {
		List<Procedure> procedures = procedureRepository.findAll(ProcedureSpecifications.filterByProcedureRequest(filterRequest));
		Optional<Claim> claimOptional = null;
		Claim claim = new Claim();

		if(procedures != null){
			claimOptional = claimRepository.findById(procedures.get(0).getClaim().getId());

			if(!claimOptional.isPresent()){
				try {
					throw new CustomNotFoundException("Claim with ID " + filterRequest.getClaimId() + " not found");
				} catch (CustomNotFoundException e) {
					e.printStackTrace();
				}
			}else{
				claim = claimOptional.get();

			}
		}

		String csvContent = generateCSVContent(procedures);

		// String downloadFolderPath = System.getenv("DOWNLOAD_FOLDER_PATH");
		Path downloadFolderPath = getDefaultDownloadPath();
		// String downloadFolderPath = environment.getProperty("download.folder.path");
		String fileName = claim.getHospitalName() + "_" + claim.getNarration() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".csv";
	

		// String fileName = "Records_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".csv";

		try {
			// Create the download folder if it doesn't exist
			File downloadFolder = new File(downloadFolderPath.toString());
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
			// log.info("Bytes: {}; headers {}, HttpStatus {}", bytes, headers,
			// HttpStatus.OK);
		} catch (IOException e) {
			// Handle the exception
			e.printStackTrace();
			// Return an error response here
			// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			// log.info(" HttpStatus {}", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	 public Path getDefaultDownloadPath() {
        // Resolve the relative path to an absolute path
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
		log.info("Home directory {}", userHome);
		log.info("The OS {}", osName);

        if (osName.contains("win")) {
            // On Windows, use the default downloads folder
			log.info("I am Windows {}",userHome+"/Downloads/");
            return Paths.get(userHome, "Downloads");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            // On Unix-based systems (Linux, macOS), use the default downloads folder
			log.info("I am Mac {}", userHome+"/Downloads/");
            return Paths.get(userHome, "Downloads");
        } else {
            // Fallback to the user's home directory
			log.info("I am nobody {}", userHome+"/Downloads/");
            return Paths.get(userHome, downloadFolderPath);
        }
    }

	@Override
	public void markProcedureAsRejected(List<Procedure> procedures) {
		for (Procedure procedure : procedures) {
			// Find all procedures with the same lookup code and status ACCEPTED
			List<Procedure> acceptedRecords = procedureRepository.findByLookUpCodeAndStatus(procedure.getLookUpCode(), Status.ACCEPTED);

			// Mark all found records as REJECTED
			acceptedRecords.forEach(record -> {
				record.setStatus(Status.REJECTED);
			});
		}
	}

	@Override
	public List<Procedure> deleteSelectedRecords(ProcedureFilterRequest filterRequest) {
		List<Procedure> procedures = procedureRepository.findAll(ProcedureSpecifications.filterByProcedureRequest(filterRequest));
		procedureRepository.deleteAll(procedures);
		return procedures;
	}
}
