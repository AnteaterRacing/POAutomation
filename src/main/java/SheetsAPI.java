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
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

class SheetsAPI {
    private static final String APPLICATION_NAME = "POAutomation";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens/sheets";
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
    public static SheetsAPI sheetsAPIInstance = null;

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsAPI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Instantiates a new instance of the serviceHandler, which will allow access to a specific google
     * spreadsheet based on an id given.
     * 
     * @return instance of SheetsController
     * @throws IOException If the credentials.json file cannot be found by the getCredentials function
     * @throws GeneralSecurityException If the credentials in the json file are not valid
     */
    private SheetsAPI() throws IOException, GeneralSecurityException
    {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential creds = getCredentials(HTTP_TRANSPORT);
        sheetsServiceHandler = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, creds)
                                         .setApplicationName(APPLICATION_NAME).build();
    }


    /**
     * Grabs an instance of the sheetsAPI class through the sheetsAPIInstance variable
     * 
     * @throws IOException If the credentials.json file cannot be found by the getCredentials function
     * @throws GeneralSecurityException If the credentials in the json file are not valid
     */
    static public SheetsAPI getSheetsAPI() throws IOException, GeneralSecurityException
    {
        if (sheetsAPIInstance == null)
        {
            synchronized(SheetsAPI.class)
            {
                if (sheetsAPIInstance == null)
                    sheetsAPIInstance = new SheetsAPI();
            }
        }
        
        return sheetsAPIInstance;
    }

    /**
     * Creates a new spreadsheet with a given name
     * 
     * @param title the Name of the spreadsheet being created
     * @return the id of the newly-created spreadsheet
     * @throws IOException
     */
    String createSpreadsheet(String title) throws IOException
    {
        Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties()
                                      .setTitle(title));
        spreadsheet = sheetsServiceHandler.spreadsheets().create(spreadsheet)
                                    .setFields("spreadsheetId").execute();
        return spreadsheet.getSpreadsheetId();
    }

    /**
     * Returns the objects contained in within each cell of the listed spreadsheet in the listed range.
     * 
     * @param spreadsheetId The unique ID of the wanted spreadsheet. Can be found within the url of the spreadsheet
     * @param range Specifies the cells that will be returned in the list
     * @return data found within the cells stated from the spreadsheet specified
     * @throws IOException If a spreadsheet does not exist
     */
    public List<List<Object>> getValues(String spreadsheetID, String range) throws IOException
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
     * @param spreadsheetId The unique ID of the wanted spreadsheet. Can be found within the url of the spreadsheet
     * @param range Specifies the range of cells being written to
     * @param valueOption option decribing the type of values being written (formulas or text)
     * @param value the values being written into the speadsheet
     * @throws IOException If speadsheet does not exists
     */
    public void writeToSpreadsheet(String spreadsheetID,
                                   String range,
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

    
    /*public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final String spreadsheetId = "1xvoBF6-zTtCcOcq3Vmnf6RG0REUDSCq9l3qV2jnGjHY";
        final String range = "POs!A1:AX3";
        SheetsAPI sheet = SheetsAPI.getSheetsAPI();
        List<List<Object>> values = sheet.getValues(spreadsheetId, range);
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
    } */
}