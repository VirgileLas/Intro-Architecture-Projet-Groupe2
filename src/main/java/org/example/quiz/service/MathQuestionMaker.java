package org.example.quiz.service;

import java.util.Random;

import org.example.quiz.model.Question;
import org.springframework.stereotype.Service;

@Service
public class MathQuestionMaker {
    private Random rand = new Random();

    public Question createNewQuestion() {
        int a = rand.nextInt(12) + 1;
        int b = rand.nextInt(12) + 1;
        int correctAnswer = a * b;

        // Generate random wrong answers that aren't the correct one
        int wrong1 = correctAnswer + (rand.nextInt(5) + 1);
        int wrong2 = correctAnswer - (rand.nextInt(5) + 1);

        int[] options = { correctAnswer, wrong1, wrong2 };

        return new Question(
                "What is " + a + " x " + b + "?",
                correctAnswer,
                options);
    }
}