import java.util.Arrays;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.google.api.services.drive.model.File;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SheetsAPITest {
    public static final String name = "Stuff";
    public static final String valueOption = "RAW";
    public static String range = "Sheet1!A1:C3";
    public static SheetsAPI sheetDriver;
    public static String id;

    @BeforeEach
    public void setUp() throws IOException, GeneralSecurityException {
        sheetDriver = SheetsAPI.getSheetsAPI();
        id = sheetDriver.createSpreadsheet(name);

        List<List<Object>> values = new ArrayList<>();
        values.add(Arrays.asList(1, 0, 0));
        values.add(Arrays.asList(0, 1, 0));
        values.add(Arrays.asList(0, 0, 1));

        sheetDriver.writeToSpreadsheet(id, range, valueOption, values);
    }

    @Test
    public void testCreatingSpreadsheet() throws IOException, GeneralSecurityException {
        DriveAPI drive = DriveAPI.getDriveAPI();
        String id = sheetDriver.createSpreadsheet(name);

        List<File> files = drive.getFiles();
        boolean seenFile = false;

        if (files != null)
            for (File d : files) {
                if (d.getId().equals(id)) {
                    seenFile = true;
                    break;
                }
            }

        assertTrue(seenFile);
        drive.deleteItem(id);
    }

    @Test
    public void testGettingValues() throws IOException, GeneralSecurityException {
        List<List<Object>> sheetValues = sheetDriver.getValues(id, range);

        List<List<Object>> expectedValues = new ArrayList<>();
        expectedValues.add(Arrays.asList("1", "0", "0"));
        expectedValues.add(Arrays.asList("0", "1", "0"));
        expectedValues.add(Arrays.asList("0", "0", "1"));

        assertEquals(expectedValues, sheetValues);
    }

    @Test
    public void testRenamingTabs() throws IOException, GeneralSecurityException {
        sheetDriver.renameTab(id, 0, "Test0");

        // Check if old tab name exists
        assertThrows(IOException.class, () -> {
            sheetDriver.getValues(id, range);
        }, "Did not throw IOException as expected");

        range = "Test0!A1:C3";
        testGettingValues();
    }

    @Test
    public void testWriteToSpreadsheet() throws IOException, GeneralSecurityException {
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

        sheetDriver.writeToSpreadsheet(id, range, valueOption, values);
        List<List<Object>> results = sheetDriver.getValues(id, range);

        assertEquals(values, results);
    }

    @AfterEach
    public void cleanUp() throws IOException, GeneralSecurityException {
        DriveAPI driveHandler = DriveAPI.getDriveAPI();
        driveHandler.deleteItem(id);
    }
}