import java.util.*;
import java.sql.*;

/**
 * A social network graph implementation using HashMaps to store user relationships.
 * Optimized for follower/following relationships between users.
 * Implements the Singleton pattern to ensure only one graph instance exists.
 */
public class Graph
{
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
    private Graph()
    {
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.userIndex = new HashMap<>();

        try
        {
            // First, load all users from the Users table
            String userQuery = "SELECT userID, username, email FROM Users";
            ResultSet resultSet = Database.getInstance().executeQuery(userQuery);
            
            // Create and load users
            while (resultSet.next())
            {
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
            
            // Now load all follower relationships from UserFollower table
            String followerQuery = "SELECT uf.userID, uf.followerID FROM UserFollower uf";
            resultSet = Database.getInstance().executeQuery(followerQuery);
            
            // Add each follower relationship
            while (resultSet.next())
            {
                int userId = resultSet.getInt("userID");
                int followerId = resultSet.getInt("followerID");
                
                // Get user objects from our index
                User user = getUserByKey(userId);
                User follower = getUserByKey(followerId);
                
                if (user != null && follower != null)
                {
                    // Add the follower relationship (follower follows user)
                    addFollower(follower, user);
                }
            }
            
        } catch (SQLException e)
        {
            System.err.println("Error loading social graph from database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to create a user object from database fields.
     * Creates a new User object with the data from the database.
     * 
     * @param userId User ID from the database
     * @param username Username from the database
     * @param email Email from the database
     * @return A new User object
     */
    private User createUserObject(int userId, String username, String email)
    {
        // Load genres for this user
        ArrayList<Genre.GenreType> genres = new ArrayList<>();
        try
        {
            // Query to get user's favorite genres
            String genreQuery = "SELECT genre FROM UserGenres WHERE userID = " + userId;
            ResultSet genreResultSet = Database.getInstance().executeQuery(genreQuery);
            
            while (genreResultSet.next())
            {
                try
                {
                    genres.add(Genre.GenreType.valueOf(genreResultSet.getString("genre")));
                } catch (IllegalArgumentException e)
                {
                    System.err.println("Invalid genre in database for user " + userId);
                }
            }
        } catch (SQLException e)
        {
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
    public static Graph getInstance()
    {
        if (instance == null)
        {
            instance = new Graph();
        }
        return instance;
    }
    
    /**
     * Adds a user to the graph
     * 
     * @param user the user to add
     */
    private void addUser(User user)
    {
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
    public boolean addFollower(User follower, User target)
    {
        // Add users if they don't exist
        addUser(follower);
        addUser(target);
        
        // Check if the relationship already exists
        if (!followers.get(target).contains(follower))
        {
            // Add the follower relationship
            followers.get(target).add(follower);  // target's followers include follower
            following.get(follower).add(target);  // follower is following target
            return true; // Relationship successfully added
        }
        return false; // Relationship already exists
    }
    
    /**
     * Removes a follower relationship between users
     * 
     * @param follower the user who is following
     * @param target the user being followed
     * @return true if the relationship was successfully removed, false if it did not exist
     */
    public boolean removeFollower(User follower, User target)
    {
        boolean removed = false;
        
        // Remove from target's followers list
        if (followers.containsKey(target))
        {
            if (followers.get(target).remove(follower))
            {
                removed = true;
            } 
        }
        
        // Remove from follower's following list
        if (following.containsKey(follower))
        {
            if (following.get(follower).remove(target))
            {
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
    private void indexUser(Object key, User user)
    {
        if (followers.containsKey(user))
        {
            userIndex.put(key, user);
        }
    }
    
    /**
     * Gets a user by its index key
     * 
     * @param key the key to look up (e.g., user ID)
     * @return the user if found, null otherwise
     */
    public User getUserByKey(Object key)
    {
        return userIndex.get(key);
    }
    
    /**
     * Gets a user by username
     * 
     * @param username the username to look up
     * @return the user if found, null otherwise
     */
    public User getUserByUsername(String username)
    {
        return userIndex.get(username);
    }
    
    /**
     * Gets all followers of a user
     * 
     * @param user the user
     * @return a list of all followers
     */
    public List<User> getFollowers(User user)
    {
        return followers.getOrDefault(user, new ArrayList<>());
    }

    /**
     * Gets all users that a user is following
     * 
     * @param user the user
     * @return a list of all users being followed
     */
    public List<User> getFollowing(User user)
    {
        return following.getOrDefault(user, new ArrayList<>());
    }
    
    /**
     * Checks if a user is following another user
     * 
     * @param follower the user who is following
     * @param target the user being followed
     * @return true if follower is following target, false otherwise
     */
    public boolean isFollowing(User follower, User target)
    {
        for (Map.Entry<User, List<User>> entry : following.entrySet())
        {
            // Use equals() to find the correct follower
            if (entry.getKey().equals(follower))
            {
                // Use contains() which will use the equals() method internally
                return entry.getValue().contains(target);
            }
        }
        return false;
    }
    
    /**
     * Gets the number of followers a user has
     * 
     * @param user the user
     * @return the number of followers
     */
    public int getFollowerCount(User user)
    {
        return followers.getOrDefault(user, Collections.emptyList()).size();
    }

    /**
     * Gets the number of users a user is following
     * 
     * @param user the user
     * @return the number of users being followed
     */
    public int getFollowingCount(User user)
    {
        return following.getOrDefault(user, Collections.emptyList()).size();
    }
    
    /**
     * DEBUGGING METHOD - Prints a graph representation of the social network.
     * This method is for development/debugging visualization only and not needed for
     * core functionality.
     * 
     * @return a formatted string showing all users with their followers and following
     */
    /*
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Social Network Graph:\n");
        
        // Create a set of all users from both maps
        Set<User> allUsers = new HashSet<>();
        allUsers.addAll(followers.keySet());
        allUsers.addAll(following.keySet());
        
        for (User user : allUsers)
        {
            sb.append("User '").append(user.getUserName()).append("' (ID: ")
              .append(user.getUserID()).append("):\n");
            
            // Get followers
            List<User> followersList = followers.get(user);
            sb.append("  Followers: [");
            if (followersList != null)
            {
                for (int i = 0; i < followersList.size(); i++)
                {
                    if (i > 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(followersList.get(i).getUserName());
                }
            }
            sb.append("]\n");
            
            // Get following
            List<User> followingList = following.get(user);
            sb.append("  Following: [");
            if (followingList != null)
            {
                for (int i = 0; i < followingList.size(); i++)
                {
                    if (i > 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(followingList.get(i).getUserName());
                }
            }
            sb.append("]\n");
        }
        
        return sb.toString();
    }
    */

    /**
     * Syncs the database with the graph structure.
     * This method exports the current state of the graph into the database.
     * It clears the existing relationships in the database and re-adds them based on the current graph state.
     */
    public void disconnect()
    {
        try
        {
            // Delete existing relationships
            String deleteQuery = "DELETE FROM UserFollower";
            Database.getInstance().executeUpdate(deleteQuery);
            
            // Iterate through all users in the graph
            for (Map.Entry<User, List<User>> entry : this.following.entrySet())
            {
                User currentUser = entry.getKey();
                List<User> followedUsers = entry.getValue();
                
                // For each user this user is following
                for (User followedUser : followedUsers)
                {
                    // Insert the relationship
                    String insertQuery = "INSERT INTO UserFollower (userID, followerID) VALUES (" +
                                         followedUser.getUserID() + ", " + currentUser.getUserID() + ")";
                    Database.getInstance().executeUpdate(insertQuery);
                }
            }
            
            System.out.println("Successfully synced graph relationships to database.");
            
        } catch (SQLException e)
        {
            System.err.println("Error syncing graph to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}