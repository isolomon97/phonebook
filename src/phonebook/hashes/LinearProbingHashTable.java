package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link LinearProbingHashTable} is an Openly Addressed {@link HashTable} implemented with <b>Linear Probing</b> as its
 * collision resolution strategy: every key collision is resolved by moving one address over. It is
 * the most famous collision resolution strategy, praised for its simplicity, theoretical properties
 * and cache locality. It <b>does</b>, however, suffer from the &quot; clustering &quot; problem:
 * collision resolutions tend to cluster collision chains locally, making it hard for new keys to be
 * inserted without collisions. {@link QuadraticProbingHashTable} is a {@link HashTable} that
 * tries to avoid this problem, albeit sacrificing cache locality.</p>
 *
 * @author Isaac Solomon
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see QuadraticProbingHashTable
 * @see CollisionResolver
 */
public class LinearProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/





    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     *
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *             we want soft deletion, {@code false} otherwise.
     */
    public LinearProbingHashTable(boolean soft) {
        primeGenerator = new PrimeGenerator();
        table = new KVPair[primeGenerator.getCurrPrime()];


        if (soft == true){
            softFlag= true;
        }
        else{
            softFlag = false;
        }
        count = 0;

    }

    /**
     * Inserts the pair &lt;key, value&gt; into this. The container should <b>not</b> allow for {@code null}
     * keys and values, and we <b>will</b> test if you are throwing a {@link IllegalArgumentException} from your code
     * if this method is given {@code null} arguments! It is important that we establish that no {@code null} entries
     * can exist in our database because the semantics of {@link #get(String)} and {@link #remove(String)} are that they
     * return {@code null} if, and only if, their key parameter is {@code null}. This method is expected to run in <em>amortized
     * constant time</em>.
     * <p>
     * Instances of {@link LinearProbingHashTable} will follow the writeup's guidelines about how to internally resize
     * the hash table when the capacity exceeds 50&#37;
     *
     * @param key   The record's key.
     * @param value The record's value.
     * @return The {@link phonebook.utils.Probes} with the value added and the number of probes it makes.
     * @throws IllegalArgumentException if either argument is {@code null}.
     */
    @Override
    public Probes put(String key, String value) {
        KVPair pair = new KVPair(key,value);
        int numProbes = 0;

        if (key == null || value == null) {
            throw new IllegalArgumentException("null argument");
        }

        if (softFlag == false) {//hard deletions


            if ((float)size()/ table.length >= .5) {//exceeds threshold, increase capacity of table
                KVPair[] temp = table;//hold onto old table

                table = new KVPair[primeGenerator.getNextPrime()];//create bigger table

                numProbes += resizeHard(temp, table);//resize and reinsert into bigger table

            }

            if (table[hash(key)] == null) {//no collision, cell is empty
                table[hash(key)] = pair;//simply insert our pair
                numProbes++;

            } else {//collision, look for empty cell
                boolean foundaHome = false;

                int index = hash(key);



                while (foundaHome == false) {//keep searching
                    if (table[index] == null) {//found cell, insert
                        table[index] = pair;
                        foundaHome = true;
                        numProbes++;


                    } else {
                        index++;//increment index
                        numProbes++;

                        if (index >= table.length) {//if index exceeds length, need to wrap around to start
                            index = 0;//so set to 0
                        }
                    }

                }

            }
        }
        else{//soft deletion

            int index = hash(key);


            if ((float)sizeWithTombstones()/ table.length >= .5){//resizing
                KVPair[] temp = table;//hold onto old table

                table = new KVPair[primeGenerator.getNextPrime()];//create bigger table

                numProbes += resizeSoft(temp, table);//resize and reinsert into bigger table


            }



            if (table[index] == null) {//no collision, cell is empty
                table[index] = pair;//simply insert our pair
                numProbes++;

            }
            else{
                boolean foundaHome = false;

                while (foundaHome == false){
                    if (table[index] == null){//found cell with neither tombstones nor entries present
                        table[index] = pair;
                        foundaHome = true;
                        numProbes++;

                    }
                    else{
                        index++;//increment index
                        numProbes++;

                        if (index >= table.length) {//if index exceeds length, need to wrap around to start
                            index = 0;//so set to 0
                        }
                    }



                }

            }




        }


        count++;
        Probes num = new Probes(value, numProbes);
        return num;
    }

    public int resizeSoft(KVPair[] oldTable, KVPair[] newTable){
        int probes = 0;


        for (int i = 0; i<oldTable.length; i++){
                if (oldTable[i] != null && oldTable[i] != TOMBSTONE){//only insert real elements, not tombstones
                    KVPair temp = oldTable[i];
                    probes++;

                    int index = hash(temp.getKey());//starting index

                    boolean foundHome = false;

                    while (foundHome == false){
                        if (newTable[index] == null){//found a home
                            newTable[index] = temp;
                            foundHome = true;

                        }
                        else{//keep looking
                            index++;

                            if (index >= newTable.length) {
                                index = 0;
                            }
                            probes++;
                        }

                    }


                }
                probes++;

        }

        return probes;
    }

    public int resizeHard(KVPair[] oldTable, KVPair[] newTable) {
        int probes = 0;

        for (int i = 0; i < oldTable.length; i++) {
            if (oldTable[i] != null) {//something there
                KVPair temp = oldTable[i];
                probes++;
                int index = hash(temp.getKey());//starting index

                boolean foundHome = false;

                while (foundHome == false) {
                    if (newTable[index] == null) {//found empty cell
                        newTable[index] = temp;
                        foundHome = true;
                    }
                    else {//keep looking
                        index++;

                        if (index >= newTable.length) {
                            index = 0;
                        }
                        probes++;
                    }

                }

            }
            probes++;

        }
        return probes;
    }







    @Override
    public Probes get(String key) {
        int numProbes = 0;
        String value = null;

        if (key == null){
            Probes probes = new Probes(null, 0);
            return probes;
        }

        if (softFlag == false) {//hard deletion

            int index = hash(key);



            boolean found = false;

            while (found == false) {
                if (table[index] != null) {
                    if (table[index].getKey() == key) {//found
                        value = table[index].getValue();
                        numProbes++;
                        break;
                    } else if ((table[index].getKey() != key) && (table[index].getKey() != null)) {//not found but still in collision chain
                        index++;//increment index
                        numProbes++;

                        if (index >= table.length) {//if index exceeds length, need to wrap around to start
                            index = 0;//so set to 0
                        }


                    }
                }
                else if (table[index] == null) {//end of collision chain, didn't find
                    numProbes++;
                    break;

                }
            }
        }
        else{//soft deletion

            int index = hash(key);

            boolean found = false;

            while (found == false){
                if (table[index] != null){
                    if (table[index].getKey() == key){
                        value = table[index].getValue();
                        numProbes++;
                        break;

                    }
                    else if(table[index] == TOMBSTONE || table[index].getKey() != key){//tombstone or not what we want, keep looking
                        index++;//increment index
                        numProbes++;

                        if (index >= table.length) {//if index exceeds length, need to wrap around to start
                            index = 0;//so set to 0
                        }

                    }

                }
                else if (table[index] == null) {//end of collision chain, didn't find
                    numProbes++;
                    break;

                }

            }


        }


        Probes probes = new Probes(value, numProbes);
        return probes;
    }


    /**
     * <b>Return</b> the value associated with key in the {@link HashTable}, and <b>remove</b> the {@link phonebook.utils.KVPair} from the table.
     * If key does not exist in the database
     * or if key = {@code null}, this method returns {@code null}. This method is expected to run in <em>amortized constant time</em>.
     *
     * @param key The key to search for.
     * @return The {@link phonebook.utils.Probes} with associated value and the number of probe used. If the key is {@code null}, return value {@code null}
     * and 0 as number of probes; if the key doesn't exist in the database, return {@code null} and the number of probes used.
     */
    @Override
    public Probes remove(String key) {
        int numProbes = 0;
        String value = null;

        if (key == null){
            Probes probes = new Probes(null, 0);
            return probes;
        }

        if (softFlag == false) {//hard deletion


            int index = hash(key);

            if (table[index] == null){//nothing there
                Probes probes = new Probes(null, 1);
                return probes;

            }


            if (table[index].getKey() == key) {//found
                value = table[index].getValue();//grab value
                numProbes++;
                table[index] = null;//set to null
                count--;


                index++;//increment before reinserting cluster

                if(index == table.length){
                    index = 0;
                }
                int count = index;


                if (table[index] == null){//no cluster, just increment probes
                    numProbes++;

                }
                else {
                    while (table[count] != null) {//reinsert stuff in cluster
                        numProbes++;
                        KVPair temp = table[count];
                        table[count] = null;

                        numProbes += put(temp.getKey(), temp.getValue()).getProbes();
                        count++;

                        if (count >= table.length) {
                            count = 0;
                        }

                    }
                    numProbes++;
                }


            }
            else {//search through collision chain
                boolean found = false;

                while (found == false) {
                    if (table[index] != null) {//something there
                        if (table[index].getKey() == key) {//found
                            value = table[index].getValue();
                            numProbes++;
                            table[index] = null;
                            count--;

                            found = true;

                            index++;//increment before reinserting cluster

                            if(index == table.length){
                                index = 0;
                            }
                            int count = index;


                            if (table[index] == null){//no cluster, just increment probes
                                numProbes++;

                            }
                            else {
                                while (table[count] != null) {//reinsert stuff in cluster
                                    KVPair temp = table[count];
                                    table[count] = null;

                                    numProbes += put(temp.getKey(), temp.getValue()).getProbes();
                                    count++;

                                    if (count >= table.length) {
                                        count = 0;
                                    }

                                }
                            }



                        } else if ((table[index].getKey() != key) && (table[index] != null)) {//keep looking
                            index++;
                            numProbes++;

                            if (index >= table.length) {
                                index = 0;//reset index
                            }


                        }
                    }
                    else if (table[index] == null) {
                        numProbes++;
                        break;
                    }


                }

            }
        }

        else{//soft deletion

            int index = hash(key);

            if (table[index] == null){//nothing there
                Probes probes = new Probes(null, 1);
                return probes;

            }

            if (table[index].getKey() == key) {//found
                value = table[index].getValue();//grab value
                numProbes++;
                table[index] = TOMBSTONE;//set to tombstone
                count--;

            }
            else{
                boolean found = false;

                while (found == false) {
                    if (table[index] != null) {//something there
                        if (table[index].getKey() == key) {//found
                            value = table[index].getValue();
                            numProbes++;
                            table[index] = TOMBSTONE;
                            count--;

                            found = true;



                        } else if ((table[index].getKey() != key) && (table[index] != null)) {//keep looking
                            index++;
                            numProbes++;

                            if (index >= table.length) {
                                index = 0;//reset index
                            }


                        }
                    }
                    else if (table[index] == null) {
                        numProbes++;
                        break;
                    }


                }

            }



        }




        Probes probes = new Probes(value, numProbes);
        return probes;
    }





    @Override
    public boolean containsKey(String key) {
        boolean contains = false;

        for (int i = 0; i<table.length; i++){
            if (table[i].getKey() == key){
                contains = true;
                break;
            }
        }


        return contains;
    }

    @Override
    public boolean containsValue(String value) {
        boolean contains = false;

        for (int i = 0; i<table.length; i++){
            if (table[i].getValue() == value){
                contains = true;
                break;
            }
        }


        return contains;
    }

    @Override
    public int size() {
        int size = 0;

        for (int i = 0; i<table.length; i++){
            if (table[i] != null && table[i] != TOMBSTONE){
                size++;
            }
        }
        count = size;
        return size;
    }

    public int sizeWithTombstones(){
        int size = 0;

        for (int i = 0; i<table.length; i++){
            if (table[i] != null){
                size++;
            }
        }
        count = size;
        return size;

    }

    @Override
    public int capacity() {
        return table.length;
    }
}
