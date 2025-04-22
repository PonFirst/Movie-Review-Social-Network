import java.util.*;

/**
 * A social network graph implementation using HashMaps to store user relationships.
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
     * Private constructor for Singleton pattern
     */
    private Graph() {
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.userIndex = new HashMap<>();
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
     * DEBUGGING METHOD - Prints a representation of the social network.
     * 
     * @return a formatted string showing all users with their followers and following
     */
    /*
    @Override
    public String printSocialNetworkStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("Social Network Graph:\n");
        
        for (Map.Entry<T, List<T>> entry : followers.entrySet()) {
            T user = entry.getKey();
            sb.append(user.toString()).append(":\n");
            sb.append("  Followers: ").append(entry.getValue().toString()).append("\n");
            sb.append("  Following: ").append(following.get(user).toString()).append("\n");
        }
        
        return sb.toString();
    }
    */

}