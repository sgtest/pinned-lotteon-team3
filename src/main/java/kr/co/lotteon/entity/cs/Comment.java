package kr.co.lotteon.entity.cs;

import jakarta.persistence.*;
import kr.co.lotteon.dto.cs.BoardDTO;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "cs_comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cno;
    private int bno;
    private String uid;

    private String group;
    private String cate;
    private String content;

    @CreationTimestamp
    private LocalDateTime rdate;

    @Transient
    private String nick;
}