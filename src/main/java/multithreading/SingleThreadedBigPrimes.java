package multithreading;

import java.math.BigInteger;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class SingleThreadedBigPrimes {

    public static void main(String[] args) {
        Long start = System.currentTimeMillis();

        SortedSet<BigInteger> primes = new TreeSet<>();

        while(primes.size() < 20) {
            // generate a random big integer number between 0 to 2^(2000-1)
            BigInteger bigInt = new BigInteger(2000, new Random());
            // find the next available number that's higher than bigInt and that is also a prime number
            primes.add(bigInt.nextProbablePrime());
        }

        Long end = System.currentTimeMillis();
        System.out.println(primes);
        System.out.println("The time taken was " + (end - start) + "ms");
    }
}
