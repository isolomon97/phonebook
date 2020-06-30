package phonebook.hashes;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link QuadraticProbingHashTable} is an Openly Addressed {@link HashTable} which uses <b>Quadratic
 * Probing</b> as its collision resolution strategy. Quadratic Probing differs from <b>Linear</b> Probing
 * in that collisions are resolved by taking &quot; jumps &quot; on the hash table, the length of which
 * determined by an increasing polynomial factor. For example, during a key insertion which generates
 * several collisions, the first collision will be resolved by moving 1^2 + 1 = 2 positions over from
 * the originally hashed address (like Linear Probing), the second one will be resolved by moving
 * 2^2 + 2= 6 positions over from our hashed address, the third one by moving 3^2 + 3 = 12 positions over, etc.
 * </p>
 *
 * <p>By using this collision resolution technique, {@link QuadraticProbingHashTable} aims to get rid of the
 * &quot;key clustering &quot; problem that {@link LinearProbingHashTable} suffers from. Leaving more
 * space in between memory probes allows other keys to be inserted without many collisions. The tradeoff
 * is that, in doing so, {@link QuadraticProbingHashTable} sacrifices <em>cache locality</em>.</p>
 *
 * @author Isaac Solomon
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see LinearProbingHashTable
 * @see CollisionResolver
 */
public class QuadraticProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/

    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *               we want soft deletion, {@code false} otherwise.
     */
    public QuadraticProbingHashTable(boolean soft) {
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

    @Override
    public Probes put(String key, String value) {
        KVPair pair = new KVPair(key,value);
        int numProbes = 0;

        if (key == null || value == null) {
            throw new IllegalArgumentException("null argument");
        }

        if(softFlag == false){//hard deletions

            if ((float)size()/ table.length >= .5) {//exceeds threshold, increase capacity of table
                KVPair[] temp = table;//hold onto old table

                table = new KVPair[primeGenerator.getNextPrime()];//create bigger table

                numProbes += resize(temp, table);//resize and reinsert into bigger table

            }


            int index = hash(key);

            if (table[index] == null){//no collision, just insert
                table[index] = pair;
                numProbes++;
            }
            else{
                numProbes++;
                int numJumps = 1;//starts at 1
                boolean foundaHome = false;

                while (foundaHome == false){

                    int offSet = (numJumps*numJumps) + numJumps;
                    int newIndex = (index+offSet)%table.length;//modulo to wrap around

                    if (table[newIndex]==null){//found empty cell
                        table[newIndex] = pair;//insert
                        foundaHome = true;
                        numProbes++;
                    }
                    else{//keep searching
                        numJumps++;
                        numProbes++;

                        if (newIndex == index){//weve made it back to starting point, means theres no empty spot, failed insert
                            break;
                        }

                    }



                }

            }



        }
        else{//soft deletions

            if ((float)sizeWithTombstones()/ table.length >= .5) {//exceeds threshold, increase capacity of table
                KVPair[] temp = table;//hold onto old table

                table = new KVPair[primeGenerator.getNextPrime()];//create bigger table

                numProbes += resize(temp, table);//resize and reinsert into bigger table

            }


            int index = hash(key);

            if (table[index] == null){//no collision, just insert
                table[index] = pair;
                numProbes++;
            }
            else{
                numProbes++;
                int numJumps = 1;//starts at 1
                boolean foundaHome = false;

                while (foundaHome == false){

                    int offSet = (numJumps*numJumps) + numJumps;
                    int newIndex = (index+offSet)%table.length;//modulo to wrap around


                    if (table[newIndex] == null){
                        table[newIndex] = pair;//insert
                        foundaHome = true;
                        numProbes++;

                    }
                    else{//keep searching
                        numJumps++;
                        numProbes++;

                        if (newIndex == index){//weve made it back to starting point, means theres no empty spot, failed insert
                            break;
                        }

                    }



                }

            }





        }



        Probes num = new Probes(value, numProbes);
        return num;
    }

    public int resize(KVPair[] oldTable, KVPair[] newTable) {
        int probes = 0;

        for (int i = 0; i<oldTable.length; i++){
            if (oldTable[i] != null && oldTable[i] != TOMBSTONE){//something here

                KVPair temp = oldTable[i];


                int index = hash(temp.getKey());//starting index

                if (newTable[index] == null){//no collision, just insert
                    newTable[index] = temp;
                    probes++;
                }
                else{
                    probes++;
                    int numJumps = 1;//starts at 1
                    boolean foundaHome = false;

                    while (foundaHome == false){

                        int offSet = (numJumps*numJumps) + numJumps;
                        int newIndex = (index+offSet)%newTable.length;//modulo to wrap around

                        if (newTable[newIndex]==null){//found empty cell
                            newTable[newIndex] = temp;//insert
                            foundaHome = true;
                            probes++;
                            break;
                        }
                        else{//keep searching
                            numJumps++;
                            probes++;


                        }


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

        if (softFlag == false){//hard bois
            int index = hash(key);

            if (table[index] == null){//nothing there, fail
                Probes probes = new Probes(value, 1);
                return probes;

            }

            if (table[index].getKey() == key){//found on first try
                value = table[index].getValue();
                Probes probes = new Probes(value, 1);
                return probes;

            }
            else {//time to search
                numProbes++;

                boolean found = false;
                int numJumps = 1;//starts at 1

                while (found == false) {

                    int offSet = (numJumps*numJumps) + numJumps;
                    int newIndex = (index+offSet)%table.length;//modulo to wrap around
                    if (table[newIndex] != null) {//something there

                        if (table[newIndex].getKey() == key) {//found

                            value = table[newIndex].getValue();
                            numProbes++;
                            break;

                        } else {//keep looking
                            numJumps++;
                            numProbes++;


                        }

                    }
                    else if (table[newIndex] == null){//end of quadratic chain, didn't find
                        numProbes++;
                        break;

                    }


                }
            }

        }

        else{//soft bois
            int index = hash(key);

            if (table[index] == null){//nothing there, fail
                Probes probes = new Probes(value, 1);
                return probes;

            }

            if (table[index].getKey() == key){//found on first try
                value = table[index].getValue();
                Probes probes = new Probes(value, 1);
                return probes;

            }
            else {
                numProbes++;

                boolean found = false;
                int numJumps = 1;//starts at 1

                while (found == false){

                    int offSet = (numJumps*numJumps) + numJumps;
                    int newIndex = (index+offSet)%table.length;//modulo to wrap around

                    if (table[index] != null){
                        if (table[newIndex].getKey() == key) {//found

                            value = table[newIndex].getValue();
                            numProbes++;
                            break;

                        }
                        else {//keep looking
                            numJumps++;
                            numProbes++;


                        }


                    }
                    else if (table[newIndex] == null){//end of quadratic chain, didn't find
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
    public Probes remove(String key) {
        int numProbes = 0;
        String value = null;

        if (key == null){
            Probes probes = new Probes(null, 0);
            return probes;
        }

        if (softFlag == false){//hard deletions

            int index = hash(key);

            if (table[index] == null){
                Probes probes = new Probes(null, 1);
                return probes;
            }

            if (table[index].getKey() == key){//found
                value = table[index].getValue();//grab value
                numProbes++;
                table[index] = null;//set to null


                //reinsertion time
                KVPair[] temp = table;//hold onto old table
                table = new KVPair[primeGenerator.getCurrPrime()];//make new table of same size to reinsert into

                numProbes += resize(temp, table);




            }

            else{//search through collision chain
                numProbes++;
                boolean found = false;
                int numJumps = 1;//starts at 1

                while (found == false){
                    int offSet = (numJumps*numJumps) + numJumps;
                    int newIndex = (index+offSet)%table.length;//modulo to wrap around

                    if (table[newIndex] != null){

                        if (table[newIndex].getKey() == key){//found

                            value = table[newIndex].getValue();
                            numProbes++;
                            table[newIndex] = null;

                            found = true;

                            KVPair[] temp = table;//hold onto old table
                            table = new KVPair[primeGenerator.getCurrPrime()];//make new table of same size to reinsert into

                            numProbes += resize(temp, table);


                            break;


                        }
                        else{//keep looking
                            numJumps++;
                            numProbes++;


                        }

                    }
                    else if(table[newIndex] == null){
                        numProbes++;
                        break;
                    }



                }


            }



        }
        else{//soft serve ice cream deletes

            int index = hash(key);

            if (table[index] == null){
                Probes probes = new Probes(null, 1);
                return probes;
            }

            if (table[index].getKey() == key) {//found
                value = table[index].getValue();//grab value
                numProbes++;
                table[index] = TOMBSTONE;//set to null
            }
            else{//search through chain
                numProbes++;
                boolean found = false;
                int numJumps = 1;//starts at 1

                while (found == false){
                    int offSet = (numJumps*numJumps) + numJumps;
                    int newIndex = (index+offSet)%table.length;//modulo to wrap around

                    if (table[newIndex] != null){

                        if (table[newIndex].getKey() == key) {//found

                            value = table[newIndex].getValue();
                            numProbes++;
                            table[newIndex] = TOMBSTONE;
                            found = true;
                            break;
                        }
                        else{//keep looking
                            numJumps++;
                            numProbes++;


                        }


                    }
                    else if(table[newIndex] == null){
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
            if (table[i] != null) {
                if (table[i].getKey() == key) {
                    contains = true;
                    break;
                }
            }
        }


        return contains;
    }

    @Override
    public boolean containsValue(String value) {
        boolean contains = false;

        for (int i = 0; i<table.length; i++){
            if (table[i] != null) {
                if (table[i].getValue() == value) {
                    contains = true;
                    break;
                }
            }
        }


        return contains;
    }
    @Override
    public int size(){
        int size = 0;

        for (int i = 0; i<table.length; i++){
            if (table[i] != null){
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