package org.example.quiz.controller;

import java.util.Optional;

import org.example.quiz.component.QuizSession;
import org.example.quiz.model.Question;
import org.example.quiz.model.Session;
import org.example.quiz.service.MathQuestionMaker;
import org.example.quiz.service.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
public class SpringQuizController {

    private final MathQuestionMaker questionMaker;
    private final QuizSession quizSession;
    private final SessionManager sessionManager;

    // 1. INJECTION DE DÉPENDANCE
    // On injecte le MathQuestionMaker, QuizSession ET SessionManager pour vérifier les tokens.
    @Autowired
    public SpringQuizController(MathQuestionMaker questionMaker, QuizSession quizSession, SessionManager sessionManager) {
        this.questionMaker = questionMaker;
        this.quizSession = quizSession;
        this.sessionManager = sessionManager;
    }

    /**
     * Vérifie le token Bearer et retourne la session si valide.
     */
    private Optional<Session> extractAndVerifyToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authHeader.substring(7);
        return sessionManager.verify(token);
    }

    // 2. SÉPARATION DES RESPONSABILITÉS (ENDPOINT)
    
    @GetMapping("/next")
    public ResponseEntity<?> getNextQuestion(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // VÉRIFICATION DU TOKEN
        Optional<Session> session = extractAndVerifyToken(authHeader);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ReplyError("Token manquant ou invalide. Connectez-vous via /api/auth/login"));
        }

        // Logique métier déléguée au service
        Question q = questionMaker.createNewQuestion();
        
        // GESTION DE L'ÉTAT (SESSION BEAN)
        quizSession.setCurrentQuestion(q);
        
        // DTO (Data Transfer Object) AUTOMATIQUE
        return ResponseEntity.ok(new QuestionDTO(q.text, q.options));
    }

    @PostMapping("/reply")
    public ResponseEntity<?> checkAnswer(@RequestParam int guess,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // VÉRIFICATION DU TOKEN
        Optional<Session> session = extractAndVerifyToken(authHeader);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ReplyError("Token manquant ou invalide. Connectez-vous via /api/auth/login"));
        }

        Question q = quizSession.getCurrentQuestion();
        
        if (q == null) {
            throw new IllegalArgumentException("No question in progress. Please call /next first.");
        }
        
        if (guess == q.result) {
            return ResponseEntity.ok(new ReplyResult("CORRECT", null));
        } else {
            return ResponseEntity.ok(new ReplyResult("WRONG", q.result));
        }
    }

    // Gestion propre des erreurs pour renvoyer du JSON
    @ExceptionHandler(IllegalArgumentException.class)
    public ReplyError handleBadRequest(IllegalArgumentException e) {
        return new ReplyError(e.getMessage());
    }

    // Records (Java 17+) pour définir des objets de données simples et immuables
    public record QuestionDTO(String text, int[] choices) {}
    public record ReplyResult(String status, Integer answer) {}
    public record ReplyError(String error) {}
}
