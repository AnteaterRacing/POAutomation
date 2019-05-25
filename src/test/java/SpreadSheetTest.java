import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

class SpreadSheetTest {
    public static final String name = "TestSheet";
    public static final String tab = "Sheet1";
    public static SpreadSheet testSheet;

    @BeforeAll
    public static void setUp() throws IOException, GeneralSecurityException {
        testSheet = new SpreadSheet(name);
    }

    @Test
    public void testWrite() throws IOException, GeneralSecurityException {
        List<List<Object>> values = new ArrayList<>();

        values.add(Arrays.asList(Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                Integer.toString(ThreadLocalRandom.current().nextInt(0, 10))));
        values.add(Arrays.asList(Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                Integer.toString(ThreadLocalRandom.current().nextInt(0, 10))));
        values.add(Arrays.asList(Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                Integer.toString(ThreadLocalRandom.current().nextInt(0, 10)),
                Integer.toString(ThreadLocalRandom.current().nextInt(0, 10))));

        testSheet.write(tab, values, false);
        List<List<Object>> results = testSheet.getValues(tab);

        assertEquals(values, results);
    }

    @Test
    public void testCopyConstructor() throws IOException, GeneralSecurityException {
        String name2 = "TestSheet2";
        SpreadSheet copy = new SpreadSheet(testSheet, name2);

        List<List<Object>> sheetResults = testSheet.getValues(tab);
        List<List<Object>> copyResults = copy.getValues(tab);

        assertEquals(sheetResults, copyResults);
        DriveAPI drive = DriveAPI.getDriveAPI();
        drive.deleteItem(copy.getID());
    }

    @Test
    public void testMoveSpreadsheet() throws IOException, GeneralSecurityException {
        DriveAPI driveHandler = DriveAPI.getDriveAPI();
        String folderID = driveHandler.createFolder("test");
        List<String> previousParents = driveHandler.getParents(testSheet.getID());

        testSheet.moveSpreadsheet(folderID);
        List<String> currentParents = driveHandler.getParents(testSheet.getID());

        assertTrue(currentParents.contains(folderID));
        assertNotEquals(previousParents, currentParents);
        driveHandler.deleteItem(folderID);
    }

    @AfterAll
    public static void cleanUp() throws IOException, GeneralSecurityException {
        DriveAPI drive = DriveAPI.getDriveAPI();
        drive.deleteItem(testSheet.getID());
    }
}