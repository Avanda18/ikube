package ikube.action.index.handler.internet;

import ikube.action.index.handler.ResourceHandler;
import ikube.model.Email;
import ikube.model.IndexContext;
import ikube.model.IndexableExchange;
import ikube.model.IndexableMessage;
import org.apache.lucene.document.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static ikube.action.index.IndexManager.addStringField;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
public class ExchangeResourceHandler extends ResourceHandler<IndexableExchange> {
    SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS Z");

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(final IndexContext indexContext, final IndexableExchange indexableExchange, final Document document, final Object resource)
            throws Exception {
        // Parse the content to be indexed from the email IndexableMessage
        parseContent(indexableExchange, document, (IndexableMessage) resource);

        super.addDocument(indexContext, document);
        return document;
    }

    void parseContent(final IndexableExchange indexableExchange, final Document document, final IndexableMessage msg) {
        // Add the contents fields
        String messageMailboxOwnerField     = indexableExchange.getMessageMailboxOwnerField();
        String messageMailboxNameField      = indexableExchange.getMessageMailboxNameField();
        String messageExchangeIdField       = indexableExchange.getMessageExchangeIdField();
        String messageInternetIdField       = indexableExchange.getMessageInternetIdField();
        String messageConversationIdField   = indexableExchange.getMessageConversationIdField();
        String messageCreatedDateField      = indexableExchange.getMessageCreatedDateField();
        String messageSentDateField         = indexableExchange.getMessageSentDateField();
        String messageReceivedDateField     = indexableExchange.getMessageReceivedDateField();
        String messageFromField             = indexableExchange.getMessageFromField();
        String messageToField               = indexableExchange.getMessageToField();
        String messageBccField              = indexableExchange.getMessageBccField();
        String messageCcField               = indexableExchange.getMessageCcField();
        String messageSubjectField          = indexableExchange.getMessageSubjectField();
        String messageBodyField             = indexableExchange.getMessageBodyField();
        String messageBodyTypeField         = indexableExchange.getMessageBodyTypeField();

        if (msg != null) {
            addStringField(messageMailboxOwnerField,   toEmailString(msg.mailboxOwner), indexableExchange, document);
            addStringField(messageMailboxNameField,    msg.mailboxName,            indexableExchange, document);
            addStringField(messageExchangeIdField,     msg.messageExchangeId,      indexableExchange, document);
            addStringField(messageInternetIdField,     msg.messageInternetId,      indexableExchange, document);
            addStringField(messageConversationIdField, msg.conversationId,         indexableExchange, document);
            addStringField(messageCreatedDateField,    toDateString(msg.created),  indexableExchange, document);
            addStringField(messageSentDateField,       toDateString(msg.sent),     indexableExchange, document);
            addStringField(messageReceivedDateField,   toDateString(msg.received), indexableExchange, document);
            addStringField(messageFromField,           toEmailString(msg.from),    indexableExchange, document);
            addStringField(messageToField,             toEmailsString(msg.to),     indexableExchange, document);
            addStringField(messageBccField,            toEmailsString(msg.bcc),    indexableExchange, document);
            addStringField(messageCcField,             toEmailsString(msg.cc),     indexableExchange, document);
            addStringField(messageSubjectField,        msg.subject,                indexableExchange, document);
            addStringField(messageBodyField,           msg.body,                   indexableExchange, document);
            addStringField(messageBodyTypeField,       msg.bodyType,               indexableExchange, document);
        }
    }

    private String toDateString(Date date){
        return (date != null) ? DATE_TIME_FORMAT.format(date) : null;
    }

    private String toEmailString(Email email){
        return (email != null) ? email.toString() : null;
    }

    private String toEmailsString(List<Email> emails){
        StringBuffer str = new StringBuffer();
        for(Email email : emails){
            if(str.length() > 0)
                str.append(", ");
            str.append(toEmailString(email));
        }
        return str.toString();
    }
}
