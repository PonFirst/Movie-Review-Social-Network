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
     * 1. Users who have written reviews of movies in categories similar to ones you've liked
     * 2. Users who are followed by users you follow (friends of friends)
     */
    public void followRecomendations() {
        User currentUser = AuthenticationManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.out.println("You need to be logged in to see recommendations.");
            return;
        }
        
        // Set to track unique recommended users to avoid duplicates
        Set<User> uniqueRecommendations = new HashSet<>();
        
        // 1. First find users who have reviewed movies in categories similar to ones you've liked
        ArrayList<User> similarCategoryUsers = recommendUsersBySimilarCategories(currentUser);
        for (User user : similarCategoryUsers) {
            uniqueRecommendations.add(user);
        }
        
        // 2. Fill any remaining spots with friends of friends
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
        System.out.println("(Based on similar movie categories and network connections)");
        
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
     * Recommends users who have written reviews for movies in categories similar 
     * to the categories of movies the current user has liked
     * 
     * @param currentUser The currently logged in user
     * @return ArrayList of up to 5 recommended users who have reviewed movies in categories similar to your liked movies
     */
    public ArrayList<User> recommendUsersBySimilarCategories(User currentUser) {
        ArrayList<User> recommendedUsers = new ArrayList<>();
        
        try {
            // 1. Find the genres of movies the current user has liked reviews for
            String likedGenresQuery = 
                "SELECT DISTINCT m.genres " +
                "FROM Likes l " +
                "JOIN Reviews r ON l.reviewID = r.reviewID " +
                "JOIN Movies m ON r.movieID = m.id " +
                "WHERE l.userID = " + currentUser.getUserID();
                
            ResultSet likedGenresRS = Database.getInstance().executeQuery(likedGenresQuery);
            
            Set<String> likedGenres = new HashSet<>();
            while (likedGenresRS.next()) {
                String genre = likedGenresRS.getString("genres");
                if (genre != null && !genre.isEmpty()) {
                    // Some movies might have multiple genres stored as a string
                    // We'll split them if necessary
                    if (genre.contains(",")) {
                        String[] genreParts = genre.split(",");
                        for (String part : genreParts) {
                            likedGenres.add(part.trim());
                        }
                    } else {
                        likedGenres.add(genre.trim());
                    }
                }
            }
            likedGenresRS.close();
            
            // If user hasn't liked any reviews, try to use their favorite genres
            if (likedGenres.isEmpty()) {
                for (Genre.GenreType genre : currentUser.getFavoriteGenres()) {
                    likedGenres.add(genre.name());
                }
            }
            
            // If still no genres to work with, return empty list
            if (likedGenres.isEmpty()) {
                System.out.println("No genre preferences found to base recommendations on.");
                return recommendedUsers;
            }
            
            // 2. Find users who have written reviews for movies in similar genres
            // but exclude users the current user is already following
            
            // First, get the list of users the current user is following
            List<User> following = Graph.getInstance().getFollowing(currentUser);
            StringBuilder excludeUsers = new StringBuilder();
            excludeUsers.append(currentUser.getUserID()); // Exclude current user
            
            for (User followedUser : following) {
                excludeUsers.append(",").append(followedUser.getUserID());
            }
            
            // Build the genre condition
            StringBuilder genreCondition = new StringBuilder();
            int genreCount = 0;
            for (String genre : likedGenres) {
                if (genreCount > 0) genreCondition.append(" OR ");
                genreCondition.append("m.genres LIKE '%").append(genre).append("%'");
                genreCount++;
            }
            
            String usersQuery = 
                "SELECT DISTINCT u.userID, u.username, " +
                "COUNT(r.reviewID) as reviewCount " +
                "FROM Users u " +
                "JOIN Reviews r ON u.userID = r.userID " +
                "JOIN Movies m ON r.movieID = m.id " +
                "WHERE (" + genreCondition.toString() + ") " +
                "AND u.userID NOT IN (" + excludeUsers.toString() + ") " +
                "GROUP BY u.userID " +
                "ORDER BY reviewCount DESC " +
                "LIMIT 5";
                
            ResultSet usersRS = Database.getInstance().executeQuery(usersQuery);
            
            System.out.println("Finding users who review movies in categories you enjoy...");
            
            while (usersRS.next() && recommendedUsers.size() < 5) {
                int userID = usersRS.getInt("userID");
                String username = usersRS.getString("username");
                
                // Get user object
                User user = Graph.getInstance().getUserByKey(userID);
                
                // Double-check that we're not recommending users the current user already follows
                if (user != null && !Graph.getInstance().isFollowing(currentUser, user) && !user.equals(currentUser)) {
                    recommendedUsers.add(user);
                    
                    // Optional: Print debug info
                    System.out.println("Recommending user " + username + 
                                    " who reviews movies in categories you like");
                }
            }
            
            usersRS.close();
            
        } catch (SQLException e) {
            System.err.println("Error recommending users by similar categories: " + e.getMessage());
        }
        
        return recommendedUsers;
    }
}