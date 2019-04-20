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
    @Test
    public void testWrite() throws IOException, GeneralSecurityException
    {
        SpreadSheet sheet = new SpreadSheet("test");
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

        sheet.write("Sheet1", values, false);
        List<List<Object>> results = sheet.getValues("Sheet1");
        
        assertEquals(values, results);
        DriveAPI drive = DriveAPI.getDriveAPI();
        drive.deleteItem(sheet.getID());
    }

    @Test
    public void testCopyConstructor() throws IOException, GeneralSecurityException
    {
        SpreadSheet sheet = new SpreadSheet("test");
        SpreadSheet copy = new SpreadSheet(sheet, "test2");

        List<List<Object>> sheetResults = sheet.getValues("Sheet1");
        List<List<Object>> copyResults = copy.getValues("Sheet1");

        assertEquals(sheetResults, copyResults);
        DriveAPI drive = DriveAPI.getDriveAPI();
        drive.deleteItem(sheet.getID());
        drive.deleteItem(copy.getID());
    }

    
}