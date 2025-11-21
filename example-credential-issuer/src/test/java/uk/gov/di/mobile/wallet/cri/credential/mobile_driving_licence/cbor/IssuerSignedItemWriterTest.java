package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;

import java.io.IOException;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class IssuerSignedItemWriterTest {

    @Mock private CBORGenerator cborGenerator;

    @Test
    void Should_WriteIssuerSignedItem() throws IOException {
        int digestId = 7;
        byte[] random = new byte[] {0x01, 0x02};
        String elementIdentifier = "test_element_identifier";
        Object elementValue = "Test Element Value";

        IssuerSignedItem valueToWrite =
                new IssuerSignedItem(digestId, random, elementIdentifier, elementValue);

        IssuerSignedItemWriter.write(cborGenerator, valueToWrite);

        InOrder inOrder = inOrder(cborGenerator);
        inOrder.verify(cborGenerator).writeStartObject(4);
        inOrder.verify(cborGenerator).writeNumberField("digestID", digestId);
        inOrder.verify(cborGenerator).writeBinaryField("random", random);
        inOrder.verify(cborGenerator).writeStringField("elementIdentifier", elementIdentifier);
        inOrder.verify(cborGenerator).writeFieldName("elementValue");
        inOrder.verify(cborGenerator).writeObject(elementValue);
        inOrder.verify(cborGenerator).writeEndObject();
        verifyNoMoreInteractions(cborGenerator);
    }
}
