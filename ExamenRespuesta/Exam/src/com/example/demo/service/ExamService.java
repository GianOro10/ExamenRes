package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.example.demo.exception.ExamNotFoundException;
import com.example.demo.exception.ExamQuestionNotFoundException;
import com.example.demo.exception.StudentNotFoundException;
import com.example.demo.model.Exam;
import com.example.demo.model.ExamQuestion;
import com.example.demo.model.Student;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("hiding")
@Service
public class ExamService<Student> {

    private ExamRepository examRepository;
    private StudentRepository studentRepository;

    @Autowired
    public ExamService(ExamRepository examRepository, StudentRepository studentRepository) {
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
    }

    public Exam createExam(List<ExamQuestion> questions) {
        // Validating that the sum of question scores is 100
        if (questions.stream().flatMapToInt(ExamQuestion::getScore).sum() != 100) {
            throw new IllegalArgumentException("The sum of question scores must be 100");
        }

        Exam exam = new Exam();
        exam.setQuestions(questions);
        return examRepository.save(exam);
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public LocalDateTime calculateExamDate(LocalDateTime examDateTime, Student student) {
        // Converting the exam date to the student's timezone
        TimeZone studentTimeZone = TimeZone.getTimeZone(student.getTimezone());
        LocalDateTime examDate = examDateTime
                .minusSeconds(studentTimeZone.getOffset(examDateTime.toEpochSecond()) / 1000);
        return examDate;
    }

    public List<ExamQuestion> getStudentExamQuestions(Long examId, Long studentId)
            throws ExamNotFoundException, StudentNotFoundException {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam with id " + examId + " not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student with id " + studentId + " not found"));

        LocalDateTime examDate = calculateExamDate(exam.getExamDateTime(), student);

        // Mapping exam questions to a new list with the same questions but with the correct date
        List<ExamQuestion> studentQuestions = exam.getQuestions().stream()
                .map(q -> new ExamQuestion(q.getId(), q.getQuestion(), q.getOption1(), q.getOption2(), q.getOption3(),
                        q.getOption4(), q.getCorrectOption(), q.getScore(), examDate))
                .collect(Collectors.toList());

        return studentQuestions;
    }

    public int gradeStudentExam(Long examId, Long studentId, List<String> answers)
            throws ExamNotFoundException, StudentNotFoundException, ExamQuestionNotFoundException {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam with id " + examId + " not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student with id " + studentId + " not found"));

        LocalDateTime examDate = calculateExamDate(exam.getExamDateTime(), student);

        int grade = 0;
        for (int i = 0; i < answers.size(); i++) {
            ExamQuestion question = exam.getQuestions().stream().filter(q -> q.getId() == (i + 1)).findFirst()
                    .orElseThrow(() -> new ExamQuestionNotFoundException(
                            "Question with id " + (i + 1) + " no aprobaste el examen"));
