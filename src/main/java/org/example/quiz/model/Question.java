package org.example.quiz.model;

public class Question {
    public String text;
    public int result;
    public int[] options;

    public Question(String text, int result, int[] options) {
        this.text = text;
        this.result = result;
        this.options = options;
    }
}
