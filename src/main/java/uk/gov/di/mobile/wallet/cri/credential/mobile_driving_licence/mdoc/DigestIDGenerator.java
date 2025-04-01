package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.util.Random;

public class DigestIDGenerator {
  private int number;
  private Random random;

  private static final int LOWER_BOUND = 0;
  private static final int UPPER_BOUND = (int) Math.pow(2, 30);

  // Constructor to set a random starting number within the lower and upper bounds
  public DigestIDGenerator() {
    this.random = new Random();
    this.number = random.nextInt(UPPER_BOUND - LOWER_BOUND) + LOWER_BOUND;
  }

  public int next() {
    return ++number;
  }
}