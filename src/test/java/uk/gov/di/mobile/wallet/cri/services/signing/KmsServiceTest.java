package uk.gov.di.mobile.wallet.cri.services.signing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KmsServiceTest {

    private KmsService kmsService;
    ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationService();
        kmsService = new KmsService(configurationService);
    }

    @Test
    void shouldCreateKmsService() {
        assertNotNull(kmsService);
    }
}
