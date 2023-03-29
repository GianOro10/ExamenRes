import java.util.List;

public class Exam {
    private Long id;
    private List<Question> questions;

    public Exam(Long id, List<Question> questions) {
        this.id = id;
        this.questions = questions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
