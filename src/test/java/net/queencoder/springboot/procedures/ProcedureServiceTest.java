package net.queencoder.springboot.procedures;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.ProcedureLookUp;
import net.queencoder.springboot.repository.ProcedureLookUpRepository;
import net.queencoder.springboot.repository.ProcedureRepository;
import net.queencoder.springboot.service.ProcedureService;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class ProcedureServiceTest {
    
    @MockBean
    private ProcedureRepository procedureRepository;

    @MockBean
    private ProcedureLookUpRepository procedureLookUpRepository;

    @InjectMocks
    private ProcedureService procedureService;

    private Procedure procedure;
    //private ProcedureLookUp procedureLookUp;

    /**
     * Given the uploaded Procedures and the Lookup procedures give the best match procedure
     */
    @Test
    public void getBestMatches(){
        // ProcedureLookUp procedureLookUp = ProcedureLookUp
        // .builder()
        // .id(1L)
        // .code("DRGS04980")
        // .description("BenzylPenicillin (penicillin G) 600mg (100,000i.u) Inj")
        // .build().
        // List<ProcedureLookUp> procedureLookUpList = new ArrayList<>();
        // procedureLookUpList.add(procedureLookUp);
        // //Given
        // given(procedureService.getAllExistingPrecedures()).willReturn();
        //When
        //Then
    }


}
