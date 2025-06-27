package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class COSEUnprotectedHeaderBuilderTest {

  private COSEUnprotectedHeaderBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new COSEUnprotectedHeaderBuilder();
  }

  @Test
  void Should_BuildCOSEUnprotectedHeader() {
    byte[] x5chain = new byte[] {1, 2, 3};

    COSEUnprotectedHeader header = builder.x5chain(x5chain).build();

    assertNotNull(header);
    assertEquals(x5chain, header.unprotectedHeader().get(33));
  }

  @Test
  void Should_ThrowIllegalArgumentException_When_X5chainIsNull() {
    assertThrows(IllegalArgumentException.class, () -> builder.x5chain(null));
  }

  @Test
  void Should_ThrowIllegalArgumentException_When_X5chainIsNotSet() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> builder.build());
    assertEquals("x5chain must be set", exception.getMessage());
  }
}
