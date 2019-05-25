import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.mail.MessagingException;

import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GmailAPITest {
    public static GmailAPI gmail = null;
    public static String draftID = null;

    @BeforeEach
    public void setUp() throws IOException, GeneralSecurityException, MessagingException {
        if (gmail == null)
            gmail = GmailAPI.getInstance();

        if (draftID == null)
            draftID = gmail.createDraft("vguy77@gmail.com", "vguy77@gmail.com", "test", "test", "me");
    }

    @Test
    public void testCreateAndDeleteDraft() throws IOException, GeneralSecurityException, MessagingException {
        List<Draft> drafts = gmail.getDrafts("me");

        boolean seenDraft = false;
        if (drafts != null)
            for (Draft d : drafts) {
                if (d.getId().equals(draftID)) {
                    seenDraft = true;
                    break;
                }
            }

        assertTrue(seenDraft);

        gmail.deleteDraft("me", draftID);
        drafts = gmail.getDrafts("me");
        seenDraft = false;
        if (drafts != null)
            for (Draft d : drafts) {
                if (d.getId().equals(draftID)) {
                    seenDraft = true;
                    break;
                }
            }

        assertTrue(!seenDraft);

        if (!seenDraft)
            draftID = null;
    }

    @Test
    public void testSendingAndDeletingEmail() throws IOException, MessagingException, GeneralSecurityException {
        String messageID = gmail.sendDraft("me", draftID);
        List<Message> inbox = gmail.getInbox("me");

        boolean seenMessage = false;
        for (Message m : inbox) {
            if (m.getId().equals(messageID)) {
                seenMessage = true;
                break;
            }
        }

        assertTrue(seenMessage);

        gmail.trashEmail("me", messageID);
        inbox = gmail.getInbox("me");
        seenMessage = false;

        for (Message m : inbox) {
            if (m.getId().equals(messageID)) {
                seenMessage = true;
                break;
            }
        }

        assertFalse(seenMessage);
    }
}

// TODO: Account for sockets timing out on HTTP requests