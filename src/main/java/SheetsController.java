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
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import javafx.util.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SheetsController {
    private static final String APPLICATION_NAME = "POAutomation";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/client_id.json";

    /**
     * Variables used to specify a specific spreadsheet
     */
    private Sheets sheetsServiceHandler;
    private String spreadsheetID;


    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
     * spreadsheet based on the id given.
     * 
     * @return instance of SheetsController
     * @throws IOException If the credentials.json file cannot be found by the getCredentials function
     * @throws GeneralSecurityException If the credentials in the json file are not valid
     */
    public SheetsController(String id) throws IOException, GeneralSecurityException
    {
        spreadsheetID = id;
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential creds = getCredentials(HTTP_TRANSPORT);
        sheetsServiceHandler = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, creds)
                                         .setApplicationName(APPLICATION_NAME).build();
    }


    /**
     * Returns the objects contained in within each cell of the listed spreadsheet in the listed range.
     * 
     * @param spreadsheetId The unique ID of the wanted spreadsheet. Can be found within the url of the spreadsheet
     * @param range Specifies the cells that will be returned in the list
     * @return data found within the cells stated from the spreadsheet specified
     * @throws IOException If a spreadsheet does not exist
     */
    public List<List<Object>> getValues(String range) throws IOException
    {
        ValueRange response = sheetsServiceHandler.spreadsheets().values()
                                .get(spreadsheetID, range)
                                .execute();
        return response.getValues();
    }


    /** 
     * Writes the specified values to the spreadsheet associated with an instance of 
     * the SheetsController class.
     * 
     * @param range Specifies the range of cells being written to
     * @param valueOption option decribing the type of values being written (formulas or text)
     * @param value the values being written into the speadsheet
     * @throws IOException If speadsheet does not exists
     */
    public void writeToSpreadsheet(String range,
                                   String valueOption,
                                   List<List<Object>> value) throws IOException
    {
        ValueRange newValues = new ValueRange().setValues(value);
        UpdateValuesResponse result = sheetsServiceHandler.spreadsheets().values()
                                            .update(spreadsheetID, range, newValues)
                                            .setValueInputOption(valueOption)
                                            .execute();
        System.out.printf("%d cells have been updated.", result.getUpdatedCells());
    }


    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final String spreadsheetId = "1xvoBF6-zTtCcOcq3Vmnf6RG0REUDSCq9l3qV2jnGjHY";
        final String range = "POs!A1:AX3";
        SheetsController sheet = new SheetsController(spreadsheetId);
        List<List<Object>> values = sheet.getValues(range);
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            //System.out.println("Name, Major");
            int count = 0;
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                //System.out.printf("%s, %s\n", row.get(0), row.get(4));
                //row.ensureCapacity(26+23);
                for(int i = 0; i < row.size(); i++)
                    System.out.printf("%s   ", row.get(i));
                
                count++;
                System.out.printf("Size: %d\n", row.size());
                System.out.println();
            }
            
            System.out.println("update spreadsheet");
            //sheet.writeToSpreadsheet(range, "RAW", values);
        }
    }
}