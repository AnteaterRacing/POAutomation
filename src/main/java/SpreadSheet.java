import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

class SpreadSheet
{
    private SheetsAPI sheetService;
    private DriveAPI driveService;
    private String spreadsheetID;
    private final String[] valueOption = {"RAW", "USER_ENTERED"};

    /**
     * Creates a new Spreadsheet or link to a pre-existing spreadsheet within the drive
     * for this particular instance of the SpreadSheet class, based on the linkExisitingSheet
     * flag.
     * 
     * @param id the name of the spreadsheet or the id of the pre-existsing spreadsheet file
     * @param linkExisitingSheet determines whether to link to a spreadsheet or make a new one
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public SpreadSheet(String id, boolean linkExisitingSheet) throws IOException, GeneralSecurityException
    {
        sheetService = SheetsAPI.getSheetsAPI();
        driveService = DriveAPI.getDriveAPI();
        spreadsheetID = linkExisitingSheet ? id : sheetService.createSpreadsheet(id);
    }

    /**
     * Creates a copy of the spreadsheet contained in the copy object.
     * 
     * @param copy the SpreadSheet we want to copy
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public SpreadSheet(SpreadSheet copy, String title) throws IOException, GeneralSecurityException
    {
        sheetService = SheetsAPI.getSheetsAPI();
        driveService = DriveAPI.getDriveAPI();
        spreadsheetID = driveService.copyFile(copy.getID(), title);
    }

    /**
     * Enters data into a part of the spreadsheet.
     * 
     * @param tab               The tab where the data will be entered
     * @param startColumn       The first column where data will be entered
     * @param startRow          The first row where data will be entered
     * @param endColumn         The last column where data will entered
     * @param endRow            The last row where data will be entered
     * @param contents          The data going into the spreadsheet. The outer list represents the
     *                          row while the inner list represents the column where the data will 
     *                          be written to
     * @param containsFormulas  States whether or not formulas are being entered
     * @throws IOException
     */
    public void write(String tab,
                      String startColumn,
                      int startRow,
                      String endColumn,
                      int endRow,
                      List<List<Object>> contents,
                      boolean containsFormulas) throws IOException
    {
        StringBuffer a1Notation = new StringBuffer(tab);
        a1Notation.append("!");
        a1Notation.append(startColumn);
        a1Notation.append(startRow);
        a1Notation.append(":");
        a1Notation.append(endColumn);
        a1Notation.append(endRow);

        sheetService.writeToSpreadsheet(
            spreadsheetID,
            a1Notation.toString(),
            !containsFormulas ? valueOption[0] : valueOption[1],
            contents);
    }

    /**
     * Moves itself to another specified folder within the Google Drive
     * 
     * @param folderID the id of the folder we will be moving the folder to
     * @throws IOException
     */
    public void moveSpreadsheet(String folderID) throws IOException
    {
        driveService.moveFile(spreadsheetID, folderID);
    }

    /**
     * Accessor for the spreadsheet id
     * 
     * @return spreadsheet id
     */
    public String getID()
    {
        return spreadsheetID;
    }

    /**
     * Returns the values of the spreadsheet in the columns and rows specified
     * as parameters to the function
     * 
     * @param tab               The tab where the data will be entered
     * @param startColumn       The first column where data will be entered
     * @param startRow          The first row where data will be entered
     * @param endColumn         The last column where data will entered
     * @param endRow            The last row where data will be entered
     * @return                  The contents from the range specified
     * @throws IOException
     */
    public List<List<Object>> getValues(String tab,
                                        String startColumn,
                                        int startRow,
                                        String endColumn,
                                        int endRow) throws IOException
    {
        StringBuffer a1Notation = new StringBuffer(tab);
        a1Notation.append("!");
        a1Notation.append(startColumn);
        a1Notation.append(startRow);
        a1Notation.append(":");
        a1Notation.append(endColumn);
        a1Notation.append(endRow);

        return sheetService.getValues(spreadsheetID, a1Notation.toString());
    }
}