import nju.SEIII.EASIEST.Main;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class OutputConsistencyTest {
    @Test
    public void testOutputConsistency() throws Exception {
        String inputFile = "src/test/resources/data/myfile.csv";
        String outputFile = "src/test/resources/data/myfile0_out.txt";
        String[] args = new String[] {"input", inputFile};

        Main.main(args);

        File expectedFile = new File("src/test/resources/data/expected.txt");
        File resultFile = new File(outputFile);

        BufferedReader expectedReader = new BufferedReader(new FileReader(expectedFile));
        BufferedReader resultReader = new BufferedReader(new FileReader(resultFile));

        String expectedLine = expectedReader.readLine();
        String resultLine = resultReader.readLine();

        while (expectedLine != null && resultLine != null) {
            assertEquals(expectedLine, resultLine);
            expectedLine = expectedReader.readLine();
            resultLine = resultReader.readLine();
        }

        expectedReader.close();
        resultReader.close();

        resultFile.delete();
    }
}
