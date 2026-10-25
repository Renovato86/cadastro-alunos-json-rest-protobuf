package br.edu.atividade.alunos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Um record é uma classe Java imutável; Jackson serializa seus componentes.
public record Aluno(
        @NotNull(message = "id é obrigatório")
        @Positive(message = "id deve ser positivo") Integer id,
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 120, message = "nome deve ter até 120 caracteres") String nome,
        @NotBlank(message = "curso é obrigatório")
        @Size(max = 120, message = "curso deve ter até 120 caracteres") String curso) {
}
