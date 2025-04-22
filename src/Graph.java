import java.util.*;
import java.sql.*;

/**
 * A social network graph implementation using HashMaps to store user relationships.
 * Optimized for follower/following relationships between users.
 */
public class Graph {
    // Singleton instance
    private static Graph instance = null;
    
    // Maps each user to their followers
    private final Map<User, List<User>> followers;
    // Maps each user to users they are following
    private final Map<User, List<User>> following;
    // Secondary index for faster user lookups
    private final Map<Object, User> userIndex;
    
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
            
            // Create and load users
            while (resultSet.next()) {
                int userId = resultSet.getInt("userID");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                
                // Create user object with additional query to load genres
                User user = createUserObject(userId, username, email);
                
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
                User user = getUserByKey(userId);
                User follower = getUserByKey(followerId);
                
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
    private User createUserObject(int userId, String username, String email) {
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
        // Note: We're setting password to null since we don't load it from the database
        return new User(userId, username, email, null, genres);
    }
    
    /**
     * Gets the singleton instance of the graph
     * 
     * @return the singleton instance
     */
    public static Graph getInstance() {
        if (instance == null) {
            instance = new Graph();
        }
        return instance;
    }
    
    /**
     * Adds a user to the graph
     * 
     * @param user the user to add
     */
    public void addUser(User user) {
        followers.putIfAbsent(user, new ArrayList<>());
        following.putIfAbsent(user, new ArrayList<>());
    }
    
    /**
     * Adds a follower relationship (follower follows target)
     * 
     * @param follower the user who is following
     * @param target the user being followed
     */
    public void addFollower(User follower, User target) {
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
    public void removeFollower(User follower, User target) {
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
    public void removeUser(User user) {
        // Remove user from all following lists
        for (List<User> userFollowers : followers.values()) {
            userFollowers.remove(user);
        }
        
        // Remove user from all follower lists
        for (List<User> userFollowing : following.values()) {
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
    public void indexUser(Object key, User user) {
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
    public User getUserByKey(Object key) {
        return userIndex.get(key);
    }
    
    /**
     * Gets a user if it exists in the graph
     * 
     * @param user the user to retrieve
     * @return the user object if found, null otherwise
     */
    public User getUser(User user) {
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
    public boolean hasUser(User user) {
        return followers.containsKey(user);
    }
    
    /**
     * Gets all followers of a user
     * 
     * @param user the user
     * @return a list of all followers
     */
    public List<User> getFollowers(User user) {
        return followers.getOrDefault(user, new ArrayList<>());
    }
    
    /**
     * Gets all users that a user is following
     * 
     * @param user the user
     * @return a list of all users being followed
     */
    public List<User> getFollowing(User user) {
        return following.getOrDefault(user, new ArrayList<>());
    }
    
    /**
     * Checks if a user is following another user
     * 
     * @param follower the potential follower
     * @param target the potential target
     * @return true if follower is following target, false otherwise
     */
    public boolean isFollowing(User follower, User target) {
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
    public List<User> getFriendsOfFriendsNotConnected(User user) {
        if (!following.containsKey(user)) {
            return new ArrayList<>();
        }
        
        // Get all users that the specified user is following
        List<User> userFollowing = following.get(user);
        
        // Create a set for faster lookup
        Set<User> result = new HashSet<>();
        
        // For each user the specified user follows
        for (User friend : userFollowing) {
            if (following.containsKey(friend)) {
                // Get the users that this friend follows
                List<User> friendFollowing = following.get(friend);
                
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
    public Set<User> getAllUsers() {
        return followers.keySet();
    }
    
    /**
     * Gets the number of followers a user has
     * 
     * @param user the user
     * @return the number of followers
     */
    public int getFollowerCount(User user) {
        return followers.getOrDefault(user, Collections.emptyList()).size();
    }
    
    /**
     * Gets the number of users a user is following
     * 
     * @param user the user
     * @return the number of users being followed
     */
    public int getFollowingCount(User user) {
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
        
        for (Map.Entry<User, List<User>> entry : followers.entrySet()) {
            User user = entry.getKey();
            // Print username instead of toString()
            sb.append("User '").append(user.getUserName()).append("' (ID: ")
              .append(user.getUserID()).append("):\n");
            
            // Use getFollowers method instead of direct map access
            List<User> followersList = getFollowers(user);
            sb.append("  Followers: [");
            for (int i = 0; i < followersList.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(followersList.get(i).getUserName());
            }
            sb.append("]\n");
            
            // Use getFollowing method for consistency
            List<User> followingList = getFollowing(user);
            sb.append("  Following: [");
            for (int i = 0; i < followingList.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(followingList.get(i).getUserName());
            }
            sb.append("]\n");
        }
        
        return sb.toString();
    }
}