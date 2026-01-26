package org.example.quiz.controller;

import org.example.quiz.component.QuizSession;
import org.example.quiz.model.Question;
import org.example.quiz.service.MathQuestionMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
public class SpringQuizController {

    private final MathQuestionMaker questionMaker;
    private final QuizSession quizSession;

    // 1. INJECTION DE DÉPENDANCE
    // On injecte le MathQuestionMaker (Service Singleton) ET le QuizSession (Composant Session Scoped).
    // Spring gère magiquement le scope session : quizSession est un proxy qui pointe vers l'objet de l'utilisateur courant.
    @Autowired
    public SpringQuizController(MathQuestionMaker questionMaker, QuizSession quizSession) {
        this.questionMaker = questionMaker;
        this.quizSession = quizSession;
    }

    // 2. SÉPARATION DES RESPONSABILITÉS (ENDPOINT)
    
    @GetMapping("/next")
    public QuestionDTO getNextQuestion() {
        // Logique métier déléguée au service
        Question q = questionMaker.createNewQuestion();
        
        // 3. GESTION DE L'ÉTAT (SESSION BEAN)
        // On utilise le bean session scoppé au lieu de manipuler directement la map HttpSession.
        // C'est plus typé et plus propre.
        quizSession.setCurrentQuestion(q);
        
        // 4. DTO (Data Transfer Object) AUTOMATIQUE
        return new QuestionDTO(q.text, q.options);
    }

    @PostMapping("/reply")
    public ReplyResult checkAnswer(@RequestParam int guess) {
        Question q = quizSession.getCurrentQuestion();
        
        if (q == null) {
            throw new IllegalArgumentException("No question in progress. Please call /next first.");
        }
        
        if (guess == q.result) {
            return new ReplyResult("CORRECT", null);
        } else {
            return new ReplyResult("WRONG", q.result);
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
