package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.security.SecureRandom;

/**
 * Generates sequential numeric IDs.
 *
 * <p>This class generates sequential numeric IDs within a defined range. The ID generation process
 * ensures that each generated ID is unique within the bounds of the generator's configuration.
 *
 * <p>The generator starts with a randomly initialized 'number' and an 'increment', both within
 * predefined bounds. Each call to the {@link #next()} method calculates the next ID in the sequence
 * by adding the 'increment' to the current 'number'.
 */
public class DigestIDGenerator {
    private int number;
    private final int increment;
    private static final int NUMBER_LOWER_BOUND = 0;
    private static final int NUMBER_ID_UPPER_BOUND = (int) Math.pow(2, 30);
    private static final int INCREMENT_LOWER_BOUND = 1;
    private static final int INCREMENT_UPPER_BOUND = 99;

    /**
     * Constructs a DigestIDGenerator with a random starting number and increment.
     *
     * <p>The starting number is randomly generated within the range defined by {@link
     * #NUMBER_LOWER_BOUND} and {@link #NUMBER_ID_UPPER_BOUND}. The increment is randomly generated
     * within the range defined by {@link #INCREMENT_LOWER_BOUND} and {@link
     * #INCREMENT_UPPER_BOUND}.
     */
    public DigestIDGenerator() {
        SecureRandom secureRandom = new SecureRandom();
        this.number =
                secureRandom.nextInt(NUMBER_ID_UPPER_BOUND - NUMBER_LOWER_BOUND)
                        + NUMBER_LOWER_BOUND;
        this.increment =
                secureRandom.nextInt(INCREMENT_UPPER_BOUND - INCREMENT_LOWER_BOUND)
                        + INCREMENT_LOWER_BOUND;
    }

    /**
     * Generates the next integer ID in the sequence.
     *
     * @return The next integer ID in the sequence.
     */
    public int next() {
        number = number + increment;
        return number;
    }
}
