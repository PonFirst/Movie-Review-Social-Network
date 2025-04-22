import java.util.*;
import java.sql.*;

/**
 * A social network graph implementation using HashMaps to store user relationships.
 * Optimized for follower/following relationships between users.
 * 
 * @param <T> Type of user vertices stored in the graph
 */
public class Graph<T> {
    // Singleton instance
    private static Graph instance = null;
    
    // Maps each user to their followers
    private final Map<T, List<T>> followers;
    // Maps each user to users they are following
    private final Map<T, List<T>> following;
    // Secondary index for faster user lookups
    private final Map<Object, T> userIndex;
    
    /**
     * Private constructor for Singleton pattern.
     * Initializes the graph and loads user relationships from the database.
     */
    private Graph() {
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.userIndex = new HashMap<>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Get database connection
            connection = Database.getInstance().getConnection();
            
            // First, load all users from the Users table
            statement = connection.prepareStatement(
                "SELECT userID, username, email FROM Users"
            );
            resultSet = statement.executeQuery();
            
            // Assuming T is a User class with these fields
            while (resultSet.next()) {
                int userId = resultSet.getInt("userID");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                
                // Create user object with additional query to load genres
                T user = createUserObject(userId, username, email);
                
                // Add user to graph
                addUser(user);
                
                // Index user by ID and username for quick lookups
                indexUser(userId, user);
                indexUser(username, user);
            }
            
            // Close resources
            resultSet.close();
            statement.close();
            
            // Now load all follower relationships from UserFollower table
            statement = connection.prepareStatement(
                "SELECT uf.userID, uf.followerID " +
                "FROM UserFollower uf"
            );
            resultSet = statement.executeQuery();
            
            // Add each follower relationship
            while (resultSet.next()) {
                int userId = resultSet.getInt("userID");
                int followerId = resultSet.getInt("followerID");
                
                // Get user objects from our index
                T user = getUserByKey(userId);
                T follower = getUserByKey(followerId);
                
                if (user != null && follower != null) {
                    // Add the follower relationship (follower follows user)
                    addFollower(follower, user);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading social graph from database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources in reverse order
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                // Note: We typically don't close the connection here as it may be reused
            } catch (SQLException e) {
                System.err.println("Error closing database resources: " + e.getMessage());
            }
        }

        // Print the social network structure for debugging
        System.out.println(printSocialNetworkStructure());
    }
    
    /**
     * Helper method to create a user object from database fields.
     * Creates a new User object with the data from the database.
     */
    @SuppressWarnings("unchecked")
    private T createUserObject(int userId, String username, String email) {
        // Load genres for this user
        ArrayList<Genre.GenreType> genres = new ArrayList<>();
        try {
            Connection connection = Database.getInstance().getConnection();
            String genreQuery = "SELECT genre FROM UserGenres WHERE userID = ?";
            try (PreparedStatement genreStatement = connection.prepareStatement(genreQuery)) {
                genreStatement.setInt(1, userId);
                ResultSet genreResultSet = genreStatement.executeQuery();
                while (genreResultSet.next()) {
                    try {
                        genres.add(Genre.GenreType.valueOf(genreResultSet.getString("genre")));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid genre in database for user " + userId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading genres for user " + userId + ": " + e.getMessage());
        }
        
        // Create a new User object with the provided data
        // Note: We're setting password to null since we don't load it from the database in this context
        User user = new User(userId, username, email, null, genres);
        
        // Cast it to type T and return
        return (T) user;
    }
    
    /**
     * Gets the singleton instance of the graph
     * 
     * @param <E> Type of user vertices stored in the graph
     * @return the singleton instance
     */
    @SuppressWarnings("unchecked")
    public static <E> Graph<E> getInstance() {
        if (instance == null) {
            instance = new Graph<E>();
        }
        return (Graph<E>) instance;
    }
    
    /**
     * Adds a user to the graph
     * 
     * @param user the user to add
     */
    public void addUser(T user) {
        followers.putIfAbsent(user, new ArrayList<>());
        following.putIfAbsent(user, new ArrayList<>());
    }
    
    /**
     * Adds a follower relationship (follower follows target)
     * 
     * @param follower the user who is following
     * @param target the user being followed
     */
    public void addFollower(T follower, T target) {
        // Add users if they don't exist
        addUser(follower);
        addUser(target);
        
        // Add the follower relationship
        followers.get(target).add(follower);  // target's followers include follower
        following.get(follower).add(target);  // follower is following target
    }
    
    /**
     * Removes a follower relationship
     * 
     * @param follower the user who is following
     * @param target the user being followed
     */
    public void removeFollower(T follower, T target) {
        if (followers.containsKey(target)) {
            followers.get(target).remove(follower);
        }
        
        if (following.containsKey(follower)) {
            following.get(follower).remove(target);
        }
    }
    
    /**
     * Removes a user and all their relationships
     * 
     * @param user the user to remove
     */
    public void removeUser(T user) {
        // Remove user from all following lists
        for (List<T> userFollowers : followers.values()) {
            userFollowers.remove(user);
        }
        
        // Remove user from all follower lists
        for (List<T> userFollowing : following.values()) {
            userFollowing.remove(user);
        }
        
        // Remove the user
        followers.remove(user);
        following.remove(user);
        
        // Remove from index if present
        userIndex.values().remove(user);
    }
    
    /**
     * Indexes a user by a specific key for faster lookups
     * Useful for looking up users by ID or username
     * 
     * @param key the key to index by (e.g., user ID)
     * @param user the user to index
     */
    public void indexUser(Object key, T user) {
        if (followers.containsKey(user)) {
            userIndex.put(key, user);
        }
    }
    
    /**
     * Gets a user by its index key
     * 
     * @param key the key to look up (e.g., user ID)
     * @return the user if found, null otherwise
     */
    public T getUserByKey(Object key) {
        return userIndex.get(key);
    }
    
    /**
     * Gets a user if it exists in the graph
     * 
     * @param user the user to retrieve
     * @return the user object if found, null otherwise
     */
    public T getUser(T user) {
        if (followers.containsKey(user)) {
            return user;
        }
        return null;
    }
    
    /**
     * Checks if the graph contains a user
     * 
     * @param user the user to check
     * @return true if the user exists, false otherwise
     */
    public boolean hasUser(T user) {
        return followers.containsKey(user);
    }
    
    /**
     * Gets all followers of a user
     * 
     * @param user the user
     * @return a list of all followers
     */
    public List<T> getFollowers(T user) {
        return followers.getOrDefault(user, new ArrayList<>());
    }
    
    /**
     * Gets all users that a user is following
     * 
     * @param user the user
     * @return a list of all users being followed
     */
    public List<T> getFollowing(T user) {
        return following.getOrDefault(user, new ArrayList<>());
    }
    
    /**
     * Checks if a user is following another user
     * 
     * @param follower the potential follower
     * @param target the potential target
     * @return true if follower is following target, false otherwise
     */
    public boolean isFollowing(T follower, T target) {
        return following.containsKey(follower) && 
               following.get(follower).contains(target);
    }
    
    /**
     * Gets a list of users who are followed by users that the specified user follows,
     * but who are not directly followed by the specified user.
     * These are essentially "friends of friends" who have no direct connection.
     * 
     * @param user the user for whom to find potential connections
     * @return a list of users who are followed by user's following but not by user directly
     */
    public List<T> getFriendsOfFriendsNotConnected(T user) {
        if (!following.containsKey(user)) {
            return new ArrayList<>();
        }
        
        // Get all users that the specified user is following
        List<T> userFollowing = following.get(user);
        
        // Create a set for faster lookup
        Set<T> result = new HashSet<>();
        
        // For each user the specified user follows
        for (T friend : userFollowing) {
            if (following.containsKey(friend)) {
                // Get the users that this friend follows
                List<T> friendFollowing = following.get(friend);
                
                // Add all the friend's following to our result set
                result.addAll(friendFollowing);
            }
        }
        
        // Remove the original user from the result (if present)
        result.remove(user);
        
        // Remove users that the original user is already following
        result.removeAll(userFollowing);
        
        // Convert set back to list and return
        return new ArrayList<>(result);
    }
    
    /**
     * Gets all users in the graph
     * 
     * @return a set of all users
     */
    public Set<T> getAllUsers() {
        return followers.keySet();
    }
    
    /**
     * Gets the number of followers a user has
     * 
     * @param user the user
     * @return the number of followers
     */
    public int getFollowerCount(T user) {
        return followers.getOrDefault(user, Collections.emptyList()).size();
    }
    
    /**
     * Gets the number of users a user is following
     * 
     * @param user the user
     * @return the number of users being followed
     */
    public int getFollowingCount(T user) {
        return following.getOrDefault(user, Collections.emptyList()).size();
    }
    
    /**
     * DEBUGGING METHOD - Prints a human-readable representation of the social network.
     * This method is for development/debugging visualization only and not needed for
     * core functionality.
     * 
     * @return a formatted string showing all users with their followers and following
     */

     public String printSocialNetworkStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("Social Network Graph:\n");
        
        for (Map.Entry<T, List<T>> entry : followers.entrySet()) {
            T user = entry.getKey();
            // Print username instead of toString()
            sb.append("User '").append(((User)user).getUserName()).append("' (ID: ")
              .append(((User)user).getUserID()).append("):\n");
            
            // Use getFollowers method instead of direct map access
            List<T> followersList = getFollowers(user);
            sb.append("  Followers: [");
            for (int i = 0; i < followersList.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(((User)followersList.get(i)).getUserName());
            }
            sb.append("]\n");
            
            // Use getFollowing method for consistency
            List<T> followingList = getFollowing(user);
            sb.append("  Following: [");
            for (int i = 0; i < followingList.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(((User)followingList.get(i)).getUserName());
            }
            sb.append("]\n");
        }
        
        return sb.toString();
    }

    

}