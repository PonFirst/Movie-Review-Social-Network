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
    private void addUser(User user) {
        followers.putIfAbsent(user, new ArrayList<>());
        following.putIfAbsent(user, new ArrayList<>());
    }
    
    /**
     * Adds a follower relationship (follower follows target)
     * 
     * @param follower the user who is following
     * @param target the user being followed
     * @return true if the relationship was added, false if it already existed
     */
    public boolean addFollower(User follower, User target) {
        // Add users if they don't exist
        addUser(follower);
        addUser(target);
        
        // Check if the relationship already exists
        if (!followers.get(target).contains(follower)) {
            // Add the follower relationship
            followers.get(target).add(follower);  // target's followers include follower
            following.get(follower).add(target);  // follower is following target
            return true; // Relationship successfully added
        }
        return false; // Relationship already exists
    }
    
    /**
     * Removes a user and all their relationships
     * 
     * @param user the user to remove
     * @return true if the user was successfully removed, false if the user did not exist
     */
    public boolean removeFollower(User follower, User target) {
        boolean removed = false;
        
        // Remove from target's followers list
        if (followers.containsKey(target)) {
            if (followers.get(target).remove(follower)) {
                removed = true;
            } 
        }
        
        // Remove from follower's following list
        if (following.containsKey(follower)) {
            if (following.get(follower).remove(target)) {
                removed = true;
            }
        }
        
        return removed;
    }
    
    /**
     * Indexes a user by a specific key for faster lookups
     * Useful for looking up users by ID or username
     * 
     * @param key the key to index by (e.g., user ID)
     * @param user the user to index
     */
    private void indexUser(Object key, User user) {
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
     * Gets a user by username
     * 
     * @param username the username to look up
     * @return the user if found, null otherwise
     */
    public User getUserByUsername(String username) {
        return userIndex.get(username);
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
     * @param follower the user who is following
     * @param target the user being followed
     * @return true if follower is following target, false otherwise
     */
    public boolean isFollowing(User follower, User target) {
        for (Map.Entry<User, List<User>> entry : following.entrySet()) {
            // Use equals() to find the correct follower
            if (entry.getKey().equals(follower)) {
                // Use contains() which will use the equals() method internally
                return entry.getValue().contains(target);
            }
        }
        return false;
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
     * Prints the latest review from users that the specified user is following.
     * 
     * @param user the user whose following list to check
     */
    public void printFollowingLatestReviews(User user) {
        if (!following.containsKey(user)) {
            return;
        }
        
        List<User> followedUsers = following.get(user);
        

        // Print the latest reviews from each followed user
        for (User followedUser : followedUsers) {
            Review latestReview = followedUser.getLatestReview();

        
        if (latestReview == null) {
            //System.out.println(followedUser.getUserName() + " has no reviews.");
            continue;
        }

            System.out.println("Latest review from " + followedUser.getUserName() + ":");
            System.out.print(latestReview);
        }
    }


    /**
     * Gets all users in the graph
     * 
     * @return a set of all users
     */
    private Set<User> getAllUsers() {
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
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Social Network Graph:\n");
        
        // Create a set of all users from both maps
        Set<User> allUsers = new HashSet<>();
        allUsers.addAll(followers.keySet());
        allUsers.addAll(following.keySet());
        
        for (User user : allUsers) {
            sb.append("User '").append(user.getUserName()).append("' (ID: ")
              .append(user.getUserID()).append("):\n");
            
            // Get followers
            List<User> followersList = followers.get(user);
            sb.append("  Followers: [");
            if (followersList != null) {
                for (int i = 0; i < followersList.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(followersList.get(i).getUserName());
                }
            }
            sb.append("]\n");
            
            // Get following
            List<User> followingList = following.get(user);
            sb.append("  Following: [");
            if (followingList != null) {
                for (int i = 0; i < followingList.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(followingList.get(i).getUserName());
                }
            }
            sb.append("]\n");
        }
        
        return sb.toString();
    }

    /**
     * Syncs the database with the graph structure.
     * This method exports the current state of the graph into the database.
     * It clears the existing relationships in the database and re-adds them based on the current graph state.
     */
    public void disconnect() {
        Connection conn = null;
        PreparedStatement deleteStatement = null;
        PreparedStatement insertStatement = null;
        
        try {
            // Get database connection
            conn = Database.getInstance().getConnection();
            
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);
            
            // Delete existing relationships
            String deleteQuery = "DELETE FROM UserFollower";
            deleteStatement = conn.prepareStatement(deleteQuery);
            deleteStatement.executeUpdate();
            
            // Prepare insert statement
            String insertQuery = "INSERT INTO UserFollower (userID, followerID) VALUES (?, ?)";
            insertStatement = conn.prepareStatement(insertQuery);
            
            // Iterate through all users in the graph
            for (Map.Entry<User, List<User>> entry : this.following.entrySet()) {
                User currentUser = entry.getKey();
                List<User> followedUsers = entry.getValue();
                
                // For each user this user is following
                for (User followedUser : followedUsers) {
                    // Insert the relationship
                    insertStatement.setInt(1, followedUser.getUserID());  // userID (being followed)
                    insertStatement.setInt(2, currentUser.getUserID());   // followerID
                    insertStatement.addBatch();
                }
            }
            
            // Execute batch insert
            insertStatement.executeBatch();
            
            // Commit the transaction
            conn.commit();
            
            System.out.println("Successfully synced graph relationships to database.");
            
        } catch (SQLException e) {
            // Rollback in case of any error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            
            System.err.println("Error syncing graph to database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Restore auto-commit and close resources
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
                
                if (deleteStatement != null) {
                    deleteStatement.close();
                }
                if (insertStatement != null) {
                    insertStatement.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

}