import java.util.List;
import java.util.Scanner;

public class UserGraphManager {
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

    public void followUser() {
        System.out.print("Enter the username of the user you want to follow: ");
        Scanner scanner = new Scanner(System.in);
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
    

    public void unfollowUser() {
        System.out.print("Enter the username of the user you want to unfollow: ");
        Scanner scanner = new Scanner(System.in);
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

    // Display a list of the latest reviews made by the people the user follows
    public void displayLatestReviews()
    {
        User currentUser = AuthenticationManager.getInstance().getCurrentUser();
        Graph.getInstance().printFollowingLatestReviews(currentUser);
    
    }
}