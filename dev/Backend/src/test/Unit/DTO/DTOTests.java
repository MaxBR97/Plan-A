package Unit.DTO;

import DTO.Factories.RecordFactory;
import DTO.Records.Image.SolutionDTO;
import Model.Model;
import Model.Solution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DTOTests {

    static String SimpleCodeExample = """
                param x := 10;
                set mySet := {1,2,3};

                var myVar[mySet];

                subto sampleConstraint:
                    myVar[x] == mySet[1];

                maximize myObjective:
                    1;
            """;
    static String sourcePath = "src/test/Utilities/ZimplExamples/ExampleZimplProgram.zpl";
    static String solutionPath = "src/test/Utilities/ZimplExamples/ExampleZimplSolution.zpl";
    Model model;
    @BeforeEach
    public void beforeEach() {
        try {
            model = new Model(sourcePath);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail();
        }
    }
    @Test
    void GivenSolution_WhenCreatingSolutionDTO_StructureIsCorrect() {
        //Set up
        try {
            Solution solution = new Solution(solutionPath);
            HashSet<String> vars = new HashSet<>();
            vars.add("Soldier_Shift");
            solution.parseSolution(model, vars);
            SolutionDTO solutionDTO = RecordFactory.makeDTO(solution);

            //Tests
            assertNotNull(solutionDTO);
            //verified manually
            //TODO: make a deep check that solutionDTO is the same as solution, and/or check against unparsed solution
            // For now both the example problem and solution are badly names and informative, want to change them before progressing with this
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            fail();
        }
    }
}