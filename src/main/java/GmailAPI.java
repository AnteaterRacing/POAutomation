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
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

class GmailAPI {
    private static final String APPLICATION_NAME = "POAutomation";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens/gmail";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);
    private static final String CREDENTIALS_FILE_PATH = "/client_id.json";

    private Gmail serviceHandler;
    private static GmailAPI gmailAPIInstance = null;

    /**
     * Creates a MimeMessage object that contains information about the email. 
     * 
     * @param to the email address this email would be sent to
     * @param from the email address this email would be sent from
     * @param subject The subject of the created email
     * @param bodyText The message within the email
     * @throws Messagingexception when email i
     * @return a MimeMessage object
     */
    private MimeMessage createMockDraft(String to,
                                    String from,
                                    String subject,
                                    String bodyText) throws MessagingException
    {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

     /**
     * Creates a message object based on the MimeMessage object given as a parameter
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException 
     * @throws MessagingException 
     */
    private Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * Creates an instance of the GmailAPI class, which will allow to create, send, and read emails based on
     * id's.
     * 
     * @throws IOException If the credentials.json file cannot be found by the getCredentials function
     * @throws GeneralSecurityException If the credentials in the json file are not valid
     */
    private GmailAPI() throws IOException, GeneralSecurityException
    {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        serviceHandler = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                                  .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GmailAPI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
     * 
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static GmailAPI getInstance() throws IOException, GeneralSecurityException
    {
        if (gmailAPIInstance == null)
        {
            synchronized(GmailAPI.class)
            {
                if (gmailAPIInstance == null)
                    gmailAPIInstance = new GmailAPI();
            }
        }

        return gmailAPIInstance;
    }

    /**
     * Creates a draft with the given parameters.
     *
     * @param addressTo the email address we would send the email to
     * @param addressFrom the email address we are sending the email from
     * @param subject the subject of the email
     * @param messageBody the actual message contained in the email
     * @param userId user's email address. The special value "me"
     * can be used to indicate the authenticated user
     * @return the id of the created draft
     * @throws MessagingException
     * @throws IOException
     */
    public String createDraft(String addressTo, String addressFrom, String subject, String messageBody, String userId)
            throws MessagingException, IOException {
        MimeMessage emailContent = createMockDraft(addressTo, addressFrom, subject, messageBody);
        Message message = createMessageWithEmail(emailContent);

        Draft draft = new Draft();
        draft.setMessage(message);
        draft = serviceHandler.users().drafts().create(userId, draft).execute();

        return draft.getId();
    }

    /**
     * Sends the email draft whose id was given to the function.
     * 
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param draftID the id of the message that was sent
     * @return the id of the sent message
     * @throws IOException
     */
    public String sendDraft(String userID, String draftID) throws IOException
    {
        Draft draft = new Draft();
        draft.setId(draftID);
        Message message = serviceHandler.users().drafts().send(userID, draft).execute();

        return message.getId();
    }

    /**
     * Deletes an email draft with the given id for the user specified.
     * 
     * @param userID the id of the user. Can use "me" to specified the authenticated user
     * @param draftID the id of the draft
     * @throws IOException
     */
    public void deleteDraft(String userID, String draftID) throws IOException
    {
        serviceHandler.users().drafts().delete(userID, draftID).execute();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                                 .setApplicationName(APPLICATION_NAME).build();

        // Print the labels in the user's account.
        String user = "me";
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
    }
}