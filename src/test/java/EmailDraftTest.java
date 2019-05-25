import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.mail.MessagingException;

import com.google.api.services.gmail.model.Draft;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmailDraftTest {
    public static GmailAPI apis;
    public static EmailDraft email;
    public static AlreadySentException exception = new AlreadySentException("exception");
    public static final String NEW_ADDRESS_FROM = "vguy77@gmail.com";
    public static final String NEW_ADDRESS_TO = "vqha@uci.edu";
    public static final String NEW_SUBJECT = "TEST2";
    public static final String NEW_MESSAGE_BODY = "MESSAGE";

    @BeforeAll
    public static void setUp() throws IOException, GeneralSecurityException, MessagingException {
        apis = GmailAPI.getInstance();
        email = new EmailDraft("vqha@uci.edu", "vguy77@gmail.com", "test", "test", "me");
    }

    @Test
    public void testUpdateAddressFrom() throws IOException, MessagingException, AlreadySentException {
        email.updateAddressFrom(NEW_ADDRESS_FROM);

        List<Draft> drafts = apis.getDrafts("me");
        Draft myDraft = null;
        for (Draft d : drafts) {
            if (d.getId().equals(email.getDraftID())) {
                myDraft = d;
                break;
            }
        }

        // function should have made a draft
        assertNotEquals(null, myDraft);

        // The two drafts should be the same
        assertEquals(email.getDraftID(), myDraft.getId());
    }

    @Test
    public void testUpdateAddressTo() throws IOException, MessagingException, AlreadySentException {
        email.updateAddressTo(NEW_ADDRESS_TO);

        List<Draft> drafts = apis.getDrafts("me");
        Draft myDraft = null;
        for (Draft d : drafts) {
            if (d.getId().equals(email.getDraftID())) {
                myDraft = d;
                break;
            }
        }

        // function should have made a draft
        assertNotEquals(null, myDraft);

        // The two drafts should be the same
        assertEquals(email.getDraftID(), myDraft.getId());
    }

    @Test
    public void testUpdateAddressSubjectLine() throws IOException, MessagingException, AlreadySentException {
        email.updateSubjectLine(NEW_SUBJECT);

        List<Draft> drafts = apis.getDrafts("me");
        Draft myDraft = null;
        for (Draft d : drafts) {
            if (d.getId().equals(email.getDraftID())) {
                myDraft = d;
                break;
            }
        }

        // function should have made a draft
        assertNotEquals(null, myDraft);

        // The two drafts should be the same
        assertEquals(email.getDraftID(), myDraft.getId());
    }

    @Test
    public void testUpdateMessage() throws IOException, MessagingException, AlreadySentException {
        email.updateMessage(NEW_MESSAGE_BODY);

        List<Draft> drafts = apis.getDrafts("me");
        Draft myDraft = null;
        for (Draft d : drafts) {
            if (d.getId().equals(email.getDraftID())) {
                myDraft = d;
                break;
            }
        }

        // function should have made a draft
        assertNotEquals(null, myDraft);

        // The two drafts should be the same
        assertEquals(email.getDraftID(), myDraft.getId());
    }

    @Test
    void checkAge() {
        int age = email.checkAgeinDays();
        assertEquals(0, age);
    }

    @Test
    void checkSendingEmail() throws IOException {
        email.sendDraft();

        // check if draft is sent
        assertTrue(email.hasBeenSent());

        // check if we cannot update addressFrom
        Exception exception = assertThrows(AlreadySentException.class, () -> {
            email.updateAddressFrom(NEW_ADDRESS_FROM);
        });
        assertEquals(String.format("CANNOT EDIT ADDRESSFROM. EMAIL %s ALREADY SENT", email.getEmailID()),
                exception.getMessage());

        // check if we cannot update addressTo
        exception = assertThrows(AlreadySentException.class, () -> {
            email.updateAddressTo(NEW_ADDRESS_TO);
        });
        assertEquals(String.format("CANNOT EDIT ADDRESSTO. EMAIL %s ALREADY SENT", email.getEmailID()),
                exception.getMessage());

        // check if we cannot update Subject
        exception = assertThrows(AlreadySentException.class, () -> {
            email.updateSubjectLine(NEW_SUBJECT);
        });
        assertEquals(String.format("CANNOT EDIT SUBJECT. EMAIL %s ALREADY SENT", email.getEmailID()),
                exception.getMessage());

        // check if we cannot update Message
        exception = assertThrows(AlreadySentException.class, () -> {
            email.updateMessage(NEW_MESSAGE_BODY);
        });
        assertEquals(String.format("CANNOT EDIT MESSAGEBODY. EMAIL %s ALREADY SENT", email.getEmailID()),
                exception.getMessage());

        apis.deleteDraft("me", email.getEmailID());
    }

}