package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPairList;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

import java.lang.reflect.Array;

/**<p>{@link SeparateChainingHashTable} is a {@link HashTable} that implements <b>Separate Chaining</b>
 * as its collision resolution strategy, i.e the collision chains are implemented as actual
 * Linked Lists. These Linked Lists are <b>not assumed ordered</b>. It is the easiest and most &quot; natural &quot; way to
 * implement a hash table and is useful for estimating hash function quality. In practice, it would
 * <b>not</b> be the best way to implement a hash table, because of the wasted space for the heads of the lists.
 * Open Addressing methods, like those implemented in {@link LinearProbingHashTable} and {@link QuadraticProbingHashTable}
 * are more desirable in practice, since they use the original space of the table for the collision chains themselves.</p>
 *
 * @author Isaac Solomon
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see OrderedLinearProbingHashTable
 * @see CollisionResolver
 */
public class SeparateChainingHashTable implements HashTable{

    /* ****************************************************************** */
    /* ***** PRIVATE FIELDS / METHODS PROVIDED TO YOU: DO NOT EDIT! ***** */
    /* ****************************************************************** */

    private KVPairList[] table;
    private int count;
    private PrimeGenerator primeGenerator;
    //private static int probes = 0;

    // We mask the top bit of the default hashCode() to filter away negative values.
    // Have to copy over the implementation from OpenAddressingHashTable; no biggie.
    private int hash(String key){
        return (key.hashCode() & 0x7fffffff) % table.length;
    }

    /* **************************************** */
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  */
    /* **************************************** */
    /**
     *  Default constructor. Initializes the internal storage with a size equal to the default of {@link PrimeGenerator}.
     */
    public SeparateChainingHashTable(){
        primeGenerator = new PrimeGenerator();
        int prime = primeGenerator.getCurrPrime();


        table = new KVPairList[prime];

        for (int i = 0; i<table.length; i++){
            table[i] = new KVPairList();//initialize each bucket

        }

        count = 0;
    }




    @Override
    public Probes put(String key, String value) {
        Probes num = new Probes(value, 1);


        table[hash(key)].addBack(key, value);

        count++;


        return num;
    }

    @Override
    public Probes get(String key) {
        return table[hash(key)].getValue(key);

    }

    @Override
    public Probes remove(String key) {

        if (table[hash(key)].containsKey(key) == true){
            count--;
        }
        Probes result = table[hash(key)].removeByKey(key);


        return result;
    }

    @Override
    public boolean containsKey(String key) {
        return table[hash(key)].containsKey(key);
    }

    @Override
    public boolean containsValue(String value) {

        for (int i = 0; i < table.length; i++){
            if (table[i].containsValue(value) == true){
                return true;
            }
        }


        return false;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public int capacity() {
        return table.length; // Or the value of the current prime.
    }

    /**
     * Enlarges this hash table. At the very minimum, this method should increase the <b>capacity</b> of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the enlargement heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     * @see PrimeGenerator#getNextPrime()
     */
    public void enlarge() {
        int newPrime = primeGenerator.getNextPrime();

        KVPairList[] temp = table;//hold on to old table


        table = new KVPairList[newPrime];




    }

    /**
     * Shrinks this hash table. At the very minimum, this method should decrease the size of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the shrinking heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     *
     * @see PrimeGenerator#getPreviousPrime()
     */
    public void shrink(){
        int newPrime = primeGenerator.getPreviousPrime();

        KVPairList[] temp = table;//hold on to old table

        table = new KVPairList[newPrime];


    }
}
