package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.security.SecureRandom;

/**
 * Generates sequential integer IDs within a defined range.
 * <p>
 * This class uses a cryptographically secure random number generator
 * to initialize the starting point of the sequence.
 */
public class DigestIDGenerator {
  private int number;
  private static final int LOWER_BOUND = 0;
  private static final int UPPER_BOUND = (int) Math.pow(2, 30);

  /**
   * Constructs a DigestIDGenerator with a random starting number
   * within the defined {@link #LOWER_BOUND} and {@link #UPPER_BOUND}.
   */
  public DigestIDGenerator() {
    SecureRandom secureRandom = new SecureRandom();
    this.number = secureRandom.nextInt(UPPER_BOUND - LOWER_BOUND) + LOWER_BOUND;
  }

  /**
   * Generates the next sequential ID in the sequence.
   *
   * @return The next integer ID.
   */
  public int next() {
    return ++number;
  }
}