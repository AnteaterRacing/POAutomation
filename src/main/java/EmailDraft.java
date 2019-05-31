import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Period;
import java.time.ZonedDateTime;

import javax.mail.MessagingException;

class EmailDraft {
    private GmailAPI emailService;

    // Properties of the draft/email created
    private String emailID;
    private String draftID;
    private String userID;
    private String addressFrom;
    private String addressTo;
    private String subject;
    private String messageBody;

    private boolean sent;
    private ZonedDateTime dateOfCreation;

    /**
     * Creates an instance of an EmailDraft object with the given parameters
     * 
     * @param from   the email address this email will be sent from.
     * @param to     the email address that this email will be sent to
     * @param sub    the subject line
     * @param body   the message body you want to send in the email
     * @param userId The id of the user sending the email. You can use "me" to
     *               specify the account associated with the credentials.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public EmailDraft(String from, String to, String sub, String body, String userId)
            throws IOException, GeneralSecurityException, MessagingException {
        emailService = GmailAPI.getInstance();
        addressFrom = from;
        addressTo = to;
        subject = sub;
        messageBody = body;
        userID = userId;
        emailID = "N/A";
        sent = false;
        dateOfCreation = ZonedDateTime.now();

        draftID = emailService.createDraft(addressTo, addressFrom, subject, messageBody, userID);
    }

    /**
     * Modifies the addressFrom variable and replaces the old draft with a new draft
     * with the updated information
     * 
     * @param from The new email address we are sending the email from
     * @throws IOException
     * @throws MessagingException
     * @throws AlreadySentException
     */
    public void updateAddressFrom(String from) throws IOException, MessagingException {
        if (!sent) {
            addressFrom = from;
            emailService.deleteDraft(userID, draftID);
            draftID = emailService.createDraft(addressTo, addressFrom, subject, messageBody, userID);
        } else
            System.out.printf("CANNOT EDIT ADDRESSFROM. EMAIL %s ALREADY SENT\n", emailID);
    }

    /**
     * Modifies the addressTo variable and replaces the old draft with a new draft
     * with the updated information
     * 
     * @param to The new email address we are sending the email to
     * @throws IOException
     * @throws MessagingException
     * @throws AlreadySentException
     */
    public void updateAddressTo(String to) throws IOException, MessagingException {
        if (!sent) {
            addressTo = to;
            emailService.deleteDraft(userID, draftID);
            draftID = emailService.createDraft(addressTo, addressFrom, subject, messageBody, userID);
        } else
            System.out.printf("CANNOT EDIT ADDRESSTO. EMAIL %s ALREADY SENT\n", emailID);
    }

    /**
     * Modifies the subject variable and replaces the old draft with a new draft
     * with the updated information
     * 
     * @param sub The new subject line we are sending the email to
     * @throws IOException
     * @throws MessagingException
     * @throws AlreadySentException
     */
    public void updateSubjectLine(String sub) throws IOException, MessagingException {
        if (!sent) {
            subject = sub;
            emailService.deleteDraft(userID, draftID);
            draftID = emailService.createDraft(addressTo, addressFrom, subject, messageBody, userID);
        } else
            System.out.printf("CANNOT EDIT SUBJECT. EMAIL %s ALREADY SENT", emailID);
    }

    /**
     * Modifies the messageBody variable and replaces the old draft with a new draft
     * with the updated information
     * 
     * @param body The new message body we are sending the email to
     * @throws IOException
     * @throws MessagingException
     * @throws AlreadySentException
     */
    public void updateMessage(String body) throws IOException, MessagingException {
        if (!sent) {
            messageBody = body;
            emailService.deleteDraft(userID, draftID);
            draftID = emailService.createDraft(addressTo, addressFrom, subject, messageBody, userID);
        } else
            System.out.printf("CANNOT EDIT MESSAGEBODY. EMAIL %s ALREADY SENT", emailID);
    }

    /**
     * Calculates the age of the emailDraft at the time this function is executed
     * 
     * @return the age of the email in days
     */
    public int checkAgeinDays() {
        ZonedDateTime now = ZonedDateTime.now();
        return Period.between(dateOfCreation.toLocalDate(), now.toLocalDate()).getDays();
    }

    /**
     * Checks if the email draft has been sent
     * 
     * @return the sent variable
     */
    boolean hasBeenSent() {
        return sent;
    }

    /**
     * Sends the draft and sets the sent flag to true
     * 
     * @throws IOException
     */
    public void sendDraft() throws IOException {
        emailID = emailService.sendDraft(userID, draftID);
        sent = true;
    }

    /**
     * Retreives the draftID of the email attached to this object
     * 
     * @return draftID
     */
    public String getDraftID() {
        return draftID;
    }

    /**
     * Retrieves the emailId of the email sent pertaining to this object
     * 
     * @return emailID
     */
    public String getEmailID() {
        return emailID;
    }
}