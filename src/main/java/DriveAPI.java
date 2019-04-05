import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

class DriveAPI {
    private static final String APPLICATION_NAME = "POAutomation";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens/drive";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/client_id.json";

    private static DriveAPI driveAPIInstance = null;
    private Drive serviceHandler;

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveAPI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Instantiates a new instance of the serviceHandler, which will allow access to a specific google
     * drive based on the credentials given.
     * 
     * @throws IOException If the credentials.json file cannot be found by the getCredentials function
     * @throws GeneralSecurityException If the credentials in the json file are not valid
     */
    private DriveAPI() throws IOException, GeneralSecurityException
    {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        serviceHandler = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                                  .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Grabs an instance of the DriveAPI based on the driveAPIInstance variable
     * 
     * @throws IOException If the credentials.json file cannot be found by the getCredentials function
     * @throws GeneralSecurityException If the credentials in the json file are not valid
     */
    public static DriveAPI getDriveAPI() throws IOException, GeneralSecurityException
    {
        if (driveAPIInstance == null)
            driveAPIInstance = new DriveAPI();

        return driveAPIInstance;
    }

    /**
     * Copies an existing file from the Drive to a new file. The function returns the ID 
     * of the new file.
     * 
     * @param fileID the ID of the file that will be copied from
     * @param fileName the name of the new file.
     * @throws IOException If there are issues with the files involved or the drive
     */
    public String copyFile(String fileID, String fileName) throws IOException
    {
        File newCopiedFile = new File();
        newCopiedFile.set("Title", fileName);

        File newFile = serviceHandler.files().copy(fileID, newCopiedFile).execute();
        return newFile.getId();
    }

    /**
     * Creates a new folder of a given name within the Google Drive.
     * 
     * @param folderName the name of the folder that will be created
     * @return the ID of the newly created folder
     * @throws IOException If there are issues with the files involved or the drive
     */
    public String createFolder(String folderName) throws IOException
    {
        File newFolder = new File();
        newFolder.set("Title", folderName);

        newFolder.setMimeType("application/vnd.google-apps.folder");
        File createdFolder = serviceHandler.files().create(newFolder).setFields("id")
                                         .execute();
        return createdFolder.getId();
    }

    /**
     * Looks for and returns the parents of the file given.
     * 
     * @param fileID the id of the file
     * @return a list of folder ids of the file's parents
     * @throws IOException
     */
    public List<String> getParents(String fileID) throws IOException
    {
        File file = serviceHandler.files().get(fileID).setFields("parents")
                                  .execute();
        return file.getParents();
    }

    /**
     * Moves a files into a specified folder within the Google Drive
     * 
     * @param fileID the id of the file that will be moved
     * @param folderID the id of the folder that the file will be moved into
     * @throws IOException
     */
    public void moveFile(String fileID, String folderID) throws IOException
    {
        File currentFile = serviceHandler.files().get(fileID).setFields("parents")
                                         .execute();
        String prevParents = String.join(",", currentFile.getParents());

        serviceHandler.files().update(fileID, null)
                      .setAddParents(folderID).setRemoveParents(prevParents)
                      .setFields("id, parents").execute();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
       // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }
}