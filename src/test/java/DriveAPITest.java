import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.model.Sheet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveAPITest {
    public static String ID;
    public static DriveAPI drive;

    @BeforeAll
    public static void setUp() throws IOException, GeneralSecurityException {
        drive = DriveAPI.getDriveAPI();
        SheetsAPI sheets = SheetsAPI.getSheetsAPI();
        String range = "Test0!A1:C3";
        ID = sheets.createSpreadsheet("Test Sheet");

        List<List<Object>> values = new ArrayList<List<Object>>();
        values.add(Arrays.asList("1", "0", "0"));
        values.add(Arrays.asList("0", "1", "0"));
        values.add(Arrays.asList("0", "0", "1"));

        sheets.renameTab(ID, 0, "Test0");
        sheets.writeToSpreadsheet(ID, range, "RAW", values);
    }

    @Test
    public void testCopyingandDeletingFiles() throws IOException, GeneralSecurityException {
        String range = "Test0!A1:C3";
        SheetsAPI sheet = SheetsAPI.getSheetsAPI();

        String copyid = drive.copyFile(ID, "Copy of Stuff");
        List<List<Object>> copiedValues = sheet.getValues(copyid, range);
        List<List<Object>> sheetValues = sheet.getValues(ID, range);

        assertEquals(sheetValues, copiedValues);

        drive.deleteItem(copyid);
        List<File> files = drive.getFiles();
        boolean seenFile = false;

        if (files != null)
            for (File d : files) {
                if (d.getId().equals(copyid)) {
                    seenFile = true;
                    break;
                }
            }

        assertTrue(!seenFile);
    }

    @Test
    public void testMoveFile() throws IOException, GeneralSecurityException {
        String copyid = drive.copyFile(ID, "Copy of Stuff");
        String folderID = drive.createFolder("Test Folder");
        drive.moveFile(copyid, folderID);
        List<String> parents = drive.getParents(copyid);

        assertTrue(parents.contains(folderID));
        drive.deleteItem(folderID);
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        drive.deleteItem(ID);
    }
}