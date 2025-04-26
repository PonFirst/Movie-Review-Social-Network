import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class UserGraphManager{
    // Static instance of the class
    private static UserGraphManager instance;

    // Private constructor to prevent instantiation
    private UserGraphManager() {
    }

    // Public method to provide access to the single instance
    public static UserGraphManager getInstance() {
        if (instance == null) {
            synchronized (UserGraphManager.class) {
                if (instance == null) {
                    instance = new UserGraphManager();
                }
            }
        }
        return instance;
    }

    public void followUser(Scanner scanner)
    {
        System.out.print("Enter the username of the user you want to follow: ");
        String username = scanner.nextLine();
    
        User userToFollow = Graph.getInstance().getUserByUsername(username);
        User currentUser = AuthenticationManager.getInstance().getCurrentUser();


        if (userToFollow == null) {
            System.out.println("User not found.");
            return;
        }
    
        if (currentUser.equals(userToFollow)) {
            System.out.println("You cannot follow yourself.");
            return;
        }
    
        // Use Graph's method to check if already following
        if (Graph.getInstance().isFollowing(currentUser, userToFollow)) {
            System.out.println("You are already following " + userToFollow.getUserName() + ".");
            return;
        }
    
        userToFollow.displayProfile();
        System.out.print("\nDo you want to follow this user? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if (choice.equals("y")) {
            // Use Graph's method to add follower
            Graph.getInstance().addFollower(currentUser, userToFollow);
            System.out.println("You are now following " + userToFollow.getUserName() + ".");
        } else {
            System.out.println("You chose not to follow " + userToFollow.getUserName() + ".");
        }
    }
    

    public void unfollowUser(Scanner scanner)
    {
        System.out.print("Enter the username of the user you want to unfollow: ");
        String username = scanner.nextLine();
        System.out.println();
    
        User userToUnfollow = Graph.getInstance().getUserByUsername(username);
        User currentUser = AuthenticationManager.getInstance().getCurrentUser();
    
        if (userToUnfollow == null) {
            System.out.println("User not found.");
            return;
        }
    
        if (currentUser.equals(userToUnfollow)) {
            System.out.println("You cannot unfollow yourself.");
            return;
        }
    
        // Check if the user is currently following the target
        if (!Graph.getInstance().isFollowing(currentUser, userToUnfollow)) {
            System.out.println("You are not following " + userToUnfollow.getUserName() + ".");
            return;
        }
    
        userToUnfollow.displayProfile();
        System.out.print("\nDo you want to unfollow this user? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        
        if (choice.equals("y")) {
            // Use Graph's method to remove follower
            boolean unfollowSuccessful = Graph.getInstance().removeFollower(currentUser, userToUnfollow);
            
            if (unfollowSuccessful) {
                System.out.println("You have unfollowed " + userToUnfollow.getUserName() + ".");
            } else {
                System.out.println("Failed to unfollow " + userToUnfollow.getUserName() + ".");
            }
        } else {
            System.out.println("You chose not to unfollow " + userToUnfollow.getUserName() + ".");
        }
    }


    // Display a list of the latest reviews made by the people the user follows
    public void displayLatestReviews()
    {
        User currentUser = AuthenticationManager.getInstance().getCurrentUser();
        Graph.getInstance().printFollowingLatestReviews(currentUser);
    
    }



/**
 * Recommends users to follow based on multiple criteria in priority order:
 * 1. Users whose latest reviews are on movies you've liked
 * 2. Users whose latest reviews are on movies in your favorite genres
 * 3. Users who are followed by users you follow (friends of friends)
 */
public void followRecomendations() {
    User currentUser = AuthenticationManager.getInstance().getCurrentUser();
    if (currentUser == null) {
        System.out.println("You need to be logged in to see recommendations.");
        return;
    }
    
    // Set to track unique recommended users to avoid duplicates
    Set<User> uniqueRecommendations = new HashSet<>();
    
    // 1. First find users by liked reviews (highest priority)
    ArrayList<User> likedReviewUsers = findUsersByLikedReviews(currentUser);
    for (User user : likedReviewUsers) {
        uniqueRecommendations.add(user);
    }
    
    // 2. Then fill in with users who review movies in your favorite genres
    if (uniqueRecommendations.size() < 5) {
        ArrayList<User> genreUsers = findUsersByFavoriteGenres(currentUser);
        for (User user : genreUsers) {
            // Only add if we haven't reached 5 recommendations yet
            if (uniqueRecommendations.size() < 5) {
                uniqueRecommendations.add(user);
            } else {
                break;
            }
        }
    }
    
    // 3. Finally, fill in with friends of friends
    if (uniqueRecommendations.size() < 5) {
        List<User> friendsOfFriends = Graph.getInstance().getFriendsOfFriendsNotConnected(currentUser);
        for (User user : friendsOfFriends) {
            // Only add if we haven't reached 5 recommendations yet
            if (uniqueRecommendations.size() < 5) {
                uniqueRecommendations.add(user);
            } else {
                break;
            }
        }
    }
    
    // Convert to list for ordered display
    List<User> recommendedUsers = new ArrayList<>(uniqueRecommendations);
    
    // Check if we found any recommendations
    if (recommendedUsers.isEmpty()) {
        System.out.println("No user recommendations available at this time.");
        return;
    }
    
    // Print the recommendation source info once
    System.out.println("\n--- Recommended Users to Follow ---");
    System.out.println("(Based on your liked reviews, favorite genres, and network connections)");
    
    // Display the recommended users with their details
    for (int i = 0; i < recommendedUsers.size(); i++) {
        User user = recommendedUsers.get(i);
        
        System.out.println("\n" + (i+1) + ". " + user.getUserName());
        
        // Show their favorite genres
        System.out.print("   Favorite Genres: ");
        List<Genre.GenreType> genres = user.getFavoriteGenres();
        if (genres.isEmpty()) {
            System.out.print("None specified");
        } else {
            for (int j = 0; j < genres.size(); j++) {
                System.out.print(genres.get(j).toString().replace("_", " "));
                if (j < genres.size() - 1) System.out.print(", ");
            }
        }
        System.out.println();
        
        // Show their latest review if they have one
        Review latestReview = user.getLatestReview();
        if (latestReview != null) {
            System.out.println("   Latest Review:");
            System.out.print("   ");
            System.out.print(latestReview); // Using the Review's toString() method that includes truncation
        } else {
            System.out.println("   No reviews yet");
        }
    }
}


/*
    public void followRecomendations() {
        User currentUser = AuthenticationManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.out.println("You need to be logged in to see recommendations.");
            return;
        }
    
        // Get recommended users based on the current user's favorite genres
        List<User> friendsOfFriends = Graph.getInstance().getFriendsOfFriendsNotConnected(currentUser);

        // Check if friendsOfFriends is empty
        if (friendsOfFriends.isEmpty()) {
            System.out.println("No friends of friends available for recommendations.");
            return;
        }

        // Print the recommended users
        System.out.println("Recommended users to follow based on your friends of friends:");
        for (User user : friendsOfFriends) {
            System.out.println("Username: " + user.getUserName());
            System.out.println("Genres: " + user.getFavoriteGenres());
            System.out.println("Latest review:\n" + user.getLatestReview());
        }
    }
*/


















    /**
     * Finds users whose latest reviews are on the same movies you recently liked
     * using string concatenation for queries
     * @param currentUser The currently logged in user
     * @return ArrayList of up to 5 users whose reviews match your recently liked content
     */
    public ArrayList<User> findUsersByLikedReviews(User currentUser) {
        ArrayList<User> recommendedUsers = new ArrayList<>();
        
        try {
            // 1. Find the 5 most recent reviews the current user has liked
            String likedReviewsQuery = 
                "SELECT r.movieID " +
                "FROM Likes l " +
                "JOIN Reviews r ON l.reviewID = r.reviewID " +
                "WHERE l.userID = " + currentUser.getUserID() + " " +
                "ORDER BY l.likeID DESC " + // Using likeID as a chronological identifier takign advantage of auto-incrementing primary key
                "LIMIT 5";
                
            ResultSet likedReviewsRS = Database.getInstance().executeQuery(likedReviewsQuery);
            
            StringBuilder movieIDList = new StringBuilder();
            boolean hasMovies = false;
            
            while (likedReviewsRS.next()) {
                if (hasMovies) {
                    movieIDList.append(", ");
                }
                movieIDList.append(likedReviewsRS.getInt("movieID"));
                hasMovies = true;
            }
            
            // Close the ResultSet
            likedReviewsRS.close();
            
            // If user hasn't liked any reviews, return empty list
            if (!hasMovies) {
                System.out.println("No liked reviews found to base recommendations on.");
                return recommendedUsers;
            }
            
            // 2. Find users who have recently reviewed these movies (excluding the current user)
            String usersQuery = 
                "SELECT DISTINCT u.userID, u.username, r.movieID, " +
                "m.title AS movieTitle " +
                "FROM users u " +
                "JOIN reviews r ON u.userID = r.userID " +
                "JOIN movies m ON r.movieID = m.id " +
                "WHERE r.movieID IN (" + movieIDList.toString() + ") " +
                "AND u.userID != " + currentUser.getUserID() + " " +
                "AND r.reviewID IN (" +
                "    SELECT r2.reviewID FROM reviews r2 " +
                "    WHERE r2.userID = u.userID " +
                "    ORDER BY r2.reviewDate DESC LIMIT 1" +
                ") " +
                "ORDER BY r.reviewDate DESC";
                
            ResultSet usersRS = Database.getInstance().executeQuery(usersQuery);
            
            while (usersRS.next() && recommendedUsers.size() < 5) {
                int userID = usersRS.getInt("userID");
                String movieTitle = usersRS.getString("movieTitle");
                
                // Get user object
                User user = Graph.getInstance().getUserByKey(userID);
                if (user != null && !recommendedUsers.contains(user)) {
                    recommendedUsers.add(user);
                    
                    // Optional: Print debug information
                    System.out.println("Recommending user " + user.getUserName() + 
                                    " who recently reviewed " + movieTitle);
                }
            }
            
            // Close the ResultSet
            usersRS.close();
            
        } catch (SQLException e) {
            System.err.println("Error finding users by liked reviews: " + e.getMessage());
        }
        
        return recommendedUsers;
    }

    /**
     * Finds users whose latest reviews are on movies in your favorite genres
     * using string concatenation for the query
     * @param currentUser The currently logged in user
     * @return ArrayList of up to 5 users whose latest reviews match your genre preferences
     */
    public ArrayList<User> findUsersByFavoriteGenres(User currentUser) {
        ArrayList<User> recommendedUsers = new ArrayList<>();
        
        try {
            // 1. Get current user's favorite genres
            List<Genre.GenreType> favoriteGenres = currentUser.getFavoriteGenres();
            
            // If user has no favorite genres, return empty list
            if (favoriteGenres.isEmpty()) {
                System.out.println("No favorite genres found to base recommendations on.");
                return recommendedUsers;
            }
            
            // 2. Build SQL condition for favorite genres
            StringBuilder genreCondition = new StringBuilder();
            for (int i = 0; i < favoriteGenres.size(); i++) {
                if (i > 0) genreCondition.append(" OR ");
                // Directly embed the genre name in the SQL
                genreCondition.append("m.genres LIKE '%" + favoriteGenres.get(i).name() + "%'");
            }
            
            // 3. Create the complete SQL query with all values embedded
            String usersQuery = 
                "SELECT DISTINCT u.userID, u.username, m.title AS movieTitle, m.genres " +
                "FROM users u " +
                "JOIN reviews r ON u.userID = r.userID " +
                "JOIN movies m ON r.movieID = m.id " +
                "WHERE (" + genreCondition.toString() + ") " +
                "AND u.userID != " + currentUser.getUserID() + " " +
                "AND r.reviewID IN (" +
                "    SELECT r2.reviewID FROM reviews r2 " +
                "    WHERE r2.userID = u.userID " +
                "    ORDER BY r2.reviewDate DESC LIMIT 1" +
                ") " +
                "ORDER BY r.reviewDate DESC " +
                "LIMIT 5";
                
            // Execute the query directly
            ResultSet rs = Database.getInstance().executeQuery(usersQuery);
            
            // Process results
            while (rs.next()) {
                int userID = rs.getInt("userID");
                
                // Get user object
                User user = Graph.getInstance().getUserByKey(userID);
                if (user != null && !recommendedUsers.contains(user)) {
                    recommendedUsers.add(user);
                }
            }
            
            // Close the ResultSet
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error finding users by favorite genres: " + e.getMessage());
        }
        
        return recommendedUsers;
    }


}