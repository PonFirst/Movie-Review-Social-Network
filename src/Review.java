import java.util.Date;

public class Review
{
    private String text;
    private int rating;
    private User user;
    private Movie movie;
    private Date reviewDate;
    private int reviewID;
    private int likeCount;

    public Review(String text, int rating, User user, Movie movie, Date reviewDate,
                  int reviewID, int likeCount)
    {
        this.text = text;
        this.rating = rating;
        this.user = user;
        this.movie = movie;
        this.reviewDate = reviewDate;
        this.reviewID = reviewID;
        this.likeCount = likeCount;
    }

    public void editReview(String text, int rating)
    {
        this.text = text;
        this.rating = rating;
    }

    public void deleteReview()
    {
        //TODO
    }

    public void likeReview()
    {
        this.likeCount++;
    }

    public int getLikeCount()
    {
        return likeCount;
    }


}
