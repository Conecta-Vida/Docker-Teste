package br.edu.ifsp.conectaavida.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ENTIDADE: DISPOSITIVO PUSH
 * Token FCM de cada celular que instalou o app. Antes ficava em memória RAM
 * dentro de um único processo; com várias réplicas no Docker Swarm cada
 * réplica teria uma lista diferente. Agora fica no banco, compartilhado por
 * todas as instâncias da admin-api e da mobile-api.
 */
@Entity
@Table(name = "dispositivos_push", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DispositivoPush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(length = 30)
    private String plataforma;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;
}
