package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class IacasTest {

    @Mock private Iaca mockIaca;

    @Test
    void Should_CreateRecordWithListOfIacas() {
        List<Iaca> iacaList = List.of(mockIaca);

        Iacas iacas = new Iacas(iacaList);

        assertEquals(iacaList, iacas.data());
        assertEquals(1, iacas.data().size());
    }
}
