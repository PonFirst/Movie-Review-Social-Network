import java.lang.reflect.Array;
import java.util.ArrayList;

public class ReviewManager
{
    private static ReviewManager instance;
    private ArrayList<Review> reviews;

    private ReviewManager()
    {
        reviews = new ArrayList<>();
    }

    public static ReviewManager getInstance()
    {
        if (instance == null)
        {
            instance = new ReviewManager();
        }
        return instance;
    }

    public void addReviewMenu(int movieID)
    {
        System.out.println("Adding review menu");
    }

    public ArrayList findReviewsByMovie(String movieTitle)
    {

    }

}
