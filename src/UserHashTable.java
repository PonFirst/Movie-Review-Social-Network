import java.util.HashMap;
import java.util.Map;




// Singleton class to manage a hash table of users
public class UserHashTable {
    
    private static UserHashTable instance;
    private Map<Integer, User> userTable;

    // Private constructor to prevent instantiation
    private UserHashTable() {
        userTable = new HashMap<>();

        // Initialize the hash table with the users
        Database database = Database.getInstance();


    }



    // Method to get the singleton instance of UserHashTable
    public static UserHashTable getInstance() {
        if (instance == null) {
            instance = new UserHashTable();
        }
        return instance;
    }



    // Method to add a user to the hash table
    public void addUser(User user) {
        userTable.put(user.getUserID(), user);
    }

    // Method to get a user by ID from the hash table
    public User getUser(int userID) {
        return userTable.get(userID);
    }

    // Method to remove a user from the hash table
    public void removeUser(int userID) {
        userTable.remove(userID);
    }
}
