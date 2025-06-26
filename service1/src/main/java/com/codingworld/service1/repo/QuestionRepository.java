package com.codingworld.service1.repo;

import com.codingworld.service1.model.Question;
import com.codingworld.service1.model.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Set<Question> findByQuiz(Quiz quiz);
}
