package multithreading;

import java.math.BigInteger;
import java.util.Random;

public class PrimeGenerator implements Runnable {

    private Results results;

    public PrimeGenerator(Results results) {
        this.results = results;
    }

    @Override
    public void run() {
        // generate a random big integer number between 0 to 2^(2000-1)
        BigInteger bigInt = new BigInteger(2000, new Random());
        // find the next available number that's higher than bigInt and that is also a prime number
        results.addPrime(bigInt.nextProbablePrime());
    }
}
