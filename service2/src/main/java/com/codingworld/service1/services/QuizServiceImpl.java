package com.codingworld.service1.services;


import com.codingworld.service1.model.Category;
import com.codingworld.service1.model.Quiz;
import com.codingworld.service1.repo.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class QuizServiceImpl implements QuizService {
    @Autowired
    private QuizRepository quizRepository;

    @Override
    public Quiz addQuiz(Quiz quiz) {
        return this.quizRepository.save(quiz);
    }

    @Override
    public Quiz updateQuiz(Quiz quiz) {
        return this.quizRepository.save(quiz);
    }

    @Override
    public Set<Quiz> getQuizzes() {
        return new HashSet<>(this.quizRepository.findAll());
    }

    @Override
    public Quiz getQuiz(Long quizId) {
        return this.quizRepository.findById(quizId).get();
    }

    @Override
    public void deleteQuiz(Long quizId) {
        this.quizRepository.deleteById(quizId);
    }

    @Override
    public List<Quiz> getQuizzesOfCategory(Category category) {
        return this.quizRepository.findBycategory(category);
    }


    //get active quizzes

    @Override
    public List<Quiz> getActiveQuizzes() {
        return this.quizRepository.findByActive(true);
    }

    @Override
    public List<Quiz> getActiveQuizzesOfCategory(Category c) {
        return this.quizRepository.findByCategoryAndActive(c, true);
    }

}
