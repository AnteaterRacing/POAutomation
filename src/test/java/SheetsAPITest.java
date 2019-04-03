import java.util.Arrays;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SheetsAPITest
{
    @Test 
    public void testGettingValues() throws IOException, GeneralSecurityException
    {
        String id = "1VhNsnBAiF1U8ifXr3u0kdO-iC_HOG56h2AZ9zNnmzuE";
        String range = "Test0!A1:C3";
        SheetsAPI sheet = SheetsAPI.getSheetsAPI();
        List<List<Object>> sheetValues = sheet.getValues(id, range);

        List<List<Object>> expectedValues = new ArrayList<>();
        expectedValues.add(Arrays.asList("1", "0", "0"));
        expectedValues.add(Arrays.asList("0", "1", "0"));
        expectedValues.add(Arrays.asList("0", "0", "1"));

        assertEquals(expectedValues, sheetValues);
    }

    @Test 
    public void testWriteToSpreadsheet() throws IOException, GeneralSecurityException
    {
        String id = "1VhNsnBAiF1U8ifXr3u0kdO-iC_HOG56h2AZ9zNnmzuE";
        String range = "Test1!A1:C3";
        String valueOption = "RAW";
        List<List<Object>> values  = new ArrayList<>();

        values.add(Arrays.asList(
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)), 
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10))));
        values.add(Arrays.asList(
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)), 
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10))));
        values.add(Arrays.asList(
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)), 
                        Integer.toString(ThreadLocalRandom.current().nextInt(0, 10))));

        SheetsAPI sheet = SheetsAPI.getSheetsAPI();
        sheet.writeToSpreadsheet(id, range, valueOption, values);
        List<List<Object>> results = sheet.getValues(id, range);

        assertEquals(values, results);
    }
}