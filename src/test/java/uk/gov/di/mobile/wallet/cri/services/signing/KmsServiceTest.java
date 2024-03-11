package uk.gov.di.mobile.wallet.cri.services.signing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.kms.KmsClient;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class KmsServiceTest {
    @Mock KmsClient mockKmsClient;
    private KmsService kmsService;
    ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationService();
        kmsService = new KmsService(configurationService);
    }

    @Test
    void testItCreatesKmsService() {
        assertNotNull(kmsService);
    }
}
