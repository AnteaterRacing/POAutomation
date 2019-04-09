import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import javax.mail.MessagingException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GmailAPITest
{
    @Test
    public void testCreateAndDeleteDraft() throws IOException, GeneralSecurityException, MessagingException
    {
        GmailAPI gmail = GmailAPI.getInstance();
        String draftID = gmail.createDraft("vguy77@gmail.com", "vguy77@gmail.com", "test", "test", "me");
        List<Draft> drafts = gmail.getDrafts("me");

        boolean seenDraft = false;
        if (drafts != null)
            for (Draft d : drafts)
            {
                if (d.getId().equals(draftID))
                {
                    seenDraft = true;
                    break;
                }
            }

        assertTrue(seenDraft);

        gmail.deleteDraft("me", draftID);
        drafts = gmail.getDrafts("me");
        seenDraft = false;
        if (drafts != null)
            for (Draft d : drafts)
            {
                if (d.getId().equals(draftID))
                {
                    seenDraft = true;
                    break;
                }
            }

        assertTrue(!seenDraft);
    }

    @Test
    public void testSendingEmail() throws IOException, MessagingException, GeneralSecurityException
    {
        GmailAPI gmail = GmailAPI.getInstance();
        String draftID = gmail.createDraft("vguy77@gmail.com", "vguy77@gmail.com", "test", "test", "me");
        String messageID = gmail.sendDraft("me", draftID);
        List<Message> inbox = gmail.getInbox("me");

        boolean seenMessage = false;
        for (Message m : inbox)
        {
            if (m.getId().equals(messageID))
            {
                seenMessage = true;
                break;
            }
        }

        assertTrue(seenMessage);
    }
}

// TODO: Account for sockets timing out on HTTP requests