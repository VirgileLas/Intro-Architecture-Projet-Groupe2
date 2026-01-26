package org.example.quiz.controller;

import java.io.IOException;

import org.example.quiz.model.Question;
import org.example.quiz.service.MathQuestionMaker;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// @RestController
public class QuizController {

    // Manual object management (No IoC/DI)
    private MathQuestionMaker maker = new MathQuestionMaker();
    private Question currentQuestion;

    @RequestMapping("/quiz-api")
    public void handleEverything(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");

        if ("next".equals(action)) {
            this.currentQuestion = maker.createNewQuestion();
            String json = "{"
                    + "\"text\": \"" + currentQuestion.text + "\","
                    + "\"choices\": [" + currentQuestion.options[0] + ","
                    + currentQuestion.options[1] + ","
                    + currentQuestion.options[2] + "]"
                    + "}";
            response.getWriter().write(json);
        } else if ("reply".equals(action)) {
            try {
                String choiceStr = request.getParameter("guess");
                if (choiceStr == null || currentQuestion == null) {
                    response.setStatus(400);
                    response.getWriter().write("{\"error\": \"Missing guess or question\"}");
                    return;
                }
                int guess = Integer.parseInt(choiceStr);
                if (guess == currentQuestion.result) {
                    response.getWriter().write("{\"status\": \"CORRECT\"}");
                } else {
                    response.getWriter().write("{\"status\": \"WRONG\", \"answer\": " + currentQuestion.result + "}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(400);
                response.getWriter().write("{\"error\": \"Please send a valid number\"}");
            }
        }
    }
}