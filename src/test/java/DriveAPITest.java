import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.services.drive.model.File;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveAPITest
{
    @Test 
    public void testCopyingandDeletingFiles() throws IOException, GeneralSecurityException
    {
        String id = "1VhNsnBAiF1U8ifXr3u0kdO-iC_HOG56h2AZ9zNnmzuE";
        String range = "Test0!A1:C3";
        SheetsAPI sheet = SheetsAPI.getSheetsAPI();
        DriveAPI drive = DriveAPI.getDriveAPI();

        String copyid = drive.copyFile(id, "Copy of Stuff");
        List<List<Object>> copiedValues = sheet.getValues(copyid, range);
        List<List<Object>> sheetValues = sheet.getValues(id, range);

        assertEquals(sheetValues, copiedValues);

        drive.deleteItem(copyid);
        List<File> files= drive.getFiles();
        boolean seenFile = false;

        if (files != null)
            for (File d : files)
            {
                if (d.getId().equals(copyid))
                {
                    seenFile = true;
                    break;
                }
            }

        assertTrue(!seenFile);
    }

    @Test
    public void testMoveFile() throws IOException, GeneralSecurityException
    {
        String id = "1VhNsnBAiF1U8ifXr3u0kdO-iC_HOG56h2AZ9zNnmzuE";
        DriveAPI drive = DriveAPI.getDriveAPI();

        String copyid = drive.copyFile(id, "Copy of Stuff");
        String folderID = drive.createFolder("Test Folder");
        drive.moveFile(copyid, folderID);
        List<String> parents = drive.getParents(copyid);

        assertTrue(parents.contains(folderID));
        drive.deleteItem(folderID);
    }
}