import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

class SpreadSheetTest
{
    private SpreadSheet testSheet;

    @BeforeEach
    public void setupSheets() throws IOException, GeneralSecurityException
    {
        testSheet = new SpreadSheet("test1");
    }

    @Test
    public void testWrite() throws IOException, GeneralSecurityException
    {
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

        testSheet.write("Sheet1", values, false);
        List<List<Object>> results = testSheet.getValues("Sheet1");
        
        assertEquals(values, results);
    }

    @Test
    public void testCopyConstructor() throws IOException, GeneralSecurityException
    {
        SpreadSheet copy = new SpreadSheet(testSheet, "test2");

        List<List<Object>> sheetResults = testSheet.getValues("Sheet1");
        List<List<Object>> copyResults = copy.getValues("Sheet1");

        assertEquals(sheetResults, copyResults);
    }

    @AfterEach
    public void cleanUp() throws IOException, GeneralSecurityException
    {
        DriveAPI drive = DriveAPI.getDriveAPI();
        drive.deleteItem(testSheet.getID());
    }
}